package com.example.flux.controller;

import com.example.flux.model.User;
import com.example.flux.repository.UserRepository;
import com.example.flux.service.DatabaseExplorerService;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.List;
import java.util.Set;

@Controller
public class UserController {

	private static final long PROTECTED_USER_ID = 9L;
	private static final Set<String> ALLOWED_SORTS = Set.of("id", "username", "role", "enabled");

	private final UserRepository userRepository;
	private final DatabaseExplorerService databaseExplorerService;
	private final SessionRegistry sessionRegistry;

	public UserController(UserRepository userRepository, DatabaseExplorerService databaseExplorerService,
						  SessionRegistry sessionRegistry) {
		this.userRepository = userRepository;
		this.databaseExplorerService = databaseExplorerService;
		this.sessionRegistry = sessionRegistry;
	}

	@GetMapping("/users")
	public String listUsers(@RequestParam(defaultValue = "username") String sort,
							@RequestParam(defaultValue = "asc") String dir,
							@RequestParam(required = false) String role,
							Model model) {
		Sort.Direction direction = "desc".equalsIgnoreCase(dir) ? Sort.Direction.DESC : Sort.Direction.ASC;
		String sortProperty = ALLOWED_SORTS.contains(sort) ? sort : "username";
		List<User> allUsers = userRepository.findAll(Sort.by(direction, sortProperty));
		List<User> users = allUsers;

		if (role != null && !role.isBlank()) {
			users = users.stream()
				.filter(u -> u.getRole().equalsIgnoreCase(role))
				.toList();
		}

		long totalUsers = allUsers.size();
		long enabledUsers = allUsers.stream().filter(User::isEnabled).count();
		long adminUsers = allUsers.stream().filter(u -> "ROLE_ADMIN".equals(u.getRole())).count();

		model.addAttribute("users", users);
		model.addAttribute("totalUsers", totalUsers);
		model.addAttribute("enabledUsers", enabledUsers);
		model.addAttribute("adminUsers", adminUsers);
		model.addAttribute("tableSummaries", databaseExplorerService.listTableSummaries());
		model.addAttribute("currentSort", sortProperty);
		model.addAttribute("currentDir", dir);
		model.addAttribute("currentRole", role);

		return "users";
	}

	@PostMapping("/users/{id}/toggle-admin")
	public String toggleAdmin(@PathVariable Long id, Principal principal, RedirectAttributes redirectAttributes) {
		User user = userRepository.findById(id).orElse(null);
		if (user == null) {
			redirectAttributes.addFlashAttribute("error", "User not found.");
			return "redirect:/users";
		}

		if (user.getUsername().equals(principal.getName())) {
			redirectAttributes.addFlashAttribute("error", "You cannot change your own role.");
			return "redirect:/users";
		}

		if ("ROLE_ADMIN".equals(user.getRole())) {
			if (userRepository.countByRole("ROLE_ADMIN") <= 1) {
				redirectAttributes.addFlashAttribute("error", "At least one admin account must remain.");
				return "redirect:/users";
			}
			user.setRole("ROLE_USER");
			redirectAttributes.addFlashAttribute("success", user.getUsername() + " is no longer an admin and will be asked to sign back in.");
		} else {
			user.setRole("ROLE_ADMIN");
			redirectAttributes.addFlashAttribute("success", user.getUsername() + " is now an admin and will be asked to sign back in.");
		}
		userRepository.save(user);
		expireSessionsFor(user.getUsername());

		return "redirect:/users";
	}

	@PostMapping("/users/{id}/toggle-enabled")
	public String toggleEnabled(@PathVariable Long id, Principal principal, RedirectAttributes redirectAttributes) {
		User user = userRepository.findById(id).orElse(null);
		if (user == null) {
			redirectAttributes.addFlashAttribute("error", "User not found.");
			return "redirect:/users";
		}

		if (user.getUsername().equals(principal.getName())) {
			redirectAttributes.addFlashAttribute("error", "You cannot deactivate your own account.");
			return "redirect:/users";
		}

		if (user.isEnabled() && "ROLE_ADMIN".equals(user.getRole())
				&& userRepository.countByRoleAndEnabled("ROLE_ADMIN", true) <= 1) {
			redirectAttributes.addFlashAttribute("error", "At least one active admin account must remain.");
			return "redirect:/users";
		}

		user.setEnabled(!user.isEnabled());
		userRepository.save(user);

		if (user.isEnabled()) {
			redirectAttributes.addFlashAttribute("success", user.getUsername() + " has been reactivated.");
		} else {
			expireSessionsFor(user.getUsername());
			redirectAttributes.addFlashAttribute("success", user.getUsername() + " has been deactivated.");
		}

		return "redirect:/users";
	}

	@PostMapping("/users/{id}/delete")
	public String deleteUser(@PathVariable Long id, Principal principal, RedirectAttributes redirectAttributes) {
		User user = userRepository.findById(id).orElse(null);
		if (user == null) {
			redirectAttributes.addFlashAttribute("error", "User not found.");
			return "redirect:/users";
		}

		if (user.getUsername().equals(principal.getName())) {
			redirectAttributes.addFlashAttribute("error", "You cannot delete your own account.");
			return "redirect:/users";
		}

		if (PROTECTED_USER_ID == id) {
			redirectAttributes.addFlashAttribute("error", "User ID 9 cannot be deleted.");
			return "redirect:/users";
		}

		if ("ROLE_ADMIN".equals(user.getRole()) && userRepository.countByRole("ROLE_ADMIN") <= 1) {
			redirectAttributes.addFlashAttribute("error", "At least one admin account must remain.");
			return "redirect:/users";
		}

		userRepository.delete(user);
		expireSessionsFor(user.getUsername());
		redirectAttributes.addFlashAttribute("success", user.getUsername() + " has been removed.");

		return "redirect:/users";
	}

	private void expireSessionsFor(String username) {
		sessionRegistry.getAllPrincipals().stream()
			.filter(principal -> username.equals(usernameOf(principal)))
			.flatMap(principal -> sessionRegistry.getAllSessions(principal, false).stream())
			.forEach(sessionInformation -> sessionInformation.expireNow());
	}

	private String usernameOf(Object principal) {
		if (principal instanceof UserDetails userDetails) {
			return userDetails.getUsername();
		}
		return principal == null ? null : principal.toString();
	}
}

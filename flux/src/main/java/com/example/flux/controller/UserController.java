package com.example.flux.controller;

import com.example.flux.model.User;
import com.example.flux.repository.UserRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.List;

@Controller
public class UserController {

	private final UserRepository userRepository;

	public UserController(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	@GetMapping("/users")
	public String listUsers(@RequestParam(defaultValue = "username") String sort,
							@RequestParam(defaultValue = "asc") String dir,
							@RequestParam(required = false) String role,
							Model model) {
		Sort.Direction direction = "desc".equalsIgnoreCase(dir) ? Sort.Direction.DESC : Sort.Direction.ASC;
		List<User> users = userRepository.findAll(Sort.by(direction, sort));

		if (role != null && !role.isBlank()) {
			users = users.stream()
				.filter(u -> u.getRole().equalsIgnoreCase(role))
				.toList();
		}

		long totalUsers = userRepository.count();
		long enabledUsers = users.stream().filter(User::isEnabled).count();
		long adminUsers = users.stream().filter(u -> "ROLE_ADMIN".equals(u.getRole())).count();

		model.addAttribute("users", users);
		model.addAttribute("totalUsers", totalUsers);
		model.addAttribute("enabledUsers", enabledUsers);
		model.addAttribute("adminUsers", adminUsers);
		model.addAttribute("currentSort", sort);
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
			user.setRole("ROLE_USER");
			redirectAttributes.addFlashAttribute("success", user.getUsername() + " is no longer an admin.");
		} else {
			user.setRole("ROLE_ADMIN");
			redirectAttributes.addFlashAttribute("success", user.getUsername() + " is now an admin.");
		}
		userRepository.save(user);

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

		userRepository.delete(user);
		redirectAttributes.addFlashAttribute("success", user.getUsername() + " has been removed.");

		return "redirect:/users";
	}
}

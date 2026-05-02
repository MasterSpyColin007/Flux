package com.example.flux.controller;

import com.example.flux.model.User;
import com.example.flux.repository.UserRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

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
}

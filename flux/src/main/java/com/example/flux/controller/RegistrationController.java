package com.example.flux.controller;

import com.example.flux.model.User;
import com.example.flux.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class RegistrationController {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;

	public RegistrationController(UserRepository userRepository, PasswordEncoder passwordEncoder) {
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
	}

	@GetMapping("/register")
	public String showRegistrationForm() {
		return "register";
	}

	@PostMapping("/register")
	public String register(@RequestParam String username,
						   @RequestParam String password,
						   @RequestParam String confirmPassword,
						   Model model) {
		if (username == null || username.trim().length() < 3) {
			model.addAttribute("error", "Username must be at least 3 characters.");
			model.addAttribute("username", username);
			return "register";
		}

		if (password == null || password.length() < 6) {
			model.addAttribute("error", "Password must be at least 6 characters.");
			model.addAttribute("username", username);
			return "register";
		}

		if (!password.equals(confirmPassword)) {
			model.addAttribute("error", "Passwords do not match.");
			model.addAttribute("username", username);
			return "register";
		}

		if (userRepository.findByUsername(username.trim()).isPresent()) {
			model.addAttribute("error", "Username is already taken.");
			model.addAttribute("username", username);
			return "register";
		}

		User user = new User(username.trim(), passwordEncoder.encode(password), "ROLE_USER");
		userRepository.save(user);

		return "redirect:/login?registered=true";
	}
}

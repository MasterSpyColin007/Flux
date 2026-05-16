package com.example.flux.controller;

import com.example.flux.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice(annotations = Controller.class)
public class GlobalModelAdvice {

	private final UserRepository userRepository;

	public GlobalModelAdvice(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	@ModelAttribute("darkMode")
	public boolean darkMode(Authentication authentication) {
		if (authentication == null || !authentication.isAuthenticated()) {
			return false;
		}
		return userRepository.findByUsername(authentication.getName())
			.map(user -> user.isDarkMode())
			.orElse(false);
	}
}

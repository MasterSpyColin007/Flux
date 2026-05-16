package com.example.flux.controller;

import com.example.flux.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.net.URI;
import java.net.URISyntaxException;

@Controller
public class ThemeController {

	private final UserService userService;

	public ThemeController(UserService userService) {
		this.userService = userService;
	}

	@PostMapping("/theme/toggle")
	public String toggleTheme(Authentication authentication, HttpServletRequest request,
							  RedirectAttributes redirectAttributes) {
		try {
			boolean darkMode = userService.setDarkMode(
				authentication.getName(),
				!userService.getUserByUsername(authentication.getName()).isDarkMode()
			).isDarkMode();
			redirectAttributes.addFlashAttribute("success", darkMode ? "Dark mode enabled." : "Light mode enabled.");
		} catch (IllegalArgumentException ex) {
			redirectAttributes.addFlashAttribute("error", ex.getMessage());
		}

		String referer = request.getHeader("Referer");
		return "redirect:" + redirectPath(referer);
	}

	private String redirectPath(String referer) {
		if (referer == null || referer.isBlank()) {
			return "/";
		}
		try {
			URI uri = new URI(referer);
			String path = uri.getPath();
			String query = uri.getQuery();
			return path == null || path.isBlank() ? "/" : path + (query == null ? "" : "?" + query);
		} catch (URISyntaxException ex) {
			return "/";
		}
	}
}

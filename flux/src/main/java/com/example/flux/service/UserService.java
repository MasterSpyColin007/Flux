package com.example.flux.service;

import com.example.flux.model.User;
import com.example.flux.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

	private static final long PROTECTED_USER_ID = 9L;

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;

	public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
	}

	public User registerUser(String username, String email, String password) {
		if (username == null || username.trim().length() < 3) {
			throw new IllegalArgumentException("Username must be at least 3 characters.");
		}
		if (password == null || password.length() < 6) {
			throw new IllegalArgumentException("Password must be at least 6 characters.");
		}
		if (userRepository.findByUsername(username.trim()).isPresent()) {
			throw new IllegalArgumentException("Username is already taken.");
		}
		if (email != null && !email.isBlank() && userRepository.findByEmail(email.trim()).isPresent()) {
			throw new IllegalArgumentException("Email is already taken.");
		}

		User user = new User(username.trim(), passwordEncoder.encode(password), "ROLE_USER");
		user.setEmail(email == null || email.isBlank() ? null : email.trim());
		return userRepository.save(user);
	}

	public User getUserById(Long id) {
		return userRepository.findById(id)
			.orElseThrow(() -> new IllegalArgumentException("User not found."));
	}

	public List<User> getAllUsers() {
		return userRepository.findAll();
	}

	public User updateUser(Long id, String username, String bio) {
		User user = getUserById(id);
		if (username != null && !username.isBlank()) {
			user.setUsername(username.trim());
		}
		if (bio != null) {
			user.setBio(bio.trim());
		}
		return userRepository.save(user);
	}

	public void deleteUser(Long id) {
		User user = getUserById(id);
		if (PROTECTED_USER_ID == id) {
			throw new IllegalArgumentException("User ID 9 cannot be deleted.");
		}
		if ("ROLE_ADMIN".equals(user.getRole()) && userRepository.countByRole("ROLE_ADMIN") <= 1) {
			throw new IllegalArgumentException("At least one admin account must remain.");
		}
		userRepository.delete(user);
	}
}

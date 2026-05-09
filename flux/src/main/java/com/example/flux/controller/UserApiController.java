package com.example.flux.controller;

import com.example.flux.model.User;
import com.example.flux.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserApiController {

	private final UserService userService;

	public UserApiController(UserService userService) {
		this.userService = userService;
	}

	@PostMapping
	public ResponseEntity<?> register(@RequestBody Map<String, String> body) {
		try {
			User user = userService.registerUser(body.get("username"), body.get("email"), body.get("password"));
			return ResponseEntity.ok(user);
		} catch (IllegalArgumentException ex) {
			return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
		}
	}

	@GetMapping
	public List<User> getAllUsers() {
		return userService.getAllUsers();
	}

	@GetMapping("/{id}")
	public ResponseEntity<?> getUser(@PathVariable Long id) {
		try {
			return ResponseEntity.ok(userService.getUserById(id));
		} catch (IllegalArgumentException ex) {
			return ResponseEntity.notFound().build();
		}
	}

	@PutMapping("/{id}")
	public ResponseEntity<?> updateUser(@PathVariable Long id, @RequestBody Map<String, String> body) {
		try {
			return ResponseEntity.ok(userService.updateUser(id, body.get("username"), body.get("bio")));
		} catch (IllegalArgumentException ex) {
			return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
		}
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<?> deleteUser(@PathVariable Long id) {
		try {
			userService.deleteUser(id);
			return ResponseEntity.ok(Map.of("message", "User deleted."));
		} catch (IllegalArgumentException ex) {
			return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
		}
	}
}

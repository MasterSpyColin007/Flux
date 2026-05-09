package com.example.flux.controller;

import com.example.flux.model.Post;
import com.example.flux.service.PostService;
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
@RequestMapping("/api/posts")
public class PostController {

	private final PostService postService;

	public PostController(PostService postService) {
		this.postService = postService;
	}

	@PostMapping
	public ResponseEntity<?> createPost(@RequestBody Map<String, String> body) {
		try {
			Post post = postService.createPost(
				body.get("author"),
				body.get("title"),
				body.get("content"),
				body.get("imageUrl")
			);
			return ResponseEntity.ok(post);
		} catch (IllegalArgumentException ex) {
			return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
		}
	}

	@GetMapping
	public List<Post> getAllPosts() {
		return postService.getAllPosts();
	}

	@GetMapping("/{id}")
	public ResponseEntity<?> getPost(@PathVariable Long id) {
		try {
			return ResponseEntity.ok(postService.getPostById(id));
		} catch (IllegalArgumentException ex) {
			return ResponseEntity.notFound().build();
		}
	}

	@GetMapping("/user/{userId}")
	public List<Post> getUserPosts(@PathVariable Long userId) {
		return postService.getUserPosts(userId);
	}

	@PutMapping("/{id}")
	public ResponseEntity<?> updatePost(@PathVariable Long id, @RequestBody Map<String, String> body) {
		try {
			Post post = postService.updatePost(
				id,
				body.get("author"),
				body.get("title"),
				body.get("content"),
				body.get("imageUrl")
			);
			return ResponseEntity.ok(post);
		} catch (IllegalArgumentException ex) {
			return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
		}
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<?> deletePost(@PathVariable Long id, @RequestBody Map<String, String> body) {
		try {
			postService.deletePost(id, body.get("author"));
			return ResponseEntity.ok(Map.of("message", "Post deleted."));
		} catch (IllegalArgumentException ex) {
			return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
		}
	}
}

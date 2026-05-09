package com.example.flux.controller;
import com.example.flux.model.Post;
import com.example.flux.service.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/post")
public class SimplePostController {

    @Autowired
    private PostService postService;

    @PostMapping
    public ResponseEntity<?> createPost(@RequestBody Map<String, String> body) {
        try {
            Post post = postService.createPost(
                body.get("author"),
                body.get("content"),
                body.get("image")
            );
            return ResponseEntity.ok(post);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<?> getAllPosts() {
        try {
            List<Post> posts = postService.getAllPosts(0, 50);
            return ResponseEntity.ok(posts);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getPost(@PathVariable Long id) {
        try {
            Post post = postService.getPostById(id);
            return ResponseEntity.ok(post);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updatePost(@PathVariable Long id, @RequestBody Map<String, String> body) {
        try {
            Post post = postService.updatePost(
                id,
                body.get("author"),
                body.get("content"),
                body.get("image")
            );
            return ResponseEntity.ok(post);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletePost(@PathVariable Long id, @RequestBody Map<String, String> body) {
        try {
            postService.deletePost(id, body.get("author"));
            return ResponseEntity.ok(Map.of("message", "Post deleted"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getUserPosts(@PathVariable Long userId) {
        try {
            List<Post> posts = postService.getUserPosts(userId, 0, 50);
            return ResponseEntity.ok(posts);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}

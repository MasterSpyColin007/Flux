package com.example.flux.controller;

import com.example.flux.service.LikeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class LikeController {

    @Autowired
    private LikeService likeService;

    @PostMapping("/posts/{postId}/like")
    public ResponseEntity<?> likePost(@PathVariable Long postId, Authentication auth) {
        try {
            likeService.likePost(postId, auth.getName());
            return ResponseEntity.ok(Map.of("message", "Post liked successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/posts/{postId}/unlike")
    public ResponseEntity<?> unlikePost(@PathVariable Long postId, Authentication auth) {
        try {
            likeService.unlikePost(postId, auth.getName());
            return ResponseEntity.ok(Map.of("message", "Post unliked successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/posts/{postId}/likes")
    public ResponseEntity<?> getPostLikes(
            @PathVariable Long postId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            var likes = likeService.getPostLikes(postId, page, size);
            return ResponseEntity.ok(likes);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/posts/{postId}/comments/{commentId}/like")
    public ResponseEntity<?> likeComment(
            @PathVariable Long postId,
            @PathVariable Long commentId,
            Authentication auth) {
        try {
            likeService.likeComment(postId, commentId, auth.getName());
            return ResponseEntity.ok(Map.of("message", "Comment liked successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/posts/{postId}/comments/{commentId}/unlike")
    public ResponseEntity<?> unlikeComment(
            @PathVariable Long postId,
            @PathVariable Long commentId,
            Authentication auth) {
        try {
            likeService.unlikeComment(postId, commentId, auth.getName());
            return ResponseEntity.ok(Map.of("message", "Comment unliked successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/posts/{postId}/comments/{commentId}/likes")
    public ResponseEntity<?> getCommentLikes(
            @PathVariable Long postId,
            @PathVariable Long commentId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            var likes = likeService.getCommentLikes(postId, commentId, page, size);
            return ResponseEntity.ok(likes);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        }
    }
}

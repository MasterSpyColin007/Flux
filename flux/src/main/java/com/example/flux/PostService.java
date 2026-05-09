package com.example.flux.service;

import com.example.flux.model.Post;
import com.example.flux.model.User;
import com.example.flux.repository.PostRepository;
import com.example.flux.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PostService {

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UserRepository userRepository;

    public Post createPost(String authorEmail, String content, String image) {
        User author = userRepository.findByEmail(authorEmail)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        Post post = new Post();
        post.setAuthor(author);
        post.setContent(content);
        post.setImage(image);
        
        return postRepository.save(post);
    }

    public Post getPostById(Long postId) {
        return postRepository.findById(postId)
            .orElseThrow(() -> new RuntimeException("Post not found"));
    }

    public List<Post> getAllPosts(int page, int size) {
        return postRepository.findAllByOrderByCreatedAtDesc();
    }

    public List<Post> getUserPosts(Long userId, int page, int size) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        return postRepository.findByAuthorId(userId);
    }

    public Post updatePost(Long postId, String authorEmail, String content, String image) {
        Post post = getPostById(postId);
        User author = userRepository.findByEmail(authorEmail)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        if (!post.getAuthor().getId().equals(author.getId())) {
            throw new RuntimeException("Unauthorized to update this post");
        }
        
        if (content != null && !content.isEmpty()) {
            post.setContent(content);
        }
        if (image != null && !image.isEmpty()) {
            post.setImage(image);
        }
        
        return postRepository.save(post);
    }

    public void deletePost(Long postId, String authorEmail) {
        Post post = getPostById(postId);
        User author = userRepository.findByEmail(authorEmail)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        if (!post.getAuthor().getId().equals(author.getId())) {
            throw new RuntimeException("Unauthorized to delete this post");
        }
        
        postRepository.deleteById(postId);
    }

    public List<Post> getUserFeed(String userEmail, int page, int size) {
        User user = userRepository.findByEmail(userEmail)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        List<Long> followingIds = user.getFollowing().stream()
            .map(User::getId)
            .toList();
        
        return postRepository.findAllByOrderByCreatedAtDesc().stream()
            .filter(post -> followingIds.contains(post.getAuthor().getId()))
            .limit(size)
            .toList();
    }

    public void likePost(Long postId, String userEmail) {
        Post post = getPostById(postId);
        User user = userRepository.findByEmail(userEmail)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        if (!post.getLikes().contains(user)) {
            post.getLikes().add(user);
            postRepository.save(post);
        }
    }

    public void unlikePost(Long postId, String userEmail) {
        Post post = getPostById(postId);
        User user = userRepository.findByEmail(userEmail)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        post.getLikes().remove(user);
        postRepository.save(post);
    }
}

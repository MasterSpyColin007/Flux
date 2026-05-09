package com.example.flux.repository;

import com.example.flux.model.Post;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {
	List<Post> findByAuthorIdOrderByCreatedAtDesc(Long authorId);
	List<Post> findAllByOrderByCreatedAtDesc();
}

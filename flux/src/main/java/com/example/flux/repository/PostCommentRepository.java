package com.example.flux.repository;

import com.example.flux.model.PostComment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PostCommentRepository extends JpaRepository<PostComment, Long> {
	List<PostComment> findByPostIdAndParentIsNullOrderByCreatedAtAsc(Long postId);
	void deleteByPostId(Long postId);
}

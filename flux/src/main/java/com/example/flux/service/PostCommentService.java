package com.example.flux.service;

import com.example.flux.model.Post;
import com.example.flux.model.PostComment;
import com.example.flux.model.User;
import com.example.flux.repository.PostCommentRepository;
import com.example.flux.repository.PostRepository;
import com.example.flux.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PostCommentService {

	private final PostCommentRepository commentRepository;
	private final PostRepository postRepository;
	private final UserRepository userRepository;

	public PostCommentService(PostCommentRepository commentRepository, PostRepository postRepository,
							  UserRepository userRepository) {
		this.commentRepository = commentRepository;
		this.postRepository = postRepository;
		this.userRepository = userRepository;
	}

	public List<PostComment> getCommentsForPost(Long postId) {
		return commentRepository.findByPostIdAndParentIsNullOrderByCreatedAtAsc(postId);
	}

	public PostComment addComment(Long postId, String username, String content) {
		return saveComment(postId, username, content, null);
	}

	public PostComment addReply(Long postId, Long parentId, String username, String content) {
		PostComment parent = commentRepository.findById(parentId)
			.orElseThrow(() -> new IllegalArgumentException("Comment not found."));
		if (!parent.getPost().getId().equals(postId)) {
			throw new IllegalArgumentException("Reply must belong to this post.");
		}
		return saveComment(postId, username, content, parent);
	}

	private PostComment saveComment(Long postId, String username, String content, PostComment parent) {
		if (content == null || content.isBlank()) {
			throw new IllegalArgumentException("Comment cannot be empty.");
		}
		Post post = postRepository.findById(postId)
			.orElseThrow(() -> new IllegalArgumentException("Post not found."));
		User author = userRepository.findByUsername(username)
			.orElseThrow(() -> new IllegalArgumentException("User not found."));

		PostComment comment = new PostComment();
		comment.setPost(post);
		comment.setAuthor(author);
		comment.setParent(parent);
		comment.setContent(content.trim());
		return commentRepository.save(comment);
	}
}

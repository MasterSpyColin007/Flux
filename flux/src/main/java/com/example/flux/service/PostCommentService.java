package com.example.flux.service;

import com.example.flux.model.Post;
import com.example.flux.model.PostComment;
import com.example.flux.model.User;
import com.example.flux.repository.PostCommentRepository;
import com.example.flux.repository.PostRepository;
import com.example.flux.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

	public PostComment updateComment(Long postId, Long commentId, String username, boolean canModerate, String content) {
		if (!canModerate) {
			throw new IllegalArgumentException("Only admins can edit comments.");
		}
		if (content == null || content.isBlank()) {
			throw new IllegalArgumentException("Comment cannot be empty.");
		}
		PostComment comment = findCommentOnPost(postId, commentId);
		comment.setContent(content.trim());
		return commentRepository.save(comment);
	}

	@Transactional
	public void deleteComment(Long postId, Long commentId, String username, boolean canModerate) {
		PostComment comment = findCommentOnPost(postId, commentId);
		User actor = userRepository.findByUsername(username)
			.orElseThrow(() -> new IllegalArgumentException("User not found."));
		if (!canModerate && !canDeleteReply(comment, actor)) {
			throw new IllegalArgumentException("You cannot delete this comment.");
		}
		commentRepository.delete(comment);
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

	private PostComment findCommentOnPost(Long postId, Long commentId) {
		PostComment comment = commentRepository.findById(commentId)
			.orElseThrow(() -> new IllegalArgumentException("Comment not found."));
		if (!comment.getPost().getId().equals(postId)) {
			throw new IllegalArgumentException("Comment must belong to this post.");
		}
		return comment;
	}

	private boolean canDeleteReply(PostComment comment, User actor) {
		PostComment parent = comment.getParent();
		return parent != null && (
			parent.getAuthor().getId().equals(actor.getId())
				|| comment.getAuthor().getId().equals(actor.getId())
		);
	}
}

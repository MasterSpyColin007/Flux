package com.example.flux.service;

import com.example.flux.model.Post;
import com.example.flux.model.User;
import com.example.flux.repository.PostCommentRepository;
import com.example.flux.repository.PostRepository;
import com.example.flux.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

@Service
public class PostService {

	private final PostRepository postRepository;
	private final UserRepository userRepository;
	private final PostCommentRepository commentRepository;

	public PostService(PostRepository postRepository, UserRepository userRepository,
					   PostCommentRepository commentRepository) {
		this.postRepository = postRepository;
		this.userRepository = userRepository;
		this.commentRepository = commentRepository;
	}

	public Post createPost(String authorIdentifier, String title, String content, String imageUrl) {
		User author = findUser(authorIdentifier);
		Post post = new Post();
		post.setAuthor(author);
		post.setTitle(title == null || title.isBlank() ? "Untitled post" : title.trim());
		post.setContent(content);
		post.setImageUrl(normalizeImageUrl(imageUrl));
		return postRepository.save(post);
	}

	public Post getPostById(Long postId) {
		return postRepository.findById(postId)
			.orElseThrow(() -> new IllegalArgumentException("Post not found."));
	}

	public Post getManageablePost(Long postId, String actorIdentifier, boolean canModerate) {
		Post post = getPostById(postId);
		User actor = findUser(actorIdentifier);
		ensureCanManage(post, actor, canModerate);
		return post;
	}

	public List<Post> getAllPosts() {
		return postRepository.findAllByOrderByCreatedAtDesc();
	}

	public List<Post> getUserPosts(Long userId) {
		return postRepository.findByAuthorIdOrderByCreatedAtDesc(userId);
	}

	public Post updatePost(Long postId, String authorIdentifier, String title, String content, String imageUrl) {
		return updatePost(postId, authorIdentifier, title, content, imageUrl, false);
	}

	public Post updatePost(Long postId, String actorIdentifier, String title, String content, String imageUrl,
						   boolean canModerate) {
		Post post = getPostById(postId);
		User actor = findUser(actorIdentifier);
		ensureCanManage(post, actor, canModerate);

		if (title != null && !title.isBlank()) {
			post.setTitle(title.trim());
		}
		if (content != null && !content.isBlank()) {
			post.setContent(content);
		}
		if (imageUrl != null) {
			post.setImageUrl(normalizeImageUrl(imageUrl));
		}

		return postRepository.save(post);
	}

	@Transactional
	public void deletePost(Long postId, String authorIdentifier) {
		deletePost(postId, authorIdentifier, false);
	}

	@Transactional
	public void deletePost(Long postId, String actorIdentifier, boolean canModerate) {
		Post post = getPostById(postId);
		User actor = findUser(actorIdentifier);
		ensureCanManage(post, actor, canModerate);
		commentRepository.deleteByPostId(postId);
		postRepository.delete(post);
	}

	private User findUser(String identifier) {
		if (identifier == null || identifier.isBlank()) {
			throw new IllegalArgumentException("Author is required.");
		}

		return userRepository.findByEmail(identifier.trim())
			.or(() -> userRepository.findByUsername(identifier.trim()))
			.orElseThrow(() -> new IllegalArgumentException("User not found."));
	}

	private void ensureCanManage(Post post, User actor, boolean canModerate) {
		if (canModerate && "ROLE_ADMIN".equals(actor.getRole())) {
			return;
		}
		if (!post.getAuthor().getId().equals(actor.getId())) {
			throw new IllegalArgumentException("Only the post author can change this post.");
		}
	}

	private String normalizeImageUrl(String imageUrl) {
		if (imageUrl == null || imageUrl.isBlank()) {
			return null;
		}
		String trimmedImageUrl = imageUrl.trim();
		try {
			URI uri = new URI(trimmedImageUrl);
			String scheme = uri.getScheme();
			if (scheme == null || (!scheme.equalsIgnoreCase("http") && !scheme.equalsIgnoreCase("https"))
					|| uri.getHost() == null) {
				throw new IllegalArgumentException("Image URL must be an externally hosted http or https URL.");
			}
			return trimmedImageUrl;
		} catch (URISyntaxException ex) {
			throw new IllegalArgumentException("Image URL must be an externally hosted http or https URL.");
		}
	}
}

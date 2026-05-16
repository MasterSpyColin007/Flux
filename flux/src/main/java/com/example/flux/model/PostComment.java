package com.example.flux.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "post_comments")
public class PostComment {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(optional = false)
	@JoinColumn(name = "post_id")
	private Post post;

	@ManyToOne(optional = false)
	@JoinColumn(name = "user_id")
	private User author;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "parent_id")
	private PostComment parent;

	@OneToMany(mappedBy = "parent", cascade = CascadeType.REMOVE)
	@OrderBy("createdAt ASC")
	private List<PostComment> replies = new ArrayList<>();

	@Column(nullable = false, length = 1000)
	private String content;

	@Column(name = "created_at", nullable = false)
	private LocalDateTime createdAt;

	public Long getId() { return id; }
	public void setId(Long id) { this.id = id; }

	public Post getPost() { return post; }
	public void setPost(Post post) { this.post = post; }

	public User getAuthor() { return author; }
	public void setAuthor(User author) { this.author = author; }

	public PostComment getParent() { return parent; }
	public void setParent(PostComment parent) { this.parent = parent; }

	public List<PostComment> getReplies() { return replies; }
	public void setReplies(List<PostComment> replies) { this.replies = replies; }

	public String getContent() { return content; }
	public void setContent(String content) { this.content = content; }

	public LocalDateTime getCreatedAt() { return createdAt; }
	public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

	@PrePersist
	void setDefaults() {
		if (createdAt == null) {
			createdAt = LocalDateTime.now();
		}
	}
}

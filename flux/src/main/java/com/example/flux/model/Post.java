package com.example.flux.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "posts")
public class Post {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private String title;

	@Column(nullable = false, length = 2000)
	private String content;

	// Flux stores references to externally hosted images; it does not store image files.
	@Column(name = "image_url")
	private String imageUrl;

	@Column(name = "created_at", nullable = false)
	private LocalDateTime createdAt;

	@ManyToOne(optional = false)
	@JoinColumn(name = "user_id")
	private User author;

	public Long getId() { return id; }
	public void setId(Long id) { this.id = id; }

	public String getTitle() { return title; }
	public void setTitle(String title) { this.title = title; }

	public String getContent() { return content; }
	public void setContent(String content) { this.content = content; }

	public String getImageUrl() { return imageUrl; }
	public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

	public LocalDateTime getCreatedAt() { return createdAt; }
	public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

	public User getAuthor() { return author; }
	public void setAuthor(User author) { this.author = author; }

	@PrePersist
	void setDefaults() {
		if (createdAt == null) {
			createdAt = LocalDateTime.now();
		}
	}
}

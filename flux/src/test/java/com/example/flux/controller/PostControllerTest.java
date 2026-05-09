package com.example.flux.controller;

import com.example.flux.model.Post;
import com.example.flux.model.User;
import com.example.flux.repository.PostRepository;
import com.example.flux.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class PostControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private PostRepository postRepository;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Test
	void createsPostWithImageUrl() throws Exception {
		User author = saveAuthor("imageauthor", "imageauthor@example.com");

		mockMvc.perform(post("/api/posts")
				.with(user("imageauthor").roles("USER"))
				.with(csrf())
				.contentType("application/json")
				.content("""
					{
					  "author": "imageauthor@example.com",
					  "title": "Launch day",
					  "content": "Flux has image posts now.",
					  "imageUrl": "https://example.com/launch.png"
					}
					"""))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.title").value("Launch day"))
			.andExpect(jsonPath("$.imageUrl").value("https://example.com/launch.png"))
			.andExpect(jsonPath("$.author.username").value("imageauthor"));

		assertEquals(1, postRepository.findByAuthorIdOrderByCreatedAtDesc(author.getId()).size());
	}

	@Test
	void listsPostsNewestFirst() throws Exception {
		User author = saveAuthor("feedauthor", "feedauthor@example.com");
		Post older = new Post();
		older.setAuthor(author);
		older.setTitle("Older");
		older.setContent("Older post");
		older.setCreatedAt(LocalDateTime.now().minusDays(1));
		postRepository.save(older);

		Post newer = new Post();
		newer.setAuthor(author);
		newer.setTitle("Newer");
		newer.setContent("Newer post");
		newer.setCreatedAt(LocalDateTime.now());
		postRepository.save(newer);

		mockMvc.perform(get("/api/posts").with(user("feedauthor").roles("USER")))
			.andExpect(status().isOk())
			.andExpect(content().string(containsString("Newer")))
			.andExpect(jsonPath("$[0].title").value("Newer"));
	}

	@Test
	void rejectsUnknownAuthor() throws Exception {
		mockMvc.perform(post("/api/posts")
				.with(user("missingauthor").roles("USER"))
				.with(csrf())
				.contentType("application/json")
				.content("""
					{
					  "author": "nobody@example.com",
					  "content": "This should not save."
					}
					"""))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.error").value("User not found."));

		assertTrue(postRepository.findAll().stream()
			.noneMatch(post -> "This should not save.".equals(post.getContent())));
	}

	@Test
	void rejectsImageUrlThatIsNotExternallyHosted() throws Exception {
		saveAuthor("badimageauthor", "badimageauthor@example.com");

		mockMvc.perform(post("/api/posts")
				.with(user("badimageauthor").roles("USER"))
				.with(csrf())
				.contentType("application/json")
				.content("""
					{
					  "author": "badimageauthor@example.com",
					  "content": "This image URL should fail.",
					  "imageUrl": "/uploads/local.png"
					}
					"""))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.error").value("Image URL must be an externally hosted http or https URL."));
	}

	private User saveAuthor(String username, String email) {
		return userRepository.findByUsername(username)
			.orElseGet(() -> {
				User user = new User(username, passwordEncoder.encode("password"), "ROLE_USER");
				user.setEmail(email);
				return userRepository.save(user);
			});
	}
}

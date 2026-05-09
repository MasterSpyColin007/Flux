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

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@SpringBootTest
@AutoConfigureMockMvc
class PostPageControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private PostRepository postRepository;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Test
	void postListPageLoads() throws Exception {
		mockMvc.perform(get("/posts").with(user("postviewer").roles("USER")))
			.andExpect(status().isOk())
			.andExpect(view().name("posts"))
			.andExpect(content().string(containsString("Create Post")));
	}

	@Test
	void postCreatePageLoads() throws Exception {
		mockMvc.perform(get("/posts/create").with(user("postcreator").roles("USER")))
			.andExpect(status().isOk())
			.andExpect(view().name("post-form"))
			.andExpect(content().string(containsString("Create Post")));
	}

	@Test
	void userCanCreateEditViewAndDeleteOwnPost() throws Exception {
		saveAuthor("postowner", "postowner@example.com");

		mockMvc.perform(post("/posts/create")
				.with(user("postowner").roles("USER"))
				.with(csrf())
				.param("title", "Original title")
				.param("content", "Original content")
				.param("imageUrl", "https://example.com/original.png"))
			.andExpect(status().is3xxRedirection())
			.andExpect(redirectedUrlPattern("/posts/*"));

		Post post = postRepository.findAll().stream()
			.filter(savedPost -> "Original title".equals(savedPost.getTitle()))
			.findFirst()
			.orElseThrow();

		mockMvc.perform(get("/posts/" + post.getId()).with(user("postowner").roles("USER")))
			.andExpect(status().isOk())
			.andExpect(view().name("post-detail"))
			.andExpect(content().string(containsString("Original content")));

		mockMvc.perform(get("/posts/" + post.getId() + "/edit").with(user("postowner").roles("USER")))
			.andExpect(status().isOk())
			.andExpect(view().name("post-form"))
			.andExpect(content().string(containsString("Edit Post")));

		mockMvc.perform(post("/posts/" + post.getId() + "/edit")
				.with(user("postowner").roles("USER"))
				.with(csrf())
				.param("title", "Updated title")
				.param("content", "Updated content")
				.param("imageUrl", "https://example.com/updated.png"))
			.andExpect(status().is3xxRedirection())
			.andExpect(redirectedUrlPattern("/posts/*"));

		assertEquals("Updated title", postRepository.findById(post.getId()).get().getTitle());

		mockMvc.perform(get("/posts/" + post.getId() + "/delete").with(user("postowner").roles("USER")))
			.andExpect(status().isOk())
			.andExpect(view().name("post-delete"))
			.andExpect(content().string(containsString("Delete Post")));

		mockMvc.perform(post("/posts/" + post.getId() + "/delete")
				.with(user("postowner").roles("USER"))
				.with(csrf()))
			.andExpect(status().is3xxRedirection())
			.andExpect(redirectedUrl("/posts"));

		assertFalse(postRepository.findById(post.getId()).isPresent());
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

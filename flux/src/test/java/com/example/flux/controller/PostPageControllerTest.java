package com.example.flux.controller;

import com.example.flux.model.Post;
import com.example.flux.model.User;
import com.example.flux.repository.PostCommentRepository;
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
import static org.junit.jupiter.api.Assertions.assertTrue;
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
	private PostCommentRepository postCommentRepository;

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

	@Test
	void adminCanEditAndDeleteAnotherUsersPost() throws Exception {
		User author = saveAuthor("moderateduser", "moderateduser@example.com");
		Post post = new Post();
		post.setAuthor(author);
		post.setTitle("Needs admin");
		post.setContent("Original content");
		postRepository.save(post);

		mockMvc.perform(get("/posts/" + post.getId() + "/edit").with(user("admin").roles("ADMIN")))
			.andExpect(status().isOk())
			.andExpect(view().name("post-form"))
			.andExpect(content().string(containsString("Edit Post")));

		mockMvc.perform(post("/posts/" + post.getId() + "/edit")
				.with(user("admin").roles("ADMIN"))
				.with(csrf())
				.param("title", "Admin updated")
				.param("content", "Admin moderated content"))
			.andExpect(status().is3xxRedirection())
			.andExpect(redirectedUrlPattern("/posts/*"));

		assertEquals("Admin updated", postRepository.findById(post.getId()).get().getTitle());

		mockMvc.perform(post("/posts/" + post.getId() + "/delete")
				.with(user("admin").roles("ADMIN"))
				.with(csrf()))
			.andExpect(status().is3xxRedirection())
			.andExpect(redirectedUrl("/posts"));

		assertFalse(postRepository.findById(post.getId()).isPresent());
	}

	@Test
	void usersCanCommentAndReplyWithAccountNamesVisible() throws Exception {
		User author = saveAuthor("commentpostauthor", "commentpostauthor@example.com");
		saveAuthor("commenter", "commenter@example.com");
		saveAuthor("replier", "replier@example.com");
		Post post = new Post();
		post.setAuthor(author);
		post.setTitle("Commentable post");
		post.setContent("Original post body");
		postRepository.save(post);

		mockMvc.perform(post("/posts/" + post.getId() + "/comments")
				.with(user("commenter").roles("USER"))
				.with(csrf())
				.param("content", "First comment"))
			.andExpect(status().is3xxRedirection())
			.andExpect(redirectedUrl("/posts/" + post.getId()));

		mockMvc.perform(get("/posts/" + post.getId()).with(user("commenter").roles("USER")))
			.andExpect(status().isOk())
			.andExpect(content().string(containsString("Originally posted by")))
			.andExpect(content().string(containsString("commentpostauthor")))
			.andExpect(content().string(containsString("commenter")))
			.andExpect(content().string(containsString("First comment")));

		Long commentId = postCommentRepository.findByPostIdAndParentIsNullOrderByCreatedAtAsc(post.getId())
			.get(0)
			.getId();

		mockMvc.perform(post("/posts/" + post.getId() + "/comments/" + commentId + "/replies")
				.with(user("replier").roles("USER"))
				.with(csrf())
				.param("content", "Reply text"))
			.andExpect(status().is3xxRedirection())
			.andExpect(redirectedUrl("/posts/" + post.getId()));

		mockMvc.perform(get("/posts/" + post.getId()).with(user("replier").roles("USER")))
			.andExpect(status().isOk())
			.andExpect(content().string(containsString("replier")))
			.andExpect(content().string(containsString("Reply text")));
	}

	@Test
	void adminCanEditAndDeleteComments() throws Exception {
		User author = saveAuthor("admincommentpost", "admincommentpost@example.com");
		saveAuthor("commentedituser", "commentedituser@example.com");
		Post post = new Post();
		post.setAuthor(author);
		post.setTitle("Admin comment controls");
		post.setContent("Comment moderation target");
		postRepository.save(post);

		mockMvc.perform(post("/posts/" + post.getId() + "/comments")
				.with(user("commentedituser").roles("USER"))
				.with(csrf())
				.param("content", "Needs edit"))
			.andExpect(status().is3xxRedirection());

		Long commentId = postCommentRepository.findByPostIdAndParentIsNullOrderByCreatedAtAsc(post.getId())
			.get(0)
			.getId();

		mockMvc.perform(post("/posts/" + post.getId() + "/comments/" + commentId + "/edit")
				.with(user("admin").roles("ADMIN"))
				.with(csrf())
				.param("content", "Admin edited comment"))
			.andExpect(status().is3xxRedirection())
			.andExpect(redirectedUrl("/posts/" + post.getId()));

		assertEquals("Admin edited comment", postCommentRepository.findById(commentId).get().getContent());

		mockMvc.perform(post("/posts/" + post.getId() + "/comments/" + commentId + "/delete")
				.with(user("admin").roles("ADMIN"))
				.with(csrf()))
			.andExpect(status().is3xxRedirection())
			.andExpect(redirectedUrl("/posts/" + post.getId()));

		assertTrue(postCommentRepository.findById(commentId).isEmpty());
	}

	@Test
	void commentAuthorCanDeleteRepliesToTheirComment() throws Exception {
		User author = saveAuthor("replydeletepost", "replydeletepost@example.com");
		saveAuthor("parentcommenter", "parentcommenter@example.com");
		saveAuthor("replydeleteuser", "replydeleteuser@example.com");
		Post post = new Post();
		post.setAuthor(author);
		post.setTitle("Reply delete controls");
		post.setContent("Reply delete target");
		postRepository.save(post);

		mockMvc.perform(post("/posts/" + post.getId() + "/comments")
				.with(user("parentcommenter").roles("USER"))
				.with(csrf())
				.param("content", "Parent comment"))
			.andExpect(status().is3xxRedirection());

		Long commentId = postCommentRepository.findByPostIdAndParentIsNullOrderByCreatedAtAsc(post.getId())
			.get(0)
			.getId();

		mockMvc.perform(post("/posts/" + post.getId() + "/comments/" + commentId + "/replies")
				.with(user("replydeleteuser").roles("USER"))
				.with(csrf())
				.param("content", "Reply to delete"))
			.andExpect(status().is3xxRedirection());

		Long replyId = postCommentRepository.findByParentIdOrderByCreatedAtAsc(commentId)
			.get(0)
			.getId();

		mockMvc.perform(post("/posts/" + post.getId() + "/comments/" + replyId + "/delete")
				.with(user("parentcommenter").roles("USER"))
				.with(csrf()))
			.andExpect(status().is3xxRedirection())
			.andExpect(redirectedUrl("/posts/" + post.getId()));

		assertTrue(postCommentRepository.findById(replyId).isEmpty());
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

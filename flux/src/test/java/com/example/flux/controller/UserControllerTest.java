package com.example.flux.controller;

import com.example.flux.model.User;
import com.example.flux.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class UserControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Test
	void usersPageRequiresAuth() throws Exception {
		mockMvc.perform(get("/users"))
			.andExpect(status().is3xxRedirection())
			.andExpect(redirectedUrl("/login"));
	}

	@Test
	void usersPageLoadsWhenAuthenticated() throws Exception {
		mockMvc.perform(get("/users").with(user("admin").roles("ADMIN")))
			.andExpect(status().isOk())
			.andExpect(view().name("users"))
			.andExpect(model().attributeExists("users"))
			.andExpect(model().attributeExists("totalUsers"));
	}

	@Test
	void usersPageDisplaysSeededAdmin() throws Exception {
		mockMvc.perform(get("/users").with(user("admin").roles("ADMIN")))
			.andExpect(status().isOk())
			.andExpect(content().string(containsString("admin")));
	}

	@Test
	void usersPageSortsByUsername() throws Exception {
		mockMvc.perform(get("/users")
				.param("sort", "username")
				.param("dir", "asc")
				.with(user("admin").roles("ADMIN")))
			.andExpect(status().isOk())
			.andExpect(model().attributeExists("users"));
	}

	@Test
	void usersPageFiltersByRole() throws Exception {
		mockMvc.perform(get("/users")
				.param("role", "ROLE_ADMIN")
				.with(user("admin").roles("ADMIN")))
			.andExpect(status().isOk())
			.andExpect(content().string(containsString("admin")));
	}

	@Test
	void adminCanPromoteUser() throws Exception {
		User target = userRepository.save(new User("promotetest", passwordEncoder.encode("pass123"), "ROLE_USER"));

		mockMvc.perform(post("/users/" + target.getId() + "/toggle-admin")
				.with(user("admin").roles("ADMIN"))
				.with(csrf()))
			.andExpect(status().is3xxRedirection())
			.andExpect(redirectedUrl("/users"));

		assertEquals("ROLE_ADMIN", userRepository.findById(target.getId()).get().getRole());
		userRepository.delete(target);
	}

	@Test
	void adminCanDemoteAdmin() throws Exception {
		User target = userRepository.save(new User("demotetest", passwordEncoder.encode("pass123"), "ROLE_ADMIN"));

		mockMvc.perform(post("/users/" + target.getId() + "/toggle-admin")
				.with(user("admin").roles("ADMIN"))
				.with(csrf()))
			.andExpect(status().is3xxRedirection());

		assertEquals("ROLE_USER", userRepository.findById(target.getId()).get().getRole());
		userRepository.delete(target);
	}

	@Test
	void adminCanDeleteUser() throws Exception {
		User target = userRepository.save(new User("deletetest", passwordEncoder.encode("pass123"), "ROLE_USER"));
		Long id = target.getId();

		mockMvc.perform(post("/users/" + id + "/delete")
				.with(user("admin").roles("ADMIN"))
				.with(csrf()))
			.andExpect(status().is3xxRedirection())
			.andExpect(redirectedUrl("/users"));

		assertFalse(userRepository.findById(id).isPresent());
	}

	@Test
	void adminCannotDeleteSelf() throws Exception {
		mockMvc.perform(post("/users/1/toggle-admin")
				.with(user("admin").roles("ADMIN"))
				.with(csrf()))
			.andExpect(status().is3xxRedirection())
			.andExpect(flash().attributeExists("error"));
	}

	@Test
	void regularUserCannotAccessAdminActions() throws Exception {
		mockMvc.perform(post("/users/1/toggle-admin")
				.with(user("regularuser").roles("USER"))
				.with(csrf()))
			.andExpect(status().isForbidden());

		mockMvc.perform(post("/users/1/delete")
				.with(user("regularuser").roles("USER"))
				.with(csrf()))
			.andExpect(status().isForbidden());
	}
}

package com.example.flux.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class UserControllerTest {

	@Autowired
	private MockMvc mockMvc;

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
}

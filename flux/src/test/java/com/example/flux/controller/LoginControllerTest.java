package com.example.flux.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class LoginControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void loginPageLoads() throws Exception {
		mockMvc.perform(get("/login"))
			.andExpect(status().isOk())
			.andExpect(view().name("login"));
	}

	@Test
	void loginPageShowsErrorOnBadCredentials() throws Exception {
		mockMvc.perform(post("/login")
				.param("username", "wrong")
				.param("password", "wrong")
				.with(csrf()))
			.andExpect(status().is3xxRedirection())
			.andExpect(redirectedUrl("/login?error=true"));
	}

	@Test
	void loginPageDisplaysErrorMessage() throws Exception {
		mockMvc.perform(get("/login").param("error", "true"))
			.andExpect(status().isOk())
			.andExpect(model().attributeExists("error"))
			.andExpect(content().string(containsString("Invalid username or password")));
	}

	@Test
	void loginPageDisplaysLogoutMessage() throws Exception {
		mockMvc.perform(get("/login").param("logout", "true"))
			.andExpect(status().isOk())
			.andExpect(model().attributeExists("logout"))
			.andExpect(content().string(containsString("You have been logged out")));
	}

	@Test
	void loginSucceedsWithDbUser() throws Exception {
		mockMvc.perform(post("/login")
				.param("username", "admin")
				.param("password", "password")
				.with(csrf()))
			.andExpect(status().is3xxRedirection())
			.andExpect(redirectedUrl("/"));
	}

	@Test
	void unauthenticatedAccessRedirectsToLogin() throws Exception {
		mockMvc.perform(get("/"))
			.andExpect(status().is3xxRedirection())
			.andExpect(redirectedUrl("/login"));
	}
}

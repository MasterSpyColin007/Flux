package com.example.flux.controller;

import com.example.flux.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class RegistrationControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private UserRepository userRepository;

	@Test
	void registrationPageLoads() throws Exception {
		mockMvc.perform(get("/register"))
			.andExpect(status().isOk())
			.andExpect(view().name("register"))
			.andExpect(content().string(containsString("Create your account")));
	}

	@Test
	void registrationFailsWithShortUsername() throws Exception {
		mockMvc.perform(post("/register")
				.param("username", "ab")
				.param("password", "password123")
				.param("confirmPassword", "password123")
				.with(csrf()))
			.andExpect(status().isOk())
			.andExpect(model().attributeExists("error"))
			.andExpect(content().string(containsString("at least 3 characters")));
	}

	@Test
	void registrationFailsWithShortPassword() throws Exception {
		mockMvc.perform(post("/register")
				.param("username", "newuser")
				.param("password", "short")
				.param("confirmPassword", "short")
				.with(csrf()))
			.andExpect(status().isOk())
			.andExpect(model().attributeExists("error"))
			.andExpect(content().string(containsString("at least 6 characters")));
	}

	@Test
	void registrationFailsWhenPasswordsDontMatch() throws Exception {
		mockMvc.perform(post("/register")
				.param("username", "newuser")
				.param("password", "password123")
				.param("confirmPassword", "different456")
				.with(csrf()))
			.andExpect(status().isOk())
			.andExpect(model().attributeExists("error"))
			.andExpect(content().string(containsString("do not match")));
	}

	@Test
	void registrationFailsWhenUsernameTaken() throws Exception {
		mockMvc.perform(post("/register")
				.param("username", "admin")
				.param("password", "password123")
				.param("confirmPassword", "password123")
				.with(csrf()))
			.andExpect(status().isOk())
			.andExpect(model().attributeExists("error"))
			.andExpect(content().string(containsString("already taken")));
	}

	@Test
	void registrationSucceedsAndRedirects() throws Exception {
		mockMvc.perform(post("/register")
				.param("username", "testuser")
				.param("password", "password123")
				.param("confirmPassword", "password123")
				.with(csrf()))
			.andExpect(status().is3xxRedirection())
			.andExpect(redirectedUrl("/login?registered=true"));

		assertTrue(userRepository.findByUsername("testuser").isPresent());
	}

	@Test
	void newUserCanLoginAfterRegistration() throws Exception {
		mockMvc.perform(post("/register")
				.param("username", "logintest")
				.param("password", "password123")
				.param("confirmPassword", "password123")
				.with(csrf()))
			.andExpect(status().is3xxRedirection());

		mockMvc.perform(post("/login")
				.param("username", "logintest")
				.param("password", "password123")
				.with(csrf()))
			.andExpect(status().is3xxRedirection())
			.andExpect(redirectedUrl("/"));
	}
}

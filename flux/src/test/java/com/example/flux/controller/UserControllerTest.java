package com.example.flux.controller;

import com.example.flux.model.User;
import com.example.flux.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
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

	@Autowired
	private JdbcTemplate jdbcTemplate;

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
			.andExpect(model().attributeExists("totalUsers"))
			.andExpect(model().attributeExists("tableSummaries"));
	}

	@Test
	void usersPageRequiresAdminRole() throws Exception {
		mockMvc.perform(get("/users").with(user("regularuser").roles("USER")))
			.andExpect(status().isForbidden());
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
	void usersPageFallsBackForUnknownSort() throws Exception {
		mockMvc.perform(get("/users")
				.param("sort", "missing")
				.with(user("admin").roles("ADMIN")))
			.andExpect(status().isOk())
			.andExpect(model().attribute("currentSort", "username"));
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
	@Transactional
	void adminCannotDeleteProtectedUserIdNine() throws Exception {
		saveProtectedUserNine();

		mockMvc.perform(post("/users/9/delete")
				.with(user("admin").roles("ADMIN"))
				.with(csrf()))
			.andExpect(status().is3xxRedirection())
			.andExpect(redirectedUrl("/users"))
			.andExpect(flash().attribute("error", "User ID 9 cannot be deleted."));

		assertTrue(userRepository.findById(9L).isPresent());
	}

	@Test
	void adminCanDeactivateAndReactivateUser() throws Exception {
		User target = userRepository.save(new User("deactivatetest", passwordEncoder.encode("pass123"), "ROLE_USER"));

		mockMvc.perform(post("/users/" + target.getId() + "/toggle-enabled")
				.with(user("admin").roles("ADMIN"))
				.with(csrf()))
			.andExpect(status().is3xxRedirection())
			.andExpect(redirectedUrl("/users"))
			.andExpect(flash().attribute("success", "deactivatetest has been deactivated."));

		assertFalse(userRepository.findById(target.getId()).get().isEnabled());

		mockMvc.perform(post("/users/" + target.getId() + "/toggle-enabled")
				.with(user("admin").roles("ADMIN"))
				.with(csrf()))
			.andExpect(status().is3xxRedirection())
			.andExpect(redirectedUrl("/users"))
			.andExpect(flash().attribute("success", "deactivatetest has been reactivated."));

		assertTrue(userRepository.findById(target.getId()).get().isEnabled());
		userRepository.delete(target);
	}

	@Test
	void userCanTogglePersistentDarkMode() throws Exception {
		User target = userRepository.save(new User("darkmodetest", passwordEncoder.encode("pass123"), "ROLE_USER"));

		mockMvc.perform(post("/theme/toggle")
				.with(user("darkmodetest").roles("USER"))
				.with(csrf()))
			.andExpect(status().is3xxRedirection());

		assertTrue(userRepository.findById(target.getId()).get().isDarkMode());

		mockMvc.perform(get("/posts").with(user("darkmodetest").roles("USER")))
			.andExpect(status().isOk())
			.andExpect(content().string(containsString("dark-mode")));

		userRepository.delete(target);
	}

	@Test
	void adminCannotDeactivateSelf() throws Exception {
		mockMvc.perform(post("/users/1/toggle-enabled")
				.with(user("admin").roles("ADMIN"))
				.with(csrf()))
			.andExpect(status().is3xxRedirection())
			.andExpect(flash().attribute("error", "You cannot deactivate your own account."));
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
	@Transactional
	void cannotDemoteLastAdmin() throws Exception {
		User target = userRepository.save(new User("lastadmindemote", passwordEncoder.encode("pass123"), "ROLE_ADMIN"));
		userRepository.findAll().stream()
			.filter(user -> !user.getId().equals(target.getId()))
			.filter(user -> "ROLE_ADMIN".equals(user.getRole()))
			.forEach(user -> {
				user.setRole("ROLE_USER");
				userRepository.save(user);
			});

		mockMvc.perform(post("/users/" + target.getId() + "/toggle-admin")
				.with(user("outsideadmin").roles("ADMIN"))
				.with(csrf()))
			.andExpect(status().is3xxRedirection())
			.andExpect(flash().attribute("error", "At least one admin account must remain."));

		assertEquals("ROLE_ADMIN", userRepository.findById(target.getId()).get().getRole());
	}

	@Test
	@Transactional
	void cannotDeleteLastAdmin() throws Exception {
		User target = userRepository.save(new User("lastadmindelete", passwordEncoder.encode("pass123"), "ROLE_ADMIN"));
		userRepository.findAll().stream()
			.filter(user -> !user.getId().equals(target.getId()))
			.filter(user -> "ROLE_ADMIN".equals(user.getRole()))
			.forEach(user -> {
				user.setRole("ROLE_USER");
				userRepository.save(user);
			});

		mockMvc.perform(post("/users/" + target.getId() + "/delete")
				.with(user("outsideadmin").roles("ADMIN"))
				.with(csrf()))
			.andExpect(status().is3xxRedirection())
			.andExpect(flash().attribute("error", "At least one admin account must remain."));

		assertTrue(userRepository.findById(target.getId()).isPresent());
	}

	@Test
	@Transactional
	void apiCannotDeleteLastAdmin() throws Exception {
		User target = userRepository.save(new User("lastadminapi", passwordEncoder.encode("pass123"), "ROLE_ADMIN"));
		userRepository.findAll().stream()
			.filter(user -> !user.getId().equals(target.getId()))
			.filter(user -> "ROLE_ADMIN".equals(user.getRole()))
			.forEach(user -> {
				user.setRole("ROLE_USER");
				userRepository.save(user);
			});

		mockMvc.perform(delete("/api/users/" + target.getId())
				.with(user("outsideadmin").roles("ADMIN"))
				.with(csrf()))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.error").value("At least one admin account must remain."));

		assertTrue(userRepository.findById(target.getId()).isPresent());
	}

	@Test
	@Transactional
	void apiCannotDeleteProtectedUserIdNine() throws Exception {
		saveProtectedUserNine();

		mockMvc.perform(delete("/api/users/9")
				.with(user("admin").roles("ADMIN"))
				.with(csrf()))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.error").value("User ID 9 cannot be deleted."));

		assertTrue(userRepository.findById(9L).isPresent());
	}

	@Test
	@Transactional
	void cannotDeactivateLastActiveAdmin() throws Exception {
		User target = userRepository.save(new User("lastactiveadmin", passwordEncoder.encode("pass123"), "ROLE_ADMIN"));
		userRepository.findAll().stream()
			.filter(user -> !user.getId().equals(target.getId()))
			.filter(user -> "ROLE_ADMIN".equals(user.getRole()))
			.forEach(user -> {
				user.setEnabled(false);
				userRepository.save(user);
			});

		mockMvc.perform(post("/users/" + target.getId() + "/toggle-enabled")
				.with(user("outsideadmin").roles("ADMIN"))
				.with(csrf()))
			.andExpect(status().is3xxRedirection())
			.andExpect(flash().attribute("error", "At least one active admin account must remain."));

		assertTrue(userRepository.findById(target.getId()).get().isEnabled());
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

		mockMvc.perform(post("/users/1/toggle-enabled")
				.with(user("regularuser").roles("USER"))
				.with(csrf()))
			.andExpect(status().isForbidden());
	}

	@Test
	void databaseApiRequiresAdminRole() throws Exception {
		mockMvc.perform(get("/api/database/tables").with(user("regularuser").roles("USER")))
			.andExpect(status().isForbidden());
	}

	@Test
	void adminCanListDatabaseTables() throws Exception {
		mockMvc.perform(get("/api/database/tables").with(user("admin").roles("ADMIN")))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$[?(@.name == 'USERS' || @.name == 'users')]").exists());
	}

	@Test
	void adminCanGetRowsFromKnownTable() throws Exception {
		mockMvc.perform(get("/api/database/tables/users").with(user("admin").roles("ADMIN")))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$[0].USERNAME").value("admin"));
	}

	@Test
	void unknownTableReturnsBadRequest() throws Exception {
		mockMvc.perform(get("/api/database/tables/not_a_table").with(user("admin").roles("ADMIN")))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.error").value("Unknown database table: not_a_table"));
	}

	private void saveProtectedUserNine() {
		jdbcTemplate.update("""
			MERGE INTO users (id, username, password, enabled, role) KEY (id)
			VALUES (9, 'protected9', ?, true, 'ROLE_USER')
			""", passwordEncoder.encode("pass123"));
	}
}

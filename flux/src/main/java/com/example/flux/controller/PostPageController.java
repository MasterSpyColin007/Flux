package com.example.flux.controller;

import com.example.flux.model.Post;
import com.example.flux.service.PostService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

@Controller
public class PostPageController {

	private final PostService postService;

	public PostPageController(PostService postService) {
		this.postService = postService;
	}

	@GetMapping("/posts")
	public String listPosts(Model model) {
		model.addAttribute("posts", postService.getAllPosts());
		return "posts";
	}

	@GetMapping("/posts/create")
	public String showCreateForm(Model model) {
		model.addAttribute("mode", "create");
		model.addAttribute("post", new Post());
		return "post-form";
	}

	@PostMapping("/posts/create")
	public String createPost(@RequestParam(required = false) String title,
							 @RequestParam String content,
							 @RequestParam(required = false) String imageUrl,
							 Principal principal,
							 RedirectAttributes redirectAttributes) {
		try {
			Post post = postService.createPost(principal.getName(), title, content, imageUrl);
			redirectAttributes.addFlashAttribute("success", "Post created.");
			return "redirect:/posts/" + post.getId();
		} catch (IllegalArgumentException ex) {
			redirectAttributes.addFlashAttribute("error", ex.getMessage());
			return "redirect:/posts/create";
		}
	}

	@GetMapping("/posts/{id}")
	public String viewPost(@PathVariable Long id, Model model) {
		model.addAttribute("post", postService.getPostById(id));
		return "post-detail";
	}

	@GetMapping("/posts/{id}/edit")
	public String showEditForm(@PathVariable Long id, Model model) {
		model.addAttribute("mode", "edit");
		model.addAttribute("post", postService.getPostById(id));
		return "post-form";
	}

	@PostMapping("/posts/{id}/edit")
	public String updatePost(@PathVariable Long id,
							 @RequestParam(required = false) String title,
							 @RequestParam String content,
							 @RequestParam(required = false) String imageUrl,
							 Principal principal,
							 RedirectAttributes redirectAttributes) {
		try {
			postService.updatePost(id, principal.getName(), title, content, imageUrl);
			redirectAttributes.addFlashAttribute("success", "Post updated.");
			return "redirect:/posts/" + id;
		} catch (IllegalArgumentException ex) {
			redirectAttributes.addFlashAttribute("error", ex.getMessage());
			return "redirect:/posts/" + id + "/edit";
		}
	}

	@GetMapping("/posts/{id}/delete")
	public String showDeleteConfirmation(@PathVariable Long id, Model model) {
		model.addAttribute("post", postService.getPostById(id));
		return "post-delete";
	}

	@PostMapping("/posts/{id}/delete")
	public String deletePost(@PathVariable Long id, Principal principal, RedirectAttributes redirectAttributes) {
		try {
			postService.deletePost(id, principal.getName());
			redirectAttributes.addFlashAttribute("success", "Post deleted.");
			return "redirect:/posts";
		} catch (IllegalArgumentException ex) {
			redirectAttributes.addFlashAttribute("error", ex.getMessage());
			return "redirect:/posts/" + id + "/delete";
		}
	}
}

package com.example.flux.controller;

import com.example.flux.model.Post;
import com.example.flux.service.PostCommentService;
import com.example.flux.service.PostService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class PostPageController {

	private final PostService postService;
	private final PostCommentService commentService;

	public PostPageController(PostService postService, PostCommentService commentService) {
		this.postService = postService;
		this.commentService = commentService;
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
							 Authentication authentication,
							 RedirectAttributes redirectAttributes) {
		try {
			Post post = postService.createPost(authentication.getName(), title, content, imageUrl);
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
		model.addAttribute("comments", commentService.getCommentsForPost(id));
		return "post-detail";
	}

	@PostMapping("/posts/{id}/comments")
	public String addComment(@PathVariable Long id,
							 @RequestParam String content,
							 Authentication authentication,
							 RedirectAttributes redirectAttributes) {
		try {
			commentService.addComment(id, authentication.getName(), content);
			redirectAttributes.addFlashAttribute("success", "Comment added.");
		} catch (IllegalArgumentException ex) {
			redirectAttributes.addFlashAttribute("error", ex.getMessage());
		}
		return "redirect:/posts/" + id;
	}

	@PostMapping("/posts/{id}/comments/{commentId}/replies")
	public String addReply(@PathVariable Long id,
						   @PathVariable Long commentId,
						   @RequestParam String content,
						   Authentication authentication,
						   RedirectAttributes redirectAttributes) {
		try {
			commentService.addReply(id, commentId, authentication.getName(), content);
			redirectAttributes.addFlashAttribute("success", "Reply added.");
		} catch (IllegalArgumentException ex) {
			redirectAttributes.addFlashAttribute("error", ex.getMessage());
		}
		return "redirect:/posts/" + id;
	}

	@PostMapping("/posts/{id}/comments/{commentId}/edit")
	public String updateComment(@PathVariable Long id,
								@PathVariable Long commentId,
								@RequestParam String content,
								Authentication authentication,
								RedirectAttributes redirectAttributes) {
		try {
			commentService.updateComment(id, commentId, authentication.getName(), isAdmin(authentication), content);
			redirectAttributes.addFlashAttribute("success", "Comment updated.");
		} catch (IllegalArgumentException ex) {
			redirectAttributes.addFlashAttribute("error", ex.getMessage());
		}
		return "redirect:/posts/" + id;
	}

	@PostMapping("/posts/{id}/comments/{commentId}/delete")
	public String deleteComment(@PathVariable Long id,
								@PathVariable Long commentId,
								Authentication authentication,
								RedirectAttributes redirectAttributes) {
		try {
			commentService.deleteComment(id, commentId, authentication.getName(), isAdmin(authentication));
			redirectAttributes.addFlashAttribute("success", "Comment deleted.");
		} catch (IllegalArgumentException ex) {
			redirectAttributes.addFlashAttribute("error", ex.getMessage());
		}
		return "redirect:/posts/" + id;
	}

	@GetMapping("/posts/{id}/edit")
	public String showEditForm(@PathVariable Long id, Authentication authentication, Model model,
							   RedirectAttributes redirectAttributes) {
		try {
			Post post = postService.getManageablePost(id, authentication.getName(), isAdmin(authentication));
			model.addAttribute("mode", "edit");
			model.addAttribute("post", post);
			return "post-form";
		} catch (IllegalArgumentException ex) {
			redirectAttributes.addFlashAttribute("error", ex.getMessage());
			return "redirect:/posts/" + id;
		}
	}

	@PostMapping("/posts/{id}/edit")
	public String updatePost(@PathVariable Long id,
							 @RequestParam(required = false) String title,
							 @RequestParam String content,
							 @RequestParam(required = false) String imageUrl,
							 Authentication authentication,
							 RedirectAttributes redirectAttributes) {
		try {
			postService.updatePost(id, authentication.getName(), title, content, imageUrl, isAdmin(authentication));
			redirectAttributes.addFlashAttribute("success", "Post updated.");
			return "redirect:/posts/" + id;
		} catch (IllegalArgumentException ex) {
			redirectAttributes.addFlashAttribute("error", ex.getMessage());
			return "redirect:/posts/" + id + "/edit";
		}
	}

	@GetMapping("/posts/{id}/delete")
	public String showDeleteConfirmation(@PathVariable Long id, Authentication authentication, Model model,
										 RedirectAttributes redirectAttributes) {
		try {
			model.addAttribute("post", postService.getManageablePost(id, authentication.getName(), isAdmin(authentication)));
			return "post-delete";
		} catch (IllegalArgumentException ex) {
			redirectAttributes.addFlashAttribute("error", ex.getMessage());
			return "redirect:/posts/" + id;
		}
	}

	@PostMapping("/posts/{id}/delete")
	public String deletePost(@PathVariable Long id, Authentication authentication, RedirectAttributes redirectAttributes) {
		try {
			postService.deletePost(id, authentication.getName(), isAdmin(authentication));
			redirectAttributes.addFlashAttribute("success", "Post deleted.");
			return "redirect:/posts";
		} catch (IllegalArgumentException ex) {
			redirectAttributes.addFlashAttribute("error", ex.getMessage());
			return "redirect:/posts/" + id + "/delete";
		}
	}

	private boolean isAdmin(Authentication authentication) {
		return authentication.getAuthorities().stream()
			.map(GrantedAuthority::getAuthority)
			.anyMatch("ROLE_ADMIN"::equals);
	}
}

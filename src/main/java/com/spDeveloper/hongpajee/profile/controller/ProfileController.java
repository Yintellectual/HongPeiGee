package com.spDeveloper.hongpajee.profile.controller;

import static com.spDeveloper.hongpajee.user.entity.UserRole.*;

import java.security.Principal;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.spDeveloper.hongpajee.live.controller.LiveController;
import com.spDeveloper.hongpajee.navbar.service.NavbarRepository;
import com.spDeveloper.hongpajee.post.entity.Article;
import com.spDeveloper.hongpajee.post.repository.ArticleRepository;
import com.spDeveloper.hongpajee.tag.service.TagPool;
import com.spDeveloper.hongpajee.user.config.RedisUserDetailsManager;

@Controller
public class ProfileController {

	@Autowired
	NavbarRepository navbarRepository;
	@Autowired
	ArticleRepository articleRepository;
	@Autowired
	LiveController liveController;
	@Autowired
	TagPool<Article> tagPool;
	@Autowired
	RedisUserDetailsManager redisUserDetailsManager;

	DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss:nnnnnnnnnnnn")
			.withZone(ZoneId.systemDefault());

	/**
	 * You will never want a user open the profile page of another user.
	 */
	@GetMapping("/profile/{username}")
	public String displayProfile(@PathVariable("username") String username, Model model, Principal principal) {

		String principle_name = null;
		if (principal == null || principal.getName() == null) {
			return "redirect:/error?falseAuthentication";
		} else {
			principle_name = principal.getName();
			if (!principle_name.equals(username)) {
				return "redirect:/error?falseAuthentication";
			} else {
				// good authentication
				model.addAttribute("navItems", navbarRepository.getReadOnly());
				model.addAttribute("currentPageId", "index");
				model.addAttribute("username", username);
				
				UserDetails userDetails = redisUserDetailsManager.loadUserByUsername(username);
				Set<String> roles = userDetails.getAuthorities().stream().map(au->au.getAuthority()).collect(Collectors.toSet());
				
				if(roles.contains("ROLE_OWNER")) {
					model.addAttribute("announcement", liveController.getAnnouncement());
				}
				
				model.addAttribute("roles", roles);
				
				return "profile";
			}
		}
	}
	
	@PostMapping("/user/profile/changePassword")
	public String changePassword(@RequestParam("newPassword1") String newPassword1,@RequestParam("newPassword2") String newPassword2, @RequestParam("oldPassword") String oldPassword, Principal principal) {
		String username = principal.getName();
		String newPassword = "";
		if(newPassword1!=null&&newPassword1.equals(newPassword2)) {
			newPassword = newPassword1;
		}else {
			return "redirect:/profile/"+ username;
		}
		redisUserDetailsManager.changePassword(oldPassword, newPassword);
		return "redirect:/profile/"+ username;
	}
}

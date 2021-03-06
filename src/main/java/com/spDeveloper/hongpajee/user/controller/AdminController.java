package com.spDeveloper.hongpajee.user.controller;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.spDeveloper.hongpajee.aliyun.ApsaraEmbassador;
import com.spDeveloper.hongpajee.aop.aspect.ArchiveAspect;
import com.spDeveloper.hongpajee.navbar.service.NavbarRepository;
import com.spDeveloper.hongpajee.opinion.repository.LikeRepository;
import com.spDeveloper.hongpajee.opinion.repository.ReplyRepository;
import com.spDeveloper.hongpajee.post.controller.ArticleController;
import com.spDeveloper.hongpajee.post.entity.Article;
import com.spDeveloper.hongpajee.post.repository.ArticleRepository;
import com.spDeveloper.hongpajee.profile.repository.UserDescriptionRepository;
import com.spDeveloper.hongpajee.tag.service.TagPool;
import com.spDeveloper.hongpajee.user.config.RedisUserDetailsManager;
import com.spDeveloper.hongpajee.user.entity.UserRole;
import com.spDeveloper.hongpajee.util.map.AccumulatorMap;
import com.spDeveloper.hongpajee.video.repository.VideoRepository;

@Controller
public class AdminController {
	@Autowired
	NavbarRepository navbarRepository;
	@Autowired
	ArticleRepository articleRepository;
	@Autowired
	LikeRepository likeRepository;
	@Autowired
	ReplyRepository replyRepository;
	@Autowired
	TagPool<Article> tagPool;
	@Autowired
	RedisUserDetailsManager userDetailsManager;
	@Autowired
	AccumulatorMap accumulatorMap;
	@Autowired
	VideoRepository videoRepository;
	@Autowired
	ApsaraEmbassador apsaraEmbassador;
	@Autowired
	ArchiveAspect archiveAspect;
	@Autowired
	ArticleController articleController;
	@Autowired
	UserDescriptionRepository userDescriptionRepository;
	
	@GetMapping("/owner/users/management")
	public String userManagement(Model model, Principal principal) {
		
		articleController.addCommonModleArrtibutes(model, principal);
		
		List<UserDetails> owners = userDetailsManager.loadUserByRole(UserRole.ROLE_OWNER);
		List<UserDetails> admins = userDetailsManager.loadUserByRole(UserRole.ROLE_ADMIN);
		List<UserDetails> users = userDetailsManager.loadUserByRole(UserRole.ROLE_USER);
		model.addAttribute("owners", owners);
		model.addAttribute("admins", admins);
		model.addAttribute("users", users);
		
		model.addAttribute("nicknames", userDescriptionRepository.getNickNames(userDetailsManager.getUsernames()));
		
		return "user_management";
	}
	@GetMapping("/owner/admins/demote")
	public String demoteAdmin(@RequestParam("username") String username) {
		userDetailsManager.updateRole(username, UserRole.ROLE_USER);
		return "redirect:/owner/users/management";
	}
	@GetMapping("/owner/users/promote")
	public String promoteUser(@RequestParam("username") String username) {
		userDetailsManager.updateRole(username, UserRole.ROLE_ADMIN);
		return "redirect:/owner/users/management";
	}

	@GetMapping("/owner/admins/archieve")
	@ResponseBody
	public String showArchieves() {
		return archiveAspect.readArchive().stream().map(s->"<p><pre>"+s+"</pre></p>").collect(Collectors.joining("<hr/>"));
	} 
	
	@GetMapping("/owner/admins/archieve/delete")
	public String deleteArchieves(Principal principal) {
		archiveAspect.cleanArchive();
		return "redirect:/profile/"+principal.getName();
	}
}

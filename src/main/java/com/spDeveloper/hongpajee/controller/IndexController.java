package com.spDeveloper.hongpajee.controller;

import java.security.Principal;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.spDeveloper.hongpajee.navbar.service.NavbarRepository;

import com.spDeveloper.hongpajee.post.entity.Article;
import com.spDeveloper.hongpajee.post.repository.ArticleRepository;
import com.spDeveloper.hongpajee.tag.service.TagPool;

@Controller
public class IndexController {
 
	@Autowired
	NavbarRepository navbarRepository;
	@Autowired
	ArticleRepository articleRepository;
	@Autowired
	TagPool<Article> tagPool;
	
	DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss").withZone(ZoneId.systemDefault());
	
	@GetMapping("/")
	public String index(Model model, Principal principal) {
		
//		model.addAttribute("navItems", navbarRepository.getReadOnly());
//		model.addAttribute("currentPageId", "index");
//		List<Article> articles = new ArrayList<>(tagPool.getByTag("homepage"));
//		Collections.sort(articles);
//		
//		
//		model.addAttribute("articles", articles);
//		model.addAttribute("df", df);
//		
//		String username = null; 
//		if(principal==null||principal.getName()==null) {
//			username="null";
//		}else {
//			username = principal.getName();
//		}
//		
//		model.addAttribute("username", username);
//		
		return "redirect:/tag?tag=homepage";
	}
}

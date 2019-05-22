package com.spDeveloper.hongpajee.controller;

import java.security.Principal;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Set;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.spDeveloper.hongpajee.navbar.service.NavbarRepository;
import com.spDeveloper.hongpajee.opinion.repository.LikeRepository;
import com.spDeveloper.hongpajee.opinion.repository.ReplyRepository;
import com.spDeveloper.hongpajee.post.controller.ArticleController;
import com.spDeveloper.hongpajee.post.entity.Article;
import com.spDeveloper.hongpajee.post.repository.ArticleRepository;
import com.spDeveloper.hongpajee.tag.service.TagPool;
import com.spDeveloper.hongpajee.util.map.AccumulatorMap;

@Controller
public class IndexController {

	@Autowired
	NavbarRepository navbarRepository;
	@Autowired
	ArticleRepository articleRepository;
	@Autowired
	LikeRepository likeRepository;
	@Autowired
	AccumulatorMap accumulatorMap;
	@Autowired
	ReplyRepository replyRepository;
	@Autowired
	TagPool<Article> tagPool;

	@Autowired
	ArticleController articleController;
	
	DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss").withZone(ZoneId.systemDefault());

	@RequestMapping(value = "/", method = RequestMethod.GET)
	public String index(Model model, Principal principal) {
		//add common model attributes
		articleController.addCommonModleArrtibutes(model, principal);
		
		//add cards by tag homepage
		List<Article> articles = new ArrayList<>(articleRepository.findByTag("homepage"));
		Collections.sort(articles);
		articles.forEach(arti -> {
			arti.setExtension("likeCount", "" + likeRepository.getLikeCount(arti.getUuid()));
			if (principal != null) {
				arti.setExtension("isLike", "" + likeRepository.isLike(arti.getUuid(), principal.getName()));
			} else {
				arti.setExtension("isLike", "" + false);
			}
			arti.setExtension("viewCount", "" + accumulatorMap.get(arti.getUuid()));
			arti.setExtension("replyCount", "" + replyRepository.getCount(arti.getUuid()));
		});
		model.addAttribute("articles", articles);
		
		//add currentPageId
		model.addAttribute("currentPageId", "index");
		
		//add announcement
		
		//add FAQ
		
		
		return "index";
	}
}

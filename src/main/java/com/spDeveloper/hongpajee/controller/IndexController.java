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

	@RequestMapping(value = "/", method = RequestMethod.GET)
	public String index(Model model, Principal principal) {

		// model.addAttribute("navItems", navbarRepository.getReadOnly());
		// model.addAttribute("currentPageId", "index");
		// List<Article> articles = new ArrayList<>(tagPool.getByTag("homepage"));
		// Collections.sort(articles);
		//
		//
		// model.addAttribute("articles", articles);
		// model.addAttribute("df", df);
		//
		// String username = null;
		// if(principal==null||principal.getName()==null) {
		// username="null";
		// }else {
		// username = principal.getName();
		// }
		//
		// model.addAttribute("username", username);
		//
		return "redirect:/tag?tag=homepage";
	}

	@PostMapping(value = { "test", "test/v3" }, consumes = "application/json", produces = "application/json")
	@ResponseBody
	public JsonObject echo(HttpServletRequest request, HttpEntity<JsonObject> body) {
		System.out.println("==================================>>>>>>>>>>>>>>>");
		System.out.println("headers");
		Enumeration<String> headerNames = request.getHeaderNames();
		if (headerNames.hasMoreElements()) {
			String headerName = headerNames.nextElement();
			System.out.println(headerName + ": " + request.getHeader(headerName));
		}
		System.out.println("----------------------------------------");
		System.out.println("body");
		System.out.println(body.getBody());

		double time = body.getBody().get("time").getAsDouble();

		System.out.println("<<<<<<<<<<<<==================================");
		JsonObject result = new JsonObject();
		result.add("code", new JsonPrimitive(0));
		result.add("msg", new JsonPrimitive("ok"));
		JsonArray jsonArray = new JsonArray();
		for (int i = 0; i < 10; i++) {
			JsonObject copy = body.getBody().deepCopy();
			copy.addProperty("time", time+i);
			jsonArray.add(copy);
		}

		result.add("danmuku", jsonArray);

		return result;
	}

	@GetMapping(value = { "test", "test/v3" }, consumes = "application/json", produces = "application/json")
	@ResponseBody
	public String echoGet(HttpServletRequest request) {
		System.out.println("==================================>>>>>>>>>>>>>>>");
		System.out.println("headers");
		Enumeration<String> headerNames = request.getHeaderNames();
		if (headerNames.hasMoreElements()) {
			String headerName = headerNames.nextElement();
			System.out.println(headerName + ": " + request.getHeader(headerName));
		}

		System.out.println("<<<<<<<<<<<<==================================");
		return "";
	}
}

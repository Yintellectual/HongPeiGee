package com.spDeveloper.hongpajee.opinion.like.controller;

import java.security.Principal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.spDeveloper.hongpajee.opinion.repository.LikeRepository;



@Controller
public class LikeController {
	@Autowired
	LikeRepository opinionRepository;
	
	private final String ARTICLE_UUID = "articleUuid"; 
	
	@PostMapping(value="like",  produces = "application/json", 
			consumes = {"application/json"})
	@ResponseBody
	public JsonObject like(@RequestBody JsonObject json, Principal principal) {
		JsonObject result = new JsonObject(); 
		
		if(!json.has(ARTICLE_UUID)) {
			throw new RuntimeException("article uuid not provided"); 
		}else if(principal==null){
			throw new RuntimeException("can not like before log in");
		}
		
		String articleUuid =  json.get(ARTICLE_UUID).getAsString();
		String userUuid = principal.getName();	
		opinionRepository.like(articleUuid, userUuid);
		
		result.add("count", new JsonPrimitive(opinionRepository.getLikeCount(articleUuid)+""));
		return result;

	}
	
	
	
	@PostMapping(value="un_like",  produces = "application/json", 
			consumes = {"application/json"})
	@ResponseBody
	public JsonObject unLike(@RequestBody JsonObject json, Principal principal) {
		JsonObject result = new JsonObject(); 
		
		if(!json.has(ARTICLE_UUID)) {
			throw new RuntimeException("article uuid not provided"); 
		}else if(principal==null){
			throw new RuntimeException("can not unlike before log in");
		}
		
		String articleUuid =  json.get(ARTICLE_UUID).getAsString();
		String userUuid = principal.getName();	
		opinionRepository.unlike(articleUuid, userUuid);
		
		result.add("count", new JsonPrimitive(opinionRepository.getLikeCount(articleUuid)+""));
		return result;

	}
	
	
	
}

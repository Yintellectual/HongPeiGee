package com.spDeveloper.hongpajee.opinion.reply.controller;

import java.security.Principal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.gson.JsonObject;
import com.spDeveloper.hongpajee.opinion.reply.entity.Reply;
import com.spDeveloper.hongpajee.opinion.repository.LikeRepository;
import com.spDeveloper.hongpajee.opinion.repository.ReplyRepository;

@Controller
public class ReplyController {

	@Autowired
	ReplyRepository replyRepository;
	@Autowired
	LikeRepository likeRepository;
	
	
	@PostMapping("/reply/{articleUuid}")
	public String reply(@PathVariable("articleUuid") String articleUuid, @RequestParam String message, Principal principal) {
		
		if(principal==null) {
			return "redirect:/login/login";
		}
		
		String username = principal.getName();
		Reply reply = new Reply(message, username, articleUuid);
		replyRepository.add(reply);
		
		return "redirect:/article/"+articleUuid;
	}
	@PostMapping(value="/admin/reply/delete",  produces = "application/json", 
			consumes = {"application/json"})
	@ResponseBody
	public String reply(@RequestBody JsonObject json) {
		String replyUuid = json.get("replyUuid").getAsString();
		
		likeRepository.removeArticle(replyUuid);
		replyRepository.remove(replyUuid);
		
		JsonObject result = new JsonObject();
		result.addProperty("status", "success");
		return result.toString();
	}
}

package com.spDeveloper.hongpajee.message.controller;

import java.security.Principal;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.gson.Gson;
import com.spDeveloper.hongpajee.garbageCollector.entity.GarbageCollector;
import com.spDeveloper.hongpajee.message.entity.HeavyMessage;
import com.spDeveloper.hongpajee.message.entity.Message;
import com.spDeveloper.hongpajee.message.entity.content.Content;
import com.spDeveloper.hongpajee.message.entity.content.HTMLContent;
import com.spDeveloper.hongpajee.message.entity.content.proxy.HTMLContentVirtualProxy;
import com.spDeveloper.hongpajee.message.entity.content.repository.ContentRepository;
import com.spDeveloper.hongpajee.message.entity.util.MessageUtils;
import com.spDeveloper.hongpajee.message.repository.MessageRepository;

@Controller
public class MessageTestController {

	@Autowired
	Gson gson;
	@Autowired
	MessageRepository genericMessageRepository;
	@Autowired
	GarbageCollector garbageCollector;
	@Autowired
	ContentRepository contentRepository;
	
	
	@PostMapping("/user/message/test")
	@ResponseBody
	public String testMessage(@RequestParam("input") String input, Principal principal) {
		String username = principal.getName();
		
		//Message htmlMessage = MessageUtils.createHTMLMessage(username, input);
		
		//genericMessageRepository.save(htmlMessage);
		
		//Message recoveredMessage = genericMessageRepository.find(htmlMessage.getUuid(), HTMLMessage.class);
				
		HTMLContent htmlContent = new HTMLContent(input);
		HTMLContentVirtualProxy htmlContentVirtualProxy = new HTMLContentVirtualProxy(htmlContent);
		
		System.out.println("Controller: "+gson.toJson(htmlContentVirtualProxy));
		contentRepository.add(htmlContentVirtualProxy);
		
		return "";
	}
	@GetMapping("/user/message/test")
	public String messageTestInput() {
		return "message_test"; 
	}
	@GetMapping("/user/content/test")
	@ResponseBody
	public String contentHTML(@RequestParam("uuid") String uuid) {
		Content content = contentRepository.find(uuid);
		return content.getClass().getName()+": <hr/>"+content.getString();
	}
	
	@GetMapping("/test/gc")
	@ResponseBody
	public String gc() {
		garbageCollector.collectAll();
		return "Carbage Collected.";
	}
	
	//list all messages 
	
	//load html on request
	
}

package com.spDeveloper.hongpajee.faq.controller;

import java.security.Principal;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.spDeveloper.hongpajee.faq.repository.FAQAnswerService;
import com.spDeveloper.hongpajee.faq.repository.FAQInboxService;
import com.spDeveloper.hongpajee.faq.repository.FAQQuestionAnswerRelationshipService;
import com.spDeveloper.hongpajee.message.entity.Message;
import com.spDeveloper.hongpajee.message.entity.util.MessageUtils;
import com.spDeveloper.hongpajee.post.controller.ArticleController;
import com.spDeveloper.hongpajee.profile.entity.UserDescription;
import com.spDeveloper.hongpajee.profile.repository.UserDescriptionRepository;

@Controller
public class FAQController {

	@Autowired
	Gson gson;
	@Autowired
	FAQInboxService faqInboxService;
	@Autowired
	FAQAnswerService faqAnswerService;
	@Autowired
	FAQQuestionAnswerRelationshipService faqQuestionAnswerRelationshipService;
	@Autowired
	UserDescriptionRepository userDescriptionRepository;
	@Autowired
	ArticleController articleController;

	@PostMapping(value = "/faq/believer/pray", produces = "application/json", consumes = { "application/json" })
	@ResponseBody
	public JsonObject handlePrayer(@RequestBody JsonObject input, Principal principal) {
		boolean success = true;
		String errorMessage = null;

		try {
			String prayer = input.get("prayer").getAsString();
			Boolean no_need_answer = false;
			if (input.has("no_need_answer") && "on".equals(input.get("no_need_answer").getAsString())) {
				no_need_answer = true;
			}
			System.out.println(no_need_answer);
			System.out.println("*******Debugging************");
			
			Message message = MessageUtils.createTextMessage(principal.getName(), prayer);

			System.out.println(message.getClass());
			System.out.println(message.getContentString());

			faqInboxService.add(message);

		} catch (Exception e) {
			success = false;
			errorMessage = "未知错误:" + e.getMessage();
			e.printStackTrace();
		}

		JsonObject result = new JsonObject();
		if (success) {
			result.addProperty("status", "success");
		} else {
			result.addProperty("status", "error");
			JsonObject errorJson = new JsonObject();
			errorJson.addProperty("msg", errorMessage);
			result.add("error", errorJson);
		}
		return result;
	}

	@GetMapping(value = "/faq/god/prayer/answer")
	public String answerPrayer(@RequestParam("uuid") String uuid, Model model, Principal principal) throws InterruptedException, TimeoutException {
		articleController.addCommonModleArrtibutes(model, principal);
		model.addAttribute("uuid", uuid);
		Message message = faqInboxService.find(uuid);
		model.addAttribute("content", message.getContentString());
		model.addAttribute("believer_username", message.getUsername());
		UserDescription userDescription = userDescriptionRepository.get(message.getUsername());
		String nickname = message.getUsername();
		if(userDescription!=null&&userDescription.getNickname()!=null) {
			nickname = userDescription.getNickname();
		}
		model.addAttribute("believer_nickname", nickname);
		model.addAttribute("timestamp", message.getTimestamp());
		model.addAttribute("isImportant", faqInboxService.isMessageImportant(uuid));
		model.addAttribute("isPinned", faqInboxService.isUserPinned(message.getUsername()));
		return "prayer_answer";
	}
	
	@PostMapping(value="/faq/god/prayer/answer")
	public String receiveAnswer(@RequestParam("uuid") String question, @RequestParam("answer") String input, Principal principal) {
		
		Message message = MessageUtils.createHTMLMessage(principal.getName(), input);
		faqAnswerService.add(message);
		faqQuestionAnswerRelationshipService.addRelationship(question, message.getUuid());
		return "redirect:/faq/god/prayer/management";
	}
	
	@GetMapping(value = "/faq/god/answers/delete")
	@ResponseBody
	public JsonObject deleteAnswer(@RequestParam("uuid") String uuid) {
		faqAnswerService.remove(uuid);
		JsonObject result = new JsonObject();
		result.addProperty("status", "success");
		return result;
	}
	
	@GetMapping(value = "/faq/answers/list")
	@ResponseBody
	public JsonObject listAnswers() {
		JsonObject result = new JsonObject();
		JsonArray answers = new JsonArray();
		faqAnswerService.getAllAnswers().stream().map(answer->{
			JsonObject jsonObject = new JsonObject();
			JsonArray questionsJson = new JsonArray();
			faqQuestionAnswerRelationshipService.listQuestions(answer.getUuid()).stream().map(qstn->{
				JsonObject qstnJson = new JsonObject();
				qstnJson.addProperty("nickname", nickname(qstn.getUsername()));
				qstnJson.addProperty("timestamp", qstn.getTimestamp());
				qstnJson.addProperty("uuid", qstn.getUuid());
				qstnJson.addProperty("content", qstn.getContentString());
				return qstnJson;
			}).forEach(questionsJson::add);
			jsonObject.add("questions", questionsJson);
			jsonObject.addProperty("nickname", nickname(answer.getUsername()));
			jsonObject.addProperty("timestamp", answer.getTimestamp());
			jsonObject.addProperty("uuid", answer.getUuid());
			jsonObject.addProperty("content", answer.getContentString());
			return jsonObject; 
		}).forEach(answers::add);
		result.add("data", answers);
		return result;
	}
	private String nickname(String username) {
		UserDescription userDescription = userDescriptionRepository.get(username);
		String nickname = username;
		if(userDescription != null && userDescription.getNickname()!=null) {
			nickname = userDescription.getNickname();
		}
		return nickname;
	}
	
	@GetMapping(value = "/faq/god/prayer/list")
	@ResponseBody
	public JsonObject listPrayers(@RequestParam(value="believer", required=true) String believer) throws InterruptedException, TimeoutException {
		JsonObject result = new JsonObject();
		JsonArray data = new JsonArray();
		List<Message> messages = null;
		messages = faqInboxService.findByUser(believer);
		
		messages.forEach(msg -> {
			JsonObject message = new JsonObject();
			message.addProperty("uuid", msg.getUuid());
			message.addProperty("timestamp", msg.getTimestamp());
			UserDescription userDescription = userDescriptionRepository.get(msg.getUsername());
			String nickname = null;
			if(userDescription ==null) {
				
			}else {
				nickname = userDescription.getNickname();
			}
			
			if(nickname==null) {
				nickname = msg.getUsername();
			}	
			
			message.addProperty("username", nickname);
			message.addProperty("isRead", faqInboxService.isMessageRead(msg.getUuid())+"");
			message.addProperty("isImportant", faqInboxService.isMessageImportant(msg.getUuid())+"");
			data.add(gson.toJsonTree(message));
		});
		result.add("data", data);
		return result;
	}

	@GetMapping("/faq/god/prayer/management")
	public String managePrayers(Model model, Principal principal) {
		articleController.addCommonModleArrtibutes(model, principal);
		return "prayer_management";
	}
	
	@GetMapping(value = "/faq/god/prayer/content/html")
	@ResponseBody
	public JsonObject showHtmlContent(@RequestParam("uuid") String uuid) throws InterruptedException, TimeoutException {
		Message message = faqInboxService.find(uuid);
		String html = null;
		if (message != null) {
			html = message.getContentString();
		}

		JsonObject result = new JsonObject();
		result.addProperty("data", html);
		return result;
	}
	
	@GetMapping(value = "/faq/god/believer/hide")
	@ResponseBody
	public JsonObject hideUser(@RequestParam("username") String username) {
		faqInboxService.hideUser(username);
		JsonObject result = new JsonObject();
		result.addProperty("status", "success");
		return result;
	}
	
	@GetMapping(value = "/faq/god/believer/hide/toggle")
	@ResponseBody
	public JsonObject hideUsertoggle(@RequestParam("username") String username) {
		boolean isHidden = faqInboxService.hideUserToggle(username);
		JsonObject result = new JsonObject();
		result.addProperty("status", "success");
		JsonObject data = new JsonObject();
		data.addProperty("isHidden", isHidden+"");
		result.add("data", data);
		return result;
	}
	
	@GetMapping(value = "/faq/god/believer/pin/toggle")
	@ResponseBody
	public JsonObject pinUserToggle(@RequestParam("username") String username) {
		boolean isPinned = faqInboxService.pinUserToggle(username);
		JsonObject result = new JsonObject();
		result.addProperty("status", "success");
		JsonObject data = new JsonObject();
		data.addProperty("isPinned", isPinned+"");
		result.add("data", data);
		return result;
	}
	
	@GetMapping(value = "/faq/god/message/read/toggle")
	@ResponseBody
	public JsonObject toggleMessageRead(@RequestParam("uuid") String uuid) {
		boolean isRead = faqInboxService.toggleMessageRead(uuid);
		JsonObject result = new JsonObject();
		result.addProperty("status", "success");
		JsonObject data = new JsonObject();
		data.addProperty("isRead", isRead+"");
		result.add("data", data);
		return result;
	}
	
	@GetMapping(value = "/faq/god/message/important/toggle")
	@ResponseBody
	public JsonObject toggleMessageImportant(@RequestParam("uuid") String uuid) {
		boolean isImportant= faqInboxService.toggleMessageImportant(uuid);
		JsonObject result = new JsonObject();
		result.addProperty("status", "success");
		JsonObject data = new JsonObject();
		data.addProperty("isImportant", isImportant+"");
		result.add("data", data);
		return result;
	}
	
	@GetMapping("/faq/god/believer/list")
	@ResponseBody
	public JsonObject listBelivers() {
		JsonObject result = new JsonObject();
		JsonArray data = new JsonArray();
		
		Set<String> usernames = faqInboxService.findUsers();
		List<String> pinnedUsernames = faqInboxService.listPinnedUsers();
		
		List<JsonObject> nonPinnedUsers = usernames.stream()
//				.filter(username->{
//					return !faqInboxService.isUserHidden(username);
//				})
				.filter(username->{
					return !pinnedUsernames.contains(username);
				})
				.map(this::usernameToJsonObject)
				.sorted(new Comparator<JsonObject>() {
					@Override
					public int compare(JsonObject o1, JsonObject o2) {
						return o1.get("nickname").getAsString().compareTo(o2.get("nickname").getAsString());
					}
				})
				.collect(Collectors.toList());
		
		List<JsonObject> pinnedUsers = pinnedUsernames.stream().map(this::usernameToJsonObject).collect(Collectors.toList());
		pinnedUsers.forEach(data::add);
		nonPinnedUsers.forEach(data::add);
		result.add("data",data);
		return result;
	}
	private JsonObject usernameToJsonObject(String username) {
		JsonObject beliverJson =  new JsonObject();
		beliverJson.addProperty("username", username);
		UserDescription userDescription = userDescriptionRepository.get(username);
		String nickname = null;
		if(userDescription == null) {
			nickname = username;
		}else {
			nickname = userDescription.getNickname();
			if(nickname == null) {
				nickname = username;
			}
		}
		beliverJson.addProperty("nickname", nickname);
		beliverJson.addProperty("hidden", faqInboxService.isUserHidden(username)+"");
		beliverJson.addProperty("pinned", faqInboxService.isUserPinned(username)+"");
		return beliverJson;
	}
	
	
}

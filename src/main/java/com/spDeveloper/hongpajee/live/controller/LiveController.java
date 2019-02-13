package com.spDeveloper.hongpajee.live.controller;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.gson.JsonObject;
import com.spDeveloper.hongpajee.aliyun.ApsaraEmbassador;
import com.spDeveloper.hongpajee.aliyun.ApsaraEmbassador.ApsaraLiveStream;
import com.spDeveloper.hongpajee.navbar.service.NavbarRepository;
import com.spDeveloper.hongpajee.opinion.repository.LikeRepository;
import com.spDeveloper.hongpajee.opinion.repository.ReplyRepository;
import com.spDeveloper.hongpajee.post.entity.Article;
import com.spDeveloper.hongpajee.post.repository.ArticleRepository;
import com.spDeveloper.hongpajee.tag.service.TagPool;
import com.spDeveloper.hongpajee.util.map.AccumulatorMap;
import com.spDeveloper.hongpajee.video.repository.VideoRepository;

@Controller
public class LiveController {

	private String announcement = "<h1></h1>";
	
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
	UserDetailsManager userDetailsManager;
	@Autowired
	AccumulatorMap accumulatorMap;
	@Autowired
	VideoRepository videoRepository;
	@Autowired
	ApsaraEmbassador apsaraEmbassador;

	public String getAnnouncement() {
		return announcement;
	}
	
	@PostMapping(name="/owner/live/streamKey", produces = "application/json", 
			consumes = {"application/json"})
	@ResponseBody
	public JsonObject streamKey(@RequestBody JsonObject jsonObject) {
		this.announcement = jsonObject.get("announcement").getAsString();
		ApsaraLiveStream liveStream = apsaraEmbassador.getCurrentApsaraLiveStream();
		return liveStream.toJsonObject();
	}

	@GetMapping("/user/live")
	public String live(Model model, Principal principal) {

		model.addAttribute("navItems", navbarRepository.getReadOnly());

		String username = null;
		List<String> roles = null;
		username = principal.getName();
		roles = userDetailsManager.loadUserByUsername(username).getAuthorities().stream()
					.map(ga -> ga.getAuthority()).collect(Collectors.toList());
		
		model.addAttribute("roles", roles);
		model.addAttribute("username", username);
		model.addAttribute("announcement", announcement);
		model.addAttribute("livePlayer", true);
		model.addAttribute("pullURL", apsaraEmbassador.getCurrentLivePullURL());

		return "live";
	}

}

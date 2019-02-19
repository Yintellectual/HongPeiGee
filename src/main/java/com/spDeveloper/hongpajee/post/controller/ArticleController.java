package com.spDeveloper.hongpajee.post.controller;

import java.security.Principal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.exceptions.ServerException;
import com.aliyuncs.vod.model.v20170321.CreateUploadVideoResponse;
import com.aliyuncs.vod.model.v20170321.GetVideoInfoResponse;
import com.aliyuncs.vod.model.v20170321.GetVideoPlayAuthResponse;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.spDeveloper.hongpajee.aliyun.ApsaraEmbassador;
import com.spDeveloper.hongpajee.aop.annotation.Accumulated;
import com.spDeveloper.hongpajee.aop.annotation.Archived;
import com.spDeveloper.hongpajee.exception.OutdatedEditeeException;
import com.spDeveloper.hongpajee.navbar.service.NavbarRepository;
import com.spDeveloper.hongpajee.opinion.reply.entity.Reply;
import com.spDeveloper.hongpajee.opinion.repository.LikeRepository;
import com.spDeveloper.hongpajee.opinion.repository.ReplyRepository;
import com.spDeveloper.hongpajee.post.entity.Article;
import com.spDeveloper.hongpajee.post.repository.ArticleRepository;
import com.spDeveloper.hongpajee.profile.repository.UserDescriptionRepository;
import com.spDeveloper.hongpajee.tag.service.TagPool;
import com.spDeveloper.hongpajee.util.map.AccumulatorMap;
import com.spDeveloper.hongpajee.video.repository.VideoRepository;

@Controller
public class ArticleController {

	@Autowired
	DateTimeFormatter df;

	Logger logger = LoggerFactory.getLogger(ArticleController.class);
	private final String EXPECTED_ARTICLE_TIMESTAMP = "expectedArticleTimestamp";

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
	@Autowired
	UserDescriptionRepository userDescriptionRepository;

	@PostConstruct
	public void init() {
	}

	public void addCommonModleArrtibutes(Model model, Principal principal) {
		// 1. username
		// 2. nickname @nullable
		// 3. navItems
		// 4. df
		// 5. roles
		String username = null;
		List<String> roles = null;
		if (principal != null) {
			username = principal.getName();
			roles = userDetailsManager.loadUserByUsername(username).getAuthorities().stream()
					.map(ga -> ga.getAuthority()).collect(Collectors.toList());

		} else {
			username = "null";
			roles = new ArrayList<>();

		}
		model.addAttribute("roles", roles);
		model.addAttribute("username", username);
		model.addAttribute("nickname", userDescriptionRepository.getNickName(username));
		model.addAttribute("navItems", navbarRepository.getReadOnly());
		model.addAttribute("df", df);
		
		String style = "body {	background-color: %s; }\n"+
		".card-footer { 	background-color: %s; }\n"+
".container { background-color: %s; }\n"+
		".bg-light {	background-color: %s; }";
		
		String body_bg_color = "#FC909A !important";//"#FB6775 !important";
		String card_footer_bg_color= "#FEB7BE !important";//"#FD8C97 !important";
		String container_bg_color = "#FFE3E6 !important";//"#FEB7BE !important";
		String bg_light = "#FA6D7A !important";//"#F94758 !important";
		
		model.addAttribute("style", String.format(style, body_bg_color, card_footer_bg_color, container_bg_color, bg_light));
	}

	@GetMapping("/tag")
	public String viewByTag(@RequestParam(name = "navItemId", required = false) String navItemId,
			@RequestParam("tag") List<String> tag, Model model, Principal principal,
			HttpServletRequest servletRequest) {

		addCommonModleArrtibutes(model, principal);

		List<Article> articles = new ArrayList<>(articleRepository.findByTag(tag));
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

		if (navItemId == null) {
			model.addAttribute("currentPageId", "index");
		} else {
			model.addAttribute("currentPageId", navItemId);
		}

		return "index.html";
	}

	@Archived
	@PostMapping("/admin/article/addOrUpdate")
	public String addOrUpdate(@RequestParam(name = "uuid", required = false) String uuid,
			@RequestParam("title") String title, @RequestParam("picture") String picture,
			@RequestParam("abstraction") String abstraction, @RequestParam("content") String content,
			HttpServletRequest request, @RequestParam("tags") List<String> tags,
			@RequestParam(name = "videoIds", required = false) List<String> videoIds) {

		Article article = new Article(title, abstraction, picture, content, videoIds);
		if (tags == null || tags.isEmpty()) {
			article.addTag("homepage");
		} else {
			tags.forEach(article::addTag);
		}

		if (uuid == null || uuid.isEmpty()) {
			// add
			articleRepository.add(article);
		} else {
			// update
			article.setUuid(uuid);
			articleRepository.add(article);
		}
		return "redirect:/article/" + article.getUuid();
	}

	@GetMapping("/admin/article/addOrUpdate/{uuid}")
	public String editForm(@PathVariable("uuid") String uuid, Model model, HttpServletRequest request,
			Principal principal) {
		addCommonModleArrtibutes(model, principal);
		model.addAttribute("tags", tagPool.getAllTags());
				// 0. create a repository of owned videos.
		// 1. list all owned video details
		// 2. provide a multi-select of videos in article_form.html
		// 3. article is stored with a list of video ids (has been implemented)
		// 4. on display, the controller is responsible to create a list of videos from
		// the list of video ids

		if ("new".equals(uuid)) {
			// add
		} else {
			// update
			Article article = articleRepository.find(uuid);
			if (article == null) {
				return "redirect:/error?articleNotFound";
			} else {
				articleRepository.revive(article);
				model.addAttribute("article", article);
				request.getSession().setAttribute(EXPECTED_ARTICLE_TIMESTAMP, article.getLastEditedTime());
			}
		}
		return "article_form";
	}

	@Archived
	@GetMapping("/admin/article/delete/{uuid}")
	public String delete(@PathVariable("uuid") String uuid) {

		articleRepository.delete(uuid);
		replyRepository.removeArticle(uuid);
		likeRepository.removeArticle(uuid);
		return "redirect:/";
	}

	@PostMapping(value = "/admin/video/list", produces = "application/json", consumes = { "application/json" })
	@ResponseBody
	public List<JsonObject> like(@RequestBody JsonObject json) throws ServerException, ClientException {
		Set<String> videoIds = videoRepository.toSet();
		List<JsonObject> jsonObjects = videoIds.stream().map(arg0 -> {
			try {
				return apsaraEmbassador.getVideoInfoRequest(arg0);
			} catch (ServerException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return null;
			} catch (ClientException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return null;
			}
		}).filter(rsp -> (rsp != null)).map(rsp -> {
			JsonObject o = new JsonObject();
			o.add("videoId", new JsonPrimitive(rsp.getVideo().getVideoId()));
			if (rsp.getVideo().getCoverURL() == null) {

			} else {
				o.add("coverImg", new JsonPrimitive(rsp.getVideo().getCoverURL()));
			}
			o.add("timestamp", new JsonPrimitive(rsp.getVideo().getCreateTime()));
			o.add("title", new JsonPrimitive(rsp.getVideo().getTitle()));
			return o;
		}).filter(o -> o.get("timestamp") != null).filter(o -> o.get("timestamp").getAsString() != null)
				.filter(o -> !o.get("timestamp").getAsString().isEmpty()).collect(Collectors.toList());
		Collections.sort(jsonObjects, (a, b) -> {
			return b.get("timestamp").getAsString().compareTo(a.get("timestamp").getAsString());
		});
		return jsonObjects;
	}

	@Accumulated
	@GetMapping("/article/{uuid}")
	public String article(@PathVariable("uuid") String uuid, Model model, Principal principal) {
		addCommonModleArrtibutes(model, principal);
		Article article = articleRepository.find(uuid);
		articleRepository.revive(article);
		
		article.setExtension("likeCount", "" + likeRepository.getLikeCount(article.getUuid()));
		article.setExtension("replyCount", "" + replyRepository.getCount(uuid));
		article.setExtension("viewCount", "" + accumulatorMap.get(uuid));
		model.addAttribute("article", article);

		List<Reply> replies = replyRepository.get(uuid);
		replies.forEach(re -> {
			if (principal == null) {
				re.setExtension("isLiked", "false");
			} else {
				re.setExtension("isLiked", "" + likeRepository.isLike(re.getUuid(), principal.getName()));
			}
			re.setExtension("likeCount", "" + likeRepository.getLikeCount(re.getUuid()));
			String nickname = userDescriptionRepository.getNickName(re.getUsername());
			if(nickname!=null) {
				re.setUsername(nickname);
			}
		});
		if (principal != null) {
			article.setExtension("isLiked", "" + likeRepository.isLike(article.getUuid(), principal.getName()));
		} else {
			article.setExtension("isLiked", "false");
		}
		Map<String, String> playAuthMap = new HashMap<>();
		Map<String, String> videoTitleMap = new HashMap<>();
		Map<String, String> videoCoverMap = new HashMap<>();
		for (String videoId : article.getVideoIds()) {
			Long expectedLastEditedTime = article.getLastEditedTime();
			GetVideoPlayAuthResponse getVideoPlayAuthResponse = null;
			try {
				getVideoPlayAuthResponse = apsaraEmbassador.getVideoPlayAuthResponse(videoId);
			} catch (ServerException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();

			} catch (ClientException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				try {
					article.removeVideoIds(expectedLastEditedTime, videoId);
					expectedLastEditedTime = article.getLastEditedTime();
				} catch (OutdatedEditeeException e1) {
					// TODO Auto-generated catch block
					throw new RuntimeException(e1);
				}
			}

			if (getVideoPlayAuthResponse != null) {
				String playAuth = getVideoPlayAuthResponse.getPlayAuth();
				playAuthMap.put(videoId, playAuth);
			}

			GetVideoInfoResponse getVideoInfoResponse = null;
			try {
				getVideoInfoResponse = apsaraEmbassador.getVideoInfoRequest(videoId);
			} catch (ServerException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();

			} catch (ClientException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				try {
					article.removeVideoIds(expectedLastEditedTime, videoId);
				} catch (OutdatedEditeeException e1) {
					// TODO Auto-generated catch block
					throw new RuntimeException(e1);
				}

			}
			if (getVideoInfoResponse != null) {
				videoTitleMap.put(videoId, getVideoInfoResponse.getVideo().getTitle());
				videoCoverMap.put(videoId, getVideoInfoResponse.getVideo().getCoverURL());
			}

		}
		model.addAttribute("playAuthMap", playAuthMap);
		model.addAttribute("videoTitleMap", videoTitleMap);
		model.addAttribute("videoCoverMap", videoCoverMap);

		model.addAttribute("replies", replies);

		return "article";
	}

}

package com.spDeveloper.hongpajee.video.controller;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.exceptions.ServerException;
import com.aliyuncs.vod.model.v20170321.CreateUploadVideoResponse;
import com.aliyuncs.vod.model.v20170321.GetVideoInfoResponse;
import com.aliyuncs.vod.model.v20170321.GetVideoInfoResponse.Video;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.reflect.TypeToken;
import com.spDeveloper.hongpajee.aliyun.ApsaraEmbassador;
import com.spDeveloper.hongpajee.navbar.service.NavbarRepository;
import com.spDeveloper.hongpajee.opinion.repository.LikeRepository;
import com.spDeveloper.hongpajee.opinion.repository.ReplyRepository;
import com.spDeveloper.hongpajee.post.controller.ArticleController;
import com.spDeveloper.hongpajee.post.entity.Article;
import com.spDeveloper.hongpajee.post.repository.ArticleRepository;
import com.spDeveloper.hongpajee.tag.service.TagPool;
import com.spDeveloper.hongpajee.util.map.AccumulatorMap;
import com.spDeveloper.hongpajee.video.entity.ApsaraVideo;
import com.spDeveloper.hongpajee.video.repository.VideoRepository;

@Controller
public class VideoController {

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
	ApsaraEmbassador apsaraEmbassador;
	@Autowired
	VideoRepository videoRepository;
	@Autowired
	Gson gson;
	@Autowired
	ArticleController articleController;
	
	@GetMapping("/admin/video/upload")
	public String uploadPage(Model model, Principal principal) throws ServerException, ClientException {
		articleController.addCommonModleArrtibutes(model, principal);
		return "apsara_vod_upload_web";
	}

	@PostMapping(value = "/admin/video/upload", produces = "application/json", consumes = { "application/json" })
	@ResponseBody
	public JsonObject upload(@RequestBody JsonObject json) throws ServerException, ClientException {
		JsonObject result = new JsonObject();
		String title = json.get("title").getAsString();
		String fileName = json.get("fileName").getAsString();
		JsonObject extension = json.get("extension").getAsJsonObject();
		CreateUploadVideoResponse createUploadVideoResponse = apsaraEmbassador.getCreateUploadVideoResponse(title,
				fileName, extension);

		result.add("uploadAddress", new JsonPrimitive(createUploadVideoResponse.getUploadAddress()));
		result.add("uploadAuth", new JsonPrimitive(createUploadVideoResponse.getUploadAuth()));
		result.add("videoId", new JsonPrimitive(createUploadVideoResponse.getVideoId()));

		return result;
	}

	@PostMapping(value = "/admin/video/upload/success", produces = "application/json", consumes = {
			"application/json" })
	@ResponseBody
	public JsonObject videoUploadSuccess(@RequestBody JsonObject json) throws ServerException, ClientException {
		JsonObject result = new JsonObject();
		String videoId = json.get("videoId").getAsString();

		videoRepository.add(videoId);

		result.add("status", new JsonPrimitive("success"));

		return result;
	}

	@PostMapping(value = "/admin/video/delete", produces = "application/json", consumes = { "application/json" })
	@ResponseBody
	public JsonObject delete(@RequestBody JsonObject json) throws ServerException, ClientException {
		JsonObject result = new JsonObject();
		JsonElement videoIdsJson = json.get("videoIds");

		List<String> videoIds = gson.fromJson(videoIdsJson, new TypeToken<List<String>>() {
		}.getType());
		if (videoIds.isEmpty()) {
			result.add("status", new JsonPrimitive("success"));
			return result;
		}

		String status = "success";
		if (videoIds == null) {
			status = "fall";
		} else {
			apsaraEmbassador.deleteVideos(videoIds);
		}
		videoIds.forEach(videoRepository::remove);
		result.add("status", new JsonPrimitive(status));
		return result;
	}

	@GetMapping("/admin/video/management")
	public String videoManagement(Model model, Principal principal) throws ServerException {
		articleController.addCommonModleArrtibutes(model, principal);
		
		Set<String> videoIds = videoRepository.toSet();

		List<ApsaraVideo> apsaraVideos = new ArrayList<>();

		for (String vid : videoIds) {
			try {
				String playAuth = apsaraEmbassador.getVideoPlayAuthResponse(vid).getPlayAuth();
				GetVideoInfoResponse getVideoInfoResponse = apsaraEmbassador.getVideoInfoRequest(vid);
				String cover = getVideoInfoResponse.getVideo().getCoverURL();
				String createTime = getVideoInfoResponse.getVideo().getCreateTime();
				String title = getVideoInfoResponse.getVideo().getTitle();
				ApsaraVideo apsaraVideo = new ApsaraVideo(vid, playAuth, cover, createTime, title);
				apsaraVideos.add(apsaraVideo);
			} catch (ClientException e) {
				videoRepository.remove(vid);
			}
		}

		Collections.sort(apsaraVideos, (a, b) -> {
			return b.getCreateTime().compareTo(a.getCreateTime());
		});
		model.addAttribute("apsaraVideos", apsaraVideos);

		return "videoManagement";
	}

}

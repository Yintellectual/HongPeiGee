package com.spDeveloper.hongpajee;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Base64;
import java.util.concurrent.ThreadLocalRandom;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.stereotype.Controller;
import org.springframework.web.client.RestTemplate;

import com.aliyun.vod.upload.impl.UploadVideoImpl;
import com.aliyun.vod.upload.req.UploadVideoRequest;
import com.aliyun.vod.upload.resp.UploadVideoResponse;
import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.exceptions.ServerException;
import com.aliyuncs.profile.DefaultProfile;
import com.aliyuncs.vod.model.v20170321.CreateUploadVideoRequest;
import com.aliyuncs.vod.model.v20170321.CreateUploadVideoResponse;
import com.aliyuncs.vod.model.v20170321.GetVideoPlayAuthRequest;
import com.aliyuncs.vod.model.v20170321.GetVideoPlayAuthResponse;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.spDeveloper.hongpajee.aliyun.ApsaraEmbassador;
import com.spDeveloper.hongpajee.navbar.entity.NavItem;
import com.spDeveloper.hongpajee.opinion.reply.entity.Reply;
import com.spDeveloper.hongpajee.post.entity.Article;
import com.spDeveloper.hongpajee.post.repository.ArticleRepository;
import com.spDeveloper.hongpajee.redis.RedisJsonDAO;
import com.spDeveloper.hongpajee.tag.service.TagPool;
import com.spDeveloper.hongpajee.user.config.RedisUserDetailsManager;
import com.spDeveloper.hongpajee.util.map.AccumulatorMap;


@EnableScheduling
@SpringBootApplication
public class HongPeiJeeApplication {

	@Autowired
	ArticleRepository articleRepository;
	@Autowired
	TagPool tagPool;
	@Autowired
	RedisJsonDAO dao;
	@Autowired
	ApsaraEmbassador apsaraEmbassador;

	@Bean
	DateTimeFormatter df() {
		return DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss").withZone(ZoneId.systemDefault());
	}

	@Bean
	RestTemplate restTemplate(Gson gson) {
		RestTemplate restTemplate = new RestTemplate();
		return restTemplate;
	}

	@Bean
	AccumulatorMap accumulatorMap() {
		return new AccumulatorMap();
	}

	@EventListener(ApplicationReadyEvent.class)
	public void doSomethingAfterStartup() {
		accumulatorMap().fromMap(dao.recoverMap("accumulatorMap", Long.class));
	}

	@Scheduled(cron = "0 0/5 * * * *")
	public void fiveMinuteClock() {
		dao.persist(accumulatorMap().toMap(), "accumulatorMap");
	}

	@Bean
	Gson gson() {
		return new GsonBuilder().registerTypeAdapter(NavItem.class, new NavItem.NavItemTypeAdapter())
				.registerTypeAdapter(Article.class, new Article.ArticleTypeAdapter())
				.registerTypeAdapter(Article.LazyLoadContent.class, new Article.LazyLoadContentTypeAdapter())
				.registerTypeAdapter(Reply.class, new Reply.ReplyTypeAdapter()).create();
	}

	public static void main(String[] args) {
		SpringApplication.run(HongPeiJeeApplication.class, args);
	}

	@Bean
	DefaultAcsClient defaultAcsClient() {
		String regionId = "cn-shanghai";
		DefaultProfile profile = DefaultProfile.getProfile(regionId, "LTAIjMVCPTKxTBn0",
				"zL3ZwWTqwwc6KxlLlyex4rAy3vuCbG");
		DefaultAcsClient client = new DefaultAcsClient(profile);
		return client;
	}

	@Autowired
	RedisUserDetailsManager redisUserDetailsManager;

	/**
	 * @return
	 */
	@Bean
	CommandLineRunner commandLineRunner() {

		return new CommandLineRunner() {

			@Override
			public void run(String... args) throws Exception {

			}
		};
	}
}

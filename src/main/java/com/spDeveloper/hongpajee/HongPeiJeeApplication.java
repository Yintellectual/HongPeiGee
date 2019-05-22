package com.spDeveloper.hongpajee;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Stack;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.el.Expression;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.ClassPathResource;
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
import com.spDeveloper.hongpajee.message.entity.HeavyMessage;
import com.spDeveloper.hongpajee.message.entity.Message;
import com.spDeveloper.hongpajee.message.entity.content.HTMLContent;
import com.spDeveloper.hongpajee.message.entity.content.TextContent;
import com.spDeveloper.hongpajee.message.entity.content.proxy.HTMLContentVirtualProxy;
import com.spDeveloper.hongpajee.message.entity.content.proxy.TextContentVirtualProxy;
import com.spDeveloper.hongpajee.message.repository.MessageRepository;
import com.spDeveloper.hongpajee.navbar.entity.NavItem;
import com.spDeveloper.hongpajee.opinion.reply.entity.Reply;
import com.spDeveloper.hongpajee.post.entity.Article;
import com.spDeveloper.hongpajee.post.entity.LazyLoadContent;
import com.spDeveloper.hongpajee.post.repository.ArticleRepository;
import com.spDeveloper.hongpajee.redis.RedisJsonDAO;
import com.spDeveloper.hongpajee.tag.service.TagPool;
import com.spDeveloper.hongpajee.text.entity.MutableLine;
import com.spDeveloper.hongpajee.text.comparison.LineListsComparisonService;
import com.spDeveloper.hongpajee.text.entity.Line;
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
				.registerTypeAdapter(LazyLoadContent.class, new LazyLoadContent.LazyLoadContentTypeAdapter())
				.registerTypeAdapter(Reply.class, new Reply.ReplyTypeAdapter())
				.registerTypeAdapter(MutableLine.class, new MutableLine.MutableLineTypeAdapter())
				.registerTypeAdapter(HTMLContent.class, new HTMLContent.HTMLContentTypeAdapter())
				.registerTypeAdapter(HeavyMessage.class, new HeavyMessage.HeavyMessageTypeAdapter())
				.registerTypeAdapter(TextContent.class, new TextContent.TextContentTypeAdapter())
				.registerTypeAdapter(HTMLContentVirtualProxy.class, new HTMLContentVirtualProxy.HTMLContentVirtualProxyTypeAdapter())
				.registerTypeAdapter(TextContentVirtualProxy.class, new TextContentVirtualProxy.TextContentVirtualProxyTypeAdapter())
				.create();
				
	}

	public static void main(String[] args) {
		SpringApplication.run(HongPeiJeeApplication.class, args);
	}



	@Autowired
	RedisUserDetailsManager redisUserDetailsManager;
	@Autowired
	LineListsComparisonService lineListComparisonService;
	@Autowired
	MessageRepository genericMessageRepository;
	@Bean
	CommandLineRunner commandLineRunner() {

		
		/*
		 * Experimenting the Bridge Design Pattern
		 * 
		 * In Bridge, there is an "Implementor" interface which is realized by different "ConcreteImplementors".
		 * 
		 * Then what is the different between bridge and the common implementation of interfaces?
		 * 
		 * The answer is that the Bridge take a further step of abstract to derive a second layer of abstraction from the original one. 
		 * The advantage of having an extract layer of abstraction is that we can handle two kinds of the changes independently. 
		 * 
		 * For example, in spring boot, we have a bean called NaiveUserServiceImplementation which is abstracted as an UserService.
		 * It is somewhat impossible to switch to another UserService implementation at runtime. However, sometimes we have to make the switching. 
		 *  
		 * Now, we can follow the bridge pattern and define a new interface called UserServiceImplementor. 
		 * Also, re-factor NaiveUserServiceImplementation into UserServiceAbstractor, which has a UserServiceImplementor.
		 * 
		 * Now we can define many different ConcreteUserServiceImplementors. In UserServiceAbstractor, we can switch between the ConcreteUserServiceImplementors.
		 * 
		 * */
		
		return new CommandLineRunner() {
			
			@Override
			public void run(String... args) throws Exception {

//				List<Line> lines = Files.lines(new ClassPathResource("static/application.properties").getFile().toPath()).map(MutableLine::new).collect(Collectors.toList());
//				List<Line> lines2 = Files.lines(new ClassPathResource("static/application2.properties").getFile().toPath()).map(MutableLine::new).collect(Collectors.toList());	

//				lineListComparisonService.compareTwoLineLists(lines, lines2).forEach(System.out::println);
				
			}
		};
		
		
	}
}


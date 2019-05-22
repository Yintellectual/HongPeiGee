package com.spDeveloper.hongpajee.message.entity.content.dao;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import com.google.gson.Gson;
import com.spDeveloper.hongpajee.message.entity.content.Content;
import com.spDeveloper.hongpajee.message.entity.content.HTMLContent;
import com.spDeveloper.hongpajee.message.entity.content.TextContent;
import com.spDeveloper.hongpajee.message.entity.content.proxy.HTMLContentVirtualProxy;
import com.spDeveloper.hongpajee.message.entity.content.proxy.TextContentVirtualProxy;

@Service
public class TextContentDAO {

	@Autowired
	StringRedisTemplate stringRedisTemplate;
	@Autowired
	Gson gson;

	private HashOperations<String, String, String> hashOperations;
	private SetOperations<String, String> setOperations;

	@PostConstruct
	public void init() {
		hashOperations = stringRedisTemplate.opsForHash();
		setOperations = stringRedisTemplate.opsForSet();
		TextContentVirtualProxy.textContentDAO = this;
	}

	public TextContent find(String uuid) {
		if (setOperations.isMember("HPG:CONTENT:set:htmlContents", uuid)) {
			String json = hashOperations.get("HPG:CONTENT:hash:htmlContents", uuid);
			return gson.fromJson(json, TextContent.class);
		} else {
			throw new MissingContentDAOException();

		}
	}

	public void save(TextContent content) {
		if (content instanceof TextContent) {
			String json = gson.toJson(content);
			hashOperations.put("HPG:CONTENT:hash:htmlContents", content.getUuid(), json);
			setOperations.add("HPG:CONTENT:set:htmlContents", content.getUuid());
		} else {
			throw new MissingContentDAOException();
		}
	}
}

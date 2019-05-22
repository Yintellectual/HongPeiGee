package com.spDeveloper.hongpajee.message.entity.content.repository;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.spDeveloper.hongpajee.message.entity.HeavyMessage.HeavyMessageTypeAdapter;
import com.spDeveloper.hongpajee.message.entity.content.Content;

@Service
public class ContentRepository {
	public static final Logger logger = LoggerFactory.getLogger(ContentRepository.class);

	@Autowired
	Gson gson;
	@Autowired
	StringRedisTemplate stringRedisTemplate;

	private HashOperations<String, String, String> hashOperations;
	private SetOperations<String, String> setOperations;

	Map<String, Content> repository = new ConcurrentHashMap<>();
	private ReadWriteLock readWriteLock = new ReentrantReadWriteLock();

	@PostConstruct
	public void init() {
		hashOperations = stringRedisTemplate.opsForHash();
		setOperations = stringRedisTemplate.opsForSet();
		recoverFromDatabase();
		
		System.out.println("******************Debugging**********");
		System.out.println(repository);
		
		HeavyMessageTypeAdapter.contentRepository = this;
	}

	private void recoverFromDatabase() {
		Set<String> uuids = setOperations.members("HPG:CONTENT:REPOSITORY:set:content");
		uuids.forEach(uuid -> {
			Content content;
			try {
				content = recover(uuid);
				repository.put(uuid, content);
			} catch (JsonSyntaxException | ClassNotFoundException e) {
				// TODO Auto-generated catch block
				logger.error("Failed to recover content of uuid=" + uuid);
			}
		});
	}

	public void add(Content content) {
		try {
			readWriteLock.writeLock().lock();
			repository.put(content.getUuid(), content);
			save(content);
		} finally {
			readWriteLock.writeLock().unlock();
		}
	}

	public Content find(String uuid) {
		try {
			readWriteLock.readLock().lock();
			return repository.get(uuid);
		} finally {
			readWriteLock.readLock().unlock();
		}
	}

	public Content remove(String uuid) {
		try {
			readWriteLock.writeLock().lock();
			Content content = find(uuid);
			repository.remove(uuid);
			delete(uuid);
			return content;
		} finally {
			readWriteLock.writeLock().unlock();
		}
	}

	private void save(Content content) {
		String json = gson.toJson(content);
		hashOperations.put("HPG:CONTENT:REPOSITORY:hash:content", content.getUuid(), json);
		hashOperations.put("HPG:CONTENT:REPOSITORY:hash:contentType", content.getUuid(), content.getClass().getName());
		setOperations.add("HPG:CONTENT:REPOSITORY:set:content", content.getUuid());
	}

	private Content recover(String uuid) throws JsonSyntaxException, ClassNotFoundException {
		String json = hashOperations.get("HPG:CONTENT:REPOSITORY:hash:content", uuid);
		String type = hashOperations.get("HPG:CONTENT:REPOSITORY:hash:contentType", uuid);
		return (Content) gson.fromJson(json, Class.forName(type));
	}

	private void delete(String uuid) {
		hashOperations.delete("HPG:CONTENT:REPOSITORY:hash:content", uuid);
		hashOperations.delete("HPG:CONTENT:REPOSITORY:hash:contentType", uuid);
		setOperations.remove("HPG:CONTENT:REPOSITORY:set:content", uuid);
	}
}

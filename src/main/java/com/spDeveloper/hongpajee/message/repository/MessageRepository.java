package com.spDeveloper.hongpajee.message.repository;

import java.util.Collection;
import java.util.LinkedList;
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
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.spDeveloper.hongpajee.message.entity.HeavyMessage;
import com.spDeveloper.hongpajee.message.entity.Message;

import com.spDeveloper.hongpajee.message.entity.content.repository.ContentRepository;

@Service
public class MessageRepository {

	private Logger logger = LoggerFactory.getLogger(MessageRepository.class);

	@Autowired
	Gson gson;
	@Autowired
	StringRedisTemplate stringRedisTemplate;
	@Autowired
	ContentRepository contentRepository;

	Map<String, Message> repository = new ConcurrentHashMap<>();
	private ReadWriteLock readWriteLock = new ReentrantReadWriteLock();

	private HashOperations<String, String, String> hashOperations;
	private SetOperations<String, String> setOperations;

	@PostConstruct
	public void init() {
		hashOperations = stringRedisTemplate.opsForHash();
		setOperations = stringRedisTemplate.opsForSet();
		loadFromDatabase();
	}

	private void loadFromDatabase() {
		try {
			readWriteLock.writeLock().lock();
			Set<String> uuids = setOperations.members("HPG:MESSAGE:REPOSITORY:set:message");
			uuids.forEach(uuid -> {
				try {
					repository.put(uuid, recover(uuid));
				} catch (JsonSyntaxException | ClassNotFoundException e) {
					// TODO Auto-generated catch block
					logger.error("Failed to recover message of uuid=" + uuid);
				}
			});
		} finally {
			readWriteLock.writeLock().unlock();
		}
	}

	public void add(Message message) {
		try {
			readWriteLock.writeLock().lock();
			repository.put(message.getUuid(), message);
			contentRepository.add(message.getContent());
			save(message);
		} finally {
			readWriteLock.writeLock().unlock();
		}
	}

	public Message find(String uuid) {
		try {
			readWriteLock.readLock().lock();
			return repository.get(uuid);
		} finally {
			readWriteLock.readLock().unlock();
		}
	}

	public Collection<Message> findAll() {
		try {
			readWriteLock.readLock().lock();
			return repository.values();
		} finally {
			readWriteLock.readLock().unlock();
		}
	}

	public Message remove(String uuid) {
		try {
			readWriteLock.writeLock().lock();
			Message message = repository.get(uuid);
			repository.remove(uuid);
			delete(uuid);
			return message;
		} finally {
			readWriteLock.writeLock().unlock();
		}
	}

	private void delete(String uuid) {
		hashOperations.delete("HPG:MESSAGE:REPOSITORY:hash:message", uuid);
		hashOperations.delete("HPG:MESSAGE:REPOSITORY:hash:messageType", uuid);
		setOperations.remove("HPG:MESSAGE:REPOSITORY:set:message", uuid);
	}

	private void save(Message message) {
		hashOperations.put("HPG:MESSAGE:REPOSITORY:hash:message", message.getUuid(), gson.toJson(message));
		hashOperations.put("HPG:MESSAGE:REPOSITORY:hash:messageType", message.getUuid(), message.getClass().getName());
		setOperations.add("HPG:MESSAGE:REPOSITORY:set:message", message.getUuid());
	}

	private Message recover(String uuid) throws JsonSyntaxException, ClassNotFoundException {
		String type = hashOperations.get("HPG:MESSAGE:REPOSITORY:hash:messageType", uuid);
		String json = hashOperations.get("HPG:MESSAGE:REPOSITORY:hash:message", uuid);
		return (Message) gson.fromJson(json, Class.forName(type));
	}
}

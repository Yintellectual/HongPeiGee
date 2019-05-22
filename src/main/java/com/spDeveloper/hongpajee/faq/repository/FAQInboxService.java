package com.spDeveloper.hongpajee.faq.repository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import com.spDeveloper.hongpajee.message.entity.Message;
import com.spDeveloper.hongpajee.message.repository.MessageRepository;
import com.spDeveloper.hongpajee.redis.RedisJsonDAO;
import com.spDeveloper.hongpajee.util.list.PinableList;

@Service
public class FAQInboxService {

	@Autowired
	MessageRepository messageRepository;
	@Autowired
	RedisJsonDAO redisJsonDAO;
	@Autowired
	StringRedisTemplate stringRedisTemplate;
	
	
	private ReadWriteLock readWriteLock = new ReentrantReadWriteLock();
	private SetOperations<String, String> setOperations;
	private ListOperations<String, String> listOperations;
	private HashOperations<String, String, String> hashOperations;
	/**
	 * "HPG:FAQ:INBOX:list:messages:username:{username}"
	 * "HPG:FAQ:INPUT:set:usernames"
	 * */
	MultiValueMap<String, String> messagesPerUser = new LinkedMultiValueMap<>();
	Set<String> hiddenUsernames = new ConcurrentSkipListSet<>();
	List<String> pinnedUsernames = new CopyOnWriteArrayList<>();
	Set<String> readMessages = new ConcurrentSkipListSet<>();
	Set<String> importantMessages = new ConcurrentSkipListSet<>();
	
	private void recoverMessagesPerUserFromDatabase() {
		Set<String> users = setOperations.members("HPG:FAQ:INPUT:set:usernames");
		users.forEach(username->{
			List<String> messages = listOperations.range("HPG:FAQ:INBOX:list:messages:username:"+username, 0, -1);
			messagesPerUser.addAll(username, messages);
		});
	}
	
	private void recoverHiddenUsernames() {
		hiddenUsernames.addAll(setOperations.members("HPG:FAQ:INPUT:set:usernames:hidden"));
	}
	private void recoverPinnedUsernames() {
		pinnedUsernames.addAll(listOperations.range("HPG:FAQ:INPUT:list:usernames:pinned", 0, -1));
	}
	private void recoverReadMessages() {
		readMessages.addAll(setOperations.members("HPG:FAQ:INPUT:set:messages:read"));
	}
	private void recoverImportantMessages() {
		importantMessages.addAll(setOperations.members("HPG:FAQ:INPUT:set:messages:important"));
	}
	private void recoverFromDatabase() {
		recoverMessagesPerUserFromDatabase();
		recoverHiddenUsernames();
		recoverPinnedUsernames();
		recoverImportantMessages();
		recoverReadMessages();
	}
	
	@PostConstruct
	public void init() {
		setOperations = stringRedisTemplate.opsForSet();
		listOperations = stringRedisTemplate.opsForList();
		hashOperations = stringRedisTemplate.opsForHash();
		try {
			readWriteLock.writeLock().lock();
			recoverFromDatabase();
		} finally {
			readWriteLock.writeLock().unlock();
		}
	}
	private boolean toggleMembership(Set<String> set, String element, String redisKey) {
		try {
			readWriteLock.writeLock().lock();
			if(set.contains(element)) {
				set.remove(element);
				setOperations.remove(redisKey, element);
			}else {
				set.add(element);	
				setOperations.add(redisKey, element);
			}
			return set.contains(element);
		}finally {
			readWriteLock.writeLock().unlock();
		}
	}
	private boolean getMembership(Set<String> set, String element) {
		try {
			readWriteLock.readLock().lock();
			return set.contains(element);
		}finally {
			readWriteLock.readLock().unlock();
		}
	}
	
	
	public boolean toggleMessageImportant(String uuid) {
		return toggleMembership(importantMessages, uuid, "HPG:FAQ:INPUT:set:messages:important");
	}
	public boolean toggleMessageRead(String uuid) {
		return toggleMembership(readMessages, uuid, "HPG:FAQ:INPUT:set:messages:read");
	}
	public boolean isMessageImportant(String uuid) {
		return getMembership(importantMessages, uuid);
	}	
	public boolean isMessageRead(String uuid) {
		return getMembership(readMessages, uuid);
	}
	public void hideUser(String username) {
		try {
			readWriteLock.writeLock().lock();
			hiddenUsernames.add(username);
			setOperations.add("HPG:FAQ:INPUT:set:usernames:hidden", username);
		}finally {
			readWriteLock.writeLock().unlock();
		}
	}
	public void undoHideUser(String username) {
		try {
			readWriteLock.writeLock().lock();
			hiddenUsernames.remove(username);
			setOperations.remove("HPG:FAQ:INPUT:set:usernames:hidden", username);
		}finally {
			readWriteLock.writeLock().unlock();
		}
	}
	public boolean isUserHidden(String username) {
		try {
			readWriteLock.readLock().lock();
			return hiddenUsernames.contains(username);
		}finally {
			readWriteLock.readLock().unlock();
		}
	}
	public Set<String> listHiddenUsers() {
		try {
			readWriteLock.readLock().lock();
			return new HashSet<>(hiddenUsernames);
		}finally {
			readWriteLock.readLock().unlock();
		}
	}
	
	public boolean pinUserToggle(String username) {
		try {
			readWriteLock.writeLock().lock();
			if(isUserPinned(username)) {
				undoPinUser(username);
				return false;
			}else {
				pinUser(username);
				return true;
			}
		}finally {
			readWriteLock.writeLock().unlock();
		}
	}
	
	public boolean hideUserToggle(String username) {
		try {
			readWriteLock.writeLock().lock();
			if(isUserHidden(username)) {
				undoHideUser(username);
				return false;
			}else {
				hideUser(username);
				return true;
			}
		}finally {
			readWriteLock.writeLock().unlock();
		}
	}
	public void pinUser(String username) {
		try {
			readWriteLock.writeLock().lock();
			pinnedUsernames.remove(username);
			pinnedUsernames.add(0, username);
			listOperations.remove("HPG:FAQ:INPUT:list:usernames:pinned", 0, username);
			listOperations.leftPush("HPG:FAQ:INPUT:list:usernames:pinned", username);
		}finally {
			readWriteLock.writeLock().unlock();
		}
	}
	public void undoPinUser(String username) {
		try {
			readWriteLock.writeLock().lock();
			pinnedUsernames.remove(username);
			listOperations.remove("HPG:FAQ:INPUT:list:usernames:pinned", 0, username);
		}finally {
			readWriteLock.writeLock().unlock();
		}
	}
	public boolean isUserPinned(String username) {
		try {
			readWriteLock.readLock().lock();
			return pinnedUsernames.contains(username);
		}finally {
			readWriteLock.readLock().unlock();
		}
	}
	public List<String> listPinnedUsers(){
		try {
			readWriteLock.readLock().lock();
			return new ArrayList<>(pinnedUsernames);
		}finally {
			readWriteLock.readLock().unlock();
		}
	}

	private void saveMessageUserRelationship(Message message) {
		setOperations.add("HPG:FAQ:INPUT:set:usernames", message.getUsername());
		listOperations.leftPush("HPG:FAQ:INBOX:list:messages:username:"+message.getUsername(), message.getUuid());
	}
	private void delementMessageUserRelationship(Message message) {
		listOperations.remove("HPG:FAQ:INBOX:list:messages:username:"+message.getUsername(), 1, message.getUuid());
		Long size = listOperations.size("HPG:FAQ:INBOX:list:messages:username:"+message.getUsername());
		if(size<=0) {
			setOperations.remove("HPG:FAQ:INPUT:set:usernames", message.getUsername());
		}
	}

	
	public List<Message> findByUser(String username) {
		try {
			readWriteLock.readLock().lock();
			List<String> messageUUIDs = messagesPerUser.get(username);
			if (messageUUIDs == null || messageUUIDs.isEmpty()) {
				return new LinkedList<>();
			}
			return messageUUIDs.stream().map(messageRepository::find).collect(Collectors.toList());
		} finally {
			readWriteLock.readLock().unlock();
		}
	}

	public Set<String> findUsers(){
		return new HashSet<>(messagesPerUser.keySet());
	}
	public void add(Message message) throws InterruptedException, TimeoutException {
		try {
			
			
			readWriteLock.writeLock().lock();
			
			messageRepository.add(message);
			
			List<String> uuids = messagesPerUser.get(message.getUsername());
			uuids.add(0, message.getUuid());
			messagesPerUser.remove(message.getUsername());
			messagesPerUser.addAll(message.getUsername(), uuids);
			saveMessageUserRelationship(message);
		} finally {
			readWriteLock.writeLock().unlock();
		}
	}

	public Message delete(String uuid) {
		try {
			readWriteLock.writeLock().lock();
			Message message = messageRepository.remove(uuid);
			String username = message.getUsername();
			List<String> messages = messagesPerUser.get(username);
			messages.remove(uuid);
			messagesPerUser.remove(username);
			messagesPerUser.addAll(username, messages);
			delementMessageUserRelationship(message);
			return message;
		} finally {
			readWriteLock.writeLock().unlock();
		}
	}

	public Message find(String uuid) throws InterruptedException, TimeoutException {
		try {
			readWriteLock.readLock().lock();
			return messageRepository.find(uuid);
		}finally {
			readWriteLock.readLock().unlock();
		}
	}
}

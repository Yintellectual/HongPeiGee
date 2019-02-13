package com.spDeveloper.hongpajee.opinion.repository;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.spDeveloper.hongpajee.post.entity.Article;
import com.spDeveloper.hongpajee.redis.RedisJsonDAO;

@Service
public class LikeRepository {

	Map<String, ConcurrentLinkedDeque<String>> like = new ConcurrentHashMap<>();
	ReentrantLock likeLock = new ReentrantLock();
	
	@Autowired
	RedisJsonDAO redisJsonDAO;
	
	@PostConstruct
	public void init() {
		load();
	}
	
	public void save() {
		redisJsonDAO.persist(like, "opinionLike");
	}
	private void load() {
		like = (Map<String, ConcurrentLinkedDeque<String>>) redisJsonDAO.recoverMap("opinionLike", new ConcurrentLinkedDeque<String>().getClass());
	} 
	
	
	// add the user to the head of the list if not exist
	public void like(String articleUuid, String userUuid) {
		likeLock.lock();
		try {
			addArticleUuid(articleUuid);
			ConcurrentLinkedDeque<String> userUuids = like.get(articleUuid);
			if(!userUuids.contains(userUuid)) {
				userUuids.addFirst(userUuid);		
			}
			save();
		}finally {
			likeLock.unlock();
		}
	}

	// remove the user if exist
	public void unlike(String articleUuid, String userUuid) {
		addArticleUuid(articleUuid);
		ConcurrentLinkedDeque<String> userUuids = like.get(articleUuid);
		userUuids.remove(userUuid);
		save();
	}

	private void addArticleUuid(String uuid) {
		like.putIfAbsent(uuid, new ConcurrentLinkedDeque<>());
	}
	public void removeArticle(String articleUuid) {
		like.remove(articleUuid);
	}
	
	public Integer getLikeCount(String articleUuid) {
		addArticleUuid(articleUuid);
		return like.get(articleUuid).size();
	}
	public List<String> getLikeList(String articleUuid){
		addArticleUuid(articleUuid);
		Iterator<String> iter = like.get(articleUuid).iterator();
		List<String> result = new ArrayList<>();
		while(iter.hasNext()) {
			result.add(iter.next());
		}
		return result;
	}
	
	public boolean isLike(String articleUuid, String userUuid) {
		addArticleUuid(articleUuid);
		return like.get(articleUuid).contains(userUuid);
	}
	
}

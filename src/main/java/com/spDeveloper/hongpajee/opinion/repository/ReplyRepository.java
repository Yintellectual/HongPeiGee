package com.spDeveloper.hongpajee.opinion.repository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.spDeveloper.hongpajee.opinion.reply.entity.Reply;
import com.spDeveloper.hongpajee.post.entity.Article;
import com.spDeveloper.hongpajee.redis.RedisJsonDAO;

@Service
public class ReplyRepository {

	Map<String, ConcurrentLinkedDeque<String>> replyUuidStorage = new ConcurrentHashMap<>();
	Map<String, Reply> replyStorage = new ConcurrentHashMap<>();
	ReentrantLock lock = new ReentrantLock();

	@Autowired
	RedisJsonDAO redisJsonDAO;

	@PostConstruct
	public void init() {
		load();
	}

	public void save() {
		redisJsonDAO.persist(replyStorage, "replyStorage");
		redisJsonDAO.persist(replyUuidStorage, "replyUuidStorage");
	}

	private void load() {
		replyStorage = redisJsonDAO.recoverMap("replyStorage", Reply.class);
		replyUuidStorage = (Map<String, ConcurrentLinkedDeque<String>>) redisJsonDAO.recoverMap("replyUuidStorage",
				new ConcurrentLinkedDeque<String>().getClass());
	}

	public void add(Reply reply) {
		introduceArticle(reply.getArticleUuid());
		remove(reply.getUuid());
		replyUuidStorage.get(reply.getArticleUuid()).addFirst(reply.getUuid());
		replyStorage.put(reply.getUuid(), reply);
		save();
	}

	public Reply find(String replyUuid) {
		return replyStorage.get(replyUuid);
	}

	public List<Reply> get(String articleUuid) {
		introduceArticle(articleUuid);
		List<String> replyIds = new ArrayList<>(replyUuidStorage.get(articleUuid));
		List<Reply> result = replyIds.stream().map(replyStorage::get).filter(r -> r != null)
				.collect(Collectors.toList());
		Collections.sort(result);
		return result;
	}

	public int getCount(String articleUuid) {
		introduceArticle(articleUuid);
		List<String> replyIds = new ArrayList<>(replyUuidStorage.get(articleUuid));
		if (replyIds == null || replyIds.isEmpty()) {
			return 0;
		} else {
			return replyIds.size();
		}
	}

	public void remove(String replyUuid) {
		if (has(replyUuid)) {
			replyUuidStorage.get(find(replyUuid).getArticleUuid()).remove(replyUuid);
			replyStorage.remove(replyUuid);
		}
		save();
	}

	public boolean has(String replyUuid) {
		if (replyUuid == null || replyUuid.isEmpty()) {
			return false;
		} else {
			return replyStorage.containsKey(replyUuid);
		}
	}

	public void introduceArticle(String articleUuid) {
		if (articleUuid != null) {
			replyUuidStorage.putIfAbsent(articleUuid, new ConcurrentLinkedDeque<>());
		}
	}

	public void removeArticle(String articleUuid) {
		if (articleUuid != null) {
			ConcurrentLinkedDeque<String> replyIds = replyUuidStorage.remove(articleUuid);
			if (replyIds != null) {
				replyIds.forEach(this::remove);
			}
		}
	}

}

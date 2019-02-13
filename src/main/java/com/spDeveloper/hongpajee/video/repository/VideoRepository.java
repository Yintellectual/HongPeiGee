package com.spDeveloper.hongpajee.video.repository;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.spDeveloper.hongpajee.redis.RedisJsonDAO;

@Service
public class VideoRepository {

	@Autowired
	RedisJsonDAO dao;
	
	private Set<String> videoIds = ConcurrentHashMap.newKeySet();
	
	@PostConstruct
	public void init() {
		fromSet(dao.recoverSet("videoIds", String.class));
	}
	
	public Set<String> toSet() {
		return new HashSet<>(videoIds);
	}
	public void fromSet(Set<String> videoIds) {
		this.videoIds = ConcurrentHashMap.newKeySet(videoIds.size());
		this.videoIds.addAll(videoIds);
	}
	public void add(String videoId) {
		videoIds.add(videoId);
		dao.persist(toSet(), "videoIds");
	}
	public void remove(String videoId) {
		if(videoId==null) {
			return;
		}
		videoIds.remove(videoId);
	} 
}

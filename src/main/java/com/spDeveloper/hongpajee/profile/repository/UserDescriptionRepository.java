package com.spDeveloper.hongpajee.profile.repository;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.spDeveloper.hongpajee.profile.entity.UserDescription;
import com.spDeveloper.hongpajee.user.config.RedisUserDetailsManager;

@Service
public class UserDescriptionRepository {

	@Autowired
	RedisUserDetailsManager userDetailsManager;
	
	private Map<String, UserDescription> userDescriptions = new ConcurrentHashMap<>();
	
	@Scheduled(cron="33 22 16 * * * ")
	public void dailySchedule() {
		garbageCollection();
	}
	//remove all usernames that is not found in userDetailsManager
	public void garbageCollection() {
		Set<String> usernames = new HashSet<>(userDescriptions.keySet());
		usernames.stream().filter(s->{
			return !userDetailsManager.userExists(s);
		}).forEach(userDescriptions::remove);
	}
	
	public void add(String username, UserDescription userDescription) {
		if(username==null||userDescription==null) {
			return;
		}
		userDescriptions.put(username, userDescription);
	}
	public UserDescription get(String username) {
		if(username==null) {
			return null;
		}
		return userDescriptions.get(username);
	}
}

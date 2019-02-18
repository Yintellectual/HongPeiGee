package com.spDeveloper.hongpajee.profile.repository;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.spDeveloper.hongpajee.profile.entity.UserDescription;
import com.spDeveloper.hongpajee.redis.RedisJsonDAO;
import com.spDeveloper.hongpajee.user.config.RedisUserDetailsManager;

@Service
public class UserDescriptionRepository {

	@Autowired
	RedisUserDetailsManager userDetailsManager;
	@Autowired
	RedisJsonDAO redisJsonDAO;
	
	private Map<String, UserDescription> userDescriptions = new ConcurrentHashMap<>();
	private Set<String> nicknames = ConcurrentHashMap.newKeySet();

	public static class NicknameExistsException extends Exception {
		public NicknameExistsException(String msg) {
			super(msg);
		}
		public NicknameExistsException(Throwable e) {
			super(e);
		}
	}
	
	@PostConstruct
	public void init() {
		load();
	}
	
	public void save() {
		System.out.println("User Descriptions: "+ userDescriptions);
		redisJsonDAO.persist(userDescriptions, "userDescriptions");
		redisJsonDAO.persist(nicknames, "nicknames");
	}
	public void load() {
		userDescriptions.putAll(redisJsonDAO.recoverMap("userDescriptions", UserDescription.class));
		nicknames.addAll(redisJsonDAO.recoverSet("nicknames", String.class));
	}
	
	@Scheduled(cron="33 22 16 * * * ")
	public void dailySchedule() {
		collectGarbage();
		save();
	}
	//remove all usernames that is not found in userDetailsManager
	public void collectGarbage() {
		Set<String> usernames = new HashSet<>(userDescriptions.keySet());
		usernames.stream().filter(s->{
			return !userDetailsManager.userExists(s);
		}).forEach(this::remove);
	}
	
	public void add(String username, UserDescription userDescription) throws NicknameExistsException {
		if(username==null||userDescription==null) {
			return;
		}else {
			String nickname = userDescription.getNickname();
			if(Objects.isNull(nickname)) {
				//a username may has no corresponding nickname
			}else {
				collectGarbage();
				
				if(nicknames.contains(nickname)) {
					throw new NicknameExistsException("nickname: ["+nickname+"]");
				}else {
					UserDescription oldUserDescription = this.get(username);
					if(oldUserDescription!=null) {
						String oldNickname = oldUserDescription.getNickname();
						if(oldNickname!=null) {
							nicknames.remove(oldNickname);
						}
					}
					nicknames.add(nickname);
				}
			}
			userDescriptions.put(username, userDescription);
			save();
		}
	}
	
	public UserDescription get(String username) {
		if(username==null) {
			return null;
		}
		return userDescriptions.get(username);
	}
	
	public String getNickName(String username) {
		UserDescription userDescription = this.get(username);
		if(userDescription ==null) {
			return null;
		}else {
			return userDescription.getNickname();	
		}
	}
	public void setNickName(String username, String nickname) throws NicknameExistsException {
		UserDescription userDescription = get(username);
		if(userDescription==null) {
			userDescription = new UserDescription(nickname, username);
		}else {
			userDescription.setNickname(nickname);
		}
		add(username, userDescription);
	}
	public void remove(String username) {
		if(username==null) {
			return ;
		}else {
			UserDescription userDescription = this.get(username);
			userDescriptions.remove(username);
			nicknames.remove(userDescription.getNickname());
		}
	}
	public Map<String, String> getNickNames(Collection<String> usernames){
		Map<String, String> result = new HashMap<>();
		if(usernames==null) {
			return null;
		}else {
			for(String username:usernames) {
				if(username==null) {
					
				}else {
					String nickname = this.getNickName(username);
					if(nickname==null) {
						
					}else {
						result.put(username, nickname);
					}
				}
			}
			return result;
		}
		
	} 
}

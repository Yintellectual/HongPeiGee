package com.spDeveloper.hongpajee.profile.repository;

import static org.junit.Assert.*;

import org.aspectj.lang.annotation.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.User;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.spDeveloper.hongpajee.HongPeiJeeApplication;
import com.spDeveloper.hongpajee.profile.entity.UserDescription;
import com.spDeveloper.hongpajee.user.config.RedisUserDetailsManager;


@ActiveProfiles("test")
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = HongPeiJeeApplication.class)
public class UserDescriptionRepositoryGarbageCollectionTest {

	@Autowired
	RedisUserDetailsManager redisUserDetailsManager;
	@Autowired
	UserDescriptionRepository userDescriptionRepository;
	
	
	@Test
	public void mockRUDMCanCreateUser() {
		redisUserDetailsManager.createUser(User.withUsername("testUser1").password("").roles("").build());
		assertTrue("Mock RedisUserDetailsManager should be able to hold the created user. ", redisUserDetailsManager.userExists("testUser1"));
	}
	
	
	public void gcDoesNothingIfUserExists() {
		redisUserDetailsManager.createUser(User.withUsername("testUser1").password("").roles("").build());
		userDescriptionRepository.add("testUser1", new UserDescription("testUser1", "Nado"));
		userDescriptionRepository.garbageCollection();
		assertTrue("The GC should not delete description for existing users.", userDescriptionRepository.get("testUser1").getNickname().equals("Nado"));
	}
}

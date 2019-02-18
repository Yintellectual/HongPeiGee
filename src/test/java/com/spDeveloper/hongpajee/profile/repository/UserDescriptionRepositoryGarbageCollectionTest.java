package com.spDeveloper.hongpajee.profile.repository;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.anything;

import org.aspectj.lang.annotation.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.User;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.spDeveloper.hongpajee.HongPeiJeeApplication;
import com.spDeveloper.hongpajee.profile.entity.UserDescription;
import com.spDeveloper.hongpajee.profile.repository.UserDescriptionRepository.NicknameExistsException;
import com.spDeveloper.hongpajee.user.config.RedisUserDetailsManager;
import static org.mockito.Mockito.*;

@ActiveProfiles("test")
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = HongPeiJeeApplication.class)
public class UserDescriptionRepositoryGarbageCollectionTest {

	@Autowired
	RedisUserDetailsManager redisUserDetailsManager;
	@Autowired
	UserDescriptionRepository userDescriptionRepository;
	
	
	@Test
	public void gcDoesNothingIfUserExists() throws NicknameExistsException {
		when(redisUserDetailsManager.userExists(anyString())).thenReturn(true);
		String username = "testUser1";
		String nickname = "Nado";
		userDescriptionRepository.add(username, new UserDescription(nickname, username) );
		userDescriptionRepository.collectGarbage();
		assertTrue(redisUserDetailsManager.userExists("testUser1"));
		assertTrue("The GC should not delete description for existing users.", userDescriptionRepository.get(username).getNickname().equals(nickname));
	}
	@Test
	public void gcRemovesNonexistingUser() throws NicknameExistsException {
		when(redisUserDetailsManager.userExists(anyString())).thenReturn(false);
		userDescriptionRepository.add("testUser2", new UserDescription("testUser2", "Nado"));
		userDescriptionRepository.collectGarbage();
		assertTrue("The GC should delete description for non existing users.", userDescriptionRepository.get("testUser2")==null);
	}
	@Test
	public void userDescriptionRepositoryIsNotNull() {
		assertNotNull(userDescriptionRepository);
	}
	
}

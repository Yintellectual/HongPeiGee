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
public class UserDescriptionRepositoryTest {

	@Autowired
	UserDescriptionRepository userDescriptionRepository;
	
	
	@Test(expected = NicknameExistsException.class)
	public void returnFalseIfNicknameAlreadyExists() throws NicknameExistsException {
		String username1 = "testUser1";
		String username2 = "testUser2";
		String nickname = "Nado";
		userDescriptionRepository.add(username1, new UserDescription(nickname, username1));
		userDescriptionRepository.add(username2, new UserDescription(nickname, username2));
	}
	
	@Test
	public void rootNicknameIsTheOne(){
		String nickname = "至高";
		String username = "root";
		try {
			userDescriptionRepository.setNickName(username, nickname);
		} catch (NicknameExistsException e) {
			//do nothing
		}
		assertEquals("Root has nickname 至高", nickname, userDescriptionRepository.getNickName(username));
	}	
}

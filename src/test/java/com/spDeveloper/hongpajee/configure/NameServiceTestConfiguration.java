package com.spDeveloper.hongpajee.configure;

import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

import com.spDeveloper.hongpajee.user.config.RedisUserDetailsManager;

@Profile("test")
@Configuration
public class NameServiceTestConfiguration {
    @Bean
    @Primary
    public RedisUserDetailsManager nameService() {
        return Mockito.mock(RedisUserDetailsManager.class);
    }
}
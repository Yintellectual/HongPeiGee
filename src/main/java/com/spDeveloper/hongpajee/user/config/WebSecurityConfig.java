package com.spDeveloper.hongpajee.user.config;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {
    @Autowired
    RedisUserDetailsManager redisUserDetailsManager;
	
	@Override
    public RedisUserDetailsManager userDetailsService(){
    	RedisUserDetailsManager manager = redisUserDetailsManager;
        //InMemoryUserDetailsManager manager = new InMemoryUserDetailsManager();
        manager.createUser(User.withUsername("user").password("password").roles("USER").build());
        manager.createUser(User.withUsername("admin").password("password").roles("ADMIN", "USER").build());
        manager.createUser(User.withUsername("owner").password("password").roles("OWNER", "ADMIN", "USER").build());
        return manager;
    }
    
    @Override
    protected void configure(HttpSecurity httpSecurity) throws Exception {
    	
        httpSecurity
        .csrf().disable()
        .authorizeRequests()
        	.antMatchers("/login/**").permitAll()
        	.antMatchers("/owner/**").hasAnyRole("OWNER")
        	.antMatchers("/admin/**").hasAnyRole("ADMIN")
        	.antMatchers("/api/**").hasAnyRole("ADMIN")
        	.antMatchers("/user/**").hasAnyRole("USER")
        	.antMatchers("/owner/**").hasAnyRole("OWNER")
        	.antMatchers("/faq/believer/**").hasAnyRole("USER")
        	.antMatchers("/faq/god/**").hasRole("OWNER")
        	.antMatchers("/faq/angle/**").hasRole("ADMIN")
        	.anyRequest().permitAll()
            .and()
        .formLogin()
            .loginPage("/login/login").permitAll()
            .and()
        .logout()
            .permitAll();	
    }

}
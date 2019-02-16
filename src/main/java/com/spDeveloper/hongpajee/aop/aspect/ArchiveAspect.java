package com.spDeveloper.hongpajee.aop.aspect;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.apache.commons.text.StringEscapeUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import com.spDeveloper.hongpajee.aop.annotation.Accumulated;
import com.spDeveloper.hongpajee.aop.annotation.Archived;
import com.spDeveloper.hongpajee.util.map.AccumulatorMap;

@Aspect
@Component
public class ArchiveAspect {
	
	@Autowired
	DateTimeFormatter df;
	@Autowired
	StringRedisTemplate stringRedisTemplate;
	
	private ListOperations<String, String> listOperations;
	
	@PostConstruct
	public void init(){
		listOperations = stringRedisTemplate.opsForList();
	}
	
	@Pointcut("@annotation(archived)")
	public void callAt(Archived archived) {
	}

	@AfterReturning(value="callAt(archived)", returning="returnValue")
	public void after(JoinPoint jp, Archived archived, Object returnValue) throws Throwable {
		String method = jp.getSignature().getName();
		String args = null;
		if(jp.getArgs().length==0) {
			args = "";
		}else if(jp.getArgs().length==1){
			args = jp.getArgs()[0].toString();
		}else {
			args = Arrays.stream(jp.getArgs()).filter(Objects::nonNull).map(Object::toString).map(s->{
				//take only the first 36 characters
				if(s.length()>36) {
					String result = s.substring(0, 37)+"...";
					return result;
				}else {
					return s;
				}
			}).map(s->{
				return StringEscapeUtils.escapeHtml4(s);
			}).collect(Collectors.joining(", "));
		}
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		listOperations.leftPush("ACHIEVE:article:modification", "{"+auth.getName()+"}:("+LocalDateTime.now().format(df)+"):"+"["+method+"]"+"["+args+"]");
	}
	public List<String> readArchive(){
		return listOperations.range("ACHIEVE:article:modification", 0, -1);
	}
	public void cleanArchive(){
		stringRedisTemplate.delete("ACHIEVE:article:modification");
	}
}
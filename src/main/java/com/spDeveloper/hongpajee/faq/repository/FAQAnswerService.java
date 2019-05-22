package com.spDeveloper.hongpajee.faq.repository;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import com.spDeveloper.hongpajee.message.entity.Message;
import com.spDeveloper.hongpajee.message.repository.MessageRepository;
import com.spDeveloper.hongpajee.redis.RedisJsonDAO;

@Service
public class FAQAnswerService {


	@Autowired
	MessageRepository messageRepository;
	@Autowired
	RedisJsonDAO redisJsonDAO;
	@Autowired
	StringRedisTemplate stringRedisTemplate;
	@Autowired
	FAQQuestionAnswerRelationshipService faqQuestionAnswerRelationshipService;
	
	private ReadWriteLock readWriteLock = new ReentrantReadWriteLock();
	private SetOperations<String, String> setOperations;
	private ListOperations<String, String> listOperations;
	private HashOperations<String, String, String> hashOperations;
	
	private List<String> answers = new LinkedList<>();
	
	@PostConstruct
	public void init() {
		setOperations = stringRedisTemplate.opsForSet();
		listOperations = stringRedisTemplate.opsForList();
		hashOperations = stringRedisTemplate.opsForHash();
		try {
			readWriteLock.writeLock().lock();
			recoverFromDatabase();
		} finally {
			readWriteLock.writeLock().unlock();
		}
	}
	
	private void recoverFromDatabase() {
		answers.addAll(listOperations.range("HPG:FAQ:ANSWER:list:answers", 0, -1));
	}
	public void add(Message message) {
		try {
			readWriteLock.writeLock().lock();
			messageRepository.add(message);
			answers.add(0, message.getUuid());
			listOperations.leftPush("HPG:FAQ:ANSWER:list:answers", message.getUuid());
		}finally {
			readWriteLock.writeLock().unlock();
		}
	}
	public void remove(String uuid) {
		try {
			readWriteLock.writeLock().lock();
			messageRepository.remove(uuid);
			answers.remove(uuid);
			listOperations.remove("HPG:FAQ:ANSWER:list:answers", 0, uuid);
			faqQuestionAnswerRelationshipService.removeAnswer(uuid);
		}finally {
			readWriteLock.writeLock().unlock();
		}
	}
	public List<Message> getAllAnswers(){
		try {
			readWriteLock.readLock().lock();
			return new ArrayList<>(answers).stream().map(messageRepository::find).collect(Collectors.toList());
		}finally {
			readWriteLock.readLock().unlock();
		}
	}
	
}

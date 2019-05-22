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
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import com.spDeveloper.hongpajee.message.entity.Message;
import com.spDeveloper.hongpajee.message.repository.MessageRepository;
import com.spDeveloper.hongpajee.redis.RedisJsonDAO;

@Service
public class FAQQuestionAnswerRelationshipService {


	@Autowired
	MessageRepository messageRepository;
	@Autowired
	RedisJsonDAO redisJsonDAO;
	@Autowired
	StringRedisTemplate stringRedisTemplate;
	
	private ReadWriteLock readWriteLock = new ReentrantReadWriteLock();
	private SetOperations<String, String> setOperations;
	private ListOperations<String, String> listOperations;
	private HashOperations<String, String, String> hashOperations;
	
	private MultiValueMap<String, String> answersByQuestion = new LinkedMultiValueMap<>();
	private MultiValueMap<String, String> questionsByAnswer = new LinkedMultiValueMap<>();
	
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
		recoverMultiValueMap(answersByQuestion, "answersByQuestion");
		recoverMultiValueMap(questionsByAnswer, "questionsByAnswer");
	}
	
	private void recoverMultiValueMap(MultiValueMap<String, String> mvp, String name) {
		Set<String> keys = setOperations.members("HPG:FAQ:Q_A:set:"+name+":keys");
		for(String key :keys) {
			List<String> values = listOperations.range("HPG:FAQ:Q_A:list:"+name+":values:key:"+key, 0, -1);
			mvp.addAll(key, values);
		}
	}
	private void addToDatabase(String mvpName, String key, String value) {
		setOperations.add("HPG:FAQ:Q_A:set:"+mvpName+":keys", key);
		listOperations.leftPush("HPG:FAQ:Q_A:list:"+mvpName+":values:key:"+key, value);
	}
	
	public void removeAnswer(String uuid) {
		try {
			readWriteLock.writeLock().lock();
			questionsByAnswer.remove(uuid);
			setOperations.remove("HPG:FAQ:Q_A:set:questionsByAnswer:keys", uuid);
			stringRedisTemplate.delete("HPG:FAQ:Q_A:list:questionsByAnswer:values:key:"+uuid);
			
			Set<String> questions = new HashSet<>(answersByQuestion.keySet());
			questions.forEach(question->{
				removeValue(answersByQuestion, question, uuid, "answersByQuestion");
			});
		}finally {
			readWriteLock.writeLock().unlock();
		}
	}
	public void addRelationship(String question, String answer) {
		try {
			readWriteLock.writeLock().lock();
			answersByQuestion.add(question, answer);
			addToDatabase("answersByQuestion", question, answer);
			questionsByAnswer.add(answer, question);
			addToDatabase("questionsByAnswer", answer, question);
		}finally {
			readWriteLock.writeLock().unlock();
		}
	}
	public void removeRelationship(String question, String answer) {
		try {
			readWriteLock.writeLock().lock();
			removeValue(answersByQuestion, question, answer, "answersByQuestion");
			removeValue(questionsByAnswer, answer, question, "questionsByAnswer");
		}finally {
			readWriteLock.writeLock().unlock();
		}
	}
	private void removeValue(MultiValueMap<String, String> mvp, String key, String value, String mvpName) {
		List<String> values = mvp.remove(key);
		values.remove(value);
		mvp.addAll(key, values);	
		listOperations.remove("HPG:FAQ:Q_A:list:"+mvpName+":values:key:"+key, 0, value);
	}
	public List<Message> listQuestions(String uuid){
		try {
			readWriteLock.readLock().lock();
			List<String> questions = questionsByAnswer.get(uuid);
			if(questions==null) {
				return new ArrayList<>();
			}else {
				return questions.stream().map(messageRepository::find).collect(Collectors.toList());	
			}
			
		}finally {
			readWriteLock.readLock().unlock();
		}
	}
	
}

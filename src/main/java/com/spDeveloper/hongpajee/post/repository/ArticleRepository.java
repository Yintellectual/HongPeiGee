package com.spDeveloper.hongpajee.post.repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.google.gson.Gson;
import com.spDeveloper.hongpajee.exception.OutdatedEditeeException;
import com.spDeveloper.hongpajee.post.entity.Article;
import com.spDeveloper.hongpajee.post.entity.LazyLoadContent;
import com.spDeveloper.hongpajee.redis.RedisJsonDAO;
import com.spDeveloper.hongpajee.tag.service.TagPool;

@Service
public class ArticleRepository {

	@Autowired
	RedisJsonDAO redisJsonDAO;
	@Autowired
	Gson gson;

	@Bean
	public TagPool<Article> articleTagPool() {
		return new TagPool<>();
	}

	@Autowired
	TagPool<Article> tagPool;

	Map<String, Article> repository = new HashMap<>();
	Map<String, LazyLoadContent> lazyLoadRepository = new HashMap<>();

	@PostConstruct
	public void init() {
		restore();
	}

	public void save() {
		dryAll();
		redisJsonDAO.persist(repository, "articleRepository");
		redisJsonDAO.persist(lazyLoadRepository, "lazyLoadRepository");
	}

	public void restore() {
		repository = redisJsonDAO.recoverMap("articleRepository", Article.class);
		lazyLoadRepository = redisJsonDAO.recoverMap("lazyLoadRepository", LazyLoadContent.class);
		repository.values().forEach(tagPool::addElement);
	}

	public List<Article> findAll() {
		return new ArrayList<>(repository.values());
	}

	public void add(Article article) {
		delete(article.getUuid());
		dry(article);
		repository.put(article.getUuid(), article);
		tagPool.addElement(article);
		save();
	}

	public void delete(String uuid) {
		Article article = find(uuid);
		if (article != null) {
			tagPool.removeElement(article);
			repository.remove(uuid);
			lazyLoadRepository.remove(uuid);
			save();
		}
	}

	public Article find(String uuid) {
		return repository.get(uuid);
	}

	public Set<Article> findByTag(String tag) {
		return tagPool.getByTag(tag);
	}

	public Set<Article> findByTag(List<String> tags) {
		if (tags == null || tags.isEmpty()) {
			return null;
		} else if (tags.size() == 1) {
			return findByTag(tags.get(0));
		} else {
			return tags.stream().map(this::findByTag).flatMap(s -> s.stream()).collect(Collectors.toSet());
		}
	}

	@Scheduled(cron = "0 5 4 * * *")
	public void dryAll() {
		repository.values().forEach(this::dry);
	}

	/**
	 * Cut off the LazyLoadContent object from JVM and store the information into a
	 * repository. Whenever you want to save the content, do dry the article, DO NOT
	 * rely on the timelyDrier method.
	 */
	public void dry(Article article) {
		ReadWriteLock lock = article.getLock();
		lock.writeLock().lock();
		try {
			if (article.getLazyLoadContent() == null) {
				return;
			} else {
				LazyLoadContent lazyLoadContent = article.getLazyLoadContent();
				article.setLazyLoadContent(null);
				lazyLoadRepository.put(article.getUuid(), lazyLoadContent);
			}
		} finally {
			lock.writeLock().unlock();
		}
	}

	/**
	 * Recover LazyLoadContent object from repository back into JVM, but only when
	 * lazyLoadContent is null. That is to say, this method does nothing if the
	 * article is not dried. Therefore, articles tends to be undried. There must
	 * something works like a garbage collector that dries all the articles timely.
	 */
	public void revive(Article article) {
		if (article.getLazyLoadContent() == null) {
			ReadWriteLock lock = article.getLock();
			lock.writeLock().lock();
			try {
				LazyLoadContent lazyLoadContent = lazyLoadRepository.get(article.getUuid());
				article.setLazyLoadContent(lazyLoadContent);
			} finally {
				lock.writeLock().unlock();
			}
		} else {

		}
	}
}

package com.spDeveloper.hongpajee.redis;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import com.google.gson.Gson;
import com.spDeveloper.hongpajee.util.map.AccumulatorMap;

@Service
public class RedisJsonDAO {

	@Autowired
	StringRedisTemplate stringRedisTemplate;
	@Autowired
	Gson gson;

	SetOperations<String, String> setOps;
	ListOperations<String, String> listOps;
	HashOperations<String, String, String> hashOps;

	@PostConstruct
	public void init() {
		setOps = stringRedisTemplate.opsForSet();
		listOps = stringRedisTemplate.opsForList();
		hashOps = stringRedisTemplate.opsForHash();
	}

	// keyset is stored in a redis set, while key-value pairs are stored in a redis
	// hashmap
	public void persist(Map<String, ? extends Object> map, String name) {
		if (map == null || map.isEmpty()) {
			return;
		}

		String keySet = "JSONStorage:map:keys:name:" + name;
		delete(keySet);
		persist(map.keySet(), "map:keys:" + name);

		String hashKey = "JSONStorage:map:values:name:" + name;
		delete(hashKey);
		Map<String, String> result = new HashMap<>();
		map.keySet().stream().forEach(key -> {

			result.put(key, gson.toJson(map.get(key)));
		});
		hashOps.putAll(hashKey, result);

	}

	public void persist(Set<? extends Object> set, String name) {
		if (set == null || set.isEmpty()) {
			return;
		}
		delete("JSONStorage:set:name:" + name);
		setOps.add("JSONStorage:set:name:" + name,
				set.stream().map(gson::toJson).collect(Collectors.toList()).toArray(new String[0]));
	}

	public void persist(List<? extends Object> list, String name) {
		if (list == null || list.isEmpty()) {
			return;
		}
		String key = "JSONStorage:list:name:" + name;
		delete(key);
		listOps.rightPushAll(key, list.stream().map(gson::toJson).collect(Collectors.toList()).toArray(new String[0]));
	}

	public <T> Map<String, T> recoverMap(String name, Class<T> classOfT) {
		Map<String, T> result = new HashMap<>();
		Set<String> keys = recoverSet("map:keys:" + name, String.class);
		keys.forEach(key -> {

			String value = hashOps.get("JSONStorage:map:values:name:" + name, key);

			result.put(key, gson.fromJson(value, classOfT));
		});
		return result;
	}

	public <T> Set<T> recoverSet(String name, Class<T> classOfT) {
		return setOps.members("JSONStorage:set:name:" + name).stream().map(json -> {
			return (T) gson.fromJson(json, classOfT);
		}).collect(Collectors.toSet());
	}

	public <T> List<T> recoverList(String name, Class<T> classOfT) {
		String key = "JSONStorage:list:name:" + name;
		return listOps.range(key, 0, -1).stream().map(json -> {
			return (T) gson.fromJson(json, classOfT);
		}).collect(Collectors.toList());
	}

	public void delete(String key) {
		stringRedisTemplate.delete(key);
	}

}

package com.spDeveloper.hongpajee.post.entity;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.context.request.RequestContextListener;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.spDeveloper.hongpajee.exception.OutdatedEditeeException;
import com.spDeveloper.hongpajee.navbar.entity.NavItem;
import com.spDeveloper.hongpajee.tag.entity.TagHolder;
import com.spDeveloper.hongpajee.util.extendable.Extendable;

import lombok.Data;

/**
 * rewrite this class to use the proxy design pattern, more specifically, synchronization proxy design pattern .
 * */
@Data
public class Article extends Extendable implements Comparable<Article>, TagHolder {

	// it will ignore the lazyloadcontent
	public static class ArticleTypeAdapter implements JsonSerializer<Article>, JsonDeserializer<Article> {

		@Override
		public Article deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
				throws JsonParseException {
			// TODO Auto-generated method stub
			JsonObject data = json.getAsJsonObject();
			String uuid = data.get("uuid").getAsString();
			Long lastEditedTime = data.get("lastEditedTime").getAsLong();
			String title = data.get("title").getAsString();
			String abstraction = data.get("abstraction").getAsString();
			String picture = data.get("picture").getAsString();
			List<String> videoIds = context.deserialize(data.get("videoIds"), new ArrayList<String>().getClass());
			Article result = new Article(title, abstraction, picture, videoIds);
			List<String> tags = context.deserialize(data.get("tags"), new ArrayList<String>().getClass());
			result.setUuid(uuid);
			result.setLastEditedTime(lastEditedTime);
			if (tags != null && !tags.isEmpty()) {
				tags.forEach(result::addTag);
			}
			return result;
		}

		@Override
		public JsonElement serialize(Article src, Type typeOfSrc, JsonSerializationContext context) {
			// TODO Auto-generated method stub
			JsonObject result = new JsonObject();
			result.add("uuid", new JsonPrimitive(src.getUuid()));
			result.add("lastEditedTime", new JsonPrimitive(src.getLastEditedTime()));
			result.add("title", new JsonPrimitive(src.getTitle()));
			result.add("abstraction", new JsonPrimitive(src.getAbstraction()));
			result.add("picture", new JsonPrimitive(src.getPicture()));
			result.add("tags", context.serialize(src.getTags()));
			result.add("videoIds", context.serialize(src.getVideoIds()));
			return result;
		}

	}

	private ReadWriteLock lock = new ReentrantReadWriteLock();
	private String uuid;
	private Long lastEditedTime;
	private String title;
	private String abstraction;
	private String picture;
	private List<String> videoIds = new CopyOnWriteArrayList<>();
	private LazyLoadContent lazyLoadContent;
	private Set<String> tags;

	public Article() {
		this.uuid = UUID.randomUUID().toString();
		this.lastEditedTime = new Date().getTime();
		this.tags = new ConcurrentSkipListSet<>();
	}

	public Article(String title, String abstraction, String picture, List<String> videoIds) {
		this();
		this.title = title;
		this.abstraction = abstraction;
		this.picture = picture;
		if (videoIds == null) {
			videoIds = new ArrayList<>();
		}
		this.videoIds = new CopyOnWriteArrayList<>(videoIds);
	}

	public Article(String title, String abstraction, String picture, String content, List<String> videoIds) {
		this(title, abstraction, picture, videoIds);
		try {
			this.setContent(lastEditedTime, content);
		} catch (OutdatedEditeeException e) {
			throw new RuntimeException(e);
		}
	}

	public void addVideoIds(Long supposedLastEditedTime, String videoIds) throws OutdatedEditeeException {
		lock.writeLock().lock();
		try {
			if (getLastEditedTime().equals(supposedLastEditedTime)) {
				this.videoIds.add(videoIds);
				setLastEditedTime(new Date().getTime());
			} else {
				throw new OutdatedEditeeException(
						"The article [" + this.toString() + "] has been edited by someone else.");
			}
		} finally {
			lock.writeLock().unlock();
		}
	}

	public void removeVideoIds(Long supposedLastEditedTime, String videoIds) throws OutdatedEditeeException {
		lock.writeLock().lock();
		try {
			if (getLastEditedTime().equals(supposedLastEditedTime)) {
				this.videoIds.remove(videoIds);
				setLastEditedTime(new Date().getTime());
			} else {
				throw new OutdatedEditeeException(
						"The article [" + this.toString() + "] has been edited by someone else.");
			}
		} finally {
			lock.writeLock().unlock();
		}
	}

	public void setVideoIds(Long supposedLastEditedTime, List<String> videoIds) throws OutdatedEditeeException {
		lock.writeLock().lock();
		try {
			if (getLastEditedTime().equals(supposedLastEditedTime)) {
				this.videoIds = new CopyOnWriteArrayList<>(videoIds);
				setLastEditedTime(new Date().getTime());
			} else {
				throw new OutdatedEditeeException(
						"The article [" + this.toString() + "] has been edited by someone else.");
			}
		} finally {
			lock.writeLock().unlock();
		}
	}

	public List<String> getVideoIds() {
		lock.readLock().lock();
		try {
			return new ArrayList<>(this.videoIds);
		} finally {
			lock.readLock().unlock();
		}
	}

	public void setTtile(Long supposedLastEditedTime, String title) throws OutdatedEditeeException {
		lock.writeLock().lock();
		try {
			if (getLastEditedTime().equals(supposedLastEditedTime)) {
				this.title = title;
				setLastEditedTime(new Date().getTime());
			} else {
				throw new OutdatedEditeeException(
						"The article [" + this.toString() + "] has been edited by someone else.");
			}
		} finally {
			lock.writeLock().unlock();
		}
	}

	public String getTitle() {
		lock.readLock().lock();
		try {
			return title;
		} finally {
			lock.readLock().unlock();
		}
	}

	public void setAbstraction(Long supposedLastEditedTime, String abstraction) throws OutdatedEditeeException {
		lock.writeLock().lock();
		try {
			if (getLastEditedTime().equals(supposedLastEditedTime)) {
				this.abstraction = abstraction;
				setLastEditedTime(new Date().getTime());
			} else {
				throw new OutdatedEditeeException(
						"The article [" + this.toString() + "] has been edited by someone else.");
			}
		} finally {
			lock.writeLock().unlock();
		}
	}

	public String getAbstraction() {
		lock.readLock().lock();
		try {
			return abstraction;
		} finally {
			lock.readLock().unlock();
		}
	}

	public void setPicture(Long supposedLastEditedTime, String picture) throws OutdatedEditeeException {
		lock.writeLock().lock();
		try {
			if (getLastEditedTime().equals(supposedLastEditedTime)) {
				this.picture = picture;
				setLastEditedTime(new Date().getTime());
			} else {
				throw new OutdatedEditeeException(
						"The article [" + this.toString() + "] has been edited by someone else.");
			}
		} finally {
			lock.writeLock().unlock();
		}
	}

	public String getPicture() {
		lock.readLock().lock();
		try {
			return picture;
		} finally {
			lock.readLock().unlock();
		}
	}

	public void update(Long supposedLastEditedTime, String title, String abstraction, String picture,
			List<String> videoIds) throws OutdatedEditeeException {
		lock.writeLock().lock();
		try {
			if (getLastEditedTime().equals(supposedLastEditedTime)) {
				if (title != null) {
					setTitle(title);
				}
				if (abstraction != null) {
					setAbstraction(abstraction);
				}
				if (picture != null) {
					setPicture(picture);
				}
				if (videoIds != null) {
					setVideoIds(videoIds);
				}
			} else {
				throw new OutdatedEditeeException(
						"The article [" + this.toString() + "] has been edited by someone else.");
			}
		} finally {
			lock.writeLock().unlock();
		}
	}

	public void setContent(Long supposedLastEditedTime, String content) throws OutdatedEditeeException {
		lock.writeLock().lock();
		try {
			if (getLastEditedTime().equals(supposedLastEditedTime)) {

				if (lazyLoadContent == null) {
					lazyLoadContent = new LazyLoadContent();
				} else {

				}
				lazyLoadContent.setContent(content);
				setLastEditedTime(new Date().getTime());

			} else {
				throw new OutdatedEditeeException(
						"The article [" + this.toString() + "] has been edited by someone else.");
			}
		} finally {
			lock.writeLock().unlock();
		}
	}

	public String getContent() {
		lock.readLock().lock();
		try {
			if (lazyLoadContent == null) {
				return null;
			} else {
				return lazyLoadContent.getContent();
			}
		} finally {
			lock.readLock().unlock();
		}
	}

	public LazyLoadContent getLazyLoadContent() {
		lock.readLock().lock();
		try {
			return lazyLoadContent;
		} finally {
			lock.readLock().unlock();
		}
	}

	public void setLazyLoadContent(LazyLoadContent lazyLoadContent) {
		lock.writeLock().lock();
		try {
			this.lazyLoadContent = lazyLoadContent;
		} finally {
			lock.writeLock().unlock();
		}
	}

	
	@Override
	public boolean equals(Object o) {
		if (o != null && o instanceof Article) {
			Article other = (Article) o;
			return getUuid().equals(other.getUuid());
		} else {
			return false;
		}
	}

	@Override
	public int hashCode() {
		return getUuid().hashCode();
	}

	@Override
	public int compareTo(Article o) {
		if (o.getLastEditedTime() == null) {
			throw new RuntimeException("ERROR: an Article has no last-edited-time!");
		}
		// TODO Auto-generated method stub
		return o.getLastEditedTime().compareTo(getLastEditedTime());
	}

	@Override
	public void addTag(String e) {
		if (e == null || e.isEmpty()) {
			return;
		} else {
			this.tags.add(e);
		}
	}

	@Override
	public List<String> getTags() {
		return new ArrayList<>(tags);
	}

	@Override
	public boolean hasTag(String e) {
		if (e == null || e.isEmpty()) {
			return false;
		} else {
			return tags.contains(e);
		}
	}

	@Override
	public void removeTag(String tag) {
		if (tag == null || tag.isEmpty()) {
			return;
		} else {
			tags.remove(tag);
		}
	}
}

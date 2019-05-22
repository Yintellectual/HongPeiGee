package com.spDeveloper.hongpajee.message.entity.content.proxy;

import java.lang.reflect.Type;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.spDeveloper.hongpajee.garbageCollector.entity.GarbageCollector;
import com.spDeveloper.hongpajee.garbageCollector.entity.GarbageProducer;
import com.spDeveloper.hongpajee.message.entity.content.Content;
import com.spDeveloper.hongpajee.message.entity.content.TextContent;
import com.spDeveloper.hongpajee.message.entity.content.dao.HTMLContentDAO;
import com.spDeveloper.hongpajee.message.entity.content.dao.MissingContentException;
import com.spDeveloper.hongpajee.message.entity.content.dao.TextContentDAO;
import com.spDeveloper.hongpajee.message.entity.content.visitor.ContentVisitor;

public class TextContentVirtualProxy implements Content, GarbageProducer {

	private Logger logger = LoggerFactory.getLogger(TextContentVirtualProxy.class);
	private TextContent content;
	private String uuid;
	public static TextContentDAO textContentDAO;
	public static GarbageCollector garbageCollector;

	// multiple reads are allowed
	// the thread who has the write lock can pick up the read lock
	// no read is allowed for threads who has not the write lock, when the write
	// lock is picked up.
	private ReadWriteLock readWriteLock = new ReentrantReadWriteLock();

	
	public static class TextContentVirtualProxyTypeAdapter implements JsonSerializer<TextContentVirtualProxy>, JsonDeserializer<TextContentVirtualProxy>{

		@Override
		public TextContentVirtualProxy deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
				throws JsonParseException {
			JsonObject jsonObject = json.getAsJsonObject();
			String contentUuid = jsonObject.get("uuid").getAsString();
			return new TextContentVirtualProxy(contentUuid);
		}

		@Override
		public JsonElement serialize(TextContentVirtualProxy src, Type typeOfSrc, JsonSerializationContext context) {
			JsonObject result = new JsonObject();
			result.addProperty("uuid", src.getUuid());
			return result;
		}
	}
	
	public TextContentVirtualProxy(TextContent content) {
		// TODO Auto-generated constructor stub
		this.uuid = content.getUuid();
		textContentDAO.save(content);
		garbageCollector.register(this);
	}
	public TextContentVirtualProxy(String contentUuid) {
		this.uuid = contentUuid;
	}

	private void loadContentFromRepository() {
		if (content == null) {
			content = textContentDAO.find(uuid);
			if (content == null) {
				throw new MissingContentException();
			}
		}
	}

	@Override
	public String getString() {
		try {
			if (readWriteLock.readLock().tryLock(3, TimeUnit.SECONDS)) {
				String result = null;
				loadContentFromRepository();
				result = content.getString();
				garbageCollector.register(this);
				return result;
			} else {
				logger.warn("The write lock of this content is owned by another thread.");
				return null;
			}
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			throw new RuntimeException("ReadLock#tryLock(3,SECONDS) got interrupted.", e1);
		} finally {
			readWriteLock.readLock().unlock();
		}

	}

	@Override
	public void accept(ContentVisitor visitor) {
		try {
			readWriteLock.writeLock().lock();
			loadContentFromRepository();
			content.accept(visitor);
			textContentDAO.save(content);
			garbageCollector.register(this);
			return;
		} finally {
			readWriteLock.writeLock().unlock();
		}
	}

	@Override
	public synchronized void collectGarbage() {
		try {
			readWriteLock.writeLock().lock();
			content = null;
		}finally {
			readWriteLock.writeLock().unlock();;
		}
	}

	@Override
	public String getUuid() {
		// TODO Auto-generated method stub
		return uuid;
	}

}

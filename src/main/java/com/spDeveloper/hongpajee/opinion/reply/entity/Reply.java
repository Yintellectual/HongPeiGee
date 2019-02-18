package com.spDeveloper.hongpajee.opinion.reply.entity;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.spDeveloper.hongpajee.post.entity.Article;
import com.spDeveloper.hongpajee.util.extendable.Extendable;

import lombok.Data;

@Data
public class Reply extends Extendable implements Comparable<Reply> {

	private String message;
	private String username;
	private Long time;
	private String uuid;
	private String articleUuid;
	
	
	public static class ReplyTypeAdapter implements JsonSerializer<Reply>, JsonDeserializer<Reply>{

		@Override
		public Reply deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
				throws JsonParseException {
			// TODO Auto-generated method stub
			
			JsonObject data = json.getAsJsonObject();
			String message = data.get("message").getAsString();
			String username = data.get("username").getAsString();
			Long time = data.get("time").getAsLong();
			String uuid = data.get("uuid").getAsString();
			String articleUuid = data.get("articleUuid").getAsString();
			
			Reply result = new Reply(message, username, articleUuid);
			result.setUuid(uuid);
			result.setTime(time);
			
			return result;
		}

		@Override
		public JsonElement serialize(Reply src, Type typeOfSrc, JsonSerializationContext context) {
			// TODO Auto-generated method stub
			JsonObject result = new JsonObject();
			result.add("message", new JsonPrimitive(src.getMessage()));
			result.add("username", new JsonPrimitive(src.getUsername()));
			result.add("time", new JsonPrimitive(src.getTime()));
			result.add("uuid", new JsonPrimitive(src.getUuid()));
			result.add("articleUuid", new JsonPrimitive(src.getArticleUuid()));
			return result;
		}


	}
	
	
	public Reply() {
		time = new Date().getTime();
		uuid = UUID.randomUUID().toString();
	}
	public Reply(String message, String username, String articleUuid) {
		this();
		this.message = message;
		this.username = username;
		this.articleUuid = articleUuid;
	}
	public Reply(Reply reply) {
		this.time = reply.getTime();
		this.uuid = reply.getUuid();
		this.message = reply.getMessage();
		this.articleUuid = reply.getArticleUuid();
		this.username = reply.getUsername();
	}
	
	@Override
	public int compareTo(Reply o) {
		// TODO Auto-generated method stub
		return o.getTime().compareTo(getTime());
	}
	@Override
	public boolean equals(Object o) {
		if (o!=null && o instanceof Reply) {
			Reply other = (Reply) o;
			return getUuid().equals(other.getUuid());
		}else {
			return false;
		}
	}
	@Override
	public int hashCode() {
		return uuid.hashCode();
	}
}

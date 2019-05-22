package com.spDeveloper.hongpajee.message.entity;

import java.lang.reflect.Type;
import java.util.Date;
import java.util.UUID;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.spDeveloper.hongpajee.message.entity.content.Content;
import com.spDeveloper.hongpajee.message.entity.content.proxy.HTMLContentVirtualProxy;
import com.spDeveloper.hongpajee.message.entity.content.repository.ContentRepository;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HeavyMessage implements Message {

	private String uuid;
	private Long timestamp;
	private String username;
	private Content content;
	
	public HeavyMessage(String username, Content content) {
		this.uuid = UUID.randomUUID().toString();
		this.timestamp = new Date().getTime();
		this.username = username;
		this.content = content;
	}
	
	public static class HeavyMessageTypeAdapter implements JsonSerializer<HeavyMessage>, JsonDeserializer<HeavyMessage>{
		
		public static ContentRepository contentRepository;
		
		@Override
		public HeavyMessage deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
				throws JsonParseException {
			JsonObject jsonObject = json.getAsJsonObject();
			String uuid = jsonObject.get("uuid").getAsString();
			Long timestamp = jsonObject.get("timestamp").getAsLong();
			String username = jsonObject.get("username").getAsString();
			Content content = contentRepository.find(jsonObject.get("contentUuid").getAsString());
			System.out.println("**************HeavyMessage:");
			System.out.println(jsonObject.get("contentUuid").getAsString());
			return new HeavyMessage(uuid, timestamp, username, content);
		}

		@Override
		public JsonElement serialize(HeavyMessage src, Type typeOfSrc, JsonSerializationContext context) {
			JsonObject result = new JsonObject();
			result.addProperty("uuid", src.getUuid());
			result.addProperty("timestamp", src.getTimestamp());
			result.addProperty("username", src.getUsername());
			result.addProperty("contentUuid", src.getContent().getUuid());
			return result;
		}
		
	}
	
	
	@Override
	public String getUuid() {
		// TODO Auto-generated method stub
		return this.uuid;
	}

	@Override
	public Long getTimestamp() {
		// TODO Auto-generated method stub
		return this.timestamp;
	}

	@Override
	public String getUsername() {
		// TODO Auto-generated method stub
		return this.username;
	}

	@Override
	public String getContentString() {
		// TODO Auto-generated method stub
		return this.content.getString();
	}

}

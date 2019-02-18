package com.spDeveloper.hongpajee.profile.entity;

import java.lang.reflect.Type;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.spDeveloper.hongpajee.post.entity.Article;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDescription {

	public static class ArticleTypeAdapter implements JsonSerializer<UserDescription>, JsonDeserializer<UserDescription> {

		@Override
		public UserDescription deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
				throws JsonParseException {
			JsonObject jsonObject = json.getAsJsonObject();
			String nickname = jsonObject.get("nickname").getAsString();
			String username = jsonObject.get("username").getAsString();
			return new UserDescription(nickname, username);
		}

		@Override
		public JsonElement serialize(UserDescription src, Type typeOfSrc, JsonSerializationContext context) {
			JsonObject result = new JsonObject();
			result.addProperty("nickname", src.getNickname());
			result.addProperty("username", src.getUsername());
			return result;
		}

	}
	
	private String nickname; 
	private String username;
}
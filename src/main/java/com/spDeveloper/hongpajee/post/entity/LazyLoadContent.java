package com.spDeveloper.hongpajee.post.entity;

import java.lang.reflect.Type;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import lombok.Data;

/**
 * Rewrite this class to use the virtual proxy pattern. 
 * */
@Data
public class LazyLoadContent {

	private String content;

	public static class LazyLoadContentTypeAdapter
			implements JsonSerializer<LazyLoadContent>, JsonDeserializer<LazyLoadContent> {

		@Override
		public LazyLoadContent deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
				throws JsonParseException {
			// TODO Auto-generated method stub
			String content = json.getAsJsonObject().get("content").getAsString();
			LazyLoadContent lazyLoadContent = new LazyLoadContent();
			lazyLoadContent.setContent(content);
			return lazyLoadContent;
		}

		@Override
		public JsonElement serialize(LazyLoadContent src, Type typeOfSrc, JsonSerializationContext context) {
			// TODO Auto-generated method stub
			JsonObject result = new JsonObject();
			result.add("content", new JsonPrimitive(src.getContent()));
			return result;
		}

	}

	public String getContent() {
		return content;
	}
}
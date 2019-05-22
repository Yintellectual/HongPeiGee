package com.spDeveloper.hongpajee.text.entity;

import java.lang.reflect.Type;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.spDeveloper.hongpajee.message.entity.content.HTMLContent;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
public class MutableLine implements Line {

	private String content;
	private String uuid;
	private String updateUUID;
	private LineStatus status;
	
	public static class MutableLineTypeAdapter implements JsonSerializer<MutableLine>, JsonDeserializer<MutableLine>{

		@Override
		public MutableLine deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
				throws JsonParseException {
			JsonObject jsonObject = json.getAsJsonObject();
			String content = jsonObject.get("content").getAsString();
			String uuid = jsonObject.get("uuid").getAsString();
			String updateUUID = null;
			if(jsonObject.get("updateUUID")!=null) {
				updateUUID = jsonObject.get("updateUUID").getAsString();;	
			}			
			LineStatus status = LineStatus.valueOf(jsonObject.get("status").getAsString());
			return new MutableLine(content, uuid, updateUUID, status);
		}

		@Override
		public JsonElement serialize(MutableLine src, Type typeOfSrc, JsonSerializationContext context) {
			// TODO Auto-generated method stub
			JsonObject result = new JsonObject();
			result.addProperty("content", src.content);
			result.addProperty("uuid", src.uuid);
			result.addProperty("updateUUID", src.updateUUID);
			result.addProperty("status", src.status.name());
			return result;
		}
		
	} 
	
	
	public static List<Line> toLines(List<String> strings){
		if(strings==null) {
			return null;
		}else {
			return strings.stream().map(MutableLine::new).collect(Collectors.toList());
		}	
	}
	
	public MutableLine() {
		// TODO Auto-generated constructor stub
	}
	public MutableLine(String content) {
		this.content = content;
		this.updateUUID = null;
		this.uuid = UUID.randomUUID().toString();
		this.status = LineStatus.unchanged;
	}
	public MutableLine(String content, String updateUUID) {
		this.content = content;
		this.updateUUID = updateUUID;
		this.uuid = UUID.randomUUID().toString();
		this.status = LineStatus.unchanged;
	}
	public MutableLine(String content, String uuid, String updateUUID, LineStatus status) {
		// TODO Auto-generated constructor stub
		this.content = content;
		this.uuid = uuid;
		this.updateUUID = updateUUID;
		this.status = status;
	}
}

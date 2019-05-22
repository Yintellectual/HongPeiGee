package com.spDeveloper.hongpajee.message.entity.content;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.spDeveloper.hongpajee.message.entity.content.visitor.ContentVisitor;
import com.spDeveloper.hongpajee.text.entity.Line;
import com.spDeveloper.hongpajee.text.entity.MutableLine;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TextContent implements Content{

	private String uuid;
	private List<Line> lines;
	
	public static class TextContentTypeAdapter implements JsonSerializer<TextContent>, JsonDeserializer<TextContent>{

		@Override
		public TextContent deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
				throws JsonParseException {
			JsonObject jsonObject = json.getAsJsonObject();
			String uuid = jsonObject.get("uuid").getAsString(); 
			List<Line> lines = new ArrayList<>();
			JsonArray linesJsonArray = jsonObject.get("lines").getAsJsonArray();
			linesJsonArray.forEach(je->{
				lines.add(context.deserialize(je, MutableLine.class));
			});
			return new TextContent(uuid, lines);
		}

		@Override
		public JsonElement serialize(TextContent src, Type typeOfSrc, JsonSerializationContext context) {
			JsonObject result = new JsonObject();
			result.addProperty("uuid", src.uuid);
			JsonArray linesJson = new JsonArray();
			src.lines.forEach(line->linesJson.add(context.serialize(line)));
			result.add("lines", linesJson);
			return result;
		}
	}
	
	public TextContent(String input) {
		List<String> strings = Arrays.asList(input.split("\\r?\\n"));
		this.lines = strings.stream().map(MutableLine::new).collect(Collectors.toList());
		this.uuid = UUID.randomUUID().toString();
	}
	
	@Override
	public String getString() {
		// TODO Auto-generated method stub
		if(lines==null) {
			return null;
		}else {
			return StringUtils.join(lines.stream().map(l->l.getContent()).toArray(), "\n");
		}
	}

	@Override
	public void accept(ContentVisitor visitor) {
		// TODO Auto-generated method stub
		visitor.visit(this);
	}

	@Override
	public String getUuid() {
		// TODO Auto-generated method stub
		return uuid;
	}
}

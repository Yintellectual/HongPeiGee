package com.spDeveloper.hongpajee.navbar.entity;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.spDeveloper.hongpajee.tag.entity.TagHolder;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor

/**
 *  set display name = "driver" will result in a bar without any text. 
 * 
 * */
public class NavItem implements TagHolder{
	
	public static class NavItemTypeAdapter implements JsonSerializer<NavItem>, JsonDeserializer<NavItem>{

		@Override
		public JsonElement serialize(NavItem src, Type typeOfSrc, JsonSerializationContext context) {
			JsonObject result = new JsonObject();
			result.add("id", new JsonPrimitive(src.getId()));
			result.add("link", new JsonPrimitive(src.getLink()));
			result.add("display", new JsonPrimitive(src.getDisplay()));
			result.add("tags", context.serialize(src.getTags()));
			if(src.getHasDropdown()) {
				result.add("dropdown", context.serialize(src.getDropdown()));
			}else {
				
			}
			return result;
		}
		
		@Override
		public NavItem deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
				throws JsonParseException {
			String display = json.getAsJsonObject().get("display").getAsString();
			String link = json.getAsJsonObject().get("link").getAsString();
			String id= json.getAsJsonObject().get("id").getAsString();
			NavItem navItem = new NavItem(display, link, id);
			List<String> tags = new ArrayList<>();
			json.getAsJsonObject().get("tags").getAsJsonArray().forEach(t->{
				tags.add(context.deserialize(t, String.class));
			});
			
			if(json.getAsJsonObject().has("dropdown")) {
				List<NavItem> dropdown  = new ArrayList<>();
				
				JsonArray arr = json.getAsJsonObject().getAsJsonArray("dropdown");
				for(int i=0;i<arr.size();i++) {
					dropdown.add(context.deserialize(arr.get(i), typeOfT));
				}
				
				navItem.setDropdown(dropdown);
			}else {
				
			}
			return navItem;
		}
	}
		
	
	
	private String display;
	private String link;
	private String id;
	private List<String> tags = new ArrayList<>();
	
	private List<NavItem> dropdown = new ArrayList<>();

	public NavItem(String display, String link, String id) {
		this.display = display;
		this.link = link;
		this.id = id;
	}
	
	public String getLink() {
		if(getHasDropdown()) {
			return "#";
		}else {
			return link;
		}
	}
	public void addDropdown(NavItem navItem) {
		dropdown.add(navItem);
	}
	public boolean getHasDropdown() {
		return dropdown!=null && !dropdown.isEmpty();
	}
	
	@Override
	public boolean equals(Object o) {
		if(o != null && o instanceof NavItem) {
			return ((NavItem) o).getId().equals(this.id);
		}else {
			return false;
		}
	}
	
	@Override
	public int hashCode() {
		return this.id.hashCode();
	}
	
	
	@Override
	public Object clone() {
		NavItem result = new NavItem(display, link, id);
		this.getTags().forEach(result::addTag);
		if(!getHasDropdown()) {
			
		}else {
			result.setDropdown(dropdown.stream().map(NavItem::clone).map(o->(NavItem)o).collect(Collectors.toList()));
		}
		return result;
	}

	public void setTags(List<String> tags) {
		if(tags==null||tags.isEmpty()) {
			
		}else {
			tags.forEach(this::addTag);
		}
	}
	
	@Override
	public void addTag(String tag) {
		// TODO Auto-generated method stub
		tags.add(tag);
		generateLinkByTags();
	}

	public void generateLinkByTags() {
		StringBuilder sb = new StringBuilder("/tag?navItemId=");
		sb.append(getId());
		sb.append('&');
		tags.forEach(t->{
			sb.append("tag=");
			sb.append(t);
			sb.append('&');
		});
		//remove the last character, could be '?' or '&'.
		sb.setLength(sb.length() - 1);
		setLink(sb.toString());
	}
	
	
	@Override
	public boolean hasTag(String tag) {
		// TODO Auto-generated method stub
		return tags.contains(tag);
	}

	@Override
	public void removeTag(String tag) {
		// TODO Auto-generated method stub
		tags.remove(tag);
		generateLinkByTags();
	}
}

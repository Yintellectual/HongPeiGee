package com.spDeveloper.hongpajee.util.set;

import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Set;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OperationalSet<T> {
	
	public static class GsonSerializer implements JsonSerializer<OperationalSet>, JsonDeserializer<OperationalSet>{
		@Override
		public JsonElement serialize(OperationalSet src, Type typeOfSrc, JsonSerializationContext context) {
			JsonObject jsonObject = new JsonObject();
			if (src instanceof NoneOperationalSet) {
				NoneOperationalSet noneOperationalSet = (NoneOperationalSet) src;
				jsonObject.addProperty("name", noneOperationalSet.getName());
			}else {
				jsonObject.addProperty("operation", src.getOperation().toString());
				jsonObject.add("operant1", serialize(src.getOperant1(), typeOfSrc, context));
				jsonObject.add("operant2", serialize(src.getOperant2(), typeOfSrc, context));
			}
			return jsonObject;
		}

		@Override
		public OperationalSet deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
				throws JsonParseException {
			JsonObject jsonObject = json.getAsJsonObject();
			if(jsonObject.has("name")) {
				NoneOperationalSet noneOperationalSet = new NoneOperationalSet();
				noneOperationalSet.setName(jsonObject.get("name").getAsString());
				return noneOperationalSet;
			}else {
				OperationalSet result = new OperationalSet();
				SetOperation operation = SetOperation.valueOf(jsonObject.get("operation").getAsString());
				result.setOperation(operation);
				result.setOperant1(deserialize(jsonObject.get("operant1"), typeOfT, context));
				result.setOperant2(deserialize(jsonObject.get("operant2"), typeOfT, context));
				return result;
			}
		}
	}
	
	private SetOperation operation;
	private OperationalSet<T> operant1;
	private OperationalSet<T> operant2;
	
	
	/**
	 * always return a new hashset.
	 * */
	public HashSet<T> toSet(){
		switch (operation) {
		case none:
			if(operant1==null) {
				return null;
			}else {
				HashSet<T> result = new HashSet<>();
				HashSet<T> set1 = operant1.toSet();
				if(set1==null) {
					return null;
				}else {
					result.addAll(set1);
					return result;
				}
			}
		case union:
			if(operant1!=null&&operant2!=null) {
				HashSet<T> set1 = operant1.toSet();
				HashSet<T> set2 = operant2.toSet();
				if(set1==null) {
					return set2;
				}else if(set2==null){
					return set1;
				}else {
					if(set1.size()>set2.size()) {
						set1.addAll(set2);
						return set1;
					}else {
						set2.addAll(set1);
						return set2;
					}	
				}
			}else if(operant1==null&&operant2==null){
				return null;
			}else if(operant1==null){
				return operant2.toSet();
			}else if(operant2==null){
				return operant1.toSet();
			}else {
				throw new RuntimeException("OperationalSet: switch case [union] report unchecked condition.");
			}
		case difference:
			if(operant1!=null&&operant2!=null) {
				HashSet<T> set1 = operant1.toSet();
				HashSet<T> set2 = operant2.toSet();
				if(set1==null) {
					return null;
				}else if(set2==null){
					return set1;
				}else {
					set1.removeAll(set2);
					return set1;
				}
			}else if(operant1==null&&operant2==null){
				return null;
			}else if(operant1==null){
				return null;
			}else if(operant2==null){
				return operant1.toSet();
			}else {
				throw new RuntimeException("OperationalSet: switch case [difference] report unchecked condition.");
			}
		case intersection:
			if(operant1!=null&&operant2!=null) {
				HashSet<T> set1 = operant1.toSet();
				HashSet<T> set2 = operant2.toSet();
				if(set1==null) {
					return null;
				}else if(set2==null){
					return null;
				}else {
					set1.retainAll(set2);
					return set1;
				}
			}else if(operant1==null&&operant2==null){
				return null;
			}else if(operant1==null){
				return null;
			}else if(operant2==null){
				return null;
			}else {
				throw new RuntimeException("OperationalSet: switch case [intersection] report unchecked condition.");
			}
		default:
			return null;
		}
	}
}

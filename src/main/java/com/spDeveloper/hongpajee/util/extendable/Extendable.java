package com.spDeveloper.hongpajee.util.extendable;

import java.util.ArrayList;
import java.util.List;

import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

public class Extendable {
	private MultiValueMap<String, String> extensions = new LinkedMultiValueMap<>();
	
	public void removeExtensions() {
		this.extensions = new LinkedMultiValueMap<>();
	}
	public void removeExtension(String key) {
		this.extensions.remove(key);
	}
	//at least one key and on value. 
	public void setExtension(String ... args) {
		if(args.length<2) {
			return ;
		}
		
		String key = args[0];
		List<String> values = new ArrayList<>();
		for(int i=1;i<args.length;i++) {
			values.add(args[i]);
		}
		
		removeExtension(key);
		extensions.addAll(key, values);
	}
	public List<String> getExtension(String key) {
		return extensions.get(key);
	}
}

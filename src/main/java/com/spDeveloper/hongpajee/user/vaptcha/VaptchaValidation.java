package com.spDeveloper.hongpajee.user.vaptcha;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpRequest;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class VaptchaValidation {

	@Autowired
	Gson gson;
	@Autowired
	RestTemplate restTemplate;
	//地址: http://api.vaptcha.com/v2/validate
	String url = "http://api.vaptcha.com/v2/validate";
	//vid
	private String id = "5c41681afc650e4f1c0422b4";
	//secretkey
	private String secretkey = "efc686dd6a6a41f592f0abdbc305f11f";
	//scene, default is empty string
	private String scene = "";
	
	//2019-01-22: ip is ignored by vaptcha service
	public boolean validate(String token, String ip) {
		
		
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
		
		MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
		formData.add("id", id);
		formData.add("secretkey", secretkey);
		formData.add("scene", scene);
		formData.add("token", token);
		formData.add("ip", ip);
		
		HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<MultiValueMap<String,String>>(formData, headers);		
		
		ResponseEntity<String> result = restTemplate.exchange(url, HttpMethod.POST, request, String.class);
		
		return gson.fromJson(result.getBody(), JsonElement.class).getAsJsonObject().get("success").getAsInt()==1;
	}
}

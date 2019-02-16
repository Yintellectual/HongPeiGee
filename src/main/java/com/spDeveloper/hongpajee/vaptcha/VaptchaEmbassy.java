package com.spDeveloper.hongpajee.vaptcha;

import java.time.Instant;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpRequest;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.google.gson.Gson;
import com.google.gson.JsonElement;

@Service
public class VaptchaEmbassy {

	@Autowired
	Gson gson;
	@Autowired
	RestTemplate restTemplate;
	
	@Value("${vaptcha.vid}")
	String vid;
	@Value("${vaptcha.key}")
	String secretkey;
	@Value("${vaptcha.smskey}")
	String smskey;

	//2019-01-22: ip is ignored by vaptcha service
	public boolean validateVaptchaToken(String token, String ip) {
		//地址: http://api.vaptcha.com/v2/validate
		String url = "http://api.vaptcha.com/v2/validate";
		//scene, default is empty string
		String scene = "";
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
		
		MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
		formData.add("id", vid);
		formData.add("secretkey", secretkey);
		formData.add("scene", scene);
		formData.add("token", token);
		formData.add("ip", ip);
		
		HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<MultiValueMap<String,String>>(formData, headers);		
		
		ResponseEntity<String> result = restTemplate.exchange(url, HttpMethod.POST, request, String.class);
		
		return gson.fromJson(result.getBody(), JsonElement.class).getAsJsonObject().get("success").getAsInt()==1;
	}
	
	public boolean sendSMS(String phone, String countrycode, String token) {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
		MultiValueMap<String, String> map = new LinkedMultiValueMap<>();

		String label = "www.hanchen.site";// 希望显示的验证码来源名称 string
		String time = Instant.now().getEpochSecond() + "";// 当前unix时间戳 string
		String version = "1.0";

		map.add("vid", vid);
		map.add("smskey", smskey);
		map.add("label", label);
		map.add("phone", phone);
		map.add("countrycode", countrycode);
		map.add("time", time);
		map.add("version", version);
		if(token!=null) {
			map.add("token", token);
		}
		HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);

		ResponseEntity<String> httpResponse = restTemplate.exchange("http://smsapi.vaptcha.com/sms/verifycode",
				HttpMethod.POST, request, String.class);
		if(httpResponse.getBody().contains("200")) {
			return true;
		}else {
			return false;
		}
	}

	public boolean validateCode(String code, String phone, String countrycode) {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
		MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
		
		String time =Instant.now().getEpochSecond() + "";	//当前unix时间戳	string
		String version = "1.0";//	当前版本（1.0）
		
		map.add("code", code);
		map.add("vid", vid);
		map.add("smskey", smskey);
		map.add("phone", phone);
		map.add("countrycode", countrycode);
		map.add("time", time);
		map.add("version", version);
		
		HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);

		ResponseEntity<String> httpResponse = restTemplate.exchange("http://smsapi.vaptcha.com/sms/verify",
				HttpMethod.POST, request, String.class);

		if(httpResponse.getBody().contains("200")) {
			return true;
		}else {
			return false;
		}
	}
}

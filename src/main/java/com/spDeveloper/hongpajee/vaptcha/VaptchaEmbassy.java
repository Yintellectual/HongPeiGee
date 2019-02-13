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

@Service
public class VaptchaEmbassy {

	@Autowired
	RestTemplate restTemplate;

	@Value("${vaptcha.vid}")
	String vid;
	@Value("${vaptcha.smskey}")
	String smskey;

	public void fireSMS(String phone, String countrycode) {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
		MultiValueMap<String, String> map = new LinkedMultiValueMap<>();

		String token;// 由vaptcha验证码验证成功后返回的token（一键通过或绘制图形） string
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

		HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);

		ResponseEntity<String> httpResponse = restTemplate.exchange("http://smsapi.vaptcha.com/sms/verifycode",
				HttpMethod.POST, request, String.class);
	}

	public void validateCode(String code, String phone, String countrycode) {
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
		System.out.println(httpResponse.getStatusCode());
		System.out.println(httpResponse.getHeaders());
		System.out.println(httpResponse.getBody());
		System.out.println(httpResponse);
	}
}

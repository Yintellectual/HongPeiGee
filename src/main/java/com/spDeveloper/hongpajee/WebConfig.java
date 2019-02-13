package com.spDeveloper.hongpajee;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.GsonHttpMessageConverter;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import com.google.gson.Gson;

@Configuration
public class WebConfig implements WebMvcConfigurer{

	@Autowired
	Gson gson;
	
	public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
	    GsonHttpMessageConverter gsonHttpMessageConverter = new GsonHttpMessageConverter();
        gsonHttpMessageConverter.setGson(gson);
        gsonHttpMessageConverter.setSupportedMediaTypes(Arrays
            .asList(MediaType.APPLICATION_JSON));
		
		converters.add(0, gsonHttpMessageConverter);
	}
	
}

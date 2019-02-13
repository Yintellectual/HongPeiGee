package com.spDeveloper.hongpajee.user.controller;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.config.annotation.authentication.configurers.provisioning.UserDetailsManagerConfigurer.UserDetailsBuilder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.spDeveloper.hongpajee.user.vaptcha.VaptchaValidation;

@Controller
public class LoginController {

	@Value("${vaptcha.vid}")
	String vaptchaVid;
	
	@Autowired
	UserDetailsManager userDetailsManager;
	@Autowired
	VaptchaValidation vaptchaValidation;
	
	@GetMapping("/login/login")
	public String getLogin() {
		return "login";
	}
	
	@GetMapping("/login/register")
	public String getRegister(Model model) {
		model.addAttribute("vaptcha_vid", vaptchaVid);
		return "register";
	}
	
	
	@PostMapping("/login/register")
	public String postRegister(@RequestParam("username") String username, @RequestParam("password") String password, @RequestParam("vaptcha_token") String vaptcha_token, HttpServletRequest request) {
		
		if(vaptchaValidation.validate(vaptcha_token, request.getRemoteAddr())) {
			userDetailsManager.createUser(User.withUsername(username).password(password).roles("USER").build());
			return "redirect:/";	
		}else {
			return "redirect:/login/register?error=vaptchaFail"; 
		}
	}
}

package com.spDeveloper.hongpajee.user.controller;

import java.security.Principal;
import java.util.Objects;

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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.spDeveloper.hongpajee.user.config.RedisUserDetailsManager;
import com.spDeveloper.hongpajee.user.vaptcha.VaptchaValidation;
import com.spDeveloper.hongpajee.vaptcha.VaptchaEmbassy;

@Controller
public class LoginController {

	@Value("${vaptcha.vid}")
	String vaptchaVid;

	@Autowired
	RedisUserDetailsManager userDetailsManager;
	@Autowired
	VaptchaEmbassy vapchaEmbassy;

	@GetMapping("/login/login")
	public String getLogin() {
		return "login";
	}

	@GetMapping("/login/register")
	public String getRegister(Model model) {
		model.addAttribute("vaptcha_vid", vaptchaVid);
		return "register";
	}

	/**
	 * Registration Policy
	 * 
	 * 1.username: 1.1 not null 1.2 consists of 11 digits 1.3 pass the sms
	 * validation
	 * 
	 * 2.password: 2.1 not null 2.2 at least 8 characters
	 * 
	 * 3.Authentication via phone vs. via password 3.1 Phone has higher priority
	 * than password 3.2 Password is sufficient to login 3.3 Phone validation is
	 * necessary to change password 3.4 Phone validation is necessary to change
	 * password without login 3.5 Phone validation is necessary to change phone 3.6
	 * On report of losing phone number, the administer will conduct this procedure:
	 * 3.6.1 The user is recommended to register a new account 3.6.2 The lost
	 * account is frozen for 4 weeks 3.5.3 Any login must go through phone
	 * validation 3.5.4 any successful phone validation will break the procedure
	 * 3.6.2 The lost account will be merged into the new account
	 * 
	 * 
	 */
	@PostMapping("/login/register")
	public String postRegister(@RequestParam("username") String username, @RequestParam("password") String password1,
			@RequestParam("password_confirmation") String password2, @RequestParam("vaptcha_token") String vaptcha_token,
			@RequestParam("smscode") String smscode, HttpServletRequest request) {
		// validation >>
		if (Objects.isNull(username) || Objects.isNull(password1) || Objects.isNull(password2)
				|| Objects.isNull(vaptcha_token) || Objects.isNull(smscode)) {
			return "redirect:/login/register?error=nullValue";
		}
		username = username.trim();
		password1 = password1.trim();
		password2 = password2.trim();
		vaptcha_token = vaptcha_token.trim();
		smscode = smscode.trim();
		if (username.isEmpty() || password1.isEmpty() || password2.isEmpty() || vaptcha_token.isEmpty()
				|| smscode.isEmpty()) {
			return "redirect:/login/register?error=emptyValue";
		}

		if (!username.matches("\\d\\d\\d\\d\\d" + "\\d\\d\\d\\d\\d" + "\\d")) {
			return "redirect:/login/register?error=usernameMustBeChinesePhoneNumber";
		}

		if (!password1.equals(password2)) {
			return "redirect:/login/register?error=passwordsDisagree";
		} else if (password1.length() < 8) {
			return "redirect:/login/register?error=passwordTooShort";
		} else if (!vapchaEmbassy.validateVaptchaToken(vaptcha_token, request.getRemoteAddr())) {
			return "redirect:/login/register?error=invalidVaptcha";
		} else if (!vapchaEmbassy.validateCode(smscode, username, "86")) {
			return "redirect:/login/register?error=invalidSmsCode";
		} else {
			userDetailsManager.createUser(User.withUsername(username).password(password1).roles("USER").build());
			return "redirect:/login/login";
		}
	}

	@PostMapping(value = "/login/sendsms", produces = "application/json", consumes = { "application/json" })
	@ResponseBody
	public JsonObject sendmss(@RequestBody JsonObject json) {
		String phone = json.get("phone").getAsString();
		String token = null;
		if (json.get("token") != null) {
			token = json.get("token").getAsString();
		}
		JsonObject result = new JsonObject();
		if (vapchaEmbassy.sendSMS(phone, "86", token)) {
			result.add("status", new JsonPrimitive("success"));
		} else {
			result.add("status", new JsonPrimitive("error"));
		}

		return result;
	}
	
	/**
	 * Reset password
	 * */
	@GetMapping("/login/password/reset")
	public String resetPassword(Model model) {
		model.addAttribute("vaptcha_vid", vaptchaVid);
		return "reset_password";
	} 
	
	@PostMapping("/login/password/reset")
	public String resetPassword(@RequestParam("username") String username, @RequestParam("password") String password1,
			@RequestParam("password_confirmation") String password2, @RequestParam("vaptcha_token") String vaptcha_token,
			@RequestParam("smscode") String smscode, HttpServletRequest request) {
		// validation >>
		if (Objects.isNull(username) || Objects.isNull(password1) || Objects.isNull(password2)
				|| Objects.isNull(vaptcha_token) || Objects.isNull(smscode)) {
			return "redirect:/login/register?error=nullValue";
		}
		username = username.trim();
		password1 = password1.trim();
		password2 = password2.trim();
		vaptcha_token = vaptcha_token.trim();
		smscode = smscode.trim();
		if (username.isEmpty() || password1.isEmpty() || password2.isEmpty() || vaptcha_token.isEmpty()
				|| smscode.isEmpty()) {
			return "redirect:/login/register?error=emptyValue";
		}

		if (!username.matches("\\d\\d\\d\\d\\d" + "\\d\\d\\d\\d\\d" + "\\d")) {
			return "redirect:/login/register?error=usernameMustBeChinesePhoneNumber";
		}

		if (!password1.equals(password2)) {
			return "redirect:/login/register?error=passwordsDisagree";
		} else if (password1.length() < 8) {
			return "redirect:/login/register?error=passwordTooShort";
		} else if (!vapchaEmbassy.validateVaptchaToken(vaptcha_token, request.getRemoteAddr())) {
			return "redirect:/login/register?error=invalidVaptcha";
		} else if (!vapchaEmbassy.validateCode(smscode, username, "86")) {
			return "redirect:/login/register?error=invalidSmsCode";
		} else {
			userDetailsManager.updatePassword(userDetailsManager.loadUserByUsername(username), password2);
			return "redirect:/login/login";
		}
	}
}

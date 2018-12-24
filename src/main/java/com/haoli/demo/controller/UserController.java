package com.haoli.demo.controller;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.haoli.demo.service.UserService;

@RestController
public class UserController {
	
	@Autowired
	UserService userService;

	@PostMapping("/user/register")
	public void register(@RequestBody Map<String, Object> params) {
		
	}
	
	
	@PostMapping("/user/export")
	public void export(HttpServletRequest request, 
						HttpServletResponse response, 
						Map<String, Object> params) throws Exception {
		userService.export(request, response, params);
	}

}

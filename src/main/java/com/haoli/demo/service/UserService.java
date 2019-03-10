package com.haoli.demo.service;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.haoli.demo.dao.UserDao;
import com.haoli.sdk.web.util.microSoftOffice.ExcelUtil;

@Service
public class UserService {
	
	@Autowired
	UserDao userDao;
	
	public void export(HttpServletRequest request, HttpServletResponse response, Map<String, Object> params) throws Exception {
		OutputStream os = response.getOutputStream();
		List<Map<String, Object>> dataList = new ArrayList<Map<String, Object>>();
		Map<String, Object> varMap = new HashMap<String, Object>();
		varMap.put("dataList", dataList);
		response.setContentType("application/msexcel");
		String fileName =URLEncoder.encode("用户信息.xlsx","UTF-8");
		response.setHeader("Content-Disposition","attachment;filename=\"" + fileName + "\""); 
		ExcelUtil eu = new ExcelUtil();
		eu.exportExcel("/templates/user.xlsx", os, varMap);		
	}
}

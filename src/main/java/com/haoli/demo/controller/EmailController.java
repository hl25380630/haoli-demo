package com.haoli.demo.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.haoli.demo.dao.EmailDao;
import com.haoli.demo.domain.JsonResponse;
import com.haoli.sdk.web.domain.MailConfig;
import com.haoli.sdk.web.util.EmailUtil;
import com.haoli.sdk.web.util.QRCodeUtil;
import com.haoli.sdk.web.util.VelocityUtil;

@RestController
public class EmailController {
	

	
	@Autowired
	private VelocityUtil velocityUtil;
	
	@Autowired
	private EmailDao emailDao;

	@GetMapping("/demo/sendEmail")
	public JsonResponse<String> sendEmail() throws Exception{
		Long userId = 1L;
		Map<String, Object> qparams = new HashMap<String, Object>();
		qparams.put("userId", userId);
		MailConfig mailConfig = emailDao.getMailConfig(qparams);
		Map<String, Object> params = new HashMap<String, Object>();
		String subject = "李昊测试邮件";
		String attachment = "http://boe-ssc-object.oss-cn-beijing.aliyuncs.com/pdf/zhusu.pdf";
		String img = "https://www.baidu.com/img/bd_logo1.png";
		params.put("cid1", "qrcode");
		params.put("cid2", "img");
		String content = velocityUtil.getText("templates/mail/demoMailTemplate.vm", params);
		String[] toList = {"lihao_100@boe.com.cn"};
		try{
			EmailUtil emailUtil = new EmailUtil(mailConfig);
			emailUtil.sendEmail(toList, subject, content, null, new String[]{"attachment","img"}, new String[]{attachment,img});
			return new JsonResponse<String>("true");
		} catch(Exception e) {
			return new JsonResponse<String>("false");
		}
	}
	
	@GetMapping("/demo/testConnect")
	public JsonResponse<Boolean> testConnect() throws Exception{
		Long userId = 2L;
		Map<String, Object> qparams = new HashMap<String, Object>();
		qparams.put("userId", userId);
		MailConfig mailConfig = emailDao.getMailConfig(qparams);
		EmailUtil emailUtil = new EmailUtil(mailConfig);
		boolean flag = emailUtil.connect();
		return new JsonResponse<Boolean>(flag);
	}
}

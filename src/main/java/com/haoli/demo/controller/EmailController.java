package com.haoli.demo.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.haoli.demo.domain.JsonResponse;
import com.haoli.sdk.web.util.EmailUtil;
import com.haoli.sdk.web.util.QRCodeUtil;
import com.haoli.sdk.web.util.VelocityUtil;

@RestController
public class EmailController {
	
	private EmailUtil emailUtil;
	
	@Autowired
	private VelocityUtil velocityUtil;

	@GetMapping("/demo/sendEmail")
	public JsonResponse<String> sendEmail() throws Exception{
		Map<String, Object> params = new HashMap<String, Object>();
		String subject = "李昊测试邮件";
		String path="C:\\Users\\10063731\\Desktop\\BOE\\开发\\会议系统ics" ;
		String inviteCode = "123456";
		String codePath = path+"U"+inviteCode+".png";
		QRCodeUtil.writeToFile(inviteCode, codePath, 50, 50);
		String hotelEmail = "http://boe-ssc-object.oss-cn-beijing.aliyuncs.com/pdf/zhusu.pdf";
		String img = "https://www.baidu.com/img/bd_logo1.png";
		params.put("cid1", "qrcode");
		params.put("cid2", "img");
		params.put("code", inviteCode);
		String content = velocityUtil.getText("templates/mail/imageMail.vm", params);
		String[] toList = {"lihao_100@boe.com.cn"};

		try{
			emailUtil.sendEmail(toList, subject, content, null, new String[]{"hotel","img","qrcode"}, new String[]{hotelEmail,img,codePath});
			return new JsonResponse<String>("true");
		} catch(Exception e) {
			return new JsonResponse<String>("false");
		}
	}
}

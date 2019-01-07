package com.haoli.demo.dao;

import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

import com.haoli.sdk.web.domain.MailConfig;

@Mapper
public interface EmailDao {

	MailConfig getMailConfig(Map<String, Object> params);

}

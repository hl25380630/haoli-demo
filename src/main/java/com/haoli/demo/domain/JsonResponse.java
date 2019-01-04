package com.haoli.demo.domain;

public class JsonResponse<T> {

	private String code;
	
	private String msg;
	
	private T data;
	
	public JsonResponse(T data) {
		this.data = data;
		this.code = "200";
		this.msg = "request success";
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

	public T getData() {
		return data;
	}

	public void setData(T data) {
		this.data = data;
	}

}

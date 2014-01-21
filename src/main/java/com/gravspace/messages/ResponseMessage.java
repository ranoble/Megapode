package com.gravspace.messages;

import java.util.List;

import org.apache.http.Header;
import org.apache.http.HttpStatus;

public class ResponseMessage {
	int status;
	byte[] responseContent;
	String contentType;
	List<Header> headers;
	//List<Header> cookies;
	public int getStatus() {
		return status;
	}
	public void setStatus(int status) {
		this.status = status;
	}
	public byte[] getResponseContent() {
		return responseContent;
	}
	public void setResponseContent(byte[] responseContent) {
		this.responseContent = responseContent;
	}
	public String getContentType() {
		return contentType;
	}
	public void setContentType(String contentType) {
		this.contentType = contentType;
	}
	public List<Header> getHeaders() {
		return headers;
	}
	public void setHeaders(List<Header> headers) {
		this.headers = headers;
	}
	
}

package com.gravspace.messages;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.Header;
import org.apache.http.HttpStatus;

public class ResponseMessage {
	int status;
	byte[] responseContent;
	String contentType;
	List<Header> headers;
	
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
		if (headers == null) headers = new ArrayList<Header>();
		return headers;
	}
	public void setHeaders(List<Header> headers) {
		this.headers = headers;
	}
	
}

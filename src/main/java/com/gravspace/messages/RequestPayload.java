package com.gravspace.messages;

import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.protocol.HttpContext;

public class RequestPayload {
	final HttpRequest request;
	final HttpResponse response;
	final HttpContext context;
	
	public RequestPayload(HttpContext context, HttpRequest request, HttpResponse response){
		this.context = context;
		this.request = request;
		this.response = response;
	}

	public HttpResponse getResponse() {
		return response;
	}

	public HttpRequest getRequest() {
		return request;
	}

	public HttpContext getContext() {
		return context;
	}
}

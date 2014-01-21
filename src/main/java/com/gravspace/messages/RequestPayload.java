package com.gravspace.messages;

import org.apache.http.Header;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.RequestLine;
import org.apache.http.protocol.HttpContext;

public class RequestPayload {
	final RequestLine requestLine;
	final Header[] headers;
	final byte[] content;
	
	public RequestPayload(RequestLine requestLine, Header[] headers, byte[] content){
		this.requestLine = requestLine;
		this.headers = headers;
		this.content = content;
	}

	public RequestLine getRequestLine() {
		return requestLine;
	}

	public Header[] getHeaders() {
		return headers;
	}

	public byte[] getContent() {
		return content;
	}

	
}

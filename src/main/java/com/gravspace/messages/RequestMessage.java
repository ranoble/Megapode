package com.gravspace.messages;

public class RequestMessage implements Message {
	final String routeToken;
	final RequestPayload payload;
	
	public RequestMessage(String routeToken, RequestPayload payload){
		this.routeToken = routeToken;
		this.payload = payload;
	}

	public String getRouteToken() {
		return routeToken;
	}

	public RequestPayload getPayload() {
		return payload;
	}
}

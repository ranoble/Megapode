package com.gravspace.messages;

public class GetSession {
	final String sessionId;
	
	public GetSession(String sessionId){
		this.sessionId = sessionId;
	}
	
	public String getSessionId() {
		return sessionId;
	}
	
}

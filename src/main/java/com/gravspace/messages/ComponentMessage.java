package com.gravspace.messages;

import java.util.List;

public class ComponentMessage implements Message {
	final String routeToken;
	final List<?> parameters;
	
	public ComponentMessage(final String routeToken, final List<?> args){
		this.routeToken = routeToken;
		this.parameters = args;
	}

	public Object getRouteToken() {
		// TODO Auto-generated method stub
		return routeToken;
	}
	
	public List<?> getParameters(){
		return parameters;
	}

}

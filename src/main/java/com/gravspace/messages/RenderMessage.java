package com.gravspace.messages;

import java.util.Map;

public class RenderMessage implements Message {
	final String template; 
	final Map<String, ?> context;
	public RenderMessage(final String template, final Map<String, ?> context){
		this.template = template;
		this.context = context;
	}

	public Map<String, ?> getContext() {
		return context;
	}

	public String getTemplateName() {
		return template;
	}


}

package com.gravspace.messages;

import java.util.Map;

public class RenderMessage implements Message {
	final String template; 
	final Map<String, ?> context;
	final String renderer;
	
	public RenderMessage(final String renderer, final Map<String, ?> context){
		this.template = null;
		this.renderer = renderer;
		this.context = context;
	}
	
	public RenderMessage(final String renderer, final String template, final Map<String, ?> context){
		this.renderer = renderer;
		this.template = template;
		this.context = context;
	}

	public Map<String, ?> getContext() {
		return context;
	}

	public String getTemplateName() {
		return template;
	}

	public String getRenderer() {
		return renderer;
	}
}

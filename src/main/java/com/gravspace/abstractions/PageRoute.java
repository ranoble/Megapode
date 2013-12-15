package com.gravspace.abstractions;

import com.sun.jersey.api.uri.UriTemplate;

public class PageRoute {
	
	Class<? extends Page> pageClass;
	UriTemplate template;
	
	public PageRoute(String routePattern, Class<? extends Page> pageClass){
		template = new UriTemplate(routePattern);
		this.pageClass = pageClass;
	}
	
	public Class<? extends Page> getPageClass() {
		return pageClass;
	}
	public void setPageClass(Class<? extends Page> pageClass) {
		this.pageClass = pageClass;
	}
	public UriTemplate getTemplate() {
		return template;
	}
	public void setTemplate(UriTemplate template) {
		this.template = template;
	}
}

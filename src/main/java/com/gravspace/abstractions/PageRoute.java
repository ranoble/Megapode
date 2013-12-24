package com.gravspace.abstractions;

import com.sun.jersey.api.uri.UriTemplate;

public class PageRoute {
	
	Class<? extends IPage> pageClass;
	UriTemplate template;
	
	public PageRoute(String routePattern, Class<? extends IPage> pageClass){
		template = new UriTemplate(routePattern);
		this.pageClass = pageClass;
	}
	
	public Class<? extends IPage> getPageClass() {
		return pageClass;
	}
	public void setPageClass(Class<? extends IPage> pageClass) {
		this.pageClass = pageClass;
	}
	public UriTemplate getTemplate() {
		return template;
	}
	public void setTemplate(UriTemplate template) {
		this.template = template;
	}
}

package com.gravspace.util;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import org.apache.commons.lang.StringUtils;
import org.apache.http.client.utils.URLEncodedUtils;


public class Cookie {
	boolean secure;
	boolean httpOnly;
	String domain;
	String path;
	String name;
	String value;
	Date expires;

	public Cookie(String name, String value){
		this.name = name;
		this.value = value;
	}
	
	public Cookie(String name, String value,  Date expires){
		this.name = name;
		this.value = value;
		this.expires = expires;
	}
	
	//deleted -> value=deleted -> expires in the past
	/**
	 * TODO: Handle better
	 */
	public String toString(){
		List<String> cookieBuilder = new ArrayList<>();
		try {
			cookieBuilder.add(String.format("%s=%s", URLEncoder.encode(getName(), "UTF-8"), URLEncoder.encode(getValue(), "UTF-8")));
		} catch (UnsupportedEncodingException e) {}
		if (getDomain() != null){
			cookieBuilder.add(String.format("Domain=%s", getDomain()));
		}
		cookieBuilder.add(String.format("Path=%s", getPath()));
		
		if (getExpires() != null){
			DateFormat df = new SimpleDateFormat("dd MMM yyyy kk:mm:ss z");
			df.setTimeZone(TimeZone.getTimeZone("GMT"));
			cookieBuilder.add(String.format("expires=%s", df.format(getExpires())));
		}
		if (isSecure()){
			cookieBuilder.add("Secure");
		}
		if (isHttpOnly()){
			cookieBuilder.add("HttpOnly");
		}
		
		return StringUtils.join(cookieBuilder, "; ");
	}
	
	public boolean isSecure() {
		return secure;
	}
	public void setSecure(boolean secure) {
		this.secure = secure;
	}
	public String getDomain() {
		return domain;
	}
	public void setDomain(String domain) {
		this.domain = domain;
	}
	public String getPath() {
		if (path == null)
			return "/";
		return path;
	}
	public void setPath(String path) {
		this.path = path;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
//	public String getComment() {
//		return comment;
//	}
//	public void setComment(String comment) {
//		this.comment = comment;
//	}
//	public Map<String, String> getAttribs() {
//		return attribs;
//	}
//	public void setAttribs(Map<String, String> attribs) {
//		this.attribs = attribs;
//	}

	public boolean isHttpOnly() {
		return httpOnly;
	}

	public void setHttpOnly(boolean httpOnly) {
		this.httpOnly = httpOnly;
	}

	public Date getExpires() {
		return expires;
	}

	public void setExpires(Date expires) {
		this.expires = expires;
	}
	
//	BasicClientCookie bc;
}

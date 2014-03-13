package com.gravspace.exceptions;

import java.net.URI;

@SuppressWarnings("serial")
public class Redirect extends Exception {
	
	int code = 302;
	private URI uri;
	
	public Redirect(URI uri){
		super(uri.toString());
		this.uri = uri;
	}
	
	public Redirect(URI uri, boolean permanent){
		this(uri);
		if (permanent){
			this.code = 301;
		}
	}
	
	public Redirect(URI uri, int code){
		this(uri);
		if (isValid(code))
			this.code = code;
	}
	
	private boolean isValid(int code){
		switch (code){
			case 300:
			case 301:
			case 302:
			case 303:
			case 307:
				return true;
			default: 
				return false;
		}
	}
	
	public Throwable fillInStackTrace(){
		return this;
	}

	public int getCode() {
		return code;
	}

	public void setCode(int code) {
		this.code = code;
	}

	public URI getUri() {
		return uri;
	}

	public void setUri(URI uri) {
		this.uri = uri;
	}
}

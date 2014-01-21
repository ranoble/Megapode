package com.gravspace.messages;

public class SetSessionVar {
	final String name;
	final Object value;
	
	public  SetSessionVar(String name, Object value){
		this.name = name;
		this.value = value;
	}

	public Object getValue() {
		return value;
	}

	public String getName() {
		return name;
	}
}

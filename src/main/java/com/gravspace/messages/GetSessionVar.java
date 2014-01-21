package com.gravspace.messages;

public class GetSessionVar {
	final String name;
	
	public  GetSessionVar(String name){
		this.name = name;
	}

	public String getName() {
		return name;
	}
}

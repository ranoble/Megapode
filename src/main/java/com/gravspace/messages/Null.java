package com.gravspace.messages;

public class Null {
	public static Null instance;
	public static Null getNull(){
		if (instance == null){
			instance = new Null();
		}
		return instance;
	}
	
	public String toString(){
		return "null";
	}
}

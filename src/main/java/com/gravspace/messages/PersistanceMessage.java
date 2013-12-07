package com.gravspace.messages;

import java.util.List;

public class PersistanceMessage implements Message{
	
	final String persistanceTask;
	final List<?> args;

	public PersistanceMessage(final String persistanceTask, final List<?> args){
		this.persistanceTask = persistanceTask;
		this.args = args;
	}
	
	public String getPersistanceTask() {
		// TODO Auto-generated method stub
		return persistanceTask;
	}

	public List<?> getArgs() {
		// TODO Auto-generated method stub
		return args;
	}

}

package com.gravspace.messages;

import java.util.List;

public class TaskMessage  implements Message{
	final String task_name;
	final List<?> parameters;
	
	public TaskMessage(String task_name, List<?> parameters){
		this.task_name = task_name;
		this.parameters = parameters;
	}

	public String getTaskName() {
		// TODO Auto-generated method stub
		return task_name;
	}

	public List<?> getArgs() {
		// TODO Auto-generated method stub
		return parameters;
	}

}

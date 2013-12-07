package com.gravspace.messages;

import java.util.List;

public class CalculationMessage extends TaskMessage{

	public CalculationMessage(String task_name, List<?> parameters) {
		super(task_name, parameters);
	}

}

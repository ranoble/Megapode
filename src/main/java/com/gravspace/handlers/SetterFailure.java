package com.gravspace.handlers;

import com.gravspace.abstractions.IWidget;

public abstract class SetterFailure {

	public abstract void handleFailure(IWidget component, String field, Throwable exception);
	
}

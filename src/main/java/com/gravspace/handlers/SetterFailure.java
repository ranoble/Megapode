package com.gravspace.handlers;

import com.gravspace.abstractions.IComponent;

public abstract class SetterFailure {

	public abstract void handleFailure(IComponent component, String field, Throwable exception);
	
}

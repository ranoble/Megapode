package core;

import com.gravspace.abstractions.ConcurrantCallable;

public class CallableContainer {
	ConcurrantCallable callable;

	public ConcurrantCallable getCallable() {
		return callable;
	}

	public void setCallable(ConcurrantCallable callable) {
		this.callable = callable;
	}
	
}

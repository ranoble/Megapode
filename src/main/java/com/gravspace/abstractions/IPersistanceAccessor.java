package com.gravspace.abstractions;

import java.util.List;
import java.util.Map;

public interface IPersistanceAccessor {
	//should throw Exception of some type
	public Map<String, ?> performTask(Object... args);
}

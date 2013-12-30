package com.gravspace.abstractions;

import java.util.List;
import java.util.Map;

//Type / generics?
public interface IPersistanceAccessor {
	//should throw Exception of some type
	public Object performTask(Object... args) throws Exception;
	//getRecord
	//getRecords
	//performUpdate
}

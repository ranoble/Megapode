package com.gravspace.util;

import java.util.ArrayList;
import java.util.List;

public class TypeUtils {
	public static List<Class<?>> getListTypes(List<Object> arguments) {
		List<Class<?>> types = new ArrayList<Class<?>>();
		for (Object argument: arguments){
			types.add(argument.getClass());
		}
		return types;
	}
}

package com.gravspace.abstractions;

import java.util.List;

public interface Calculation<E> {
	public E calculate(List<?> list);
}

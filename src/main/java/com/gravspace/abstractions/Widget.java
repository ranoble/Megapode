package com.gravspace.abstractions;

import java.util.concurrent.Callable;

import scala.concurrent.Future;

public interface Widget {
	Future<String> build(Object... args);
	Callable<Future<String>> wrap(Object... args);
}

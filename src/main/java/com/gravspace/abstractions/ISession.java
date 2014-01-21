package com.gravspace.abstractions;

import scala.concurrent.Future;

public interface ISession {
	Future<Object> get(String name);
	Future<Object> set(String name, Object value);
	void kill();
}

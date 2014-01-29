package com.gravspace.abstractions;

import scala.concurrent.Future;

public interface IWidget {
	public void initialise(Object... args);
	public void collect();
	public void process();
	public Future<String> render() throws Exception;
	public Future<Object> await();

}

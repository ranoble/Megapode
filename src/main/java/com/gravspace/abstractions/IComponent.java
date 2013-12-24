package com.gravspace.abstractions;

import scala.concurrent.Future;

public interface IComponent {
	public void initialise(Object... args);
	public void collect();
	public void process();
	public String render() throws Exception;
	public Future<Object> await();
}

package com.gravspace.abstractions;

import scala.concurrent.Future;

public interface Component {
	public void collect();
	
	public void process();
	
	public String render();
	public Future<Object> await();
}

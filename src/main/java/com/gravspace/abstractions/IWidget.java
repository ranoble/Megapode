package com.gravspace.abstractions;

import scala.concurrent.Future;

public interface IWidget {
	Future<String> build(Object... args);
}

package com.gravspace.abstractions;

import java.util.Map;

import scala.concurrent.Future;

public interface IRenderer {
	Future<String> render(String template, Map<String, ?> context);
	Future<String> render(Map<String, ?> context);
	String getTemplate();
}

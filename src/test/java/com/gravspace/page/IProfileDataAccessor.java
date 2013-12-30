package com.gravspace.page;

import java.util.Map;

import scala.concurrent.Future;

public interface IProfileDataAccessor {
	public Future<Map<String, Object>> getUserProfile(Integer id);
}

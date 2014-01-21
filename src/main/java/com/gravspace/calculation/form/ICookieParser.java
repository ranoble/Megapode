package com.gravspace.calculation.form;

import java.util.Map;

import org.apache.http.Header;

import scala.concurrent.Future;

public interface ICookieParser {
	public Future<Map<String, String>> parseCookies(Header[] hraders);
}

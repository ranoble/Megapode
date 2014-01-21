package com.gravspace.calculation.form;

import java.util.List;
import java.util.Map;

import org.apache.http.Header;
import org.apache.http.NameValuePair;

import scala.concurrent.Future;

public interface IFormParser {
	public Future<List<NameValuePair>> parse(Header[] headers, byte[] data);
}
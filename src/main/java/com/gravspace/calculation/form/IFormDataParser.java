package com.gravspace.calculation.form;

import java.util.List;

import org.apache.http.NameValuePair;

public interface IFormDataParser {
	public List<NameValuePair> parse(byte[] data);
}

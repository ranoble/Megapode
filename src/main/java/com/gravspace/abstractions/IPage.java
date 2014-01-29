package com.gravspace.abstractions;

import java.util.Map;

import org.apache.http.Header;

import scala.concurrent.Future;

import com.gravspace.messages.ResponseMessage;

public interface IPage extends IWidget{
	public void initialise(String uri,
			String method,
			String query,
            Header[] headers,
            byte[] content,
            Map<String, String> params);

	public Future<ResponseMessage> build() throws Exception;
	
}

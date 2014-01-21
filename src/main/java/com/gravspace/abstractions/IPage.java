package com.gravspace.abstractions;

import java.util.Map;

import org.apache.http.Header;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.RequestLine;
import org.apache.http.protocol.HttpContext;

import com.gravspace.messages.ResponseMessage;

import scala.concurrent.Future;

public interface IPage extends IComponent{
	public void initialise(String uri,
			String method,
			String query,
            Header[] headers,
            byte[] content,
            Map<String, String> params);

	public Future<ResponseMessage> build() throws Exception;
	
}

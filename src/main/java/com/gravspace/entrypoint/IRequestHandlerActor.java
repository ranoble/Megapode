package com.gravspace.entrypoint;

import org.apache.http.Header;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.RequestLine;
import org.apache.http.protocol.HttpContext;

import com.gravspace.messages.ResponseMessage;

import akka.japi.Option;
import scala.concurrent.Future;

public interface IRequestHandlerActor {
	ResponseMessage process(final RequestLine requestLine,
            final Header[] headers,
            final byte[] content) throws Exception;
}

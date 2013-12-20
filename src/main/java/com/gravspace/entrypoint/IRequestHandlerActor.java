package com.gravspace.entrypoint;

import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.protocol.HttpContext;

import akka.japi.Option;
import scala.concurrent.Future;

public interface IRequestHandlerActor {
	String process(final HttpRequest request,
            final HttpResponse response,
            final HttpContext context) throws Exception;
}

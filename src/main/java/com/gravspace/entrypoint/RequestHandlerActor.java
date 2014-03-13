package com.gravspace.entrypoint;

import java.nio.charset.Charset;

import org.apache.http.Header;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.RequestLine;
import org.apache.http.protocol.HttpContext;

import akka.actor.ActorRef;
import akka.actor.TypedActor;
import akka.dispatch.OnSuccess;
import akka.japi.Option;
import akka.pattern.Patterns;
import akka.util.Timeout;

import com.gravspace.exceptions.PageNotFoundException;
import com.gravspace.exceptions.Redirect;
import com.gravspace.messages.RequestMessage;
import com.gravspace.messages.RequestPayload;
import com.gravspace.messages.ResponseMessage;

import scala.concurrent.Future;
import scala.concurrent.Await;
import scala.concurrent.duration.Duration;

public class RequestHandlerActor implements IRequestHandlerActor {
	
	private ActorRef coordinator;

	public RequestHandlerActor(final ActorRef coordinator){
		this.coordinator = coordinator;
	}

	@Override
	public ResponseMessage process(RequestLine requestLine, Header[] headers,
			byte[] content) throws Exception {
		RequestPayload payload = new RequestPayload(requestLine, headers, content);
		RequestMessage message = new RequestMessage("*", payload);
		Timeout timeout = new Timeout(Duration.create(1, "minute"));
		Future<Object> future = Patterns.ask(coordinator, message, timeout);

		try {
			return (ResponseMessage)Await.result(future, Duration.create(1, "minute"));
		} catch (PageNotFoundException e) {
			ResponseMessage pageNotFound = new ResponseMessage();
			pageNotFound.setStatus(404);
			pageNotFound.setContentType("text/html");
			pageNotFound.setResponseContent(e.getMessage().getBytes(Charset.forName("UTF-8")));
			return pageNotFound;
		} catch (Redirect redirect){
			return null;
		} catch (Exception e) {
			
			throw e;
		}
	}

}

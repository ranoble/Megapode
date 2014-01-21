package com.gravspace.entrypoint;

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
			//this will no longer return a string
			return (ResponseMessage)Await.result(future, Duration.create(1, "minute"));
		} catch (Exception e) {
			throw e;
		}
	}

}

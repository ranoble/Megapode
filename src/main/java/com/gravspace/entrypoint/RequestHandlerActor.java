package com.gravspace.entrypoint;

import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.protocol.HttpContext;

import akka.actor.ActorRef;
import akka.actor.TypedActor;
import akka.dispatch.OnSuccess;
import akka.japi.Option;
import akka.pattern.Patterns;
import akka.util.Timeout;

import com.gravspace.messages.RequestMessage;
import com.gravspace.messages.RequestPayload;

import scala.concurrent.Future;
import scala.concurrent.Await;
import scala.concurrent.duration.Duration;

public class RequestHandlerActor implements IRequestHandlerActor {
	
	private ActorRef coordinator;

	public RequestHandlerActor(final ActorRef coordinator){
		this.coordinator = coordinator;
	}

	public String process(HttpRequest request,
			HttpResponse response, HttpContext context) throws Exception {
		// TODO Auto-generated method stub
		RequestPayload payload = new RequestPayload(context, request, response);
		RequestMessage message = new RequestMessage("*", payload);
		Timeout timeout = new Timeout(Duration.create(1, "minute"));
		Future<Object> future = Patterns.ask(coordinator, message, timeout);

		try {
			return (String)Await.result(future, Duration.create(1, "minute"));
		} catch (Exception e) {
//			return Option.option(arg0)
			throw e;
//			System.out.println("An error occured");
//			return Option.none(e);
		}
	}

}

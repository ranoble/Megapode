package com.gravspace.abstractions;

import java.util.Map;

import org.apache.http.HttpResponse;

import scala.Function1;
import scala.PartialFunction;
import scala.concurrent.ExecutionContext;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;

import com.gravspace.messages.RequestMessage;
import com.gravspace.messages.ResponseMessage;

import akka.actor.ActorRef;
import akka.actor.UntypedActor;
import akka.dispatch.OnSuccess;
import akka.pattern.Patterns;
import akka.util.Timeout;

public class PageHandler extends UntypedActor {
	Map<String, Page> pages;
	Map<String, ActorRef> routes;
	
	public PageHandler(Map<String, Page> pages){
		this.pages = pages;
	}

	@Override
	public void onReceive(Object rawMessage) throws Exception {
		if (rawMessage instanceof RequestMessage){
			RequestMessage message = (RequestMessage)rawMessage;
			ActorRef page = routes.get(message.getRouteToken());
//			Timeout timeout = new Timeout(Duration.create(1, "minute"));
//			Future<Object> future = Patterns.ask(page, message.getPayload(), timeout);
//			future.onSuccess(new OnSuccess<Object>() {
//				@Override
//				public void onSuccess(Object response) throws Throwable {
//					PageHandler.this.getSender().tell(response, PageHandler.this.getSelf());
//				}
//			}, this.getContext().dispatcher());
			
		} 
		else {
			unhandled(rawMessage);
		}
	}

}

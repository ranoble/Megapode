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
import akka.actor.UntypedActorContext;
import akka.dispatch.OnSuccess;
import akka.pattern.Patterns;
import akka.util.Timeout;

public class PageHandler extends UntypedActor {
	Map<String, Class<? extends Page>> pages;
	Map<String, ActorRef> routes;
	
	public PageHandler(Map<String, Class<? extends Page>> pages){
		this.pages = pages;
		
	}

	@Override
	public void onReceive(Object rawMessage) throws Exception {
		if (rawMessage instanceof RequestMessage){
			RequestMessage message = (RequestMessage)rawMessage;
			//final ActorRef coordinatingActor, final UntypedActorContext actorContext
			Page page = pages.get(message.getRouteToken()).getConstructor(ActorRef.class, UntypedActorContext.class).newInstance(this.getSelf(), this.context());
			page.collect();
			page.await();
			page.process();
			String rendered = page.render();
			getSender().tell(rendered, getSelf());

			
		} 
		else {
			unhandled(rawMessage);
		}
	}

}

package com.gravspace.handlers;

import java.util.Map;

import akka.actor.ActorRef;
import akka.actor.UntypedActor;
import akka.actor.UntypedActorContext;
import akka.event.Logging;
import akka.event.LoggingAdapter;

import com.gravspace.abstractions.Page;
import com.gravspace.messages.RequestMessage;
import com.gravspace.util.Layers;

public class PageHandler extends UntypedActor {
	LoggingAdapter log = Logging.getLogger(getContext().system(), this);
	Map<String, Class<? extends Page>> pages;
	Map<String, ActorRef> routes;
	private Map<Layers, ActorRef> routers;
	
	public PageHandler(Map<Layers, ActorRef> routers, Map<String, Class<? extends Page>> pages){
		this.pages = pages;
		this.routers = routers;
	}

	@Override
	public void onReceive(Object rawMessage) throws Exception {
		log.info("Page got: "+rawMessage.getClass().getCanonicalName());
		if (rawMessage instanceof RequestMessage){
			log.info("Handelling Request");
			RequestMessage message = (RequestMessage)rawMessage;
			//final ActorRef coordinatingActor, final UntypedActorContext actorContext
			Page page = pages.get(message.getRouteToken()).getConstructor(Map.class, ActorRef.class, UntypedActorContext.class).newInstance(routers, getSender(), this.context());
			page.initialise(message.getPayload().getRequest(), 
					message.getPayload().getResponse(), 
					message.getPayload().getContext());
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

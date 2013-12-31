package com.gravspace.handlers;

import java.util.Map;

import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;

import akka.actor.ActorRef;
import akka.actor.UntypedActor;
import akka.actor.UntypedActorContext;
import akka.dispatch.Futures;
import akka.event.Logging;
import akka.event.LoggingAdapter;

import com.gravspace.abstractions.IComponent;
import com.gravspace.messages.ComponentMessage;
import com.gravspace.util.Layers;

public class ComponentHandler extends UntypedActor {
	LoggingAdapter log = Logging.getLogger(getContext().system(), this);
	Map<String, Class<? extends IComponent>> components;
	Map<String, ActorRef> routes;
	private Map<Layers, ActorRef> routers;
	
	public ComponentHandler(Map<Layers, ActorRef> routers, Map<String, Class<? extends IComponent>> components){
		this.components = components;
		this.routers = routers;
	}

	@Override
	public void onReceive(Object rawMessage) throws Exception {
		log.info("Page got: "+rawMessage.getClass().getCanonicalName());
		if (rawMessage instanceof ComponentMessage){
			log.info("Handelling Request");
			Future<String> rendered = null;
			try {
				ComponentMessage message = (ComponentMessage)rawMessage;
				IComponent component = components.get(message.getRouteToken()).getConstructor(Map.class, ActorRef.class, UntypedActorContext.class).newInstance(routers, getSender(), this.context());
				component.initialise(message.getParameters().toArray(new Object[0]));
//				Await.ready(component.await(), Duration.create(1, "minute"));
				rendered = build(component);
				
			} catch (Exception e){
				rendered = Futures.failed(e);
			}
			akka.pattern.Patterns.pipe(rendered, this.getContext().dispatcher()).to(getSender());
		} 
		else {
			unhandled(rawMessage);
		}
	}

	protected Future<String> build(IComponent component) throws Exception {
		Await.ready(component.await(), Duration.create(1, "minute"));
		component.collect();
		Await.ready(component.await(), Duration.create(1, "minute"));
//		Thread.sleep(100);
		component.process();
		Await.ready(component.await(), Duration.create(1, "minute"));
		Future<String> rendered = component.render();
		return rendered;
	}

}

package com.gravspace.handlers;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.concurrent.Callable;

import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;

import akka.actor.ActorRef;
import akka.actor.UntypedActor;
import akka.actor.UntypedActorContext;
import akka.dispatch.Futures;
import akka.event.Logging;
import akka.event.LoggingAdapter;

import com.gravspace.abstractions.IWidget;
import com.gravspace.messages.ComponentMessage;
import com.gravspace.util.Layers;
import static akka.dispatch.Futures.future;

public class ComponentHandler extends UntypedActor {
	LoggingAdapter log = Logging.getLogger(getContext().system(), this);
	Map<String, Class<? extends IWidget>> components;
	Map<String, ActorRef> routes;
	private Map<Layers, ActorRef> routers;
	
	public ComponentHandler(Map<Layers, ActorRef> routers, Map<String, Class<? extends IWidget>> components){
		this.components = components;
		this.routers = routers;
	}

	@Override
	public void onReceive(Object rawMessage) throws Exception {
//		log.info("Page got: "+rawMessage.getClass().getCanonicalName());
		if (rawMessage instanceof ComponentMessage){
//			log.info("Handelling Request");
			Future<String> rendered = null;
			try {
				final ComponentMessage message = (ComponentMessage)rawMessage;
				rendered = future(new Callable<String>() {
					  public String call() throws Exception {
						  IWidget component = components.get(message.getRouteToken()).getConstructor(Map.class, ActorRef.class, UntypedActorContext.class).newInstance(routers, getSender(), context());
						  component.initialise(message.getParameters().toArray(new Object[0]));
						  return build(component);
					  }
					}, context().dispatcher());
				
			} catch (Exception e){
				rendered = Futures.failed(e);
			}
			akka.pattern.Patterns.pipe(rendered, this.getContext().dispatcher()).to(getSender());
		} 
		else {
			unhandled(rawMessage);
		}
	}

	protected String build(IWidget component) throws Exception {
		Await.ready(component.await(), Duration.create(1, "minute"));
		component.collect();
		Await.ready(component.await(), Duration.create(1, "minute"));
//		Thread.sleep(100);
		component.process();
		Await.ready(component.await(), Duration.create(1, "minute"));
		Future<String> rendered = component.render();
		return Await.result(rendered, Duration.create(1, "minute"));
	}

}

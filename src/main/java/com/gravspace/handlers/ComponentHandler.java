package com.gravspace.handlers;

import java.util.Map;

import akka.actor.ActorRef;
import akka.actor.UntypedActor;
import akka.actor.UntypedActorContext;
import akka.event.Logging;
import akka.event.LoggingAdapter;

import com.gravspace.abstractions.Component;
import com.gravspace.messages.ComponentMessage;
import com.gravspace.util.Layers;

public class ComponentHandler extends UntypedActor {
	LoggingAdapter log = Logging.getLogger(getContext().system(), this);
	Map<String, Class<? extends Component>> components;
	Map<String, ActorRef> routes;
	private Map<Layers, ActorRef> routers;
	
	public ComponentHandler(Map<Layers, ActorRef> routers, Map<String, Class<? extends Component>> components){
		this.components = components;
		this.routers = routers;
	}

	@Override
	public void onReceive(Object rawMessage) throws Exception {
		log.info("Page got: "+rawMessage.getClass().getCanonicalName());
		if (rawMessage instanceof ComponentMessage){
			log.info("Handelling Request");
			ComponentMessage message = (ComponentMessage)rawMessage;
			Component component = components.get(message.getRouteToken()).getConstructor(Map.class, ActorRef.class, UntypedActorContext.class).newInstance(routers, getSender(), this.context());
			component.initialise(message.getParameters());
			component.collect();
			component.await();
			component.process();
			String rendered = component.render();
			getSender().tell(rendered, getSelf());
			
		} 
		else {
			unhandled(rawMessage);
		}
	}

}

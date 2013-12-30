package com.gravspace.handlers;

import java.lang.reflect.Constructor;
import java.util.Map;

import scala.concurrent.Future;

import akka.actor.ActorRef;
import akka.actor.UntypedActor;
import akka.actor.UntypedActorContext;
import akka.event.Logging;
import akka.event.LoggingAdapter;

import com.gravspace.abstractions.IRenderer;
import com.gravspace.messages.RenderMessage;
import com.gravspace.util.Layers;

public class RendererHandler extends UntypedActor {
	LoggingAdapter log = Logging.getLogger(getContext().system(), this);
	Map<String, Class<? extends IRenderer>> renderers;
	private Map<Layers, ActorRef> routers;
	
	public RendererHandler(Map<Layers, ActorRef> routers, Map<String, Class<? extends IRenderer>> renderers){
		this.renderers = renderers;
		this.routers = routers;
	}

	@Override
	public void onReceive(Object rawMessage) throws Exception {
		log.info("Renderer got: "+rawMessage.getClass().getCanonicalName());
		if (rawMessage instanceof RenderMessage){
			RenderMessage message = (RenderMessage)rawMessage;
			//final ActorRef coordinatingActor, final UntypedActorContext actorContext
			for (String key: renderers.keySet()){
				log.info(key);
			}
			Class<? extends IRenderer> renderer = renderers.get(message.getRenderer());
			Constructor<? extends IRenderer> constr = renderer.getConstructor(Map.class, ActorRef.class, UntypedActorContext.class);
			IRenderer page = constr.newInstance(routers, getSender(), this.context());
			Future<String> rendered = null;
			if (message.getTemplateName() != null){
				rendered = page.render(message.getTemplateName(), message.getContext());
			} else {
				rendered = page.render(message.getContext());
			}
			
			//log.info("Rendered: "+rendered);
			akka.pattern.Patterns.pipe(rendered, this.getContext().dispatcher()).to(getSender());
			//getSender().tell(rendered, getSelf());
		} 
		else {
			unhandled(rawMessage);
		}
	}

}

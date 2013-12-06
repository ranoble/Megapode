package com.gravspace.abstractions;

import java.lang.reflect.Constructor;
import java.util.Collections;
import java.util.Map;

import org.apache.http.HttpResponse;

import scala.Function1;
import scala.PartialFunction;
import scala.concurrent.ExecutionContext;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;

import com.gravspace.messages.RenderMessage;
import com.gravspace.messages.RequestMessage;
import com.gravspace.messages.ResponseMessage;
import com.gravspace.util.Layers;

import akka.actor.ActorRef;
import akka.actor.UntypedActor;
import akka.actor.UntypedActorContext;
import akka.dispatch.OnSuccess;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.pattern.Patterns;
import akka.util.Timeout;

public class RendererHandler extends UntypedActor {
	LoggingAdapter log = Logging.getLogger(getContext().system(), this);
	Map<String, Class<? extends Renderer>> renderers;
	private Map<Layers, ActorRef> routers;
	
	public RendererHandler(Map<Layers, ActorRef> routers, Map<String, Class<? extends Renderer>> renderers){
		this.renderers = renderers;
		this.routers = routers;
	}

	@Override
	public void onReceive(Object rawMessage) throws Exception {
		log.info("Renderer got: "+rawMessage.getClass().getCanonicalName());
		if (rawMessage instanceof RenderMessage){
			RenderMessage message = (RenderMessage)rawMessage;
			//final ActorRef coordinatingActor, final UntypedActorContext actorContext
			String template = message.getTemplateName();
			for (String key: renderers.keySet()){
				log.info(key);
			}
			Class<? extends Renderer> renderer = renderers.get(template);
			Constructor<? extends Renderer> constr = renderer.getConstructor(Map.class, ActorRef.class, UntypedActorContext.class);
			Renderer page = constr.newInstance(routers, getSender(), this.context());
			
			String rendered = page.render(message.getContext());
			log.info("Rendered: "+rendered);
			getSender().tell(rendered, getSelf());
		} 
		else {
			unhandled(rawMessage);
		}
	}

}

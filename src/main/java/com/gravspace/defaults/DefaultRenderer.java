package com.gravspace.defaults;

import java.util.Map;

import org.apache.commons.lang.NotImplementedException;
import org.apache.velocity.app.VelocityEngine;

import scala.concurrent.Future;

import akka.actor.ActorRef;
import akka.actor.UntypedActorContext;
import akka.dispatch.Futures;

import com.gravspace.abstractions.IRenderer;
import com.gravspace.annotations.Renderer;
import com.gravspace.bases.RendererBase;
import com.gravspace.util.Layers;

@Renderer
public class DefaultRenderer extends RendererBase implements IRenderer {

	public DefaultRenderer(Map<Layers, ActorRef> routers,
			ActorRef coordinatingActor, UntypedActorContext actorContext, VelocityEngine engine) {
		super(routers, coordinatingActor, actorContext, engine);
		// TODO Auto-generated constructor stub
	}

//	@Override
//	public Future<String> render(String template, Map<String, ?> context) {
//		return null;
//	}
	
	@Override
	public Future<String> render(Map<String, ?> context){ 
		return Futures.failed(
				new NotImplementedException("Implicit Template Rendering not Supported for DefaultRenderer"));
		}

	@Override
	public String getTemplate() {
		return null;
	}

}

package com.gravspace.bases;

import java.io.StringWriter;
import java.util.Map;
import java.util.Properties;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.app.VelocityEngine;

import scala.concurrent.Future;

import akka.actor.ActorRef;
import akka.actor.UntypedActorContext;
import akka.dispatch.Futures;

import com.gravspace.abstractions.ConcurrantCallable;
import com.gravspace.abstractions.IRenderer;
import com.gravspace.util.Layers;

public abstract class RendererBase extends ConcurrantCallable implements IRenderer {
	private VelocityEngine engine;

	public RendererBase(final Map<Layers, ActorRef> routers, 
			final ActorRef coordinatingActor, 
			final UntypedActorContext actorContext, 
			final VelocityEngine engine){
		super(routers, coordinatingActor, actorContext);
		this.engine = engine;
	}

	public Future<String> render(Map<String, ?> context) {
		return render(getTemplate(), context);
	}
	
	public Future<String> render(String template, Map<String, ?> context) {
		if (template == null){
			return Futures.failed(new IllegalArgumentException(
					String.format("Template not provided in class %s", this.getClass().getCanonicalName())));
		}
		try {
			
			VelocityContext v_context = new VelocityContext(context);
			StringWriter sw = new StringWriter();
			engine.mergeTemplate(template, "UTF-8", v_context, sw);
			return Futures.successful(sw.toString());
		} catch (Exception e){
			return Futures.failed(e);
		}
		
	}
	
	public abstract String getTemplate();

}

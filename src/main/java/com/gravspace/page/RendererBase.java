package com.gravspace.page;

import java.io.StringWriter;
import java.util.Map;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;

import akka.actor.ActorRef;
import akka.actor.UntypedActorContext;

import com.gravspace.abstractions.ConcurrantCallable;
import com.gravspace.abstractions.Renderer;

public abstract class RendererBase extends ConcurrantCallable implements Renderer {
	public RendererBase(final ActorRef coordinatingActor, final UntypedActorContext actorContext){
		super(coordinatingActor, actorContext);
		Velocity.init();

	}

	public String render(Map<String, ?> context) {
		VelocityContext v_context = new VelocityContext(context);
		StringWriter sw = new StringWriter();
		Template t = Velocity.getTemplate(getTemplate());		//Velocity.mergeTemplate(getTemplate(), v_context, sw);
		t.merge(v_context, sw);
		return sw.toString();
	}
	
	public abstract String getTemplate();

}

package com.gravspace.bases;

import java.io.StringWriter;
import java.util.Map;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;

import akka.actor.ActorRef;
import akka.actor.UntypedActorContext;

import com.gravspace.abstractions.ConcurrantCallable;
import com.gravspace.abstractions.Renderer;
import com.gravspace.util.Layers;

public abstract class RendererBase extends ConcurrantCallable implements Renderer {
	public RendererBase(final Map<Layers, ActorRef> routers, final ActorRef coordinatingActor, final UntypedActorContext actorContext){
		super(routers, coordinatingActor, actorContext);
		Velocity.init();

	}

	public String render(Map<String, ?> context) {
//		return "X";
		VelocityContext v_context = new VelocityContext(context);
		StringWriter sw = new StringWriter();
		Velocity.evaluate(v_context,
				sw,
                "basic template",
                "This is a new MegaPode Template... $firstname ");//getTemplate(getTemplate());		//Velocity.mergeTemplate(getTemplate(), v_context, sw);
//		t.merge(v_context, sw);
		return sw.toString();
	}
	
	public abstract String getTemplate();

}

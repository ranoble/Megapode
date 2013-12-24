package com.gravspace.bases;

import java.io.StringWriter;
import java.util.Map;
import java.util.Properties;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.app.VelocityEngine;

import akka.actor.ActorRef;
import akka.actor.UntypedActorContext;

import com.gravspace.abstractions.ConcurrantCallable;
import com.gravspace.abstractions.IRenderer;
import com.gravspace.util.Layers;

public abstract class RendererBase extends ConcurrantCallable implements IRenderer {
	private VelocityEngine engine;

	public RendererBase(final Map<Layers, ActorRef> routers, final ActorRef coordinatingActor, final UntypedActorContext actorContext){
		super(routers, coordinatingActor, actorContext);
		Properties props = new Properties();
		props.put("resource.loader", "class");
		props.put("class.resource.loader.description", "Velocity Classpath Resource Loader");
		props.put("class.resource.loader.class", "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
		engine = new VelocityEngine();
		engine.init(props);
//		Velocity.init();
//		Velocity.set

	}

	public String render(Map<String, ?> context) {
//		return "X";
		VelocityContext v_context = new VelocityContext(context);
		StringWriter sw = new StringWriter();

		engine.mergeTemplate(getTemplate(), "UTF-8", v_context, sw);
//		Velocity.evaluate(v_context,
//				sw,
//                "basic template",
//                "This is a new MegaPode Template... $firstname ");////Velocity.mergeTemplate(getTemplate(), v_context, sw);
//		t.merge(v_context, sw);
		return sw.toString();
	}
	
	public abstract String getTemplate();

}

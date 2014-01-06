package com.gravspace.page;

import java.util.Map;

import org.apache.velocity.app.VelocityEngine;

import com.gravspace.bases.RendererBase;
import com.gravspace.util.Layers;

import akka.actor.ActorRef;
import akka.actor.UntypedActorContext;

public class ProfileRenderer extends RendererBase {


	public ProfileRenderer(Map<Layers, ActorRef> routers,
			ActorRef coordinatingActor, UntypedActorContext actorContext, VelocityEngine engine) {
		super(routers, coordinatingActor, actorContext, engine);
	}

	@Override
	public String getTemplate() {
		// TODO Auto-generated method stub
		return "hi_pode.vm";
	}

}

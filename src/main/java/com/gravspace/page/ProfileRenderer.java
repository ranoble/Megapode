package com.gravspace.page;

import java.util.Map;

import com.gravspace.util.Layers;

import akka.actor.ActorRef;
import akka.actor.UntypedActorContext;

public class ProfileRenderer extends RendererBase {


	public ProfileRenderer(Map<Layers, ActorRef> routers,
			ActorRef coordinatingActor, UntypedActorContext actorContext) {
		super(routers, coordinatingActor, actorContext);
	}

	@Override
	public String getTemplate() {
		// TODO Auto-generated method stub
		return "hi_pode.vm";
	}

}

package com.gravspace.page;

import akka.actor.ActorRef;
import akka.actor.UntypedActorContext;

public class ProfileRenderer extends RendererBase {

	public ProfileRenderer(ActorRef coordinatingActor,
			UntypedActorContext actorContext) {
		super(coordinatingActor, actorContext);
	}

	@Override
	public String getTemplate() {
		// TODO Auto-generated method stub
		return "hi_pode.vm";
	}

}

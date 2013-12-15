package com.gravspace.page;

import java.util.Map;

import akka.actor.ActorRef;
import akka.actor.UntypedActorContext;

import com.gravspace.abstractions.Task;
import com.gravspace.bases.TaskBase;
import com.gravspace.util.Layers;

public class ProfileTask extends TaskBase implements Task {

	public ProfileTask(Map<Layers, ActorRef> routers,
			ActorRef coordinatingActor, UntypedActorContext actorContext) {
		super(routers, coordinatingActor, actorContext);
		
	}

	public void act(Object... args) {
		log.info("Do Something in the background");
	}

}

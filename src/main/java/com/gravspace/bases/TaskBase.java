package com.gravspace.bases;

import java.util.Map;

import akka.actor.ActorRef;
import akka.actor.UntypedActorContext;

import com.gravspace.abstractions.ConcurrantCallable;
import com.gravspace.abstractions.Task;
import com.gravspace.util.Layers;

public abstract class TaskBase extends ConcurrantCallable implements Task{

	public TaskBase(Map<Layers, ActorRef> routers,
			ActorRef coordinatingActor, UntypedActorContext actorContext) {
		super(routers, coordinatingActor, actorContext);
		// TODO Auto-generated constructor stub
	}
	
}

package com.gravspace.bases;

import java.util.List;
import java.util.Map;

import akka.actor.ActorRef;
import akka.actor.UntypedActorContext;

import com.gravspace.abstractions.ICalculation;
import com.gravspace.abstractions.ConcurrantCallable;
import com.gravspace.util.Layers;

public abstract class CalculationBase extends ConcurrantCallable implements ICalculation{

	public CalculationBase(Map<Layers, ActorRef> routers,
			ActorRef coordinatingActor, UntypedActorContext actorContext) {
		super(routers, coordinatingActor, actorContext);
		// TODO Auto-generated constructor stub
	}

}

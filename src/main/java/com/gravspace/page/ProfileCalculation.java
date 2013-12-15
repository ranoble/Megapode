package com.gravspace.page;

import java.util.List;
import java.util.Map;

import akka.actor.ActorRef;
import akka.actor.UntypedActorContext;

import com.gravspace.abstractions.Calculation;
import com.gravspace.bases.CalculationBase;
import com.gravspace.util.Layers;

public class ProfileCalculation extends CalculationBase implements
		Calculation {

	public ProfileCalculation(Map<Layers, ActorRef> routers,
			ActorRef coordinatingActor, UntypedActorContext actorContext) {
		super(routers, coordinatingActor, actorContext);
	}

	public Object calculate(Object... list) {
		
		return 3;
	}

}

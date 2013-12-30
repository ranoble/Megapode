package com.gravspace.page;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import akka.dispatch.Futures;
import scala.concurrent.Future;
import akka.actor.ActorRef;
import akka.actor.UntypedActorContext;

import com.gravspace.abstractions.ICalculation;
import com.gravspace.annotations.Calculation;
import com.gravspace.bases.CalculationBase;
import com.gravspace.impl.tasks.IProfileCalculation;
import com.gravspace.util.Layers;

@Calculation
public class ProfileCalculation extends CalculationBase implements
		ICalculation, IProfileCalculation {

	public ProfileCalculation(Map<Layers, ActorRef> routers,
			ActorRef coordinatingActor, UntypedActorContext actorContext) {
		super(routers, coordinatingActor, actorContext);
	}

	@Override
	public Future<String> getThree() {
		return Futures.successful("Three!");
	}

	

}

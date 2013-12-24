package com.gravspace.handlers;

import java.lang.reflect.Constructor;
import java.util.Map;

import akka.actor.ActorRef;
import akka.actor.UntypedActor;
import akka.actor.UntypedActorContext;
import akka.event.Logging;
import akka.event.LoggingAdapter;

import com.gravspace.abstractions.ICalculation;
import com.gravspace.messages.CalculationMessage;
import com.gravspace.util.Layers;

public class CalculationHandler extends UntypedActor {
	LoggingAdapter log = Logging.getLogger(getContext().system(), this);
	Map<String, Class<? extends ICalculation>> calculations;
	private Map<Layers, ActorRef> routers;
	
	public CalculationHandler(Map<Layers, ActorRef> routers, Map<String, Class<? extends ICalculation>> calculations){
		this.calculations = calculations;
		this.routers = routers;
	}

	@Override
	public void onReceive(Object rawMessage) throws Exception {
		log.info("CalculationHandler got: "+rawMessage.getClass().getCanonicalName());
		if (rawMessage instanceof CalculationMessage){
			CalculationMessage message = (CalculationMessage)rawMessage;

			String task_name = message.getTaskName();
			Class<? extends ICalculation> calculationClass = calculations.get(task_name);
			Constructor<? extends ICalculation> constr = calculationClass.getConstructor(Map.class, ActorRef.class, UntypedActorContext.class);
			ICalculation calculation = constr.newInstance(routers, getSender(), this.context());
			
			Object result = calculation.calculate(message.getArgs());
			getSender().tell(result, getSelf());
		} 
		else {
			unhandled(rawMessage);
		}
	}

}

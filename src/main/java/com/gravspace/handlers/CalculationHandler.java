package com.gravspace.handlers;

import java.lang.reflect.Constructor;
import java.util.Map;

import scala.concurrent.Future;

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
		for (String calc: calculations.keySet()){
			log.error(calc);
		}
	}

	@Override
	public void onReceive(Object rawMessage) throws Exception {
//		log.info("CalculationHandler got: "+rawMessage.getClass().getCanonicalName());
		if (rawMessage instanceof CalculationMessage){
			CalculationMessage message = (CalculationMessage)rawMessage;

			String task_name = message.getTaskName();
//			log.info("CalculationHandler requested method task_name: "+task_name);
			Class<? extends ICalculation> calculationClass = calculations.get(task_name);
			Constructor<? extends ICalculation> constr = calculationClass.getConstructor(Map.class, ActorRef.class, UntypedActorContext.class);
			ICalculation calculation = constr.newInstance(routers, getSender(), this.context());
			
			Future<Object> result = (Future<Object>) calculation.calculate(message.getArgs().toArray(new Object[0]));
			//Pattern.
			akka.pattern.Patterns.pipe(result, this.getContext().dispatcher()).to(getSender());
			//getSender().tell(result, getSelf());
		} 
		else {
			unhandled(rawMessage);
		}
	}

}

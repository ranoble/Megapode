package com.gravspace.handlers;

import java.lang.reflect.Constructor;
import java.util.Collections;
import java.util.Map;

import org.apache.http.HttpResponse;

import scala.Function1;
import scala.PartialFunction;
import scala.concurrent.ExecutionContext;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;

import com.gravspace.abstractions.Calculation;
import com.gravspace.abstractions.Renderer;
import com.gravspace.abstractions.Task;
import com.gravspace.messages.CalculationMessage;
import com.gravspace.messages.RenderMessage;
import com.gravspace.messages.RequestMessage;
import com.gravspace.messages.ResponseMessage;
import com.gravspace.messages.TaskMessage;
import com.gravspace.util.Layers;

import akka.actor.ActorRef;
import akka.actor.UntypedActor;
import akka.actor.UntypedActorContext;
import akka.dispatch.OnSuccess;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.pattern.Patterns;
import akka.util.Timeout;

public class CalculationHandler extends UntypedActor {
	LoggingAdapter log = Logging.getLogger(getContext().system(), this);
	Map<String, Class<? extends Calculation>> calculations;
	private Map<Layers, ActorRef> routers;
	
	public CalculationHandler(Map<Layers, ActorRef> routers, Map<String, Class<? extends Calculation>> calculations){
		this.calculations = calculations;
		this.routers = routers;
	}

	@Override
	public void onReceive(Object rawMessage) throws Exception {
		log.info("CalculationHandler got: "+rawMessage.getClass().getCanonicalName());
		if (rawMessage instanceof CalculationMessage){
			CalculationMessage message = (CalculationMessage)rawMessage;

			String task_name = message.getTaskName();
			Class<? extends Calculation> calculationClass = calculations.get(task_name);
			Constructor<? extends Calculation> constr = calculationClass.getConstructor(Map.class, ActorRef.class, UntypedActorContext.class);
			Calculation calculation = constr.newInstance(routers, getSender(), this.context());
			
			Object result = calculation.calculate(message.getArgs());
			getSender().tell(result, getSelf());
		} 
		else {
			unhandled(rawMessage);
		}
	}

}

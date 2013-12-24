package com.gravspace.bases;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import akka.actor.ActorRef;
import akka.actor.UntypedActorContext;

import com.gravspace.abstractions.ICalculation;
import com.gravspace.abstractions.ConcurrantCallable;
import com.gravspace.util.Layers;
import com.gravspace.util.TypeUtils;

public abstract class CalculationBase extends ConcurrantCallable implements ICalculation{

	public CalculationBase(Map<Layers, ActorRef> routers,
			ActorRef coordinatingActor, UntypedActorContext actorContext) {
		super(routers, coordinatingActor, actorContext);
	}
	
	public Object calculate(Object... args) throws Exception {
		List<Object> arguments = new ArrayList<>(Arrays.asList(args));
		String methodName = (String) arguments.remove(0);
		List<Class<?>> types = TypeUtils.getListTypes(arguments);
		try {
			Method method = this.getClass().getMethod(methodName, types.toArray(new Class[0]));
			return method.invoke(this, arguments.toArray(new Object[0]));
		} catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			throw e;
		}
	}

}

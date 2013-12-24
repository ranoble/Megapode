package com.gravspace.bases;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import akka.actor.ActorRef;
import akka.actor.UntypedActorContext;

import com.gravspace.abstractions.ConcurrantCallable;
import com.gravspace.abstractions.ITask;
import com.gravspace.util.Layers;
import com.gravspace.util.TypeUtils;

public abstract class TaskBase extends ConcurrantCallable implements ITask{

	public TaskBase(Map<Layers, ActorRef> routers,
			ActorRef coordinatingActor, UntypedActorContext actorContext) {
		super(routers, coordinatingActor, actorContext);
		// TODO Auto-generated constructor stub
	}
	
	public void act(Object... args) {
		List<Object> arguments = new ArrayList<>(Arrays.asList(args));
		String methodName = (String) arguments.remove(0);
		List<Class<?>> types = TypeUtils.getListTypes(arguments);
		try {
			Method method = this.getClass().getMethod(methodName, types.toArray(new Class[0]));
			method.invoke(this, arguments.toArray(new Object[0]));
		} catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	
}

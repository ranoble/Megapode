package com.gravspace.handlers;

import java.lang.reflect.Constructor;
import java.util.Map;

import akka.actor.ActorRef;
import akka.actor.UntypedActor;
import akka.actor.UntypedActorContext;
import akka.event.Logging;
import akka.event.LoggingAdapter;

import com.gravspace.abstractions.Task;
import com.gravspace.messages.TaskMessage;
import com.gravspace.util.Layers;

public class TaskHandler extends UntypedActor {
	LoggingAdapter log = Logging.getLogger(getContext().system(), this);
	Map<String, Class<? extends Task>> tasks;
	private Map<Layers, ActorRef> routers;
	
	public TaskHandler(Map<Layers, ActorRef> routers, Map<String, Class<? extends Task>> tasks){
		this.tasks = tasks;
		this.routers = routers;
	}

	@Override
	public void onReceive(Object rawMessage) throws Exception {
		log.info("TaskHandler got: "+rawMessage.getClass().getCanonicalName());
		if (rawMessage instanceof TaskMessage){
			TaskMessage message = (TaskMessage)rawMessage;

			String task_name = message.getTaskName();
			Class<? extends Task> renderer = tasks.get(task_name);
			Constructor<? extends Task> constr = renderer.getConstructor(Map.class, ActorRef.class, UntypedActorContext.class);
			Task task = constr.newInstance(routers, getSender(), this.context());
			
			task.act(message.getArgs());
		} 
		else {
			unhandled(rawMessage);
		}
	}

}

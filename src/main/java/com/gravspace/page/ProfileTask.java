package com.gravspace.page;

import java.util.Map;

import akka.actor.ActorRef;
import akka.actor.UntypedActorContext;

import com.gravspace.abstractions.ITask;
import com.gravspace.annotations.Task;
import com.gravspace.bases.TaskBase;
import com.gravspace.impl.tasks.ProfileTasks;
import com.gravspace.util.Layers;
//@Page(path="/user/<user>")
@Task
public class ProfileTask extends TaskBase implements ITask, ProfileTasks {

	public ProfileTask(Map<Layers, ActorRef> routers,
			ActorRef coordinatingActor, UntypedActorContext actorContext) {
		super(routers, coordinatingActor, actorContext);
		
	}

	@Override
	public void logTask(String user) {
		log.info(String.format("User [%s] accessed page", user)); 
		
	}

	
}

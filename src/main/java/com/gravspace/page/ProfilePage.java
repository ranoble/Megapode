package com.gravspace.page;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;
import akka.actor.ActorRef;
import akka.actor.UntypedActorContext;

import com.gravspace.bases.PageBase;
import com.gravspace.messages.CalculationMessage;
import com.gravspace.messages.PersistanceMessage;
import com.gravspace.messages.RenderMessage;
import com.gravspace.messages.TaskMessage;
import com.gravspace.util.Layers;

public class ProfilePage extends PageBase {
	
	public ProfilePage(Map<Layers, ActorRef> routers,
			ActorRef coordinatingActor, UntypedActorContext actorContext) {
		super(routers, coordinatingActor, actorContext);
	}

	String name = "The Megapode";
	private Future<Object> profileData;
	private Future<Object> calculationResult;

	

	public void collect() {
		call(new TaskMessage("simple", Arrays.asList(new Integer[]{1, 2})));
		profileData = ask(new PersistanceMessage("doX", Arrays.asList(new Integer[]{1})));
		log.info("collected");
	}

	public void process() {
		// TODO Auto-generated method stub
		calculationResult = ask(new CalculationMessage("simple", Arrays.asList(new Integer[]{1, 2})));
		log.info("processed");
	}

	public String render() throws Exception {
		
		log.info("attempting to load");
		Map<String, Object> context = (Map<String, Object>) Await.result(profileData, Duration.create(1, "minute"));
//		context.put("name",name);
		Future<Object> future = ask(new RenderMessage("hi_pode.vm", context));
		log.info("waiting...");
		Object result = Await.result(future, Duration.create(1, "minute"));
		log.info("asdasddass"+(String) result);
		return (String) result;
	}



}

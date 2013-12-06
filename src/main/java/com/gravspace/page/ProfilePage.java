package com.gravspace.page;

import java.util.HashMap;
import java.util.Map;

import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;

import akka.actor.ActorRef;
import akka.actor.UntypedActorContext;

import com.gravspace.abstractions.PageBase;
import com.gravspace.messages.RenderMessage;
import com.gravspace.util.Layers;

public class ProfilePage extends PageBase {
	
	public ProfilePage(Map<Layers, ActorRef> routers,
			ActorRef coordinatingActor, UntypedActorContext actorContext) {
		super(routers, coordinatingActor, actorContext);
	}

	String name = "The Megapode";

	

	public void collect() {
		
	}

	public void process() {
		// TODO Auto-generated method stub
		
	}

	public String render() throws Exception {
		log.info("attempting to load");
		Map<String, String> context = new HashMap<String, String>();
		context.put("name",name);
		Future<Object> future = ask(new RenderMessage("hi_pode.vm", context));
		log.info("waiting...");
		Object result = Await.result(future, Duration.create(1, "minute"));
		log.info("asdasddass"+(String) result);
//		call()
		
//		""
		// (String)result
		return (String) result;
		
	}



}

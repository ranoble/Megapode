package com.gravspace.testing;

import java.util.Properties;

import akka.actor.ActorRef;
import akka.actor.Props;

import com.gravspace.core.CoordinatingActor;
import com.gravspace.messages.RouterMessage;
import com.gravspace.messages.RouterResponseMessage;

public class TestCoordinator extends CoordinatingActor {

	ActorRef testActor;
	public TestCoordinator(Properties config, CallableContainer cc) {
		super(config);
		testActor = this.context().actorOf(Props.create(CallableActor.class, routerMap, this.self(), cc));
	}
	
	@Override
	public void onReceive(Object message) throws Exception {
		if (message instanceof GetCallable) {
			testActor.forward(message, this.getContext());//.tell(new RouterResponseMessage(routerMap), self());
		} else if (message instanceof RouterMessage) {
			getSender().tell(new RouterResponseMessage(routerMap), self());
		} else {
			super.onReceive(message);
		}

	}
	
}

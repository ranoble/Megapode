package com.gravspace.messages;

import java.util.Map;

import akka.actor.ActorRef;

import com.gravspace.util.Layers;

public class RouterResponseMessage {
	final Map<Layers, ActorRef> routers;
	
	public RouterResponseMessage(Map<Layers, ActorRef> routers){
		this.routers = routers;
	}
	
	public Map<Layers, ActorRef> getRouters(){
		return routers;
	}
}

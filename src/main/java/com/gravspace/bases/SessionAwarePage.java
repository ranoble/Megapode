package com.gravspace.bases;

import java.util.Map;

import org.apache.http.Header;

import akka.actor.ActorRef;
import akka.actor.UntypedActorContext;

import com.gravspace.util.Layers;

public abstract class SessionAwarePage extends PageBase {

	
	public SessionAwarePage(Map<Layers, ActorRef> routers,
			ActorRef coordinatingActor, UntypedActorContext actorContext) {
		super(routers, coordinatingActor, actorContext);
	}

	@Override
	public void initialise(String path,
			String method,
			String query, 
            Header[] headers,
            byte[] content,
            Map<String, String> params) {
		super.initialise(path, method, query, headers, content, params);
		
	}
}

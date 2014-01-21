package com.gravspace.handlers;

import java.util.HashMap;
import java.util.Map;

import com.gravspace.messages.GetSession;
import com.gravspace.messages.KeepAlive;
import com.gravspace.messages.KillSession;

import akka.actor.PoisonPill;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.actor.ActorRef;

public class SessionHandler extends UntypedActor {
	Map<String, ActorRef> sessions = new HashMap<>();
	@Override
	public void onReceive(Object arg0) throws Exception {
		if (arg0 instanceof GetSession){
			GetSession sessionRequest = (GetSession)arg0; 
//			sessionRequest.getSessionId();
			if (!sessions.containsKey(sessionRequest.getSessionId())){
				ActorRef session = this.getContext().actorOf(Props.create(Session.class, self(), sessionRequest.getSessionId()), 
						"Session-"+sessionRequest.getSessionId());
				sessions.put(sessionRequest.getSessionId(), session);
			}
			ActorRef session = sessions.get(sessionRequest.getSessionId());
			getSender().tell(session, self());
			session.tell(new KeepAlive(), self());
			
		} else if (arg0 instanceof KillSession) {
			KillSession sessionRequest = (KillSession)arg0; 
			ActorRef session = sessions.remove(sessionRequest.getSessionId());
			if (session == null){
				getSender().tell("OK", self());
			} else {
				getSender().tell("OK", self());
				session.tell(PoisonPill.getInstance(), self());
			}
		} else {
			unhandled(arg0);
		}

	}

}

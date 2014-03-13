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
import akka.event.Logging;
import akka.event.LoggingAdapter;

public class SessionHandler extends UntypedActor {
	LoggingAdapter log = Logging.getLogger(getContext().system(), this);
	Map<String, ActorRef> sessions = new HashMap<>();
	@Override
	public void onReceive(Object message) throws Exception {
		log.info("Session Handler got: "+message.getClass().getCanonicalName());
		if (message instanceof GetSession){
			GetSession sessionRequest = (GetSession)message; 
			if (!sessions.containsKey(sessionRequest.getSessionId())){
				ActorRef session = this.getContext().actorOf(Props.create(Session.class, self(), sessionRequest.getSessionId()), 
						"Session-"+sessionRequest.getSessionId());
				sessions.put(sessionRequest.getSessionId(), session);
			}
			ActorRef session = sessions.get(sessionRequest.getSessionId());
			getSender().tell(session, self());
			session.tell(new KeepAlive(), self());
			
		} else if (message instanceof KillSession) {
			KillSession sessionRequest = (KillSession)message; 
			ActorRef session = sessions.remove(sessionRequest.getSessionId());
			if (session == null){
				getSender().tell("OK", self());
			} else {
				getSender().tell("OK", self());
				session.tell(PoisonPill.getInstance(), self());
			}
		} else {
			unhandled(message);
		}

	}

}

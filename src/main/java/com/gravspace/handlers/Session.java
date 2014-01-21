package com.gravspace.handlers;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import scala.concurrent.Promise;

import com.gravspace.messages.GetSessionVar;
import com.gravspace.messages.KillSession;
import com.gravspace.messages.Null;
import com.gravspace.messages.SetSessionVar;
import com.gravspace.messages.KeepAlive;

import akka.actor.ActorRef;
import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import static akka.dispatch.Futures.future;

public class Session extends UntypedActor {
	LoggingAdapter log = Logging.getLogger(getContext().system(), this);
	Map<String, Object> sessionVars = new HashMap<>();
	ActorRef handler;
	String sessionId;
	Thread keepalive;
	
	public Session(ActorRef handler, String sessionId){
		this.handler = handler;
		this.sessionId = sessionId;
		keepalive();
	}
	
	@Override
	public void onReceive(Object arg0) throws Exception {
		if (arg0 instanceof KeepAlive){
			keepalive();
		} else if (arg0 instanceof GetSessionVar){
			GetSessionVar getter = (GetSessionVar)arg0;
			Object value = sessionVars.get(getter.getName());
			if (value == null){
				value = Null.getNull();
			}
			this.getSender().tell(value, self());
		} else if (arg0 instanceof SetSessionVar){
			SetSessionVar setter = (SetSessionVar)arg0;
			sessionVars.put(setter.getName(), setter.getValue());
			log.info(String.format("SETTING [%s: %s]", setter.getName(), 
					setter.getValue().toString()));
			this.getSender().tell(Boolean.TRUE, self());
		} else {
			unhandled(arg0);
		}
		
	}

	private void keepalive() {
		if (keepalive == null){
			keepalive = new Thread(){
				public void run(){
					for(;;){
						try {
							Thread.sleep(360000);
							break;
						} catch (InterruptedException e) {}
					}
					handler.tell(new KillSession(sessionId), self());
				}
			};
		}
	}
	
}

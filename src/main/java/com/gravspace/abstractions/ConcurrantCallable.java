package com.gravspace.abstractions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import com.gravspace.messages.RenderMessage;
import com.gravspace.messages.TaskMessage;

import scala.concurrent.Future;
import scala.concurrent.Promise;
import scala.concurrent.duration.Duration;
import akka.actor.ActorRef;
import akka.actor.UntypedActorContext;
import akka.dispatch.Futures;
import akka.dispatch.OnSuccess;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.pattern.Patterns;
import akka.util.Timeout;

public class ConcurrantCallable {
	protected LoggingAdapter log = null;
	protected ActorRef coordinatingActor;
	protected UntypedActorContext actorContext;
	protected List<Future<Object>> taskList;
	protected Set<Promise<Object>> awaitListeners;
	
	public ConcurrantCallable(final ActorRef coordinatingActor, final UntypedActorContext actorContext){
		this.coordinatingActor = coordinatingActor;
		this.actorContext = actorContext;
		log = Logging.getLogger(actorContext.system(), this);
		taskList = Collections.synchronizedList(new ArrayList<Future<Object>>());
		awaitListeners = new CopyOnWriteArraySet<Promise<Object>>();
	}
	
	public Future<Object> await(){
		final Promise<Object> waiter = Futures.promise();
		if (taskList.isEmpty()){
			waiter.success(null);
		}
		awaitListeners.add(waiter);
		return waiter.future();
	}

	public Future<Object> ask(Object message){
		log.info("Asking");
		Timeout timeout = new Timeout(Duration.create(1, "minute"));
		final Future<Object> future = Patterns.ask(coordinatingActor, message, timeout);
		log.info("Asking"+future.toString());
		taskList.add(future);
		
//		future.onSuccess(new OnSuccess<Object>() {
//			@Override
//			public void onSuccess(Object response) throws Throwable {
//				taskList.remove(future);
//				notifyWaiters(future);
//			}
//		}, getActorContext().dispatcher());
		return future;
	}
	
	
	
	protected void notifyWaiters(Future<Object> future) {
		if (taskList.isEmpty()){
			for (Promise<Object> waiter: awaitListeners){
				waiter.success(future);
			}
		}
	}

	public ActorRef getCoordinatingActor() {
		return coordinatingActor;
	}

	public UntypedActorContext getActorContext() {
		return actorContext;
	}
}

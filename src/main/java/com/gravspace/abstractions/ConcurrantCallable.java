package com.gravspace.abstractions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import com.gravspace.messages.CalculationMessage;
import com.gravspace.messages.ComponentMessage;
import com.gravspace.messages.PersistanceMessage;
import com.gravspace.messages.RenderMessage;
import com.gravspace.messages.TaskMessage;
import com.gravspace.util.Layers;

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
	protected Map<Layers, ActorRef> routers;
	
	public ConcurrantCallable(final Map<Layers, ActorRef> routers, final ActorRef coordinatingActor, final UntypedActorContext actorContext){
		this.coordinatingActor = coordinatingActor;
		this.actorContext = actorContext;
		this.routers = routers;
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
	
	public Future<Object> ask(RenderMessage message){
		log.info("Asking");
		Timeout timeout = new Timeout(Duration.create(1, "minute"));
		final Future<Object> future = Patterns.ask(routers.get(Layers.RENDERER), message, timeout);
		log.info("Asking"+future.toString());
		taskList.add(future);
		monitorForCompletion(future);
		return future;
	}
	
	public Future<Object> ask(ComponentMessage message){
		log.info("Asking");
		Timeout timeout = new Timeout(Duration.create(1, "minute"));
		final Future<Object> future = Patterns.ask(routers.get(Layers.COMPONENT), message, timeout);
		log.info("Asking"+future.toString());
		taskList.add(future);
		monitorForCompletion(future);
		return future;
	}
	
	public Future<Object> ask(PersistanceMessage message){
		log.info("Asking");
		Timeout timeout = new Timeout(Duration.create(1, "minute"));
		final Future<Object> future = Patterns.ask(routers.get(Layers.DATA_ACCESS), message, timeout);
		log.info("Asking"+future.toString());
		taskList.add(future);
		monitorForCompletion(future);
		return future;
	}
	
	public Future<Object> ask(CalculationMessage message){
		log.info("Asking");
		Timeout timeout = new Timeout(Duration.create(1, "minute"));
		final Future<Object> future = Patterns.ask(routers.get(Layers.CALCULATION), message, timeout);
		log.info("Asking"+future.toString());
		taskList.add(future);
		monitorForCompletion(future);
		return future;
	}
	
	public void call(TaskMessage message){
		log.info("Calling");
		routers.get(Layers.TASK).tell(message, ActorRef.noSender());
	}

	private void monitorForCompletion(final Future<Object> future) {
		future.onSuccess(new OnSuccess<Object>() {
			@Override
			public void onSuccess(Object response) throws Throwable {
				taskList.remove(future);
				notifyWaiters(future);
			}
		}, getActorContext().dispatcher());
	}
	

	
	
	
	protected void notifyWaiters(Future<Object> future) {
		if (taskList.isEmpty()){
			for (Promise<Object> waiter: awaitListeners){
				if (!waiter.isCompleted()){
					waiter.success(future);
				}
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

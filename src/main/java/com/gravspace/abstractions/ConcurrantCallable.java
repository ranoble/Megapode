package com.gravspace.abstractions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.logging.Logger;

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
import akka.dispatch.OnComplete;
import akka.dispatch.OnSuccess;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.pattern.Patterns;
import akka.util.Timeout;

public class ConcurrantCallable {
	private LoggingAdapter log = null;
	protected ActorRef coordinatingActor;
	protected UntypedActorContext actorContext;
	protected List<Future<Object>> taskList;
	protected Set<Promise<Object>> awaitListeners;
	protected Map<Layers, ActorRef> routers;
	

	
	public ConcurrantCallable(final Map<Layers, ActorRef> routers, final ActorRef coordinatingActor, final UntypedActorContext actorContext){
		this.coordinatingActor = coordinatingActor;
		setActorContext(actorContext);
		this.routers = routers;
		
		taskList = Collections.synchronizedList(new ArrayList<Future<Object>>());
		awaitListeners = new CopyOnWriteArraySet<Promise<Object>>();
	}
	
	public LoggingAdapter getLogger(){
		if (log == null){
			log = Logging.getLogger(getActorContext().system(), this);
		}
		return log;
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
//		getLogger().info("Asking");
		Timeout timeout = new Timeout(Duration.create(1, "minute"));
		final Future<Object> future = Patterns.ask(routers.get(Layers.RENDERER), message, timeout);
//		getLogger().info("Asking"+future.toString());
		addTaskToMonitoredList(future);
		return future;
	}

	protected void addTaskToMonitoredList(final Future<Object> future) {
		
		taskList.add(future);
		monitorForCompletion(future);
	}
	
	public Future<Object> ask(ComponentMessage message){
//		getLogger().info("Asking");
		Timeout timeout = new Timeout(Duration.create(1, "minute"));
		final Future<Object> future = Patterns.ask(routers.get(Layers.COMPONENT), message, timeout);
//		getLogger().info("Asking"+future.toString());
		addTaskToMonitoredList(future);
		return future;
	}
	
	public Future<Object> ask(PersistanceMessage message){
//		getLogger().info("Asking");
		Timeout timeout = new Timeout(Duration.create(1, "minute"));
		final Future<Object> future = Patterns.ask(routers.get(Layers.DATA_ACCESS), message, timeout);
//		getLogger().info("Asking"+future.toString());
		addTaskToMonitoredList(future);
		return future;
	}
	
	public Future<Object> ask(CalculationMessage message){
//		getLogger().info("Asking");
		Timeout timeout = new Timeout(Duration.create(1, "minute"));
		final Future<Object> future = Patterns.ask(routers.get(Layers.CALCULATION), message, timeout);
//		getLogger().info("Asking"+future.toString());
		addTaskToMonitoredList(future);
		return future;
	}
	
	public void call(TaskMessage message){
//		getLogger().info("Calling");
		routers.get(Layers.TASK).tell(message, ActorRef.noSender());
	}

	protected void monitorForCompletion(final Future<Object> future) {
		//this should be for oncompletes
		future.onComplete(new OnComplete<Object>() {
			@Override
			public void onComplete(Throwable arg0, Object response)
					throws Throwable {
//				getLogger().info("Task Complete, Monitor");
				taskList.remove(future);
//				getLogger().info("Task removed => "+taskList.size());
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
	
	public void setActorContext(UntypedActorContext context) {
		actorContext = context;
	}
}

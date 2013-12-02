package com.gravspace.abstractions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.protocol.HttpContext;

import scala.concurrent.Future;
import scala.concurrent.Promise;
import scala.concurrent.duration.Duration;
import akka.actor.ActorRef;
import akka.actor.UntypedActorContext;
import akka.dispatch.Futures;
import akka.dispatch.OnSuccess;
import akka.pattern.Patterns;
import akka.util.Timeout;

import com.gravspace.messages.TaskMessage;


public abstract class PageBase implements Page {
	ActorRef coordinatingActor;
	UntypedActorContext actorContext;
	List<Future<Object>> taskList;
	Set<Promise<Object>> awaitListeners;
	
	HttpRequest request;
	HttpResponse response;
	HttpContext context;
	
	public PageBase(final ActorRef coordinatingActor, final UntypedActorContext actorContext){
		this.coordinatingActor = coordinatingActor;
		this.actorContext = actorContext;
		taskList = Collections.synchronizedList(new ArrayList<Future<Object>>());
		awaitListeners = new CopyOnWriteArraySet<Promise<Object>>();
	}
	
	public void initialise(HttpRequest request, HttpResponse response,
			HttpContext context) {
		this.request = request;
		this.response = response;
		this.context = context;
	}
	
	public Future<Object> await(){
		final Promise<Object> waiter = Futures.promise();
		if (taskList.isEmpty()){
			waiter.success(null);
		}
		awaitListeners.add(waiter);
		return waiter.future();
	}

	public Future<Object> ask(TaskMessage message){
		Timeout timeout = new Timeout(Duration.create(1, "minute"));
		final Future<Object> future = Patterns.ask(coordinatingActor, message, timeout);

		taskList.add(future);
		
		future.onSuccess(new OnSuccess<Object>() {
			@Override
			public void onSuccess(Object response) throws Throwable {
				taskList.remove(future);
				notifyWaiters(future);
			}
		}, getActorContext().dispatcher());
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

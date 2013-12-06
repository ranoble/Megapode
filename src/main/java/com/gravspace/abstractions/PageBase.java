package com.gravspace.abstractions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
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
import com.gravspace.util.Layers;


public abstract class PageBase  extends ConcurrantCallable implements Page {

	
	HttpRequest request;
	HttpResponse response;
	HttpContext context;
	
	public PageBase(final Map<Layers, ActorRef> routers, final ActorRef coordinatingActor, final UntypedActorContext actorContext){
		super(routers, coordinatingActor, actorContext);

	}
	
	public void initialise(HttpRequest request, HttpResponse response,
			HttpContext context) {
		this.request = request;
		this.response = response;
		this.context = context;
	}
	
	

}

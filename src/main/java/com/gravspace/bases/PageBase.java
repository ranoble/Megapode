package com.gravspace.bases;

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

import com.gravspace.abstractions.ConcurrantCallable;
import com.gravspace.abstractions.Page;
import com.gravspace.messages.TaskMessage;
import com.gravspace.util.Layers;


public abstract class PageBase  extends ConcurrantCallable implements Page {

	
	protected HttpRequest request;
	protected HttpResponse response;
	protected HttpContext context;
	protected Map<String, String> params;
	
	
	public PageBase(final Map<Layers, ActorRef> routers, final ActorRef coordinatingActor, final UntypedActorContext actorContext){
		super(routers, coordinatingActor, actorContext);

	}
	
	public void initialise(HttpRequest request, 
			HttpResponse response,
			HttpContext context,
			Map<String, String> params) {
		this.request = request;
		this.response = response;
		this.context = context;
		this.params = params;
	}
	
	public void initialise(Object... args){}
	

}

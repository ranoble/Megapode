package com.gravspace.bases;

import java.util.Map;
import java.util.concurrent.Callable;

import org.apache.commons.beanutils.BeanUtils;

import scala.concurrent.Future;
import akka.actor.ActorRef;
import akka.actor.UntypedActorContext;
import akka.dispatch.OnComplete;
import akka.dispatch.OnSuccess;

import com.gravspace.abstractions.IComponent;
import com.gravspace.abstractions.ConcurrantCallable;
import com.gravspace.handlers.SetterFailure;
import com.gravspace.util.Layers;

public abstract class ComponentBase extends ConcurrantCallable implements IComponent{

	public ComponentBase(Map<Layers, ActorRef> routers,
			ActorRef coordinatingActor, UntypedActorContext actorContext) {
		super(routers, coordinatingActor, actorContext);
	}
	
	public void set(final String field, Future<?> source){
		set(field, source);
	}
	
	public ComponentBase getThis(){
		return this;
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void set(final String field, Future<?> source, final SetterFailure onFailure){
		source.onComplete(new OnComplete(){
			@Override
			public void onComplete(Throwable exception, Object returnValue)
					throws Throwable {
				if (exception == null){
					BeanUtils.setProperty(getThis(), field, returnValue);
				} else if (onFailure != null) {
					onFailure.handleFailure(getThis(), field, exception);
				}
			}
			
		}, this.getActorContext().dispatcher());
	}
	
}

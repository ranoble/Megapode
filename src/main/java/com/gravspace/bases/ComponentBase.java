package com.gravspace.bases;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.concurrent.Callable;

import org.apache.commons.beanutils.BeanUtils;

import scala.concurrent.Future;
import scala.concurrent.Promise;
import akka.actor.ActorRef;
import akka.actor.UntypedActorContext;
import akka.dispatch.Futures;
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
		set(field, source, null);
	}
	
	public ComponentBase getThis(){
		return this;
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void set(final String field, Future<?> source, final SetterFailure onFailure){
		getLogger().info("Registering setter on field "+field);
		final Promise<Object> waiter = Futures.promise();
		Future setterFuture = waiter.future();
		taskList.add(setterFuture);
		monitorForCompletion(setterFuture);
		source.onComplete(new OnComplete(){
			@Override
			public void onComplete(Throwable exception, Object returnValue)
					throws Throwable {

				getLogger().info("Task Complete, Setter");
				if (exception == null){
					getLogger().info("No Exception: Yay!");
					getLogger().info(returnValue.toString());
					getLogger().info(getThis().getClass().getCanonicalName());
					getLogger().info(returnValue.getClass().getCanonicalName());
					try {
						BeanUtils.setProperty(getThis(), field, returnValue);
					} catch (IllegalAccessException | InvocationTargetException e){
						e.printStackTrace();
						getLogger().error("error", e);
					}
					getLogger().info("I has set it!");
				} else if (onFailure != null) {
					getLogger().info("I has failure, but will deal with it!");
					onFailure.handleFailure(getThis(), field, exception);
				} else 
					getLogger().info("Error but I care not!!");
				waiter.success(null);
			}	
		}, this.getActorContext().dispatcher());
	}
	
}

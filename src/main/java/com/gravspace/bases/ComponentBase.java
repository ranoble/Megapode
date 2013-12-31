package com.gravspace.bases;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.List;
import java.util.concurrent.Callable;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.beanutils.PropertyUtilsBean;

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
	
	public void set(final String field, Future<?> source, final Promise<Object> waiter){
		set(field, source, waiter, null);
	}
	
	public ComponentBase getThis(){
		return this;
	}
	
	public void add(final String field, Future<?> source, final Promise<Object> waiter){
		add(field, source, waiter, null);
	}
	
	public Promise<Object> prepareSet(){
		Promise<Object> waiter = Futures.promise();
		Future setterFuture = waiter.future();
		addTaskToMonitoredList(setterFuture);
		return waiter;
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void add(final String field, Future<?> source, final Promise<Object> waiter, final SetterFailure onFailure){
		getLogger().info("Registering adder on field "+field);
		source.onComplete(new OnComplete(){
			@Override
			public void onComplete(Throwable exception, Object returnValue)
					throws Throwable {

				getLogger().info("Task Complete, Adder "+field);
				if (exception == null){
					
					try {
						((List)PropertyUtils.getIndexedProperty(getThis(), field)).add(returnValue);
					} catch (IllegalAccessException | InvocationTargetException e){
						e.printStackTrace();
						getLogger().error("error", e);
					}
					getLogger().info("I has set it! "+field+" "+returnValue.toString());
				} else if (onFailure != null) {
					getLogger().info("I has failure, but will deal with it!");
					onFailure.handleFailure(getThis(), field, exception);
				} else {
					getLogger().info("Error but I care not!! "+field);
					getLogger().error(exception, "Error");
				}
					
				waiter.success(null);
			}	
		}, this.getActorContext().dispatcher());
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void set(final String field, Future<?> source, final Promise<Object> waiter, final SetterFailure onFailure){
		getLogger().info("Registering setter on field "+field);

		source.onComplete(new OnComplete(){
			@Override
			public void onComplete(Throwable exception, Object returnValue)
					throws Throwable {

				getLogger().info("Task Complete, Setter "+field);
				if (exception == null){
					
					try {
						BeanUtils.setProperty(getThis(), field, returnValue);
						getLogger().info("I has set it! "+field+ " "+returnValue.toString());
					} catch (IllegalAccessException | InvocationTargetException e){
						getLogger().info("Bollocks! "+field+ " "+returnValue.toString());
						e.printStackTrace();
						getLogger().error("error", e);
					}
					
				} else if (onFailure != null) {
					getLogger().info("I has failure, but will deal with it! "+field);
					onFailure.handleFailure(getThis(), field, exception);
				} else {
					getLogger().error(exception, "Error");
					getLogger().info("Error on field "+field+" but I care not!!");
				}
				
				waiter.success(null);
			}	
		}, this.getActorContext().dispatcher());
	}
	
}

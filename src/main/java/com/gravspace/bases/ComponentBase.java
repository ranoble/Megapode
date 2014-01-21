package com.gravspace.bases;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.beanutils.PropertyUtilsBean;

import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.Promise;
import scala.concurrent.duration.Duration;
import akka.actor.ActorRef;
import akka.actor.UntypedActorContext;
import akka.dispatch.Futures;
import akka.dispatch.OnComplete;
import akka.dispatch.OnSuccess;

import com.gravspace.abstractions.IComponent;
import com.gravspace.abstractions.ConcurrantCallable;
import com.gravspace.handlers.SetterFailure;
import com.gravspace.messages.Null;
import com.gravspace.util.Layers;

public abstract class ComponentBase extends ConcurrantCallable implements IComponent{

	public ComponentBase(Map<Layers, ActorRef> routers,
			ActorRef coordinatingActor, UntypedActorContext actorContext) {
		super(routers, coordinatingActor, actorContext);
	}
	
	@SuppressWarnings("unchecked")
	public void set(final String field, Future<?> wrapper){
		set(field, wrapper, null);
	}
	
	@SuppressWarnings("unchecked")
	public void set(final String field, Future<?> source, final SetterFailure onFailure){
		final Promise<Object> wait = prepareSet();
		set(field, source, wait, onFailure);
		setIfAlreadyComplete(field, source, wait);
	}
	
	@SuppressWarnings("unchecked")
	public void add(final String field, Future<?> wrapper){
		add(field, wrapper, null);
	}
	
	@SuppressWarnings("unchecked")
	public void add(final String field, Future<?> source, final SetterFailure onFailure){
		final Promise<Object> wait = prepareSet();
		add(field, source, wait, onFailure);
		addIfAlreadyComplete(field, source, wait);
	}

	private void setIfAlreadyComplete(final String field, Future<?> source,
			final Promise<Object> wait) {
		if (source.isCompleted() && !wait.isCompleted()){
			Object returnValue;
			try {
				returnValue = Await.result(source, Duration.create(0, TimeUnit.SECONDS));
				if (returnValue instanceof Null){
					BeanUtils.setProperty(getThis(), field, null);
				} else {
					BeanUtils.setProperty(getThis(), field, returnValue);
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			try {
				wait.success(null);
			} catch (IllegalStateException e){}
		}
	}
	private void addIfAlreadyComplete(final String field, Future<?> source,
			final Promise<Object> wait) {
		if (source.isCompleted() && !wait.isCompleted()){
			Object returnValue;
			try {
				
				returnValue = Await.result(source, Duration.create(0, TimeUnit.SECONDS));
				if (!(returnValue instanceof Null)){
					((List)PropertyUtils.getProperty(getThis(), field)).add(returnValue);
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			wait.success(null);
		}
	}
	
	
	
	public ComponentBase getThis(){
		return this;
	}
	
	
	public Promise<Object> prepareSet(){
		Promise<Object> waiter = Futures.promise();
		Future<Object> setterFuture = waiter.future();
		addTaskToMonitoredList(setterFuture);
		return waiter;
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void add(final String field, Future<?> source, final Promise<Object> waiter, final SetterFailure onFailure){
		source.onComplete(new OnComplete(){
			@Override
			public void onComplete(Throwable exception, Object returnValue)
					throws Throwable {
				if (exception == null){
					try {
						if (!(returnValue instanceof Null)){
							((List)PropertyUtils.getProperty(getThis(), field)).add(returnValue);
						}
					} catch (IllegalAccessException | InvocationTargetException e){
						getLogger().error(exception, "Adder Exception");
						exception = e;
					}
				} 
				if (exception != null){
					handleCallbackException(field, onFailure, exception);
				}
					
				waiter.success(null);
			}	
		}, this.getActorContext().dispatcher());
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void set(final String field, Future<?> source, final Promise<Object> waiter, final SetterFailure onFailure){
//		getLogger().info("Registering setter on field "+field);

		source.onComplete(new OnComplete(){
			@Override
			public void onComplete(Throwable exception, Object returnValue)
					throws Throwable {

				if (exception == null){
					try {
						if (returnValue instanceof Null){
							BeanUtils.setProperty(getThis(), field, null);
						} else {
							BeanUtils.setProperty(getThis(), field, returnValue);
						}
					} catch (IllegalAccessException | InvocationTargetException e){
						getLogger().error(exception, "Setter Exception");
						exception = e;
					}
				}
				if (exception != null){
					handleCallbackException(field, onFailure, exception);
				}
				
				waiter.success(null);
			}

			
		}, this.getActorContext().dispatcher());
	}
	
	private void handleCallbackException(final String field,
			final SetterFailure onFailure, Throwable exception) {
		if (onFailure != null) {
			onFailure.handleFailure(getThis(), field, exception);
		} else {
			getLogger().error(exception, "Failed without failure handler");
		}
	}	
	
}

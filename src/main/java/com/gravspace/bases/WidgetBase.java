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

import com.gravspace.abstractions.IWidget;
import com.gravspace.handlers.SetterFailure;
import com.gravspace.messages.Null;
import com.gravspace.util.Layers;

public abstract class WidgetBase extends ConcurrantCallable implements IWidget{

	public WidgetBase(Map<Layers, ActorRef> routers,
			ActorRef coordinatingActor, UntypedActorContext actorContext) {
		super(routers, coordinatingActor, actorContext);
	}
	
	@SuppressWarnings("unchecked")
	public void set(final String field, Future<?> wrapper){
		set(field, wrapper, null);
	}
	
	@SuppressWarnings("unchecked")
	public void set(final String field, Future<?> source, final SetterFailure onFailure){
		final Promise<Object> wait = delayUntilComplete();
		set(field, source, wait, onFailure);
		setIfAlreadyComplete(field, source, wait, onFailure);
	}
	
	@SuppressWarnings("unchecked")
	public void add(final String field, Future<?> wrapper){
		add(field, wrapper, null);
	}
	
	@SuppressWarnings("unchecked")
	public void add(final String field, Future<?> source, final SetterFailure onFailure){
		final Promise<Object> wait = delayUntilComplete();
		add(field, source, wait, onFailure);
		addIfAlreadyComplete(field, source, wait, onFailure);
	}

	private void setIfAlreadyComplete(final String field, Future<?> source,
			final Promise<Object> wait, SetterFailure onFailure) {
		if (source.isCompleted() && !wait.isCompleted()){
			Object returnValue;
			try {
				returnValue = Await.result(source, Duration.create(0, TimeUnit.SECONDS));
				if (returnValue instanceof Null){
					BeanUtils.setProperty(getThis(), field, null);
				} else {
					BeanUtils.setProperty(getThis(), field, returnValue);
				}
			} catch (Exception exception) {
				handleCallbackException(field, onFailure, exception, wait);
			}
			try {
				wait.success(null);
			} catch (IllegalStateException e){}
		}
	}
	private void addIfAlreadyComplete(final String field, Future<?> source,
			final Promise<Object> wait, SetterFailure onFailure) {
		if (source.isCompleted() && !wait.isCompleted()){
			Object returnValue;
			try {
				returnValue = Await.result(source, Duration.create(0, TimeUnit.SECONDS));
				if (!(returnValue instanceof Null)){
					((List)PropertyUtils.getProperty(getThis(), field)).add(returnValue);
				}
			} catch (Exception exception) {
				handleCallbackException(field, onFailure, exception, wait);
			}
			
			try {
				wait.success(null);
			} catch (IllegalStateException e){}
		}
	}
	
	public WidgetBase getThis(){
		return this;
	}
	
	
	public Promise<Object> delayUntilComplete(){
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
						getLogger().error(exception, 
								String.format("Exception in add callback on widget / page [%s] field [%s]", 
										getThis().getClass().getCanonicalName(), field));
						exception = e;
					}
				} 
				if (exception != null){
					handleCallbackException(field, onFailure, exception, waiter);
				} else {
					waiter.success(null);
				}
			}	
		}, this.getActorContext().dispatcher());
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void set(final String field, Future<?> source, final Promise<Object> waiter, final SetterFailure onFailure){
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
						getLogger().error(exception, 
								String.format("Exception in set callback on widget / page [%s] field [%s]", 
										getThis().getClass().getCanonicalName(), field));
						exception = e;
					}
				}
				if (exception != null){
					handleCallbackException(field, onFailure, exception, waiter);
				} else {
					waiter.success(null);
				}
				
				
			}

			
		}, this.getActorContext().dispatcher());
	}
	
	private void handleCallbackException(final String field,
			final SetterFailure onFailure, Throwable exception, Promise<Object> promise) {
		if (onFailure != null) {
			onFailure.handleFailure(getThis(), field, exception, promise);
		} else {
			getLogger().error(exception, "Failed without failure handler, falling back.");
		}
		if (!promise.isCompleted()){
			promise.failure(exception);
		}
	}	
	
	public abstract void collect();
	
	public abstract void process();
	
	public abstract Future<String> render() throws Exception;

	public abstract void initialise(Object... args);
	
}

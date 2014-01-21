package com.gravspace.util;

import java.util.ArrayList;
import java.util.List;

import akka.dispatch.Futures;
import akka.dispatch.OnComplete;

import scala.concurrent.ExecutionContext;
import scala.concurrent.Future;
import scala.concurrent.Promise;
import static akka.dispatch.Futures.sequence;

public abstract class When {
	Promise<Object> prm = null;
	public When(final ExecutionContext context, final Object... params){
		prm = Futures.promise();
		List<Future<Object>> futures = new ArrayList<Future<Object>>();
		for (Object obj: params){
			if (obj instanceof Future){
				futures.add((Future<Object>) obj);
			}
		}
		Future<Iterable<Object>> result = sequence(futures, context);
		result.onComplete(new OnComplete<Iterable<Object>>(){

			@Override
			public void onComplete(Throwable exception, Iterable<Object> results)
					throws Throwable {
				List<Object> _params = new ArrayList<Object>();
				if (exception == null){
					
					for (Object obj: params){
						if (obj instanceof Future){
							_params.add(results.iterator().next());
						} else {
							_params.add(params);
						}
					}
				}
				Future<?> result = finishes(exception, _params.toArray(new Object[0]));
				result.onComplete(new OnComplete(){

					@Override
					public void onComplete(Throwable exception, Object result)
							throws Throwable {
						if (exception != null){
							prm.success(result);
						} else {
							prm.failure(exception);
						}
						
					}
					
				}, context);
			}
			
		}, context);
	}
	
	public Future<Object> itWill(){
		return prm.future();
	}
	
	public abstract Future<?> finishes(Throwable exception, Object... params);
	
//	public 
}

package com.gravspace.proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;

import com.gravspace.abstractions.ConcurrantCallable;
import com.gravspace.abstractions.ITask;
import com.gravspace.messages.TaskMessage;

public class CalculationProxyFactory {
	private static class TaskProxyImpl implements InvocationHandler {

		protected ConcurrantCallable caller;

		public TaskProxyImpl(ConcurrantCallable caller) {
			this.caller = caller;
		}

		@Override
		public Object invoke(Object proxy, Method method, Object[] args)
				throws Throwable {
			caller.call(new TaskMessage(proxy.getClass().getCanonicalName(), Arrays.asList(args)));
			
			return null;
		}

	}

	@SuppressWarnings("unchecked")
	public static <T> T getProxy(Class<T> iface, Class<? extends ITask> concrete, ConcurrantCallable caller) {
		return (T) Proxy.newProxyInstance(
				CalculationProxyFactory.class.getClassLoader(), new Class[] { iface },
				new TaskProxyImpl(caller));
	}
	
	@SuppressWarnings("unchecked")
	public static <T> T getProxy(Class<T> concrete, ConcurrantCallable caller) {
		return (T) Proxy.newProxyInstance(
				CalculationProxyFactory.class.getClassLoader(), new Class[] { concrete },
				new TaskProxyImpl(caller));
	}

}

package com.gravspace.proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;

import com.gravspace.abstractions.ConcurrantCallable;
import com.gravspace.abstractions.ICalculation;
import com.gravspace.abstractions.IPersistanceAccessor;
import com.gravspace.messages.CalculationMessage;
import com.gravspace.messages.PersistanceMessage;
import com.gravspace.messages.TaskMessage;

public class DataAccessorProxyFactory {
	private static class DataProxyImpl implements InvocationHandler {

		protected ConcurrantCallable caller;

		public DataProxyImpl(ConcurrantCallable caller) {
			this.caller = caller;
		}

		@Override
		public Object invoke(Object proxy, Method method, Object[] args)
				throws Throwable {
			return caller.ask(new PersistanceMessage(proxy.getClass().getCanonicalName(), Arrays.asList(args)));
		}
	}

	@SuppressWarnings("unchecked")
	public static <T> T getProxy(Class<T> iface, Class<? extends IPersistanceAccessor> concrete, ConcurrantCallable caller) {
		return (T) Proxy.newProxyInstance(
				DataAccessorProxyFactory.class.getClassLoader(), new Class[] { iface },
				new DataProxyImpl(caller));
	}

}

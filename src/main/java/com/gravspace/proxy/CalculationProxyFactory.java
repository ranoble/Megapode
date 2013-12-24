package com.gravspace.proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;

import com.gravspace.abstractions.ConcurrantCallable;
import com.gravspace.abstractions.ICalculation;
import com.gravspace.messages.CalculationMessage;
import com.gravspace.messages.TaskMessage;

public class CalculationProxyFactory {
	private static class CalculationProxyImpl implements InvocationHandler {

		protected ConcurrantCallable caller;

		public CalculationProxyImpl(ConcurrantCallable caller) {
			this.caller = caller;
		}

		@Override
		public Object invoke(Object proxy, Method method, Object[] args)
				throws Throwable {
			return caller.ask(new CalculationMessage(proxy.getClass().getCanonicalName(), Arrays.asList(args)));

		}
	}

	@SuppressWarnings("unchecked")
	public static <T> T getProxy(Class<T> iface, Class<? extends ICalculation> concrete, ConcurrantCallable caller) {
		return (T) Proxy.newProxyInstance(
				CalculationProxyFactory.class.getClassLoader(), new Class[] { iface },
				new CalculationProxyImpl(caller));
	}

}

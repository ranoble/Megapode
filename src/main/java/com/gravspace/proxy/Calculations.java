package com.gravspace.proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import scala.concurrent.Future;

import com.gravspace.abstractions.ICalculation;
import com.gravspace.bases.ConcurrantCallable;
import com.gravspace.messages.CalculationMessage;

public class Calculations {
	private static class CalculationProxyImpl implements InvocationHandler {

		protected ConcurrantCallable caller;
		protected String concreteCanonicalName;

		public CalculationProxyImpl(ConcurrantCallable caller, String concreteCanonicalName) {
			this.caller = caller;
			this.concreteCanonicalName = concreteCanonicalName;
		}

		@Override
		public Object invoke(Object proxy, Method method, Object[] args)
				throws Throwable {
			if (args == null)
				args = new Object[]{};
			List<Object> task_args = new ArrayList<>();
			task_args.add(method.getName());
			task_args.addAll(Arrays.asList(args));
			
			Future<Object> k = caller.ask(new CalculationMessage(concreteCanonicalName, task_args));
			return k;
		}
	}

	@SuppressWarnings("unchecked")
	public static <T> T get(Class<T> iface, Class<? extends ICalculation> concrete, ConcurrantCallable caller) {
		return (T) Proxy.newProxyInstance(
				Calculations.class.getClassLoader(), new Class[] { iface },
				new CalculationProxyImpl(caller, concrete.getCanonicalName()));

	}

}

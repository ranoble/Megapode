package com.gravspace.proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import scala.concurrent.Future;

import com.gravspace.abstractions.ICalculation;
import com.gravspace.abstractions.IDataAccessor;
import com.gravspace.bases.ConcurrantCallable;
import com.gravspace.messages.CalculationMessage;
import com.gravspace.messages.PersistanceMessage;
import com.gravspace.messages.TaskMessage;

public class DataAccessors {
	private static class DataProxyImpl implements InvocationHandler {

		protected ConcurrantCallable caller;
		private String concreteCanonicalName;

		public DataProxyImpl(ConcurrantCallable caller, String concreteCanonicalName) {
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
			return caller.ask(new PersistanceMessage(concreteCanonicalName, task_args));
			
		}
	}

	@SuppressWarnings("unchecked")
	public static <T> T get(Class<T> iface, Class<? extends IDataAccessor> concrete, ConcurrantCallable caller) {
		return (T) Proxy.newProxyInstance(
				DataAccessors.class.getClassLoader(), new Class[] { iface },
				new DataProxyImpl(caller, concrete.getCanonicalName()));
	}

}

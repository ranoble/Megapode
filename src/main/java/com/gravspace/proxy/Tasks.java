package com.gravspace.proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.gravspace.abstractions.ConcurrantCallable;
import com.gravspace.abstractions.ITask;
import com.gravspace.messages.TaskMessage;

public class Tasks {
	private static class TaskProxyImpl implements InvocationHandler {

		protected ConcurrantCallable caller;
		protected String concreteCanonicalName;

		public TaskProxyImpl(ConcurrantCallable caller, String concreteCanonicalName) {
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
//			System.out.println(proxy.getClass().getCanonicalName());
			caller.call(new TaskMessage(concreteCanonicalName, task_args));
			
			//a task should ALWAYS return null
			return null;
		}

	}

	@SuppressWarnings("unchecked")
	public static <T> T get(Class<T> iface, Class<? extends ITask> concrete, ConcurrantCallable caller) {
		return (T) Proxy.newProxyInstance(
				Tasks.class.getClassLoader(), new Class[] { iface },
				new TaskProxyImpl(caller, concrete.getCanonicalName()));
	}

}

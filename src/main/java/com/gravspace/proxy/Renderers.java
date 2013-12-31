package com.gravspace.proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import scala.concurrent.Future;

import com.gravspace.abstractions.ConcurrantCallable;
import com.gravspace.abstractions.ICalculation;
import com.gravspace.abstractions.IPersistanceAccessor;
import com.gravspace.abstractions.IRenderer;
import com.gravspace.bases.RendererBase;
import com.gravspace.defaults.DefaultRenderer;
import com.gravspace.messages.CalculationMessage;
import com.gravspace.messages.PersistanceMessage;
import com.gravspace.messages.RenderMessage;
import com.gravspace.messages.TaskMessage;

public class Renderers {
	private static class RendererProxyImpl implements InvocationHandler {

		protected ConcurrantCallable caller;
		private String concreteCanonicalName;

		public RendererProxyImpl(ConcurrantCallable caller, String concreteCanonicalName) {
			this.caller = caller;
			this.concreteCanonicalName = concreteCanonicalName;
		}

		@Override
		public Object invoke(Object proxy, Method method, Object[] args)
				throws Throwable {
			if (args == null)
				args = new Object[]{};
			
			RenderMessage rm =  null;
			//this is either template / or context
			if (args[0] instanceof String){
				rm = new RenderMessage(concreteCanonicalName, (String)args[0], (Map<String, ?>)args[1]);
			} else {
				rm = new RenderMessage(concreteCanonicalName, (Map<String, ?>)args[1]);
			}
//			List<Object> task_args = new ArrayList<>();
//			task_args.add(Arrays.asList(args));
			
			//RenderMessage rm = new RenderMessage(concreteCanonicalName, template, task_args[0])
			return caller.ask(rm);
			
		}
	}

	@SuppressWarnings("unchecked")
	public static <T> T get(Class<T> iface, Class<? extends IPersistanceAccessor> concrete, ConcurrantCallable caller) {
		return (T) Proxy.newProxyInstance(
				Renderers.class.getClassLoader(), new Class[] { iface },
				new RendererProxyImpl(caller, concrete.getCanonicalName()));
	}
	
	@SuppressWarnings("unchecked")
	public static IRenderer getDefault(ConcurrantCallable caller) {
		return (IRenderer) Proxy.newProxyInstance(
				Renderers.class.getClassLoader(), new Class[] { IRenderer.class },
				new RendererProxyImpl(caller, DefaultRenderer.class.getCanonicalName()));
	}

}

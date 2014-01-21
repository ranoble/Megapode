package com.gravspace.proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import scala.concurrent.Future;

import com.gravspace.abstractions.ConcurrantCallable;
import com.gravspace.abstractions.ICalculation;
import com.gravspace.abstractions.IComponent;
import com.gravspace.abstractions.IWidget;
import com.gravspace.messages.CalculationMessage;
import com.gravspace.messages.ComponentMessage;

public class Widgets {
	private static class ComponentProxyImpl implements InvocationHandler {

		protected ConcurrantCallable caller;
		protected String concreteCanonicalName;

		public ComponentProxyImpl(ConcurrantCallable caller, String concreteCanonicalName) {
			this.caller = caller;
			this.concreteCanonicalName = concreteCanonicalName;
		}

		@Override
		public Object invoke(Object proxy, Method method, Object[] args)
				throws Throwable {
			if (args == null)
				args = new Object[]{new Object[]{}};
			Object[] _arg = (Object[])args[0];
			List<Object> params = new ArrayList<Object>();
			for (Object arg: _arg){
				params.add(arg);
			}
			return caller.ask(new ComponentMessage(concreteCanonicalName, params));
		}
	}

	@SuppressWarnings("unchecked")
	public static IWidget get(Class<? extends IComponent> concrete, ConcurrantCallable caller) {
		return (IWidget)Proxy.newProxyInstance(
				Widgets.class.getClassLoader(), new Class[] { IWidget.class },
				new ComponentProxyImpl(caller, concrete.getCanonicalName()));
	}

}

package com.gravspace.proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import scala.concurrent.Future;

import akka.actor.ActorRef;
import akka.pattern.Patterns;

import com.gravspace.abstractions.ICalculation;
import com.gravspace.abstractions.IWidget;
import com.gravspace.abstractions.ISession;
import com.gravspace.abstractions.Widget;
import com.gravspace.bases.ConcurrantCallable;
import com.gravspace.messages.CalculationMessage;
import com.gravspace.messages.ComponentMessage;
import com.gravspace.messages.GetSessionVar;
import com.gravspace.messages.KillSession;
import com.gravspace.messages.SetSessionVar;

public class Sessions {
	private static class SessionProxyImpl implements InvocationHandler {

		protected ConcurrantCallable caller;
		protected ActorRef sessionRef;

		public SessionProxyImpl(ConcurrantCallable caller, ActorRef sessionRef) {
			this.caller = caller;
			this.sessionRef = sessionRef;
		}

		@Override
		public Object invoke(Object proxy, Method method, Object[] args)
				throws Throwable {
			System.out.println(method);
			if (method.getName().equals("get")){
				System.out.println("GET");
				return Patterns.ask(sessionRef, new GetSessionVar((String) args[0]), 
						10000);
				//sessionRef(msg, sender)GetSessionVar
			} else if (method.getName().equals("set")){
				System.out.println("SET");
				return Patterns.ask(sessionRef, new SetSessionVar((String) args[0], 
						args[1]), 10000);
			} else if (method.getName().equals("kill")){
				System.out.println("KILL");
//				sessionRef.tell(new KillSession(), noSender());
			} else if (method.equals("getSessionRef")){
				return sessionRef;
			}
			return null;
//			if (args == null)
//				args = new Object[]{new Object[]{}};
//			Object[] _arg = (Object[])args[0];
//			List<Object> params = new ArrayList<Object>();
//			for (Object arg: _arg){
//				params.add(arg);
//			}
//			return caller.ask(new ComponentMessage(concreteCanonicalName, params));
		}
	}

	@SuppressWarnings("unchecked")
	public static ISession get(ActorRef sessionRef, ConcurrantCallable caller) {
		return (ISession)Proxy.newProxyInstance(
				Sessions.class.getClassLoader(), new Class[] { ISession.class },
				new SessionProxyImpl(caller, sessionRef));
	}

}

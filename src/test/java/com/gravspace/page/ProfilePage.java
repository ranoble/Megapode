package com.gravspace.page;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;
import akka.actor.ActorRef;
import akka.actor.UntypedActorContext;
import akka.dispatch.Futures;

import com.gravspace.annotations.Page;
import com.gravspace.bases.PageBase;
import com.gravspace.impl.tasks.IProfileTask;
import com.gravspace.messages.CalculationMessage;
import com.gravspace.messages.PersistanceMessage;
import com.gravspace.messages.RenderMessage;
import com.gravspace.messages.TaskMessage;
import com.gravspace.proxy.DataAccessorProxyFactory;
import com.gravspace.proxy.TaskProxyFactory;
import com.gravspace.util.Layers;

@Page(path="/user/<user_id>/")
public class ProfilePage extends PageBase {
	
	public ProfilePage(Map<Layers, ActorRef> routers,
			ActorRef coordinatingActor, UntypedActorContext actorContext) {
		super(routers, coordinatingActor, actorContext);
	}

	String name = "The Megapode";
	private Future<Object> profileData;
	private Future<Object> calculationResult;

	Map<String, Object> profileContext;
	

	public void collect() {
		IProfileTask task = TaskProxyFactory.getProxy(IProfileTask.class, ProfileTask.class, this);
//		ProfileTasks task2 = TaskProxyFactory.getProxy(ProfileTask.class, this);
		task.logTask("Richard!");
//		call(new TaskMessage("simple", Arrays.asList(new Integer[]{1, 2})));
		IProfileDataAccessor dp = DataAccessorProxyFactory.getProxy(IProfileDataAccessor.class, ProfileDataAccessor.class, this);
		set("profileContext", dp.getUserProfile(1));//ask(new PersistanceMessage("doX", Arrays.asList(new Integer[]{1}))));
//		set("profileContext", profileData);
		getLogger().info("collected");
	}

	public void process() {
		
		// TODO Auto-generated method stub
		//calculationResult = ask(new CalculationMessage("simple", Arrays.asList(new Integer[]{1, 2})));
		getLogger().info((String) profileContext.get("firstname"));
		getLogger().info("processed");
	}

	public Future<String> render() throws Exception {
		
		getLogger().info("attempting to load");

		return Futures.successful("test again");
	}



}

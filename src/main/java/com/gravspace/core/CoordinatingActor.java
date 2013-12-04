package com.gravspace.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import scala.concurrent.Future;
import scala.concurrent.duration.Duration;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.dispatch.OnSuccess;
import akka.pattern.Patterns;
import akka.routing.SmallestMailboxRouter;
import akka.util.Timeout;

import com.gravspace.abstractions.Page;
import com.gravspace.abstractions.PageHandler;
import com.gravspace.messages.RequestMessage;
import com.gravspace.page.ProfilePage;

public class CoordinatingActor extends UntypedActor {
	private ActorRef pageRouter;
	private ActorRef componentRouter;
	private ActorRef taskRouter;
	private ActorRef calculationRouter;
	private ActorRef dataRequestRouter;
	
	
	public CoordinatingActor(){
		List<ActorRef> pageActors = new ArrayList<ActorRef>();
		Map<String, Class<? extends Page>> routers = new HashMap<String, Class<? extends Page>>();
		routers.put("*", ProfilePage.class);
		for (int i = 0; i < 5; i++){
			pageActors.add(this.getContext().actorOf(Props.create(PageHandler.class, routers)));
		}
		pageRouter = this.getContext().actorOf(
				  Props.empty().withRouter(SmallestMailboxRouter.create(pageActors)));
	}
	
	@Override
	public void onReceive(Object message) throws Exception {
		if (message instanceof RequestMessage){
			Timeout timeout = new Timeout(Duration.create(1, "minute"));
			Future<Object> future = Patterns.ask(pageRouter, message, timeout);
			akka.pattern.Patterns.pipe(future, this.getContext().dispatcher()).to(getSender());

		} else {
			unhandled(message);
		}

	}

}

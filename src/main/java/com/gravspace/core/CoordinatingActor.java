package com.gravspace.core;

import java.util.ArrayList;

import akka.event.Logging;
import akka.event.LoggingAdapter;

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
import com.gravspace.abstractions.Renderer;
import com.gravspace.handlers.PageHandler;
import com.gravspace.handlers.RendererHandler;
import com.gravspace.messages.RenderMessage;
import com.gravspace.messages.RequestMessage;
import com.gravspace.page.ProfilePage;
import com.gravspace.page.ProfileRenderer;
import com.gravspace.util.Layers;

public class CoordinatingActor extends UntypedActor {
	LoggingAdapter log = Logging.getLogger(getContext().system(), this);
	private HashMap<Layers, ActorRef> routerMap;
	
	
	public CoordinatingActor(){
		routerMap = new HashMap<Layers, ActorRef>();
		routerMap.put(Layers.PAGE, generatePageRouter());
		routerMap.put(Layers.RENDERER, generateRenderRouter());
	}

	private ActorRef generatePageRouter() {
		List<ActorRef> pageActors = new ArrayList<ActorRef>();
		Map<String, Class<? extends Page>> routers = new HashMap<String, Class<? extends Page>>();
		routers.put("*", ProfilePage.class);
		for (int i = 0; i < 5; i++){
			pageActors.add(this.getContext().actorOf(Props.create(PageHandler.class, routerMap, routers), "PageHandler-"+i));
		}
		return this.getContext().actorOf(
				  Props.empty().withRouter(SmallestMailboxRouter.create(pageActors)));
	}
	
	private ActorRef generateRenderRouter() {
		List<ActorRef> renderers = new ArrayList<ActorRef>();
		Map<String, Class<? extends Renderer>> routers = new HashMap<String, Class<? extends Renderer>>();
		routers.put("hi_pode.vm", ProfileRenderer.class);
		for (int i = 0; i < 5; i++){
			renderers.add(this.getContext().actorOf(Props.create(RendererHandler.class, routerMap, routers), "RenderHandler-"+i));
		}
		return  this.getContext().actorOf(
				  Props.empty().withRouter(SmallestMailboxRouter.create(renderers)));
	}
	
	@Override
	public void onReceive(Object message) throws Exception {
		log.info("Got "+message.getClass().getCanonicalName());
		if (message instanceof RequestMessage){
			log.info("Got RequestMessage");
			Timeout timeout = new Timeout(Duration.create(1, "minute"));
			Future<Object> future = Patterns.ask(routerMap.get(Layers.PAGE), message, timeout);
			akka.pattern.Patterns.pipe(future, this.getContext().dispatcher()).to(getSender());
		} else {
			unhandled(message);
		}

	}

}

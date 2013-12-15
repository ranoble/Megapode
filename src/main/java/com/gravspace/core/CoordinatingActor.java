package com.gravspace.core;

import java.util.ArrayList;

import akka.event.Logging;
import akka.event.LoggingAdapter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import scala.concurrent.Future;
import scala.concurrent.duration.Duration;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.dispatch.OnSuccess;
import akka.pattern.Patterns;
import akka.routing.SmallestMailboxRouter;
import akka.util.Timeout;

import com.gravspace.abstractions.Calculation;
import com.gravspace.abstractions.Page;
import com.gravspace.abstractions.PageRoute;
import com.gravspace.abstractions.PersistanceAccessor;
import com.gravspace.abstractions.Renderer;
import com.gravspace.abstractions.Task;
import com.gravspace.handlers.CalculationHandler;
import com.gravspace.handlers.PageHandler;
import com.gravspace.handlers.PersistanceHandler;
import com.gravspace.handlers.RendererHandler;
import com.gravspace.handlers.TaskHandler;
import com.gravspace.messages.RenderMessage;
import com.gravspace.messages.RequestMessage;
import com.gravspace.page.GetProfileData;
import com.gravspace.page.ProfileCalculation;
import com.gravspace.page.ProfilePage;
import com.gravspace.page.ProfileRenderer;
import com.gravspace.page.ProfileTask;
import com.gravspace.util.Layers;

public class CoordinatingActor extends UntypedActor {
	LoggingAdapter log = Logging.getLogger(getContext().system(), this);
	private HashMap<Layers, ActorRef> routerMap;
	
	
	public CoordinatingActor(){
		routerMap = new HashMap<Layers, ActorRef>();
//		routerMap.put(Layers.ROUTING, generateUrlRouter());
		routerMap.put(Layers.PAGE, generatePageRouter());
		routerMap.put(Layers.RENDERER, generateRenderRouter());
		routerMap.put(Layers.TASK, generateTaskRouter());
		routerMap.put(Layers.CALCULATION, generateCalculationRouter());
		routerMap.put(Layers.DATA_ACCESS, generateDataRouter());
	}

//	private ActorRef generateUrlRouter() {
////		List<ActorRef> routingActor = new ArrayList<ActorRef>();
////		Map<String, Class<? extends Page>> routers = new HashMap<String, Class<? extends Page>>();
////		for (int i = 0; i < 5; i++){
////			routingActor.add(this.getContext().actorOf(Props.create(PageHandler.class, urlMap, routers), "PageHandler-"+i));
////		}
////		return this.getContext().actorOf(
////				  Props.empty().withRouter(SmallestMailboxRouter.create(routingActor)));
//		return null;
//	}

	private ActorRef generatePageRouter() {
		List<ActorRef> pageActors = new ArrayList<ActorRef>();
		List<PageRoute> routers = new ArrayList<PageRoute>();
		routers.add(new PageRoute("/", ProfilePage.class));
		for (int i = 0; i < 5; i++){
			pageActors.add(this.getContext().actorOf(Props.create(PageHandler.class, routerMap, routers), "PageHandler-"+i));
		}
		return this.getContext().actorOf(
				  Props.empty().withRouter(SmallestMailboxRouter.create(pageActors)));
	}
	
	private ActorRef generateDataRouter() {
		Properties p = new Properties();
		p.setProperty("user", "postgres");
		p.setProperty("password", "postgres");
		p.setProperty("url", "jdbc:postgresql://localhost/megapode_test");
		List<ActorRef> dataActors = new ArrayList<ActorRef>();
		Map<String, Class<? extends PersistanceAccessor>> routers = new HashMap<String, Class<? extends PersistanceAccessor>>();
		routers.put("doX", GetProfileData.class);
		for (int i = 0; i < 5; i++){
			dataActors.add(this.getContext().actorOf(Props.create(PersistanceHandler.class, routerMap, routers, p), "DataHandler-"+i));
		}
		return this.getContext().actorOf(
				  Props.empty().withRouter(SmallestMailboxRouter.create(dataActors)));
	}
	
	private ActorRef generateTaskRouter() {
		List<ActorRef> taskActors = new ArrayList<ActorRef>();
		Map<String, Class<? extends Task>> routers = new HashMap<String, Class<? extends Task>>();
		routers.put("simple", ProfileTask.class);
		for (int i = 0; i < 5; i++){
			taskActors.add(this.getContext().actorOf(Props.create(TaskHandler.class, routerMap, routers), "TaskHandler-"+i));
		}
		return this.getContext().actorOf(
				  Props.empty().withRouter(SmallestMailboxRouter.create(taskActors)));
	}
	
	private ActorRef generateCalculationRouter() {
		List<ActorRef> calcActors = new ArrayList<ActorRef>();
		Map<String, Class<? extends Calculation>> routers = new HashMap<String, Class<? extends Calculation>>();
		routers.put("simple", ProfileCalculation.class);
		for (int i = 0; i < 5; i++){
			calcActors.add(this.getContext().actorOf(Props.create(CalculationHandler.class, routerMap, routers), "CalculationHandler-"+i));
		}
		return this.getContext().actorOf(
				  Props.empty().withRouter(SmallestMailboxRouter.create(calcActors)));
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

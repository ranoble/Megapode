package com.gravspace.core;

import java.util.ArrayList;

import akka.event.Logging;
import akka.event.LoggingAdapter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;

import scala.concurrent.Future;
import scala.concurrent.duration.Duration;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.dispatch.OnSuccess;
import akka.pattern.Patterns;
import akka.routing.SmallestMailboxRouter;
import akka.util.Timeout;

import com.gravspace.abstractions.ICalculation;
import com.gravspace.abstractions.IPage;
import com.gravspace.abstractions.PageRoute;
import com.gravspace.abstractions.IPersistanceAccessor;
import com.gravspace.abstractions.IRenderer;
import com.gravspace.abstractions.ITask;
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
	
	
	public CoordinatingActor(Properties config){
		
		routerMap = new HashMap<Layers, ActorRef>();
		routerMap.put(Layers.PAGE, generatePageRouter(
				Integer.parseInt((String) config.getProperty("pages", "5"))));
		routerMap.put(Layers.RENDERER, generateRenderRouter(
				Integer.parseInt((String) config.getProperty("renders", "5"))));
		routerMap.put(Layers.TASK, generateTaskRouter(
				Integer.parseInt((String) config.getProperty("tasks", "5")), 
				getTaskPackages(config)));
		routerMap.put(Layers.CALCULATION, generateCalculationRouter(
				Integer.parseInt((String) config.getProperty("calculations", "5"))));
		routerMap.put(Layers.DATA_ACCESS, generateDataRouter(
				Integer.parseInt((String) config.getProperty("dataaccesors", "5")), config));
	}



	private List<String> getTaskPackages(Properties config) {
		
		String property = "scan-tasks";
		List<String> taskPackages = extractPackagesFromPropertyString(config,
				property);
		taskPackages.addAll(extractPackagesFromPropertyString(config,
				"scan-all"));
		return taskPackages;
	}



	private List<String> extractPackagesFromPropertyString(Properties config,
			String property) {
		List<String> pkgs = new ArrayList<>();
		String scanString = config.getProperty(property, "");
		if (!StringUtils.isEmpty(scanString.trim())){
			String[] packages = scanString.split(",");
			for (String pkg: packages){
				pkgs.add(pkg);
			}
		}
		return pkgs;
	}



	private ActorRef generatePageRouter(int actors) {
		List<ActorRef> pageActors = new ArrayList<ActorRef>();
		List<PageRoute> routers = new ArrayList<PageRoute>();
		routers.add(new PageRoute("/test/{value}/", ProfilePage.class));
		for (int i = 0; i < actors; i++){
			pageActors.add(this.getContext().actorOf(Props.create(PageHandler.class, routerMap, routers), "PageHandler-"+i));
		}
		return this.getContext().actorOf(
				  Props.empty().withRouter(SmallestMailboxRouter.create(pageActors)));
	}
	
	private ActorRef generateDataRouter(int actors, Properties config) {
//		
		List<ActorRef> dataActors = new ArrayList<ActorRef>();
		Map<String, Class<? extends IPersistanceAccessor>> routers = new HashMap<String, Class<? extends IPersistanceAccessor>>();
		routers.put("doX", GetProfileData.class);
		for (int i = 0; i < 5; i++){
			dataActors.add(this.getContext().actorOf(Props.create(PersistanceHandler.class, routerMap, routers, config), "DataHandler-"+i));
		}
		return this.getContext().actorOf(
				  Props.empty().withRouter(SmallestMailboxRouter.create(dataActors)));
	}
	
	private ActorRef generateTaskRouter(int actors, List<String> taskPackages) {
		List<ActorRef> taskActors = new ArrayList<ActorRef>();
		Map<String, Class<? extends ITask>> routers = new HashMap<String, Class<? extends ITask>>();
		List<Class<? extends ITask>> tasks = AnnotationParser.getAnnotatedTasks(taskPackages);
		for (Class<? extends ITask> task: tasks){
			log.info(String.format("Registering task: [%s]", task.getCanonicalName()));
			routers.put(task.getCanonicalName(), task);
		}
		for (int i = 0; i < 5; i++){
			taskActors.add(this.getContext().actorOf(Props.create(TaskHandler.class, routerMap, routers), "TaskHandler-"+i));
		}
		return this.getContext().actorOf(
				  Props.empty().withRouter(SmallestMailboxRouter.create(taskActors)));
	}
	
	private ActorRef generateCalculationRouter(int actors) {
		List<ActorRef> calcActors = new ArrayList<ActorRef>();
		Map<String, Class<? extends ICalculation>> routers = new HashMap<String, Class<? extends ICalculation>>();
		routers.put("simple", ProfileCalculation.class);
		for (int i = 0; i < 5; i++){
			calcActors.add(this.getContext().actorOf(Props.create(CalculationHandler.class, routerMap, routers), "CalculationHandler-"+i));
		}
		return this.getContext().actorOf(
				  Props.empty().withRouter(SmallestMailboxRouter.create(calcActors)));
	}
	
	private ActorRef generateRenderRouter(int actors) {
		List<ActorRef> renderers = new ArrayList<ActorRef>();
		Map<String, Class<? extends IRenderer>> routers = new HashMap<String, Class<? extends IRenderer>>();
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

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
import com.gravspace.abstractions.IWidget;
import com.gravspace.abstractions.IPage;
import com.gravspace.abstractions.PageRoute;
import com.gravspace.abstractions.IDataAccessor;
import com.gravspace.abstractions.IRenderer;
import com.gravspace.abstractions.ITask;
import com.gravspace.handlers.CalculationHandler;
import com.gravspace.handlers.WidgetHandler;
import com.gravspace.handlers.PageHandler;
import com.gravspace.handlers.PersistanceHandler;
import com.gravspace.handlers.RendererHandler;
import com.gravspace.handlers.SessionHandler;
import com.gravspace.handlers.TaskHandler;
import com.gravspace.messages.RenderMessage;
import com.gravspace.messages.RequestMessage;
import com.gravspace.messages.RouterMessage;
import com.gravspace.messages.RouterResponseMessage;
import com.gravspace.page.ProfileDataAccessor;
import com.gravspace.page.ProfileCalculation;
import com.gravspace.page.ProfilePage;
import com.gravspace.page.ProfileRenderer;
import com.gravspace.page.ProfileTask;
import com.gravspace.util.Layers;

public class CoordinatingActor extends UntypedActor {
	LoggingAdapter log = Logging.getLogger(getContext().system(), this);
	protected HashMap<Layers, ActorRef> routerMap;
	
	
	public CoordinatingActor(Properties config){
		
		routerMap = new HashMap<Layers, ActorRef>();
		routerMap.put(Layers.PAGE, generatePageRouter(
				Integer.parseInt((String) config.getProperty("pages", "5")),
				getPagePackages(config)));
		routerMap.put(Layers.COMPONENT, generateComponentRouter(
				Integer.parseInt((String) config.getProperty("widgets", "5")),
				getWidgetPackages(config)));
		routerMap.put(Layers.RENDERER, generateRenderRouter(
				Integer.parseInt((String) config.getProperty("renders", "5")),
				getRenderPackages(config)));
		routerMap.put(Layers.TASK, generateTaskRouter(
				Integer.parseInt((String) config.getProperty("tasks", "5")), 
				getTaskPackages(config)));
		routerMap.put(Layers.CALCULATION, generateCalculationRouter(
				Integer.parseInt((String) config.getProperty("calculations", "5")),
				getCalculationPackages(config)));
		routerMap.put(Layers.DATA_ACCESS, generateDataRouter(
				Integer.parseInt((String) config.getProperty("dataaccesors", "5")), 
				config,
				getAccessorPackages(config)));
		routerMap.put(Layers.SESSION, this.getContext().actorOf(Props.create(SessionHandler.class)));
	}



	private List<String> getTaskPackages(Properties config) {
		String property = "scan-tasks";
		return getAnnotatedPackages(config, property);
	}
	
	private List<String> getPagePackages(Properties config) {
		String property = "scan-pages";
		return getAnnotatedPackages(config, property);
	}
	
	private List<String> getCalculationPackages(Properties config) {
		
		String property = "scan-calculations";
		List<String> packages = getAnnotatedPackages(config, property);
		packages.add("com.gravspace.calculation.form");
		return packages;
	}

	private List<String> getAccessorPackages(Properties config) {
		
		String property = "scan-dataaccessors";
		return getAnnotatedPackages(config, property);
	}
	
	private List<String> getRenderPackages(Properties config) {
		
		String property = "scan-renderers";
		List<String> packages = getAnnotatedPackages(config, property);
		packages.add("com.gravspace.defaults");
		return packages;
	}
	
	private List<String> getWidgetPackages(Properties config) {
		String property = "scan-widgets";
		return getAnnotatedPackages(config, property);
	}
	
	

	private List<String> getAnnotatedPackages(Properties config, String property) {
		List<String> pkgs = extractPackagesFromPropertyString(config,
				property);
		pkgs.addAll(extractPackagesFromPropertyString(config,
				"scan-all"));
		return pkgs;
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

	private ActorRef generatePageRouter(int actors, List<String> list) {
		List<ActorRef> pageActors = new ArrayList<ActorRef>();
//		List<PageRoute> routers = new ArrayList<PageRoute>();
		List<PageRoute> routers = AnnotationParser.getAnnotatedPages(list);
		for (PageRoute route: routers){
			log.info(String.format("Registering pages: [%s] [%s]", route.getTemplate().toString(), route.getPageClass().getCanonicalName()));
		}
//		routers.add(new PageRoute("/test/{value}/", ProfilePage.class));
		for (int i = 0; i < actors; i++){
			pageActors.add(this.getContext().actorOf(Props.create(PageHandler.class, routerMap, routers), "PageHandler-"+i));
		}
		return this.getContext().actorOf(
				  Props.empty().withRouter(SmallestMailboxRouter.create(pageActors)));
	}
	
	private ActorRef generateDataRouter(int actors, Properties config, List<String> dataPackages) {	
		List<ActorRef> dataActors = new ArrayList<ActorRef>();
		Map<String, Class<? extends IDataAccessor>> routers = new HashMap<String, Class<? extends IDataAccessor>>();
		List<Class<? extends IDataAccessor>> accessors = AnnotationParser.getAnnotatedDataAccessors(dataPackages);
		for (Class<? extends IDataAccessor> accessor: accessors){
			log.info(String.format("Registering accessor: [%s]", accessor.getCanonicalName()));
			routers.put(accessor.getCanonicalName(), accessor);
		}
		for (int i = 0; i < actors; i++){
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
		for (int i = 0; i < actors; i++){
			taskActors.add(this.getContext().actorOf(Props.create(TaskHandler.class, routerMap, routers), "TaskHandler-"+i));
		}
		return this.getContext().actorOf(
				  Props.empty().withRouter(SmallestMailboxRouter.create(taskActors)));
	}
	
	private ActorRef generateCalculationRouter(int actors, List<String> calcPackages) {
		List<ActorRef> calcActors = new ArrayList<ActorRef>();
		Map<String, Class<? extends ICalculation>> routers = new HashMap<String, Class<? extends ICalculation>>();
		List<Class<? extends ICalculation>> calculations = AnnotationParser.getAnnotatedCalculations(calcPackages);
		for (Class<? extends ICalculation> calculation: calculations){
			log.info(String.format("Registering calculation: [%s]", calculation.getCanonicalName()));
			routers.put(calculation.getCanonicalName(), calculation);
		}
		for (int i = 0; i < actors; i++){
			calcActors.add(this.getContext().actorOf(Props.create(CalculationHandler.class, routerMap, routers), "CalculationHandler-"+i));
		}
		return this.getContext().actorOf(
				  Props.empty().withRouter(SmallestMailboxRouter.create(calcActors)));
	}
	
	private ActorRef generateComponentRouter(int actors, List<String> calcPackages) {
		List<ActorRef> widgetActors = new ArrayList<ActorRef>();
		Map<String, Class<? extends IWidget>> routers = new HashMap<String, Class<? extends IWidget>>();
		List<Class<? extends IWidget>> widgets = AnnotationParser.getAnnotatedWidgets(calcPackages);
		for (Class<? extends IWidget> widget: widgets){
			log.info(String.format("Registering widget: [%s]", widget.getCanonicalName()));
			routers.put(widget.getCanonicalName(), widget);
		}
		for (int i = 0; i < actors; i++){
			widgetActors.add(this.getContext().actorOf(Props.create(WidgetHandler.class, routerMap, routers), "WidgetHandler-"+i));
		}
		return this.getContext().actorOf(
				  Props.empty().withRouter(SmallestMailboxRouter.create(widgetActors)));
	}
	
	
	private ActorRef generateRenderRouter(int actors, List<String> renderPackages) {
		List<ActorRef> renderers = new ArrayList<ActorRef>();
		
		Map<String, Class<? extends IRenderer>> routers  = new HashMap<String, Class<? extends IRenderer>>();

		List<Class<? extends IRenderer>> renders = AnnotationParser.getAnnotatedRenderers(renderPackages);
		for (Class<? extends IRenderer> renderer: renders){
			log.info(String.format("Registering renderer: [%s]", renderer.getCanonicalName()));
			routers.put(renderer.getCanonicalName(), renderer);
		}
		for (int i = 0; i < actors; i++){
			renderers.add(this.getContext().actorOf(Props.create(RendererHandler.class, routerMap, routers), "RenderHandler-"+i));
		}
		return this.getContext().actorOf(
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

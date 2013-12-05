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
import com.gravspace.abstractions.PageHandler;
import com.gravspace.abstractions.Renderer;
import com.gravspace.abstractions.RendererHandler;
import com.gravspace.messages.RenderMessage;
import com.gravspace.messages.RequestMessage;
import com.gravspace.page.ProfilePage;
import com.gravspace.page.ProfileRenderer;

public class CoordinatingActor extends UntypedActor {
	LoggingAdapter log = Logging.getLogger(getContext().system(), this);
	private ActorRef pageRouter;
	private ActorRef componentRouter;
	private ActorRef renderRouter;
	private ActorRef taskRouter;
	private ActorRef calculationRouter;
	private ActorRef dataRequestRouter;
	
	
	public CoordinatingActor(){
		generatePageRouter();
		generateRenderRouter();
	}

	private void generatePageRouter() {
		List<ActorRef> pageActors = new ArrayList<ActorRef>();
		Map<String, Class<? extends Page>> routers = new HashMap<String, Class<? extends Page>>();
		routers.put("*", ProfilePage.class);
		for (int i = 0; i < 5; i++){
			pageActors.add(this.getContext().actorOf(Props.create(PageHandler.class, routers)));
		}
		pageRouter = this.getContext().actorOf(
				  Props.empty().withRouter(SmallestMailboxRouter.create(pageActors)));
	}
	
	private void generateRenderRouter() {
		List<ActorRef> renderers = new ArrayList<ActorRef>();
		Map<String, Class<? extends Renderer>> routers = new HashMap<String, Class<? extends Renderer>>();
		routers.put("*", ProfileRenderer.class);
		for (int i = 0; i < 5; i++){
			renderers.add(this.getContext().actorOf(Props.create(RendererHandler.class, routers)));
		}
		renderRouter = this.getContext().actorOf(
				  Props.empty().withRouter(SmallestMailboxRouter.create(renderers)));
	}
	
	@Override
	public void onReceive(Object message) throws Exception {
		log.info("Got "+message.getClass().getCanonicalName());
		if (message instanceof RequestMessage){
			log.info("Got RequestMessage");
			Timeout timeout = new Timeout(Duration.create(1, "minute"));
			Future<Object> future = Patterns.ask(pageRouter, message, timeout);
			akka.pattern.Patterns.pipe(future, this.getContext().dispatcher()).to(getSender());

		} else if (message instanceof RenderMessage){
			log.info("Got RenderMessage");
			Timeout timeout = new Timeout(Duration.create(1, "minute"));
			Future<Object> future = Patterns.ask(renderRouter, message, timeout);
			akka.pattern.Patterns.pipe(future, this.getContext().dispatcher()).to(getSender());
		} else {
			unhandled(message);
		}

	}

}

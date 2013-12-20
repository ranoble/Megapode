package com.gravspace.handlers;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.protocol.HttpContext;

import akka.actor.ActorRef;
import akka.actor.Status;
import akka.actor.UntypedActor;
import akka.actor.UntypedActorContext;
import akka.event.Logging;
import akka.event.LoggingAdapter;

import com.gravspace.abstractions.Page;
import com.gravspace.abstractions.PageRoute;
import com.gravspace.exceptions.PageNotFoundException;
import com.gravspace.messages.RequestMessage;
import com.gravspace.util.Layers;

public class PageHandler extends UntypedActor {
	LoggingAdapter log = Logging.getLogger(getContext().system(), this);
	List<PageRoute> pages;
	Map<String, ActorRef> routes;
	private Map<Layers, ActorRef> routers;
	
	public PageHandler(Map<Layers, ActorRef> routers, List<PageRoute> pages){
		this.pages = pages;
		this.routers = routers;
	}
	
	public Page loadPage(String uri, HttpRequest request, HttpResponse response, HttpContext context) throws PageNotFoundException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
		Map<String, String> parameters = new HashMap<String, String>();
		for (PageRoute route: pages){
			if (route.getTemplate().match(uri, parameters)){
				Page page = route.getPageClass().getConstructor(Map.class, ActorRef.class, UntypedActorContext.class).newInstance(routers, getSender(), this.context());
				page.initialise(request, 
						response, 
						context,
						parameters
						);
				return page;
			}
		}
		
		throw new PageNotFoundException(String.format("Page matching [%s] not found", uri));
	}

	@Override
	public void onReceive(Object rawMessage) throws Exception {
		log.info("Page got: "+rawMessage.getClass().getCanonicalName());
		if (rawMessage instanceof RequestMessage){
			log.info("Handelling Request");
			RequestMessage message = (RequestMessage)rawMessage;
			HttpRequest request = message.getPayload().getRequest();
			String uri = request.getRequestLine().getUri();
			try {
				Page page = loadPage(uri, request, message.getPayload().getResponse(), message.getPayload().getContext());
				String rendered = processPage(page);
				getSender().tell(rendered, getSelf());
			} catch (PageNotFoundException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e){
//				log.error(String.format("Error in handelling [%s]", e.getClass().getCanonicalName()), e);
				getSender().tell(new Status.Failure(e), getSelf());
			}
			
		} 
		else {
			unhandled(rawMessage);
		}
	}

	private String processPage(Page page) throws Exception {
		page.collect();
		page.await();
		page.process();
		String rendered = page.render();
		return rendered;
	}

}

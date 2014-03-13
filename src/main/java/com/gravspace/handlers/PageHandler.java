package com.gravspace.handlers;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.protocol.HttpContext;

import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;

import akka.actor.ActorRef;
import akka.actor.Status;
import akka.actor.UntypedActor;
import akka.actor.UntypedActorContext;
import akka.dispatch.Futures;
import akka.event.Logging;
import akka.event.LoggingAdapter;

import com.gravspace.abstractions.IWidget;
import com.gravspace.abstractions.IPage;
import com.gravspace.abstractions.PageRoute;
import com.gravspace.exceptions.PageNotFoundException;
import com.gravspace.messages.RequestMessage;
import com.gravspace.messages.ResponseMessage;
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
	


	@Override
	public void onReceive(Object rawMessage) throws Exception {
		log.info("Page got: "+rawMessage.getClass().getCanonicalName());
		if (rawMessage instanceof RequestMessage){
			log.info("Handelling Request");
			RequestMessage message = (RequestMessage)rawMessage;
			String uri = message.getPayload().getRequestLine().getUri();
			String[] split = StringUtils.split(uri, "?", 2);
			
			String path = split[0];
			String query = "";
			if (split.length > 1){
				query = split[1];
			}

			Future<ResponseMessage> rendered = null;
			try {
				IPage page = loadPage(path, message.getPayload().getRequestLine().getMethod().toUpperCase(), query, message.getPayload().getHeaders(), message.getPayload().getContent());
				rendered = page.build();
			} catch (PageNotFoundException pnf){
				rendered = Futures.failed(pnf);
			} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e){
				log.error(String.format("Error in handelling [%s]", e.getClass().getCanonicalName()), e);
				rendered = Futures.failed(e);
			} catch (Throwable e) {
				rendered = Futures.failed(e);
			}
			akka.pattern.Patterns.pipe(rendered, this.getContext().dispatcher()).to(getSender());
		} 
		else {
			unhandled(rawMessage);
		}
	}
	
	private IPage loadPage(String path, String method, String query, Header[] headers, byte[] content) throws PageNotFoundException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
		Map<String, String> parameters = new HashMap<String, String>();
		for (PageRoute route: pages){
			if (route.getTemplate().match(path, parameters)){
				IPage page = route.getPageClass().getConstructor(Map.class, ActorRef.class, UntypedActorContext.class).newInstance(routers, getSender(), this.context());
				page.initialise(path, 
						method,
						query,
						headers, 
						content,
						parameters
						);
				return page;
			}
		}
		
		throw new PageNotFoundException(String.format("Page matching [%s] not found", path));
	}

	

}

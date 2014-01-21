package com.gravspace.calculation.form;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.http.Header;

import scala.concurrent.Future;
import akka.actor.ActorRef;
import akka.actor.UntypedActorContext;
import akka.dispatch.Futures;

import com.gravspace.abstractions.ICalculation;
import com.gravspace.annotations.Calculation;
import com.gravspace.bases.CalculationBase;
import com.gravspace.util.Layers;

@Calculation
public class CookieParser extends CalculationBase implements ICalculation, ICookieParser {

	public CookieParser(Map<Layers, ActorRef> routers,
			ActorRef coordinatingActor, UntypedActorContext actorContext) {
		super(routers, coordinatingActor, actorContext);
	}

	@Override
	public Future<Map<String, String>> parseCookies(Header[] headers) {
		for (Header header: headers){
			if (StringUtils.equalsIgnoreCase(header.getName(), "cookie")){
				return Futures.successful(parseCookies(header.getValue()));
			}
		}
		Map<String, String> cookies = new HashMap<String, String>();
		return Futures.successful(cookies);
	}
	
	public Map<String, String> parseCookies(String cookiePayload){
		Map<String, String> cookies = new HashMap<String, String>();
		String[] cookieStrings = StringUtils.split(cookiePayload, ";");
		for (String cookie: cookieStrings){
			String[] cookieValues = StringUtils.split(cookie.trim(), "=", 2);
			try {
				cookies.put(URLDecoder.decode(cookieValues[0].trim(), "UTF-8"), 
						URLDecoder.decode(cookieValues[1].trim(), "UTF-8"));
			} catch (UnsupportedEncodingException e) {}
		}
		return cookies;
	}

}

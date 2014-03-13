package com.gravspace.bases;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.http.Header;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.entity.ContentType;
import org.apache.http.message.BasicHeader;

import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.Promise;
import scala.concurrent.duration.Duration;
import akka.actor.ActorRef;
import akka.actor.UntypedActorContext;
import akka.dispatch.Futures;
import akka.dispatch.OnComplete;
import akka.pattern.Patterns;
import akka.util.Timeout;

import com.gravspace.abstractions.IPage;
import com.gravspace.abstractions.ISession;
import com.gravspace.calculation.form.CookieParser;
import com.gravspace.calculation.form.FormParser;
import com.gravspace.calculation.form.ICookieParser;
import com.gravspace.calculation.form.IFormParser;
import com.gravspace.exceptions.Redirect;
import com.gravspace.messages.GetSession;
import com.gravspace.messages.ResponseMessage;
import com.gravspace.proxy.Calculations;
import com.gravspace.proxy.Sessions;
import com.gravspace.util.Cookie;
import com.gravspace.util.Layers;



public abstract class PageBase  extends WidgetBase implements IPage {
	private static final String MEGAPODE_SESSION_ID_KEY = "MegapodeSessionId";
	protected Map<String, String> params;
	protected String method;
	protected String path;
	protected Header[] requestHeaders;
	protected Map<String, String> requestCookies;
	protected List<NameValuePair> form;
	protected byte[] requestContent;
	protected String query; 
	
	protected HttpStatus status;
	protected List<Header> responseHeaders = new ArrayList<Header>();
	protected List<Cookie> responseCookies = new ArrayList<Cookie>();
	protected String contentType = "text/html";
	
	protected ISession session;
	
	public PageBase(final Map<Layers, ActorRef> routers, final ActorRef coordinatingActor, final UntypedActorContext actorContext){
		super(routers, coordinatingActor, actorContext);
	}
	
	public void initialise(String path,
			String method,
			String query, 
            Header[] headers,
            byte[] content,
            Map<String, String> params) {
		this.path = path;
		this.requestHeaders = headers;
		this.method = method;
		this.params = params;
		this.query = query;
		this.requestContent = content;
		
		runPreflightSetup();
	}
	
	public void runPreflightSetup(){
		final Promise<Object> delay = delayUntilComplete();
		ICookieParser cookieParser = Calculations.get(ICookieParser.class, CookieParser.class, this);
		Future<Map<String, String>> parser = cookieParser.parseCookies(requestHeaders);
		set("requestCookies", parser);
		parser.onComplete(new OnComplete<Map<String, String>>(){

			@Override
			public void onComplete(Throwable exception, Map<String, String> cookies)
					throws Throwable {
				if (exception != null){
					delay.failure(exception);
				} else {
					String sessionId = cookies.get(MEGAPODE_SESSION_ID_KEY);
					if (sessionId == null){
						sessionId = UUID.randomUUID().toString();
						addCookie(new Cookie(MEGAPODE_SESSION_ID_KEY, sessionId));
					}
					Timeout timeout = new Timeout(Duration.create(1, "minute"));
					Future<Object> sessionActor = Patterns.ask(routers.get(Layers.SESSION), 
							new GetSession(sessionId), timeout);

//					Future<Object> f = new When(actorContext.dispatcher(), sessionActor){
//
//						@Override
//						public Future<?> finishes(Throwable exception,
//								Object... params) {
//							// TODO Auto-generated method stub
//							return null;
//						}
//						
//					}.itWill();
					
					
					
					sessionActor.onComplete(new OnComplete<Object>(){
						@Override
						public void onComplete(Throwable exception, Object actorRef)
								throws Throwable {
							if (exception != null){
								delay.failure(exception);	
							} else {
								ActorRef sessionRef = (ActorRef)actorRef;
								session = Sessions.get(sessionRef, getThis());
								delay.success(session);
							}
						}
						
					}, actorContext.dispatcher());
				}
			}

		}, actorContext.dispatcher());
		
		if (hasForm()){
			IFormParser formParser = Calculations.get(IFormParser.class, FormParser.class, this);
			set("form", formParser.parse(requestHeaders, requestContent));
		}

	}
	
	public void collect(){
		System.out.println("Got ");
	}
	
	public abstract void process();
	
	public abstract Future<String> render() throws Exception;
	
	public Future<ResponseMessage> build() throws Exception {
		final Promise<ResponseMessage> pageRendered = Futures.promise();
		final ResponseMessage response = new ResponseMessage();
		try {
			Await.result(await(), Duration.create(1, "minute"));
			collect();
			Await.result(await(), Duration.create(1, "minute"));
			process();
			Await.result(await(), Duration.create(1, "minute"));
			Future<String> rendered = render();
			response.setContentType(contentType);
			List<Header> headers = getCookiesAndHeaders();
			response.setHeaders(headers);
			String result = (String) Await.result(rendered, Duration.create(1, "minute"));
			response.setStatus(HttpStatus.SC_OK);
			response.setResponseContent(
					result.getBytes(Charset.forName("UTF-8")));
			pageRendered.success(response);
		} catch (Redirect redirect){
			response.setStatus(redirect.getCode());
			response.setContentType(ContentType.TEXT_HTML.getMimeType());
			response.getHeaders().add(new BasicHeader("Location", redirect.getUri().toString()));
			response.setResponseContent(
					"redirected".getBytes(Charset.forName("UTF-8")));
			pageRendered.success(response);
		} catch (Exception e){
			pageRendered.failure(e);
		}
		return pageRendered.future();
	}

	private List<Header> getCookiesAndHeaders() {
		List<Header> headers = new ArrayList<>();
		headers.addAll(responseHeaders);
		for (Cookie cookie: responseCookies){
			headers.add(new BasicHeader("Set-Cookie", cookie.toString()));
		}
		return headers;
	}
	
	private boolean hasForm() {
		return (((requestContent != null) && 
				((method.equals("POST")) || (method.equals("PUT")))));
	}

	public void initialise(Object... args){}

	public List<NameValuePair> getForm() {
		return form;
	}

	public void setForm(List<NameValuePair> form) {
		this.form = form;
	}

	public HttpStatus getStatus() {
		return status;
	}

	public void setStatus(HttpStatus status) {
		this.status = status;
	}

	public List<Header> getResponseHeaders() {
		return responseHeaders;
	}

	public void setResponseHeaders(List<Header> responseHeaders) {
		this.responseHeaders = responseHeaders;
	}

	public List<Cookie> getResponseCookies() {
		return responseCookies;
	}

	public void setResponseCookies(List<Cookie> responseCookies) {
		this.responseCookies = responseCookies;
	}

	public String getContentType() {
		return contentType;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	public Map<String, String> getParams() {
		return params;
	}

	public String getMethod() {
		return method;
	}

	public String getPath() {
		return path;
	}
	
	public void addCookie(Cookie cookie){
		responseCookies.add(cookie);
	}
	
	public void addHeader(Header header){
		responseHeaders.add(header);
	}

	public Header[] getRequestHeaders() {
		return requestHeaders;
	}

	public byte[] getRequestContent() {
		return requestContent;
	}

	public String getQuery() {
		return query;
	}

	public Map<String, String> getRequestCookies() {
		return requestCookies;
	}

	public void setRequestCookies(Map<String, String> requestCookies) {
		this.requestCookies = requestCookies;
	}

	public ISession getSession() {
		return session;
	}

	public void setSession(ISession session) {
		this.session = session;
	}
}

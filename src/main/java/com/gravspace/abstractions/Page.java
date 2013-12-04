package com.gravspace.abstractions;

import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.protocol.HttpContext;

import scala.concurrent.Future;

public interface Page extends Component{
	public void initialise(HttpRequest request,
			HttpResponse response,
            HttpContext context);
	
}
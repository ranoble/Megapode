/*
 * ====================================================================
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 */
package com.gravspace.core;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.security.KeyStore;
import java.util.Date;
import java.util.Locale;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocketFactory;

import org.apache.http.ConnectionClosedException;
import org.apache.http.Consts;
import org.apache.http.Header;
import org.apache.http.HttpConnectionFactory;
import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpServerConnection;
import org.apache.http.HttpStatus;
import org.apache.http.MethodNotSupportedException;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.DefaultBHttpServerConnection;
import org.apache.http.impl.DefaultBHttpServerConnectionFactory;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpProcessor;
import org.apache.http.protocol.HttpProcessorBuilder;
import org.apache.http.protocol.HttpRequestHandler;
import org.apache.http.protocol.HttpService;
import org.apache.http.protocol.ResponseConnControl;
import org.apache.http.protocol.ResponseContent;
import org.apache.http.protocol.ResponseDate;
import org.apache.http.protocol.ResponseServer;
import org.apache.http.protocol.UriHttpRequestHandlerMapper;
import org.apache.http.util.EntityUtils;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.actor.TypedActor;
import akka.actor.TypedProps;
import akka.japi.Creator;

import com.gravspace.entrypoint.IRequestHandlerActor;
import com.gravspace.entrypoint.RequestHandlerActor;
import com.gravspace.messages.ResponseMessage;
import com.gravspace.util.Cookie;
import com.gravspace.util.LimitedQueue;

/**
 * Basic, yet fully functional and spec compliant, HTTP/1.1 file server.
 */
public class HttpServer {

	public static void start(String[] args) throws Exception {

		int port = 8082;
		if (args.length >= 1) {
			port = Integer.parseInt(args[0]);
		}

		ActorSystem system = ActorSystem.create("Application-System");
		Properties config = new Properties();
		config.load(HttpServer.class.getResourceAsStream("/megapode.conf"));
		ActorRef master = system.actorOf(
				Props.create(CoordinatingActor.class, config), "Coordinator");

		// Set up the HTTP protocol processor
		HttpProcessor httpproc = HttpProcessorBuilder.create()
				.add(new ResponseDate()).add(new ResponseServer("Test/1.1"))
				.add(new ResponseContent()).add(new ResponseConnControl())
				.build();

		// Set up request handlers
		UriHttpRequestHandlerMapper reqistry = new UriHttpRequestHandlerMapper();
		reqistry.register("*", new HttpHandler(system, master));

		// Set up the HTTP service
		HttpService httpService = new HttpService(httpproc, reqistry);

		SSLServerSocketFactory sf = null;
		if (port == 8443) {
			// Initialize SSL context
			ClassLoader cl = HttpServer.class.getClassLoader();
			URL url = cl.getResource("my.keystore");
			if (url == null) {
				System.out.println("Keystore not found");
				System.exit(1);
			}
			KeyStore keystore = KeyStore.getInstance("jks");
			keystore.load(url.openStream(), "secret".toCharArray());
			KeyManagerFactory kmfactory = KeyManagerFactory
					.getInstance(KeyManagerFactory.getDefaultAlgorithm());
			kmfactory.init(keystore, "secret".toCharArray());
			KeyManager[] keymanagers = kmfactory.getKeyManagers();
			SSLContext sslcontext = SSLContext.getInstance("TLS");
			sslcontext.init(keymanagers, null, null);
			sf = sslcontext.getServerSocketFactory();
		}

		RequestListenerThread t = new RequestListenerThread(port, httpService,
				sf);
		t.setDaemon(false);
		t.start();

		t.join();
	}

	static class HttpHandler implements HttpRequestHandler {

		private ActorRef coordinator;
		private ActorSystem system;
		private IRequestHandlerActor requester;

		public HttpHandler(final ActorSystem system, final ActorRef coordinator) {
			super();
			this.coordinator = coordinator;
			this.system = system;

			requester = TypedActor.get(system).typedActorOf(
					new TypedProps<RequestHandlerActor>(
							RequestHandlerActor.class,
							new Creator<RequestHandlerActor>() {
								public RequestHandlerActor create() {
									return new RequestHandlerActor(coordinator);
								}
							}), "entrypoint");
		}

		public void handle(final HttpRequest request,
				final HttpResponse response, final HttpContext context)
				throws HttpException, IOException {
			Date now = new Date();
			String method = request.getRequestLine().getMethod()
					.toUpperCase(Locale.ENGLISH);
			if (!method.equals("GET") && !method.equals("HEAD")
					&& !method.equals("POST")) {
				throw new MethodNotSupportedException(method
						+ " method not supported");
			}

			byte[] entityContent = null;
			if (request instanceof HttpEntityEnclosingRequest) {
				HttpEntity entity = ((HttpEntityEnclosingRequest) request)
						.getEntity();
				entityContent = EntityUtils.toByteArray(entity);
			}

			// ByteArrayEntity
			ResponseMessage result;
			try {
				
				result = requester.process(request.getRequestLine(),
						request.getAllHeaders(), entityContent);
				HttpEntity body = new StringEntity(new String(
						result.getResponseContent()), ContentType.create(
						result.getContentType(), Consts.UTF_8));
				response.setEntity(body);
				response.setStatusCode(result.getStatus());
				response.setHeaders(result.getHeaders().toArray(new Header[0]));
				System.out.println(((new Date()).getTime() - now.getTime())
						+ " ms");
			} catch (Exception e) {
				e.printStackTrace();
//				System.out.println("Got here");
				response.setStatusCode(HttpStatus.SC_METHOD_FAILURE);
				HttpEntity body = new StringEntity(e.getMessage(),
						ContentType.create("text/plain", Consts.UTF_8));
				response.setEntity(body);
			}

		}

	}

	static class RequestListenerThread extends Thread {

		private final HttpConnectionFactory<DefaultBHttpServerConnection> connFactory;
		private final ServerSocket serversocket;
		private final HttpService httpService;
		ExecutorService executor;

		public RequestListenerThread(final int port,
				final HttpService httpService, final SSLServerSocketFactory sf)
				throws IOException {
			this.connFactory = DefaultBHttpServerConnectionFactory.INSTANCE;
			this.serversocket = sf != null ? sf.createServerSocket(port)
					: new ServerSocket(port);
			this.httpService = httpService;
			LimitedQueue<Runnable> blockingQueue = new LimitedQueue<Runnable>(
					10);
			executor = new java.util.concurrent.ThreadPoolExecutor(1, 10, 0L,
					TimeUnit.MILLISECONDS, blockingQueue);
		}

		@Override
		public void run() {
			System.out.println("Listening on port "
					+ this.serversocket.getLocalPort());
			while (!Thread.interrupted()) {
				try {
					// Set up HTTP connection

					/**
					 * TODO: Change this to use an executor service
					 */
					Socket socket = this.serversocket.accept();
					System.out.println("Incoming connection from "
							+ socket.getInetAddress());
					HttpServerConnection conn = this.connFactory
							.createConnection(socket);

					// Start worker thread
					Thread t = new WorkerThread(this.httpService, conn);
					t.setDaemon(true);
					// executor.submit(t);
					t.start();
				} catch (InterruptedIOException ex) {
					break;
				} catch (IOException e) {
					System.err
							.println("I/O error initialising connection thread: "
									+ e.getMessage());
					break;
				}
			}
		}
	}

	static class WorkerThread extends Thread {

		private final HttpService httpservice;
		private final HttpServerConnection conn;

		public WorkerThread(final HttpService httpservice,
				final HttpServerConnection conn) {
			super();
			this.httpservice = httpservice;
			this.conn = conn;
		}

		@Override
		public void run() {
			System.out.println("New connection thread");
			HttpContext context = new BasicHttpContext(null);
			try {
				while (!Thread.interrupted() && this.conn.isOpen()) {
					this.httpservice.handleRequest(this.conn, context);
				}
			} catch (ConnectionClosedException ex) {
				System.err.println("Client closed connection");
			} catch (IOException ex) {
				System.err.println("I/O error: " + ex.getMessage());
			} catch (HttpException ex) {
				System.err.println("Unrecoverable HTTP protocol violation: "
						+ ex.getMessage());
			} finally {
				try {
					this.conn.shutdown();
				} catch (IOException ignore) {
				}
			}
		}

	}

}

/*
 * Copyright (c) 2015-2016, David A. Bauer
 */
package actor4j.server.example.jetty;

import java.net.URI;

import javax.ws.rs.core.UriBuilder;

import org.eclipse.jetty.server.Server;
import org.glassfish.jersey.jetty.JettyHttpContainerFactory;
import org.glassfish.jersey.server.ResourceConfig;

import actor4j.server.example.ServerApplication;

public class WebServer {
	public static void main(String[] args) throws Exception {
		URI baseUri = UriBuilder.fromUri("http://localhost/").port(8080).build();
		ResourceConfig config = new ResourceConfig(ServerApplication.class);
		Server server = JettyHttpContainerFactory.createServer(baseUri, config); 
     }
}

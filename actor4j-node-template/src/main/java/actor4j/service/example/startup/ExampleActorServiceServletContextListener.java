/*
 * Copyright (c) 2015-2016, David A. Bauer
 */
package actor4j.service.example.startup;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

@WebListener
public class ExampleActorServiceServletContextListener implements ServletContextListener {
	@Override
	public void contextInitialized(ServletContextEvent servletContextEvent) {
		ExampleActorService.start();
	}
	
	@Override
	public void contextDestroyed(ServletContextEvent servletContextEvent) {
		ExampleActorService.stop();
	}
}

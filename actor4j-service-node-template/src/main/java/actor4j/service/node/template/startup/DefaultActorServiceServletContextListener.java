/*
 * Copyright (c) 2015-2017, David A. Bauer
 */
package actor4j.service.node.template.startup;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

@WebListener
public class DefaultActorServiceServletContextListener implements ServletContextListener {
	@Override
	public void contextInitialized(ServletContextEvent servletContextEvent) {
		DefaultActorService.start();
	}
	
	@Override
	public void contextDestroyed(ServletContextEvent servletContextEvent) {
		DefaultActorService.stop();
	}
}

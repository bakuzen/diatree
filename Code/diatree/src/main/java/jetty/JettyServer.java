package jetty;

import java.io.File;

import javax.servlet.ServletException;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.DefaultHandler;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.websocket.jsr356.server.deploy.WebSocketServerContainerInitializer;
import org.eclipse.jetty.websocket.server.WebSocketHandler;
import org.eclipse.jetty.websocket.servlet.WebSocketServletFactory;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;


public class JettyServer {

	Server server = new Server( 8080 );
	final ServletHolder servletHolder = new ServletHolder( new DefaultServlet() );
	
	public JettyServer() {
	

		try {
//			String domainPath = JettyServer.class.getProtectionDomain().getCodeSource().getLocation().getPath() + "jetty/";
//			System.out.println(domainPath);
//			System.setProperty("jetty.home",domainPath);
			Server server = new Server(8080);
			
			ResourceHandler resource_handler = new ResourceHandler();
	        resource_handler.setDirectoriesListed(true);
	        resource_handler.setWelcomeFiles(new String[]{ "index.html" });
	        resource_handler.setResourceBase(".");
	        HandlerList handlers = new HandlerList();
	        handlers.setHandlers(new Handler[] { resource_handler, new DefaultHandler() });
	        server.setHandler(handlers);
			
	        WebSocketHandler wsHandler = new WebSocketHandler() {
				@Override
				public void configure(WebSocketServletFactory factory) {
					factory.register(DiaTreeHandler.class);
				}
	        };
	        server.setHandler(wsHandler);
			

//			
//			ContextHandler context = new ContextHandler();
//	        context.setContextPath("/diatree");
////	        context.setResourceBase(".");
//	        context.setClassLoader(Thread.currentThread().getContextClassLoader());
//	        server.setHandler(context);
//
//	        context.setHandler(new DiaTreeHandler());
			
	        server.start();
			
		} catch (ServletException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		} 
	
	
		
	}
}

package jetty;

import javax.servlet.ServletException;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.websocket.server.WebSocketHandler;
import org.eclipse.jetty.websocket.servlet.WebSocketServletFactory;

public class JettyServer {

	Server server = new Server( 8080 );
	final ServletHolder servletHolder = new ServletHolder( new DefaultServlet() );
	
	public JettyServer() {
	

		try {
//			String domainPath = JettyServer.class.getProtectionDomain().getCodeSource().getLocation().getPath() + "jetty/";
//			System.out.println(domainPath);
//			System.setProperty("jetty.home",domainPath);
			Server server = new Server(8080);
			
		     WebSocketHandler wsHandler = new WebSocketHandler() {
					@Override
					public void configure(WebSocketServletFactory factory) {
//						factory.register(DiaTreeSocket.class);
						factory.setCreator(new AdvancedDiaTreeCreator());
					}
		        };
			
			ResourceHandler resource_handler = new ResourceHandler();
	        resource_handler.setDirectoriesListed(false);
	        resource_handler.setWelcomeFiles(new String[]{ "index.html" });
	        resource_handler.setResourceBase(".");
	        HandlerList handlers = new HandlerList();
	        handlers.addHandler(wsHandler);
	        handlers.addHandler(resource_handler);
//	        handlers.addHandler(new DefaultHandler());
	        server.setHandler(handlers);
	   
			
	        server.start();
			
		} catch (ServletException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		} 
		
	}
}

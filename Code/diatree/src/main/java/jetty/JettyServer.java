package jetty;


import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.websocket.server.WebSocketHandler;
import org.eclipse.jetty.websocket.servlet.WebSocketServletFactory;

public class JettyServer {
	
	private HandlerList handlers;
	private Server server;
	final ServletHolder servletHolder = new ServletHolder( new DefaultServlet() );
	
	public JettyServer(AdvancedDiaTreeCreator creator) {

		try {
			server = new Server(8080);
			
			 WebSocketHandler wsHandler = new WebSocketHandler() {
					@Override
					public void configure(WebSocketServletFactory factory) {
						factory.setCreator(creator);
					}
		        };
		    
		    ResourceHandler resource_handler = new ResourceHandler();
	        resource_handler.setDirectoriesListed(false);
	        resource_handler.setWelcomeFiles(new String[]{ "index.html" });
	        resource_handler.setResourceBase(".");
	        handlers = new HandlerList();
	        handlers.addHandler(wsHandler);
	        handlers.addHandler(resource_handler);
	        server.setHandler(handlers);
	        
	        
	        server.start();
		} 
		catch (Exception e) {
			e.printStackTrace();
		} 
	}
	
}

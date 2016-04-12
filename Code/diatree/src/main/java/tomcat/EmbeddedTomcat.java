package tomcat;


import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.startup.Tomcat;
import org.apache.log4j.Logger;

import servlet.JsonServletTest;
import servlet.MainServlet;

import java.io.File;

import javax.servlet.http.HttpServlet;

public class EmbeddedTomcat {
	
	static Logger log = Logger.getLogger(EmbeddedTomcat.class.getName());
	
	private Context ctx;
	private Tomcat tomcat;
	
	public void addServlet(String mapping, HttpServlet servlet) {
	    Tomcat.addServlet(ctx, mapping, servlet);
	    ctx.addServletMapping("/" + mapping, mapping);
	}
	
	public EmbeddedTomcat() throws LifecycleException {
	  	log.info("Starting Tomcat server...");
	  	
	  	Connector httpsConnector = new Connector();
	    httpsConnector.setPort(8081);	     
	    httpsConnector.setSecure(true);
        httpsConnector.setScheme("https");
        httpsConnector.setAttribute("keyAlias", "tomcat");
        httpsConnector.setAttribute("keystorePass", "changeit");
//      httpsConnector.setAttribute("keystoreFile", keystorePath);
        httpsConnector.setAttribute("clientAuth", "false");
        httpsConnector.setAttribute("sslProtocol", "TLS");
        httpsConnector.setAttribute("protocol", "org.apache.coyote.http11.Http11NioProtocol");
        httpsConnector.setAttribute("SSLEnabled", false); 	
	  
	  	tomcat = new Tomcat();
	  	tomcat.getService().addConnector(httpsConnector);
	    tomcat.setPort(8080);
	    tomcat.getConnector().setRedirectPort(8081);

	    String domainPath = EmbeddedTomcat.class.getProtectionDomain().getCodeSource().getLocation().getPath() + "tomcat/";
	    ctx = tomcat.addWebapp(tomcat.getHost(), "", new File(domainPath).getAbsolutePath()); 
	    
	    ctx.addWelcomeFile("index.jsp");
	    
//	    ctx = tomcat.addContext("/d", new File(".").getAbsolutePath());
//
//	    Tomcat.addServlet(ctx, "main", new MainServlet());
//	    ctx.addServletMapping("/main", "main");
	    
//	    Tomcat.addServlet(ctx, "test", new JsonServletTest());
//	    ctx.addServletMapping("/test", "test");
	    
	}
	    
	
	public void start() throws LifecycleException {
	    tomcat.start();
	    log.info("Tomcat server started on port 8080");
	}


}

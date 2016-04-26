package servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.BufferOverflowException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jfree.util.Log;

import edu.cmu.sphinx.util.props.Configurable;
import edu.cmu.sphinx.util.props.PropertyException;
import edu.cmu.sphinx.util.props.PropertySheet;
import edu.cmu.sphinx.util.props.S4Component;
import module.TreeModule;
import util.ClientUtils;


@WebServlet(urlPatterns = {"/diatree"}, asyncSupported=true)
public class DiaTreeServlet  extends HttpServlet implements Configurable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 6004898157870048197L;
	
	@S4Component(type = TreeModule.class)
	public final static String TREE_MODULE = "module";
	private TreeModule tree;
	
	HttpServletResponse response;
	
	@Override
	public void newProperties(PropertySheet ps) throws PropertyException {
		tree = (TreeModule) ps.getComponent(TREE_MODULE);
	}

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
     
        //content type must be set to text/event-stream
        response.setContentType("text/event-stream");   
 
        //encoding must be set to UTF-8
        response.setCharacterEncoding("UTF-8");
        
        this.response = response;
        
        tree.initDisplay(true, false);
        
//      hack to keep the response from being committed which is needed for us to send more data using the send method 
        while (true) {
        	try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
        }
    }
    
    public void send(String data) {
    	
    	new Thread(){ 
			public void run() {
		    	if (data.contains("<")) return;
		    	while (response == null) {
		    		try {
						Thread.sleep(500);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
		    	}
				try {
					PrintWriter writer = response.getWriter(); // needs to be a PrintWriter or unicode won't work
			    	writer.write("data:" + data+ "\n\n");
			    	writer.flush();
			    	
				} 
				catch (IOException e) {
//					e.printStackTrace();
					Log.error("Pipe Broken! Trying to reconnect...");
					ClientUtils.openNewClient();
				}
				catch (BufferOverflowException e) {
//					e.printStackTrace();
					Log.warn("Buffer overflow detected in socket to client.");
					ClientUtils.openNewClient();
				}
	    	}
		}.start();
    }


}

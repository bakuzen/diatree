package servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import javax.servlet.AsyncContext;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import edu.cmu.sphinx.util.props.Configurable;
import edu.cmu.sphinx.util.props.PropertyException;
import edu.cmu.sphinx.util.props.PropertySheet;


@WebServlet(urlPatterns = {"/diatree"}, asyncSupported=true)
public class DiaTreeServlet  extends HttpServlet implements Configurable {

	
	/**
	 * 
	 */
	private static final long serialVersionUID = 6004898157870048197L;
	
	
	HttpServletResponse response;
	
	@Override
	public void newProperties(PropertySheet ps) throws PropertyException {
		
	}

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
     
        //content type must be set to text/event-stream
        response.setContentType("text/event-stream");   
 
        //encoding must be set to UTF-8
        response.setCharacterEncoding("UTF-8");
        
        this.response = response;
        
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
					PrintWriter w = response.getWriter();
			    	w.write("data:" + data + "\n\n");
			    	w.flush();
			    	
				} 
				catch (IOException e) {
					e.printStackTrace();
				} 
			
	    	}
		}.start();
    }


}

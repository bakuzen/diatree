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
	
	private ArrayBlockingQueue<PrintWriter> writers;
	
	HttpServletResponse response;
	
	@Override
	public void newProperties(PropertySheet ps) throws PropertyException {
		writers = new ArrayBlockingQueue<PrintWriter>(1);
	}

	
    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
     
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
    	if (data.contains("<")) return;
    	if (response == null) return;
		try {
			PrintWriter w = response.getWriter();
			
			JSONParser parser = new JSONParser();
			
			String s = "{\r\n  \"name\": \""+data+"\",\r\n  \"children\": [\r\n    {\r\n     \"name\": \"parent A\",\r\n     \"children\": [\r\n       {\"name\": \"child A1\"},\r\n       {\"name\": \"child A2\"},\r\n       {\r\n       \"name\": \"child A3\",\r\n       \"children\": [\r\n        {\"name\":\"gchild1\"}\r\n       ]\r\n       \r\n       }\r\n     ]\r\n    },{\r\n     \"name\": \"parent B\",\r\n     \"children\": [\r\n       {\"name\": \"child B1\"},\r\n       {\"name\": \"child B2\"}\r\n     ]\r\n    }\r\n  ]\r\n}";
			Object obj = parser.parse(s);
			
	    	w.write("data:" + obj.toString() + "\n\n");
	    	w.flush();
		} 
		catch (IOException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

    }



}

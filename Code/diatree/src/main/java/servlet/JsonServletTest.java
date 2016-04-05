package servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class JsonServletTest extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1136659365241564996L;

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
			JSONObject json = new JSONObject();
			
			JSONParser parser = new JSONParser();
			
			String s = "{\r\n  \"name\": \"diatree!\",\r\n  \"children\": [\r\n    {\r\n     \"name\": \"parent A\",\r\n     \"children\": [\r\n       {\"name\": \"child A1\"},\r\n       {\"name\": \"child A2\"},\r\n       {\r\n       \"name\": \"child A3\",\r\n       \"children\": [\r\n        {\"name\":\"gchild1\"}\r\n       ]\r\n       \r\n       }\r\n     ]\r\n    },{\r\n     \"name\": \"parent B\",\r\n     \"children\": [\r\n       {\"name\": \"child B1\"},\r\n       {\"name\": \"child B2\"}\r\n     ]\r\n    }\r\n  ]\r\n}";
			
			try {
				Object obj = parser.parse(s);
				request.setAttribute("json", obj.toString());
			} catch (ParseException e) {
				e.printStackTrace();
			}
		
			request.getRequestDispatcher("json.jsp").forward(request, response);
		
	}
	
	
}

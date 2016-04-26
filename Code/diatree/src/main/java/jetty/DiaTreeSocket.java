package jetty;

import java.io.IOException;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketError;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;


@WebSocket
public class DiaTreeSocket  {
	
	private static Session session;
	
	public static void send(String data) {
        try {
            session.getRemote().sendString(data);
        } catch (IOException e) {
            e.printStackTrace();
        }
	}

	@OnWebSocketClose
    public void onClose(int statusCode, String reason) {
        System.out.println("Close: statusCode=" + statusCode + ", reason=" + reason);
    }

    @OnWebSocketError
    public void onError(Throwable t) {
        System.out.println("Error: " + t.getMessage());
    }

    @OnWebSocketConnect
    public void onConnect(Session s) {
    	session = s;
    }

    @OnWebSocketMessage
    public void onMessage(String message) {
        System.out.println("Message: " + message);
    }

//	public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response)
//			throws IOException, ServletException {
//		 System.out.println("Target: " + target);
//	}
	
}

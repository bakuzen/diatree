package jetty;

import java.io.IOException;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketError;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;

import edu.cmu.sphinx.util.props.Configurable;
import edu.cmu.sphinx.util.props.PropertyException;
import edu.cmu.sphinx.util.props.PropertySheet;
import edu.cmu.sphinx.util.props.S4Component;
import module.TreeModule;


@WebSocket
public class DiaTreeSocket implements Configurable {
	
	
	@S4Component(type = TreeModule.class)
	public final static String TREE_MODULE = "module";
	private TreeModule tree;
	
	private static Session session;
	
	@Override
	public void newProperties(PropertySheet ps) throws PropertyException {
		tree = (TreeModule) ps.getComponent(TREE_MODULE);
	}
	
	public void send(String data) {
        try {
        	if (session == null) return;
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
    	System.out.println("CONNECTED");
    	session = s;
    	tree.initDisplay(true, false);
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

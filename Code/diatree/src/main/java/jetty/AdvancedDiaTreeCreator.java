package jetty;

import org.eclipse.jetty.websocket.servlet.ServletUpgradeRequest;
import org.eclipse.jetty.websocket.servlet.ServletUpgradeResponse;
import org.eclipse.jetty.websocket.servlet.WebSocketCreator;

public class AdvancedDiaTreeCreator  implements WebSocketCreator{

    private DiaTreeSocket diatree;
    
    public AdvancedDiaTreeCreator(DiaTreeSocket socket) {
        this.diatree = socket;
    }
 
    @Override
    public Object createWebSocket(ServletUpgradeRequest req, ServletUpgradeResponse resp) {
          return this.diatree;
    }
	
}

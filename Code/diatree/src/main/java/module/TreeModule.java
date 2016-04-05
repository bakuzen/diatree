package module;

import java.util.Collection;
import java.util.List;

import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import edu.cmu.sphinx.util.props.PropertyException;
import edu.cmu.sphinx.util.props.PropertySheet;
import edu.cmu.sphinx.util.props.S4Component;
import inpro.incremental.IUModule;
import inpro.incremental.unit.EditMessage;
import inpro.incremental.unit.IU;
import servlet.DiaTreeServlet;

public class TreeModule extends IUModule {

	
	@S4Component(type = DiaTreeServlet.class)
	public final static String DIATREE_SERVLET = "servlet";
	
	private DiaTreeServlet servlet;
	
	@Override
	public void newProperties(PropertySheet ps) throws PropertyException {
		super.newProperties(ps);
		servlet = (DiaTreeServlet) ps.getComponent(DIATREE_SERVLET);
	}

	private void send(String data) {
		servlet.send(data);
	}

	@Override
	protected void leftBufferUpdate(Collection<? extends IU> ius, List<? extends EditMessage<? extends IU>> edits) {
		
		for (EditMessage<? extends IU> edit : edits) {
			switch(edit.getType()) {
			
			case ADD:
				JSONParser parser = new JSONParser();
				
				String s = "{\r\n  \"name\": \""+edit.getIU().toPayLoad().toLowerCase()+"\",\r\n  \"children\": [\r\n    {\r\n     \"name\": \"parent A\",\r\n     \"children\": [\r\n       {\"name\": \"child A1\"},\r\n       {\"name\": \"child A2\"},\r\n       {\r\n       \"name\": \"child A3\",\r\n       \"children\": [\r\n        {\"name\":\"gchild1\"}\r\n       ]\r\n       \r\n       }\r\n     ]\r\n    },{\r\n     \"name\": \"parent B\",\r\n     \"children\": [\r\n       {\"name\": \"child B1\"},\r\n       {\"name\": \"child B2\"}\r\n     ]\r\n    }\r\n  ]\r\n}";
				try {
					Object obj = parser.parse(s);
					send(obj.toString());
				} 
				catch (ParseException e) {
					e.printStackTrace();
				}				
				
				break;
			case COMMIT:
				break;
			case REVOKE:
				break;
			default:
				break;
				
			}
		}
	}
}

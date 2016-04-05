package module;

import java.util.Collection;
import java.util.List;

import edu.cmu.sphinx.util.props.PropertyException;
import edu.cmu.sphinx.util.props.PropertySheet;
import edu.cmu.sphinx.util.props.S4Component;
import inpro.incremental.IUModule;
import inpro.incremental.unit.EditMessage;
import inpro.incremental.unit.IU;
import servlet.DiaTreeServlet;

public class SpeechToServlet extends IUModule {
	
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
				send(edit.getIU().toPayLoad());
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

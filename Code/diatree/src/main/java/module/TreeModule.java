package module;

import java.util.Collection;
import java.util.List;

import edu.cmu.sphinx.util.props.PropertyException;
import edu.cmu.sphinx.util.props.PropertySheet;
import edu.cmu.sphinx.util.props.S4Component;
import inpro.incremental.IUModule;
import inpro.incremental.unit.EditMessage;
import inpro.incremental.unit.IU;
import model.TraversableTree;
import servlet.DiaTreeServlet;

public class TreeModule extends IUModule {

	
	@S4Component(type = DiaTreeServlet.class)
	public final static String DIATREE_SERVLET = "servlet";
	

	private DiaTreeServlet servlet;
	private TraversableTree tree;
	
	
	@Override
	public void newProperties(PropertySheet ps) throws PropertyException {
		super.newProperties(ps);
		servlet = (DiaTreeServlet) ps.getComponent(DIATREE_SERVLET);
		
	}

	private void send(String data) {
		if (data == null) return;
		servlet.send(data);
	}

	@Override
	protected void leftBufferUpdate(Collection<? extends IU> ius, List<? extends EditMessage<? extends IU>> edits) {
		
		for (EditMessage<? extends IU> edit : edits) {
			
			String word = edit.getIU().toPayLoad().toLowerCase();
			switch(edit.getType()) {
			
			case ADD:
				
				if (word.equals("okay")) {
					tree.clear();
				}
				update();
				break;
			case COMMIT:
				break;
			case REVOKE:
//				TODO we need to be able to revoke, but only when the graph seems to deem it necessary
//				model.revokeIncrement(word);
				break;
			default:
				break;
				
			}
		}
	}

	private void update() {
//		try {
			tree = new TraversableTree();
//			String json = tree.getJsonForFrame(model.getPredictedFrame());
//			System.out.println(json);
//			send(json);
//		} 
//		catch (SQLException e) {
//			e.printStackTrace();
//		}
	}
}

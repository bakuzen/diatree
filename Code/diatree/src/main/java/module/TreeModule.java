package module;

import java.sql.SQLException;
import java.util.Collection;
import java.util.List;

import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import edu.cmu.sphinx.util.props.PropertyException;
import edu.cmu.sphinx.util.props.PropertySheet;
import edu.cmu.sphinx.util.props.S4Component;
import edu.cmu.sphinx.util.props.S4String;
import inpro.incremental.IUModule;
import inpro.incremental.unit.EditMessage;
import inpro.incremental.unit.IU;
import model.DomainModel;
import model.TraversableTree;
import model.db.Domain;
import servlet.DiaTreeServlet;

public class TreeModule extends IUModule {

	
	@S4Component(type = DiaTreeServlet.class)
	public final static String DIATREE_SERVLET = "servlet";
	
	@S4String(defaultValue = "test")
	public final static String DOMAIN = "domain";
	
	private DomainModel model;
	private DiaTreeServlet servlet;
	private TraversableTree tree;
	
	
	@Override
	public void newProperties(PropertySheet ps) throws PropertyException {
		super.newProperties(ps);
		servlet = (DiaTreeServlet) ps.getComponent(DIATREE_SERVLET);
		Domain db = new Domain();
		try {
			db.setDomain(ps.getString(DOMAIN));
			model = new DomainModel(db, ps.getString(DOMAIN));
			
		} 
		catch (SQLException e) {
			e.printStackTrace();
		}
		
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
					model.newUtterance();
				}
				model.addIncrement(word);
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
		try {
			tree = new TraversableTree();
			String json = tree.getJsonForFrame(model.getPredictedFrame());
			System.out.println(json);
			send(json);
		} 
		catch (SQLException e) {
			e.printStackTrace();
		}
	}
}

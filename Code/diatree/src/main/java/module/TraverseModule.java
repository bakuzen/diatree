package module;

import java.util.Collection;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import edu.cmu.sphinx.util.props.PropertyException;
import edu.cmu.sphinx.util.props.PropertySheet;
import edu.cmu.sphinx.util.props.S4Component;
import inpro.incremental.IUModule;
import inpro.incremental.unit.EditMessage;
import inpro.incremental.unit.IU;
import model.Node;
import model.TraversableTree;
import servlet.DiaTreeServlet;

public class TraverseModule extends IUModule {

	
	@S4Component(type = DiaTreeServlet.class)
	public final static String DIATREE_SERVLET = "servlet";
	
	private DiaTreeServlet servlet;
	
	JSONObject json;
	TraversableTree tree;
	
	@Override
	public void newProperties(PropertySheet ps) throws PropertyException {
		super.newProperties(ps);
		servlet = (DiaTreeServlet) ps.getComponent(DIATREE_SERVLET);
		
		Node nimm = new Node("nimm");
		
		Node der = new Node("den");
		Node die = new Node("die");
		Node das = new Node("das");
		nimm.addChild(der);
		nimm.addChild(die);
		nimm.addChild(das);
		
		der.addChild(new Node("balken"));
		der.addChild(new Node("strich"));
		
		die.addChild(new Node("schlange"));
		die.addChild(new Node("mensa"));
		
		das.addChild(new Node("kreuz"));
		das.addChild(new Node("ding"));
		das.addChild(new Node("gewehr"));
		
		tree = new TraversableTree(nimm);
			
	}

	private void send(String data) {
		servlet.send(data);
	}

	@Override
	protected void leftBufferUpdate(Collection<? extends IU> ius, List<? extends EditMessage<? extends IU>> edits) {
		
		for (EditMessage<? extends IU> edit : edits) {
			String word = edit.getIU().toPayLoad().toLowerCase();
			System.out.println(edit);
			switch(edit.getType()) {
			
			case ADD:
				if (word.equals("okay"))
					tree.resetTraversal();
				tree.newWord(word);
				send(tree.getJsonString());
				break;
			case COMMIT:
				break;
			case REVOKE:
				tree.revokeWord(word);
				break;
			default:
				break;
				
			}
		}
	}
}

package module;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import edu.cmu.sphinx.util.props.PropertyException;
import edu.cmu.sphinx.util.props.PropertySheet;
import edu.cmu.sphinx.util.props.S4Component;
import edu.cmu.sphinx.util.props.S4String;
import inpro.incremental.IUModule;
import inpro.incremental.unit.EditMessage;
import inpro.incremental.unit.EditType;
import inpro.incremental.unit.IU;
import inpro.incremental.unit.SlotIU;
import model.DomainModel;
import model.Frame;
import model.db.Domain;
import model.iu.ConfidenceIU;
import model.iu.FrameIU;

public class INLUModule extends IUModule {
	
	@S4Component(type = TreeModule.class)
	public final static String TREE_MODULE = "module";
	private TreeModule tree;

	@S4String(defaultValue = "test")
	public final static String DOMAIN = "domain";
	
	public static DomainModel model;
	
	@Override
	public void newProperties(PropertySheet ps) throws PropertyException {
		super.newProperties(ps);
		tree = (TreeModule) ps.getComponent(TREE_MODULE);
		
		Domain db = new Domain();
		try {
			db.setDomain(ps.getString(DOMAIN));
			model = new DomainModel(db, ps.getString(DOMAIN));
		} 
		catch (SQLException e) {
			e.printStackTrace();
		}
		
		
	}
	
	@Override
	protected void leftBufferUpdate(Collection<? extends IU> ius, List<? extends EditMessage<? extends IU>> edits) {
		for (EditMessage<? extends IU> edit : edits) {
			
			String word = edit.getIU().toPayLoad().toLowerCase();
			System.out.println("CURRENT WORD: " + word);
			switch(edit.getType()) {
			
			case ADD:
				
				if (word.equals("restart")) {
					model.newUtterance();
					tree.initDisplay(false);
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
			
//			for (String intent : model.getPredictedFrame().getIntents()) {
//				System.out.println(intent + " " + model.getPredictedFrame().getEntropyForIntent(intent));
//			}
			
			for (String intent : model.getPredictedFrame().getIntents()) {
				List<EditMessage<? extends IU>> edits = new ArrayList<EditMessage<? extends IU>>();
				FrameIU frameIU = new FrameIU(model.getPredictedFrame());
				SlotIU slotIU = frameIU.getSlotIUForIntent(intent);
				if (slotIU == null || slotIU.getDistribution() == null || slotIU.getDistribution().isEmpty()) continue; // this sometimes happens when newUtterance() is called
				edits.add(new EditMessage<SlotIU>(EditType.ADD, slotIU));
				super.rightBuffer.setBuffer(edits);
				super.notifyListeners();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
	}

}

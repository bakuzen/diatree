package module;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import edu.cmu.sphinx.util.props.PropertyException;
import edu.cmu.sphinx.util.props.PropertySheet;
import edu.cmu.sphinx.util.props.S4Component;
import edu.cmu.sphinx.util.props.S4Integer;
import edu.cmu.sphinx.util.props.S4String;
import inpro.incremental.IUModule;
import inpro.incremental.unit.EditMessage;
import inpro.incremental.unit.EditType;
import inpro.incremental.unit.IU;
import inpro.incremental.unit.SlotIU;
import model.Constants;
import model.DomainModel;
import model.Frame;
import model.db.Domain;
import model.iu.FrameIU;
import util.SessionTimeout;

public class INLUModule extends IUModule {
	
	@S4Component(type = TreeModule.class)
	public final static String TREE_MODULE = "module";
	private TreeModule tree;

	@S4String(defaultValue = "test")
	public final static String DOMAIN = "domain";
	
	@S4Integer (defaultValue = 10000)
	public final static String TIMEOUT = "timeout";
	
	public static DomainModel model;
	
	private String currentIntent;
	
	@Override
	public void newProperties(PropertySheet ps) throws PropertyException {
		super.newProperties(ps);
		tree = (TreeModule) ps.getComponent(TREE_MODULE);
		currentIntent = Constants.ROOT_NAME;
		Domain db = new Domain();
		try {
			db.setDomain(ps.getString(DOMAIN));
			model = new DomainModel(db, ps.getString(DOMAIN));
		} 
		catch (SQLException e) {
			e.printStackTrace();
		}
		SessionTimeout.setVariables(this,  ps.getInt(TIMEOUT));
	}
	
	
	@Override
	protected void leftBufferUpdate(Collection<? extends IU> ius, List<? extends EditMessage<? extends IU>> edits) {
		
		
		SessionTimeout.getInstance().reset();
		
		for (EditMessage<? extends IU> edit : edits) {
			
			String word = edit.getIU().toPayLoad().toLowerCase();
			
			switch(edit.getType()) {
			
			case ADD:
				System.out.println("CURRENT WORD ADD: " + word);
				if (word.equals(Constants.RESET_KEYWORD)) {
					resetSession();
					continue;
				}
				checkContext();
				model.addIncrement(word);
				update();
				break;
			case COMMIT:
				break;
			case REVOKE:
//				TODO we need to be able to revoke, but only when the graph seems to deem it necessary
//				model.revokeIncrement(word);
//				update();
				break;
			default:
				break;
			}
		}
	}

	public void resetSession() {
		model.newUtterance();
		tree.initDisplay(true, true);		
	}


	private void checkContext() {
		if (!this.currentIntent.equals(tree.getCurrentIntent())) {
			this.currentIntent = tree.getCurrentIntent();
			model.updateContext(this.currentIntent);
		}
	}

	private void update() {
		
		try {
			
			List<EditMessage<? extends IU>> edits = new ArrayList<EditMessage<? extends IU>>();
			Frame predictedFrame = model.getPredictedFrame();
			System.out.println(predictedFrame);
			for (String intent : predictedFrame.getIntents()) {
				FrameIU frameIU = new FrameIU(predictedFrame);
				SlotIU slotIU = frameIU.getSlotIUForIntent(intent);
				
				if (slotIU == null || slotIU.getDistribution() == null || slotIU.getDistribution().isEmpty()) continue; // this sometimes happens when newUtterance() is called
				edits.add(new EditMessage<SlotIU>(EditType.ADD, slotIU));
			}
			super.rightBuffer.setBuffer(edits);
//			super.notifyListeners();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
	}

}

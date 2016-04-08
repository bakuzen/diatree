package module;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import edu.cmu.sphinx.util.props.PropertyException;
import edu.cmu.sphinx.util.props.PropertySheet;
import edu.cmu.sphinx.util.props.S4String;
import inpro.incremental.IUModule;
import inpro.incremental.unit.EditMessage;
import inpro.incremental.unit.EditType;
import inpro.incremental.unit.IU;
import model.DomainModel;
import model.db.Domain;
import model.iu.FrameIU;

public class INLUModule extends IUModule {

	@S4String(defaultValue = "test")
	public final static String DOMAIN = "domain";
	
	private DomainModel model;
	
	@Override
	public void newProperties(PropertySheet ps) throws PropertyException {
		super.newProperties(ps);
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
			switch(edit.getType()) {
			
			case ADD:
				
				if (word.equals("okay")) {
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
		List<EditMessage<FrameIU>> edits = new ArrayList<EditMessage<FrameIU>>();
		
		
		try {
			edits.add(new EditMessage<FrameIU>(EditType.ADD, new FrameIU(model.getPredictedFrame())));
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		super.rightBuffer.setBuffer(edits);
		super.notifyListeners();
		
	}

}

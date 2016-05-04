package module;

import java.util.Collection;
import java.util.List;

import edu.cmu.sphinx.util.props.PropertyException;
import edu.cmu.sphinx.util.props.PropertySheet;
import inpro.incremental.unit.EditMessage;
import inpro.incremental.unit.IU;
import model.Constants;
import util.EndpointTimeout;
import util.SessionTimeout;

public class NLUModule extends INLUModule {
	
	@Override
	public void newProperties(PropertySheet ps) throws PropertyException {
		super.newProperties(ps);
	}
	
	
	@Override
	protected void leftBufferUpdate(Collection<? extends IU> ius, List<? extends EditMessage<? extends IU>> edits) {
		
		
		SessionTimeout.getInstance().reset();
		EndpointTimeout.getInstance().reset();
		
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


}

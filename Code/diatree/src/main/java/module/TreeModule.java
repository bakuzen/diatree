package module;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import edu.cmu.sphinx.util.props.PropertyException;
import edu.cmu.sphinx.util.props.PropertySheet;
import edu.cmu.sphinx.util.props.S4Component;
import inpro.incremental.IUModule;
import inpro.incremental.unit.EditMessage;
import inpro.incremental.unit.IU;
import inpro.incremental.unit.SlotIU;
import model.TraversableTree;
import servlet.DiaTreeServlet;

public class TreeModule extends IUModule {

	
	@S4Component(type = DiaTreeServlet.class)
	public final static String DIATREE_SERVLET = "servlet";
	

	private DiaTreeServlet servlet;
	private TraversableTree tree;
	private LinkedList<SlotIU> confirmStack;
	
	
	@Override
	public void newProperties(PropertySheet ps) throws PropertyException {
		super.newProperties(ps);
		servlet = (DiaTreeServlet) ps.getComponent(DIATREE_SERVLET);
		confirmStack = new LinkedList<SlotIU>();
		
	}

	private void send(String data) {
		if (data == null) return;
		servlet.send(data);
	}

	@Override
	protected void leftBufferUpdate(Collection<? extends IU> ius, List<? extends EditMessage<? extends IU>> edits) {
		
		for (EditMessage<? extends IU> edit : edits) {
			
			SlotIU decisionIU = (SlotIU) edit.getIU();
			SlotIU slotIU = (SlotIU) edit.getIU().groundedIn().get(0);
			String concept = slotIU.getDistribution().getArgMax().getEntity();
			String decision = decisionIU.getDistribution().getArgMax().getEntity();
			if ("verified".equals(decision)){
				SlotIU sIU = popConfirmStack();
				String c = sIU.getDistribution().getArgMax().getEntity();

				if ("yes".equals(concept)) {
					System.out.println("verified! need to expand " + c);
				}
				if ("no".equals(concept)) {
					System.out.println("verification failed! need to abort " + c);
				}
				resetUtterance();
			}
			else if ("confirm".equals(decision)) {
				pushToConfirmStack(slotIU);
				System.out.println("need to confirm " + concept);
			}
			else if ("select".equals(decision)) {
				if (!checkConfirmStackIsEmpty()) {
					throw new RuntimeException("Cannot select until the stack has been handled!");
				}
				System.out.println("need to expand " + concept);

				resetUtterance();
			}
		}
	}

	private void pushToConfirmStack(SlotIU slotIU) {
		confirmStack.push(slotIU);
	}
	
	private boolean checkConfirmStackIsEmpty() {
		return confirmStack.isEmpty();
	}
	
	private SlotIU popConfirmStack() {
		return confirmStack.pop();
	}

	private void resetUtterance() {
		INLUModule.model.newUtterance();
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

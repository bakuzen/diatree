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
import model.Node;
import model.TraversableTree;
import servlet.DiaTreeServlet;

public class TreeModule extends IUModule {

	
	@S4Component(type = DiaTreeServlet.class)
	public final static String DIATREE_SERVLET = "servlet";
	

	private DiaTreeServlet servlet;
	private TraversableTree tree;
	private LinkedList<SlotIU> confirmStack;
	private LinkedList<Node> expandedNodes;
	private LinkedList<String> remainingIntents;
	public static boolean firstDisplay;
	
	
	@Override
	public void newProperties(PropertySheet ps) throws PropertyException {
		super.newProperties(ps);
		servlet = (DiaTreeServlet) ps.getComponent(DIATREE_SERVLET);
		reset();
	}
	
	public void reset() {
		confirmStack = new LinkedList<SlotIU>();
		expandedNodes = new LinkedList<Node>();
		remainingIntents = new LinkedList<String>();
		setFirstDisplay(true);
	}

	private void send(String data) {
		if (data == null) return;
		servlet.send(data);
	}

	@Override
	protected void leftBufferUpdate(Collection<? extends IU> ius, List<? extends EditMessage<? extends IU>> edits) {
		
		if (isFirstDisplay()) 
			initDisplay();
		
		for (EditMessage<? extends IU> edit : edits) {
			
			SlotIU decisionIU = (SlotIU) edit.getIU();
			if (edit.getIU().groundedIn().isEmpty()) continue; // simple check in case something gets through that shouldn't
			SlotIU slotIU = (SlotIU) edit.getIU().groundedIn().get(0);
			String concept = slotIU.getDistribution().getArgMax().getEntity();
			String intent = slotIU.getName();
			String decision = decisionIU.getDistribution().getArgMax().getEntity();
//			System.out.println("decision:" + decision + " intent:" + intent + " concept:" + concept);
			if ("verified".equals(decision)){
				if (!confirmStack.isEmpty()) {
					SlotIU sIU = popConfirmStack();
					String c = sIU.getDistribution().getArgMax().getEntity();
					String i = sIU.getName();
	
					if ("yes".equals(concept)) {
						expand(i, c);
					}
					if ("no".equals(concept)) {
						abortConfirmation(i, c);
					}
					resetUtterance();
				}
			}
			else if ("confirm".equals(decision)) {
				pushToConfirmStack(slotIU);
				offerConfirmation(intent, concept);
			}
			else if ("select".equals(decision)) {
//				if (!checkConfirmStackIsEmpty()) {
//					throw new RuntimeException("Cannot select until the stack has been handled!");
//				}
				expand(intent, concept);

				resetUtterance();
			}
			else if ("wait".equals(decision)) {
				System.out.println("waiting...");
			}
			update();
		}
	}
	
	private void abortConfirmation(String intent, String concept) {
		Node childToConfirm = getTopNode().getChildNode(intent).getChildNode(concept+"?");
		childToConfirm.setName(concept);
	}

	private void abort(String intent, String concept) {
		getTopNode().clearChildren();
		popExpandedNode();
	}

	private void expand(String intent, String concept) {
		Node top = getTopNode();
		Node child = top.getChildNode(intent);
		if (child == null) return;
		child.clearChildren();
		Node n = new Node(intent +":" + concept);
		top.clearChildren();
		top.addChild(n);
		this.pushExpandedNode(n);
		this.removeRemainingIntent(intent);
		this.addRemainingIntents();
	}

	private void offerConfirmation(String intent, String concept) {
		offerExpansion(intent);
		if (getTopNode() == null) return; 
		if (getTopNode().getChildNode(intent) == null) return;
		Node childToConfirm = getTopNode().getChildNode(intent).getChildNode(concept);
		childToConfirm.setName(concept + "?");
	}

	private void offerExpansion(String intent) {
		Node top = getTopNode();
		
		Node forExpansion = top.getChildNode(intent);
		if (forExpansion == null) return;
		for (String concept : getPossibleConceptsForIntent(intent)) {
			forExpansion.addChild(new Node(concept));
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
	
	private List<String> getPossibleIntents() {
		return INLUModule.model.getPossibleIntents();
	}
	
	private List<String> getPossibleConceptsForIntent(String intent) {
		return INLUModule.model.getPossibleIntentsForConcept(intent);
	}
	
	private void pushExpandedNode(Node n) {
		expandedNodes.push(n);
	}
	
	private Node popExpandedNode() {
		return expandedNodes.pop();
	}
	
	private Node getTopNode() {
		System.out.println("top node:"+expandedNodes.peek());
		return expandedNodes.peek();
	}
	
	private Node getRootNote() {
		return expandedNodes.peekLast();
	}
	

	private void update() {
		
		
//		try {
			tree = new TraversableTree(this.getRootNote());
			String json = tree.getJsonString();
			System.out.println(json);
			send(json);
//		} 
//		catch (SQLException e) {
//			e.printStackTrace();
//		}
	}
	
	private void removeRemainingIntent(String intent) {
		remainingIntents.remove(intent);
	}

	private void initDisplay() {
		reset();
		remainingIntents = new LinkedList<String>(getPossibleIntents());
		
		
		Node root = new Node("");
		this.pushExpandedNode(root);
		this.addRemainingIntents();
		this.setFirstDisplay(false);
	}

	private void addRemainingIntents() {
		Node top = getTopNode();
		for (String intent : remainingIntents) {
			Node i = new Node(intent);
			top.addChild(i);
		}
	}

	public boolean isFirstDisplay() {
		return firstDisplay;
	}

	public void setFirstDisplay(boolean firstDisplay) {
		this.firstDisplay = firstDisplay;
	}
}

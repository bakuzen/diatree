package module;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;

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
import sium.nlu.stat.Distribution;

public class TreeModule extends IUModule {
	
	static Logger log = Logger.getLogger(Distribution.class.getName());

	
	@S4Component(type = DiaTreeServlet.class)
	public final static String DIATREE_SERVLET = "servlet";
	

	private DiaTreeServlet servlet;
	private TraversableTree tree;
	private LinkedList<SlotIU> confirmStack;
	private LinkedList<Node> expandedNodes;
	private LinkedList<String> remainingIntents;
	private boolean firstDisplay;
	
	
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
			initDisplay(false);
		
		for (EditMessage<? extends IU> edit : edits) {
			
			SlotIU decisionIU = (SlotIU) edit.getIU();
			if (edit.getIU().groundedIn().isEmpty()) continue; // simple check in case something gets through that shouldn't
			
			SlotIU slotIU = (SlotIU) edit.getIU().groundedIn().get(0);
			String concept = slotIU.getDistribution().getArgMax().getEntity();
			String intent = slotIU.getName();
			Double confidence = slotIU.getConfidence();
			String decision = decisionIU.getDistribution().getArgMax().getEntity();
			
//			System.out.println("decision:" + decision + " intent:" + intent + " concept:" + concept + " confidence:" + confidence );
			
			if ("verified".equals(decision)){
				if (!checkConfirmStackIsEmpty()) {
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
				else {
					if ("no".equals(concept))
						abort();
				}
			}
			else if ("confirm".equals(decision)) {
				pushToConfirmStack(slotIU);
				offerConfirmation(intent, concept);
			}
			else if ("select".equals(decision)) {
				if (!checkConfirmStackIsEmpty()) {
//					throw new RuntimeException("Cannot select until the stack has been handled!");
					logString("select!", "don't do this!", "");
					return;
				}
				expand(intent, concept);

				resetUtterance();
			}
			else if ("wait".equals(decision)) {
				// waiting....
			}
			update();
		}
	}
	
	private void abortConfirmation(String intent, String concept) {
		log.info(logString("abortConfirmation", intent, concept));
		if (!getTopNode().hasChild(intent)){
			return;
		}
		Node childToConfirm = getTopNode().getChildNode(intent).getChildNode(concept+"?");
		if (childToConfirm == null) return;
		childToConfirm.setName(concept);
	}

	private String logString(String method, String intent, String concept) {
		String tolog = method + "(" + intent + "," + concept +")";
//		System.out.println(tolog);
		return tolog;
				
	}

	private void abort() {
		log.info(logString("abort()", "", ""));
		if (getTopNode() == getRootNode() || getTopNode() == null) {
			initDisplay(false);
			return;
		}
		getTopNode().clearChildren();
		popExpandedNode();
	}

	private void expand(String intent, String concept) {
		log.info(logString("expand", intent, concept));
		if ("confirm".equals(intent)) {
			if ("yes".equals(concept)) {
				return; // ignore this
			}
			if ("no".equals(concept)) {
				abort();
			}
		}
		if (this.intentSettled(intent)) {
//			been here, done that
			return;
		}
//		another case is if someone is referring to an intent (not a concept of an intent)
//		when that happens, show the expansion of that intent
		if ("intent".equals(intent)) {
			offerExpansion(concept);
			return;
		}
		
		Node top = getTopNode();
		if (!top.hasChild(intent))  // this could happen when someone says the same word more than once
			return;
		
		
//		Node child = top.getChildNode(intent);
//		child.clearChildren();
		Node n = new Node(intent +":" + concept);
		top.clearChildren();
		top.addChild(n);
		this.pushExpandedNode(n);
		this.removeRemainingIntent(intent);
		this.clearConfirmStack();
		this.addRemainingIntents();
	}

	private void clearConfirmStack() {
		this.confirmStack.clear();
	}

	private void offerConfirmation(String intent, String concept) {
		log.info(logString("offerConfirmation", intent, concept));
		if (this.intentSettled(intent)) {
//			no need to confirm something that has been expanded already
			return;
		}
		if (!getTopNode().hasChild(intent)) {
			return;
		}
		if (!getTopNode().getChildNode(intent).hasChild(concept)) {
			return;
		}
		offerExpansion(intent);
		Node childToConfirm = getTopNode().getChildNode(intent).getChildNode(concept);
		childToConfirm.setName(concept + "?");
	}

	private void offerExpansion(String intent) {
		log.info(logString("offerExpansion", intent, ""));

		if (this.intentSettled(intent)) {
//			been here, done that
			return;
		}

		Node top = getTopNode();
		if (!top.hasChild(intent)) {
			return; // this avoids problems with repetitions
		}
		
		Node forExpansion = top.getChildNode(intent);
		if (forExpansion.isExpanded()) {
			return; // another case of repetition
		}
		
		forExpansion.setExpanded(true);
		for (String concept : getPossibleConceptsForIntent(intent)) {
			forExpansion.addChild(new Node(concept));
		}
	}

	private boolean intentSettled(String intent) {
		for (Node i : expandedNodes) {
			if (i.getName().startsWith(intent)) return true;
		}
		return false;
	}

	private void pushToConfirmStack(SlotIU slotIU) {
		logString("pushToConfirmStack", slotIU.toPayLoad(), "");
//		don't add the same thing multiple times
		for (SlotIU sIU : confirmStack) {
			if (sIU.getName().equals(slotIU.getName()))
				return;
		}
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
		if (!expandedNodes.isEmpty())
			expandedNodes.peek().setExpanded(false);
		return expandedNodes.pop();
	}
	
	private Node getTopNode() {
		System.out.println("top node:"+expandedNodes.peek());
		return expandedNodes.peek();
	}
	
	private Node getRootNode() {
		return expandedNodes.peekLast();
	}
	
	private void update() {
		tree = new TraversableTree(this.getRootNode());
		tree.setDepth(getNodeDepth());
		String json = tree.getJsonString();
		send(json);
	}
	
	private int getNodeDepth() {
		return expandedNodes.size() + 2;
	}

	private void removeRemainingIntent(String intent) {
		remainingIntents.remove(intent);
	}

	 public void initDisplay(boolean update) {
		reset();
		remainingIntents = new LinkedList<String>(getPossibleIntents());
		remainingIntents.remove("intent"); // keyword, but used in a similar way--shouldn't be displayed
		
		Node root = new Node("");
		this.pushExpandedNode(root);
		this.addRemainingIntents();
		this.setFirstDisplay(false);
		if (update)
			update();
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

	public void setFirstDisplay(boolean fd) {
		firstDisplay = fd;
	}
}

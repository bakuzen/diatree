package module;

import java.sql.SQLException;
import java.util.ArrayList;
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
import model.Constants;
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
	private List<String> remainingIntents;
	private String currentIntent;
	private boolean firstDisplay;
	
	
	@Override
	public void newProperties(PropertySheet ps) throws PropertyException {
		super.newProperties(ps);
		servlet = (DiaTreeServlet) ps.getComponent(DIATREE_SERVLET);
		reset();
	}
	
	public void reset() {
		setCurrentIntent(Constants.ROOT_NAME);
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
			initDisplay(false, true);
		
		for (EditMessage<? extends IU> edit : edits) {
			
			SlotIU decisionIU = (SlotIU) edit.getIU();
			if (edit.getIU().groundedIn().isEmpty()) continue; // simple check in case something gets through that shouldn't
			
			SlotIU slotIU = (SlotIU) edit.getIU().groundedIn().get(0);
			String concept = slotIU.getDistribution().getArgMax().getEntity();
			String intent = slotIU.getName();
			Double confidence = slotIU.getConfidence();
			String decision = decisionIU.getDistribution().getArgMax().getEntity();
			
			System.out.println("decision:" + decision + " intent:" + intent + " concept:" + concept + " confidence:" + confidence );
			
//			when handling the result of a clarification request
			if (Constants.VERIFIED.equals(decision)){
				if (!checkConfirmStackIsEmpty()) {
//					when we are handling a CR, we have a stack of "QUD" of sorts 
					SlotIU sIU = popConfirmStack();
					String c = sIU.getDistribution().getArgMax().getEntity();
					String i = sIU.getName();
					
					if (Constants.YES.equals(concept)) {
						expandIntent(i, c);
					}
					if (Constants.NO.equals(concept)) {
						abortConfirmation(i, c);
					}
					resetUtterance();
				}
				else {
					if (Constants.NO.equals(concept))
						abort();
				}
			}
//			when we want to invoke a clarification request
			else if (Constants.CONFIRM.equals(decision)) {
				pushToConfirmStack(slotIU);
				offerConfirmation(intent, concept);
			}
//			when an intent has a concept with a high enough probability 
			else if (Constants.SELECT.equals(decision)) {
				if (!checkConfirmStackIsEmpty()) {
					logString("select!", "don't do this!", "confirmation stack isn't empty!");
					return;
				}
				expandIntent(intent, concept);
				resetUtterance();
			}
//			in all other cases, just wait for more input
			else if (Constants.WAIT.equals(decision)) {
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
		childToConfirm.setHasBeenTraversed(false);
	}

	private String logString(String method, String intent, String concept) {
		String tolog = method + "(" + intent + "," + concept +")";
		return tolog;
				
	}

	private void abort() {
		log.info(logString("abort()", "", ""));
		if (getTopNode() == getRootNode() || getTopNode() == null) {
			initDisplay(false, true);
			return;
		}
		getTopNode().clearChildren();
		popExpandedNode();
	}
	
	private void expandIntent(String intent, String concept) {
		log.info(logString("expandIntent", intent, concept));
		if (Constants.CONFIRM.equals(intent)) {
			if (Constants.YES.equals(concept)) {
				return; // ignore this
			}
			if (Constants.NO.equals(concept)) {
				abort();
			}
		}
		if (this.intentSettled(intent)) {
//			been here, done that
			return;
		}
//		another case is if someone is referring to an intent (not a concept of an intent)
//		when that happens, show the expansion of that intent
		if (Constants.INTENT.equals(intent) && hasConcepts(concept)) {
			offerExpansion(concept);
			return;
		}
		
		Node top = getTopNode();
//		if (!top.hasChild(intent))  // this could happen when someone says the same word more than once
//			return;
		
	
//		Node child = top.getChildNode(intent);
//		child.clearChildren();
//		Node n = new Node(intent +":" + concept);
		if (Constants.INTENT.equals(intent)) {
			Node n = new Node(concept);
			this.setCurrentIntent(concept);
			remainingIntents = getPossibleIntents();
			top.clearChildren();
			top.addChild(n);
			this.pushExpandedNode(n);

		}
		else {
			Node n = new Node(intent + ":"+ concept);
			this.removeRemainingIntent(intent);
			top.clearChildren();
			top.addChild(n);
			this.pushExpandedNode(n);
			
		}
		
		this.clearConfirmStack();
		this.branchIntents();
	}

	private boolean hasConcepts(String concept) {
		try {
			return !INLUModule.model.getDB().getConceptsForIntent(concept).isEmpty();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
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
		if (!getTopNode().getChildNode(intent).hasChild(concept)) {
			return;
		}
		if (offerExpansion(intent)) {
			Node childToConfirm = getTopNode().getChildNode(intent).getChildNode(concept);
			childToConfirm.setName(concept + "?");
			childToConfirm.setHasBeenTraversed(true);
		}
	}

	private boolean offerExpansion(String intent) {
		log.info(logString("offerExpansion", intent, ""));

		if (this.intentSettled(intent)) {
//			been here, done that
			return false;
		}

		Node top = getTopNode();
		if (!top.hasChild(intent)) {
			return false; // this avoids problems with repetitions
		}
		
		Node forExpansion = top.getChildNode(intent);
		if (forExpansion.isExpanded()) {
			return true; // another case of repetition
		}
		
		forExpansion.setExpanded(true);
		for (String concept : getPossibleConceptsForIntent(intent)) {
			forExpansion.addChild(new Node(concept));
		}
		forExpansion.setHasBeenTraversed(true);
//		this.setCurrentIntent(intent);
//		remainingIntents = getPossibleConceptsForIntent(intent);
//		System.out.println("remainingIntents: " + remainingIntents);
		this.branchIntents();
		return true;
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
		try {
			return INLUModule.model.getDB().getChildIntentsForIntent(this.getCurrentIntent());
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return new LinkedList<String>();
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

	 public void initDisplay(boolean update, boolean reset) {
		 
		if (reset) 
			reset();
		remainingIntents = getPossibleIntents();
		remainingIntents.remove("intent"); // keyword, but used in a similar way--shouldn't be displayed
		
		Node root = new Node("");
		this.pushExpandedNode(root);
		this.branchIntents();
		this.setFirstDisplay(false);
		if (update)
			update();
	}

	private void branchIntents() {
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

	public String getCurrentIntent() {
		return currentIntent;
	}

	public void setCurrentIntent(String currentIntent) {
		System.out.println("setting new intent: " + currentIntent);
		this.currentIntent = currentIntent;

	}
}

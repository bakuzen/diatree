package module;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;

import edu.cmu.sphinx.util.props.PropertyException;
import edu.cmu.sphinx.util.props.PropertySheet;
import edu.cmu.sphinx.util.props.S4Boolean;
import edu.cmu.sphinx.util.props.S4Component;
import inpro.incremental.IUModule;
import inpro.incremental.unit.EditMessage;
import inpro.incremental.unit.IU;
import inpro.incremental.unit.SlotIU;
import inpro.incremental.unit.WordIU;
import jetty.DiaTreeSocket;
import model.Constants;
import model.CustomFunction;
import model.CustomFunctionRegistry;
import model.Node;
import model.TraversableTree;
import sium.nlu.stat.Distribution;
import util.EndpointTimeout;

public class TreeModule extends IUModule {
	
	static Logger log = Logger.getLogger(TreeModule.class.getName());

	@S4Component(type = DiaTreeSocket.class)
	public final static String DIATREE_SOCKET = "socket";
	
	@S4Boolean(defaultValue = true)
	public final static String IS_INCREMENTAL = "isIncremental";

	private DiaTreeSocket socket;
	private TraversableTree tree;
	private LinkedList<SlotIU> confirmStack;
	private LinkedList<Node> expandedNodes;
	private List<String> remainingIntents;
	private String currentIntent;
	private boolean firstDisplay;
	private PropertySheet propertySheet;
	private LinkedList<String> expectedStack;
	private LinkedList<String> wordStack;
	private boolean isIncremental;
	
	@Override
	public void newProperties(PropertySheet ps) throws PropertyException {
		super.newProperties(ps);
		this.setPropertySheet(ps);
		socket = (DiaTreeSocket) ps.getComponent(DIATREE_SOCKET);
		this.setIncremental(ps.getBoolean(IS_INCREMENTAL));
		if (!this.isIncremental())
			EndpointTimeout.setVariables(this, 2000);
		reset();
	}
	
	public void reset() {
		setCurrentIntent(Constants.ROOT_NAME);
		wordStack = new LinkedList<String>();
		confirmStack = new LinkedList<SlotIU>();
		expandedNodes = new LinkedList<Node>();
		remainingIntents = new LinkedList<String>();
		expectedStack = new LinkedList<String>();
		setFirstDisplay(true);
	}

	private void send(String data) {
		if (data == null) return;
		socket.send(data);
	}

	@Override
	protected void leftBufferUpdate(Collection<? extends IU> ius, List<? extends EditMessage<? extends IU>> edits) {
		
		if (!this.isIncremental()) EndpointTimeout.getInstance().reset();
		
		if (isFirstDisplay())
			initDisplay(false, true);
		
		for (EditMessage<? extends IU> edit : edits) {
			
			if (edit.getIU() instanceof WordIU) {
				handleWordIU(edit);
				continue;
			}
			
			SlotIU decisionIU = (SlotIU) edit.getIU();
			if (edit.getIU().groundedIn().isEmpty()) continue; // simple check in case something gets through that shouldn't
			
			SlotIU slotIU = (SlotIU) edit.getIU().groundedIn().get(0);
			String concept = slotIU.getDistribution().getArgMax().getEntity();
			String intent = slotIU.getName();
			Double confidence = slotIU.getConfidence();
			Distribution<String> dist = slotIU.getDistribution();
			String decision = decisionIU.getDistribution().getArgMax().getEntity();
			
			System.out.println("decision:" + decision + " intent:" + intent + " concept:" + concept + " confidence:" + confidence );
			
//			when handling the result of a clarification request
			if (Constants.VERIFIED.equals(decision)){
				if (!checkConfirmStackIsEmpty()) {
//					when we are handling a CR, we have a stack of "QUD" of sorts 
					SlotIU sIU = popConfirmStack();
					String c = sIU.getDistribution().getArgMax().getEntity();
					Distribution<String> d = sIU.getDistribution();
					String i = sIU.getName();
					
					if (Constants.YES.equals(concept)) {
						expandIntent(i, c, d);
					}
					else if (Constants.NO.equals(concept)) {
						abortConfirmation(i, c);
					}
					resetUtterance();
				}
				else {
					if (Constants.NO.equals(concept)) {
						abort();
					}
				}
				break; //this means we don't step through any more of the possible intents
			}
//			when we want to invoke a clarification request
			else if (Constants.CONFIRM.equals(decision)) {
				pushToConfirmStack(slotIU);
				offerConfirmation(intent, concept, dist);
				resetUtterance();
				break;
			}
//			when an intent has a concept with a high enough probability 
			else if (Constants.SELECT.equals(decision)) {
				if (!checkConfirmStackIsEmpty()) {
					logString("select!", "don't do this!", "confirmation stack isn't empty!");
					break;
				}
				expandIntent(intent, concept, dist);
				resetUtterance();
			}
//			in all other cases, just wait for more input
			else if (Constants.WAIT.equals(decision)) {
				indicateWaiting();
			}
		}
		update();
	}
	
	private void handleWordIU(EditMessage<? extends IU> edit) {
		switch (edit.getType()) {
		case ADD:
			wordStack.push(edit.getIU().toPayLoad());
			break;
		case COMMIT:
			break;
		case REVOKE:
			wordStack.pop();
			break;
		default:
			break;
		}
	}

	private void indicateWaiting() {
		if (tree == null) return;
		Node n = tree.getCurrentActiveNode();
		if (n == null) return;
		boolean isConsidered = n.isToConsider();
		System.out.println("found node: " + n);
		n.setHasBeenTraversed(false);
		n.setToConsider(false);
		update();
		n.setHasBeenTraversed(true);
		n.setToConsider(isConsidered);
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
		this.resetUtterance();
	}

	private String logString(String method, String intent, String concept) {
		String tolog = method + "(" + intent + "," + concept +")";
		return tolog;
	}

	private void abort() {
		log.info(logString("abort()", "", ""));
		if (getTopNode() == getRootNode() || getTopNode() == null) {
			setCurrentIntent(getRootNode().getName());
			initDisplay(false, true);
			return;
		}
		Node top = getTopNode();
		top.setHasBeenTraversed(true);
		boolean foundExpanded = false;
		for (Node child : top.getChildren()) {
			if (child.isExpanded()) {
				child.clearChildren();
				child.setExpanded(false);
				child.setHasBeenTraversed(false);
				foundExpanded = true;
			}
		}
		if (foundExpanded) {
			resetUtterance();
			return;
		}
		
		getTopNode().clearChildren();
		Node abortedNode = popExpandedNode();
		getTopNode().clearChildren();
		
		
//		setCurrentIntent(getTopNode().getName());
		addRemainingIntent(abortedNode.getName().split(":")[0]);
		branchIntents();
		resetUtterance();
		
		if (getTopNode() == getRootNode() || getTopNode() == null) {
			initDisplay(false, true);
		}
	}
	
	private void addRemainingIntent(String intent) {
		remainingIntents.add(intent);
	}

	public void returnFromCustomFunction() {
		getTopNode().setHasBeenTraversed(false);
		this.clearConfirmStack();
		this.branchIntents();
	}
	
	private void expandIntent(String intent, String concept, Distribution<String> dist) {
		log.info(logString("expandIntent", intent, concept));
		if (Constants.CONFIRM.equals(intent)) {
			if (Constants.YES.equals(concept)) {
				return; // ignore this
			}
			if (Constants.NO.equals(concept)) {
				abort();
			}
		}
		if (this.intentSettled(intent) || this.intentSettled(concept)) {
//			been here, done that
			return;
		}
		if (!expectedStack.isEmpty()) {
			if (!expected(intent)) {
				expectedStack.clear();
				return;
			}
		}
		
		if (isCustomFunction(concept)) {
			Node top = getTopNode();
			Node n = new Node(concept);
			n.setProbability(dist.getProbabilityForItem(concept));
			this.pushExpandedNode(n);
			n.setHasBeenTraversed(true);
			this.removeRemainingIntent(concept);
			top.clearChildren();
			top.addChild(n);
			performCustomFunction(concept);
			return;
		}
		
		Node top = getTopNode();
		top.setHasBeenTraversed(false);
		
//		another case is if someone is referring to an intent (not a concept of an intent)
//		when that happens, show the expansion of that intent
		if (Constants.INTENT.equals(intent) && hasConcepts(concept)) {
			offerExpansion(concept, dist);
			return;
		}
		
		if (Constants.INTENT.equals(intent)) {
			Node n = new Node(concept);
			n.setHasBeenTraversed(true);
			n.setProbability(dist.getProbabilityForItem(concept));
			this.setCurrentIntent(concept);
			remainingIntents = getPossibleIntents();
			top.clearChildren();
			top.addChild(n);
			this.pushExpandedNode(n);
		}
		else {
			Node n = new Node(intent + ":"+ concept);
			n.setProbability(dist.getProbabilityForItem(concept));
			this.removeRemainingIntent(intent);
			top.clearChildren();
			top.addChild(n);
			this.pushExpandedNode(n);
		}
		
		this.getRootNode().setHasBeenTraversed(false);
		this.clearConfirmStack();
		this.branchIntents();
	}
	
	private boolean expected(String intent) {
		return expectedStack.contains(intent);
	}

	private void performCustomFunction(String intent) {
		try {
			CustomFunction function  = CustomFunctionRegistry.getFunction(intent);
			if (function == null) return;
			function.run(this);
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		
	}

	private boolean isCustomFunction(String intent) {
		try {
			return INLUModule.model.getDB().isCustomFunction(intent);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
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

	private void offerConfirmation(String intent, String concept, Distribution<String> dist) {
		log.info(logString("offerConfirmation", intent, concept));
		if (this.intentSettled(intent) || this.intentSettled(concept)) {
//			been here, done that
			this.clearConfirmStack();
			return;
		}
//		if (!getTopNode().getChildNode(intent).hasChild(concept)) {
//			return;
//		}
		if (offerExpansion(intent, dist)) {
			Node childToConfirm = getTopNode().getChildNode(intent).getChildNode(concept);
			if (childToConfirm != null) {
				childToConfirm.setName(concept + "?");
				childToConfirm.setHasBeenTraversed(true);
			}
		}
	}

	private boolean offerExpansion(String intent, Distribution<String> dist) {
		log.info(logString("offerExpansion", intent, ""));

		if (this.intentSettled(intent)) {
//			been here, done that
			return false;
		}

		Node top = getTopNode();
		top.setHasBeenTraversed(false);
		
		if (!top.hasChild(intent)) {
			return false; // this avoids problems with repetitions
		}
		
		Node forExpansion = top.getChildNode(intent);
		if (forExpansion.isExpanded()) {
			return true; // another case of repetition
		}
		
		forExpansion.setExpanded(true);
		for (String concept : getPossibleConceptsForIntent(intent)) {
			Node n = new Node(concept);
			n.setProbability(dist.getProbabilityForItem(concept));
			forExpansion.addChild(n);
		}
		forExpansion.setHasBeenTraversed(true);
		addExpected(intent);
//		remainingIntents = getPossibleConceptsForIntent(intent);
//		System.out.println("remainingIntents: " + remainingIntents);
		this.branchIntents();
		return true;
	}

	private void addExpected(String intent) {
		expectedStack.push(intent);
	}

	private boolean intentSettled(String intent) {
//		if (intent.equals(Constants.INTENT)) return false;
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
		} 
		catch (SQLException e) {
			e.printStackTrace();
		}
		return new LinkedList<String>();
	}
	
	private List<String> getPossibleConceptsForIntent(String intent) {
		return INLUModule.model.getPossibleIntentsForConcept(intent);
	}
	
	private void pushExpandedNode(Node n) {
		n.setToConsider(false);
		expandedNodes.push(n);
	}
	
	private Node popExpandedNode() {
		if (!expandedNodes.isEmpty())
			expandedNodes.peek().setExpanded(false);
		return expandedNodes.pop();
	}
	
	public Node getTopNode() {
		return expandedNodes.peek();
	}
	
	private Node getRootNode() {
		return expandedNodes.peekLast();
	}
	
	public void update() {
			tree = new TraversableTree(this.getRootNode());
			tree.setDepth(getNodeDepth());
			String json = tree.getJsonString();
			if (this.isIncremental()) 			
				send(json);
			else {
				String json2 = tree.getJsonStringForWords(wordStack);
				send(json2);
			}
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
		root.setHasBeenTraversed(true);
		this.clearConfirmStack();
		this.clearExpandedStack();
		this.pushExpandedNode(root);
		this.branchIntents();
		this.setCurrentIntent(Constants.ROOT_NAME);
		this.setFirstDisplay(false);
		if (update)
			update();
	}

	private void clearExpandedStack() {
		expandedNodes.clear();
	}

	private void branchIntents() {
		Node top = getTopNode();
		boolean traversed = remainingIntents.size() == 1 ? true : false;
		for (String intent : remainingIntents) {
			Node i = new Node(intent);
			i.setHasBeenTraversed(traversed);
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
		clearExpectedStack();
		
		System.out.println("setting new intent: " + currentIntent);
		if (currentIntent == null || currentIntent.equals("")) currentIntent = Constants.ROOT_NAME;
		this.currentIntent = currentIntent;

	}

	private void clearExpectedStack() {
		if (expectedStack != null) 
			this.expectedStack.clear();
	}

	public PropertySheet getPropertySheet() {
		return propertySheet;
	}

	public void setPropertySheet(PropertySheet propertySheet) {
		this.propertySheet = propertySheet;
	}

	public boolean isIncremental() {
		return isIncremental;
	}

	public void setIncremental(boolean isIncremental) {
		this.isIncremental = isIncremental;
	}
}

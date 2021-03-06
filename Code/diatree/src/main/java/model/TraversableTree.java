package model;

import java.util.LinkedList;
import java.util.TreeSet;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class TraversableTree {
	

	
	private Node root;
	private LinkedList<Node> trail;
	private int depth;
	private Node currentActiveNode;
	
	public TraversableTree() {
		this.setRoot(null);
		trail = new LinkedList<Node>();
	}
	
	public TraversableTree(Node r) {
		this.setRoot(r);
		trail = new LinkedList<Node>();
	}
	
	public boolean hasCurrent() {
		return !trail.isEmpty();
	}
	
	public void newWord(String word) {
		word = word.toLowerCase();
		if (!this.hasCurrent()) {
			if (getRoot().getName().equals(word)) { 
				getRoot().toggleHasBeenTraversed();
				addCurrent(getRoot());
			}
		}
		else {
			for (Node child : getCurrent().getChildren()) {
				if (child.getName().equals(word)) {
					child.toggleHasBeenTraversed();
					addCurrent(child);
				}
			}
		}
	}

	public Node getRoot() {
		return root;
	}

	public void setRoot(Node root) {
		this.root = root;
	}

	public Node getCurrent() {
		return trail.peek();
	}
	
	public void addCurrent(Node n) {
		trail.push(n);
	}
	
	public boolean inTrail(String n) {
		for (Node t : this.trail) 
			if (t.getName().equals(n)) {
				return true;
			}
		return false;
	}
	
	public String getJsonStringForWords(LinkedList<String> words) {
		JSONObject root = new JSONObject();
		String out = "";
		for (String word : words) out = word + " " + out;
		try {
			root.put("words", out.trim());
		} 
		catch (JSONException e) {
			e.printStackTrace();
		}
		return root.toString();
	}

	public String getJsonString() {
		
		try {
			if (getRoot() == null) return null;
			JSONObject rootJSON = new JSONObject();
			rootJSON.put("name", getRoot().getName());
			rootJSON.put("iden", getRoot().getIden());
			rootJSON.put("depth", getDepth());
			if (getRoot().hasBeenTraversed()) {
				rootJSON.put("type", "red");
				rootJSON.put("level", "red");
				setCurrentActiveNode(getRoot());
			}
			jsonHelp(root, rootJSON);
			
			return rootJSON.toString();
		} catch (JSONException e) {
			
			e.printStackTrace();
		}
		
		return null;
	}
	
	private int getDepth() {
		return this.depth;
	}

	private void jsonHelp(Node n, JSONObject j) throws JSONException {
		
		if (n.getChildren().size() > 0) {
			JSONArray childrenJSON = new JSONArray();
			j.put("children", childrenJSON);
			
			int i=0;
			TreeSet<Node> children = new TreeSet<Node>(); //the order might be off, so we can reorder things this way
			for (Node child : n.getChildren()) children.add(child);
			for (Node c : children) {
				JSONObject child = new JSONObject();
				child.put("name", c.getName());
				child.put("iden", c.getIden());
				if (c.hasBeenTraversed()) {
					child.put("type", "red");
					child.put("level", "red");
					setCurrentActiveNode(c);
				}
				else if (c.isToConsider()) {
					child.put("type", "steelblue");
					child.put("level", "steelblue");
				}
				childrenJSON.put(i++, child);
				if (i >= Constants.NODE_DISPLAY_LIMIT) {
					child.put("name", "...");
					child.put("iden", "...");
					break;
				}
				
				jsonHelp(c, child);
			}
		}
		
	}

	public void resetTraversal() {
		helpResetTraversal(getRoot());
		this.trail.clear();
	}

	private void helpResetTraversal(Node n) {
		n.toggleOffHasBeenTraversed();
		for (Node c : n.getChildren())
			helpResetTraversal(c);
	}

	public void revokeWord(String word) {
		if (trail.isEmpty()) return;
		if (trail.peek().getName().equals(word.toLowerCase())) {
			trail.peek().toggleOffHasBeenTraversed();
			trail.pop();
		}
	}

	public String getJsonForFrame(Frame predictedFrame) {
		
		Frame argmaxFrame = predictedFrame.getArgMaxFrame();
		
		this.setRoot(new Node(""));
		if (this.getCurrent() == null) this.addCurrent(getRoot());
		
		if (argmaxFrame.size() == 0) {
			for (String intent : predictedFrame.getIntents())
				this.getRoot().addChild(new Node(intent));
				this.addCurrent(getRoot());
		}
		else {
			for (String intent : predictedFrame.getArgMaxFrame().getIntents()) {
				if (!this.inTrail(intent)) {
					Node current = getCurrent();
					Node n = new Node(intent + ":"+ predictedFrame.getValueForIntent(intent));
					current.addChild(n);
					this.addCurrent(n);
				}
				else {
					
				}
			}
		}
		
		
		return getJsonString();
	}

	public void clear() {
		this.trail.clear();
		this.setRoot(null);
	}

	public void setDepth(int nodeDepth) {
		this.depth = nodeDepth;
		
	}

	public Node getCurrentActiveNode() {
		return currentActiveNode;
	}

	public void setCurrentActiveNode(Node currentActiveNode) {
		this.currentActiveNode = currentActiveNode;
	}
	
	

}

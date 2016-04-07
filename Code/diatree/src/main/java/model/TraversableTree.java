package model;

import java.util.LinkedList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class TraversableTree {
	

	
	private Node root;
	private LinkedList<Node> trail;
	
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
			System.out.println("current: " + this.getCurrent().getName());
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

	public String getJsonString() {
		
		try {
			JSONObject rootJSON = new JSONObject();
			rootJSON.put("name", getRoot().getName());
			if (getRoot().hasBeenTraversed()) {
				rootJSON.put("type", "red");
				rootJSON.put("level", "red");
			}
			jsonHelp(root, rootJSON);
			
			return rootJSON.toString();
		} catch (JSONException e) {
			
			e.printStackTrace();
		}
		
		return null;
	}
	
	private void jsonHelp(Node n, JSONObject j) throws JSONException {
		
		if (n.getChildren().size() > 0) {
			JSONArray childrenJSON = new JSONArray();
			j.put("children", childrenJSON);
			
			int i=0;
			for (Node c : n.getChildren()) {
				JSONObject child = new JSONObject();
				child.put("name", c.getName());
				if (c.hasBeenTraversed()) {
					child.put("type", "red");
					child.put("level", "red");
				}
				childrenJSON.put(i++, child);
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
	
	

}

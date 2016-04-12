package model;

import java.util.TreeSet;


public class Node implements Comparable<Node> {
	
	private String name;
	private TreeSet<Node> children;
	private boolean hasBeenTraversed;
	private boolean isExpanded;
	
	public Node(String n) {
		this.setName(n);
		this.children = new TreeSet<Node>();
	}
	public void clearChildren() {
		children.clear();
	}
	public void addChild(Node child) {
		children.add(child);
	}
	public TreeSet<Node> getChildren() {
		return children;
	}
	public boolean hasBeenTraversed() {
		return hasBeenTraversed;
	}
	public void setHasBeenTraversed(boolean hasBeenTraversed) {
		this.hasBeenTraversed = hasBeenTraversed;
	}
	public void toggleHasBeenTraversed() {
		setHasBeenTraversed(true);
	}
	public void toggleOffHasBeenTraversed() {
		setHasBeenTraversed(false);
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name.toLowerCase();
	}
	@Override
	public int compareTo(Node o) {
		return this.getName().compareTo(o.getName());
	}
	public Node getChildNode(String intent) {
		for (Node child : getChildren())
			if (child.getName().equals(intent))
				return child;
		return null;
	}
	public String toString() {
		return "node:"+this.getName() + " children:" + this.getChildren();
	}
	public boolean hasChild(String intent) {
		Node n = getChildNode(intent);
		if (n == null) return false;
		return true;
	}
	public boolean isExpanded() {
		return isExpanded;
	}
	public void setExpanded(boolean isExpanded) {
		this.isExpanded = isExpanded;
	}
}
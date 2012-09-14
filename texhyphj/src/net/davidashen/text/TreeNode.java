package net.davidashen.text;

import java.util.Hashtable;
import java.util.Map;

/**
 * Tree structure for representing hyphenation rules in a type safe manner.
 * 
 */
public class TreeNode {
	final private String segment;
	final private int[] hyphenation;
	final private boolean blank;
	final private Map<Character, TreeNode> children = new Hashtable<Character, TreeNode>();

	/**
	 * Create a root node to create all other nodes inside.
	 */
	public static TreeNode createRoot() {
		return new TreeNode("");
	}

	/**
	 * Create a node with no hyphenation information (a blank node).
	 */
	public TreeNode(String segment) {
		this.segment = segment;
		this.hyphenation = new int[segment.length() + 1];
		this.blank = true;
	}

	/**
	 * Create a node with hyphenation information.
	 */
	public TreeNode(String segment, int[] hyphenationData) {
		this.segment = segment;
		this.hyphenation = hyphenationData;
		this.blank = false;
	}

	/**
	 * Add a child rule to this node. The child node must match a longer, more
	 * specialized, segment than the segment of the node it is added to.
	 * Grandchildren will recursively added to children of this node.
	 * 
	 * @param segment
	 *            The string of text that this rule matches against
	 * @param hyphenation
	 *            They hypenation information for this match
	 */
	public void createChild(String segment, int[] hyphenation) {
		if (!segment.startsWith(this.segment)) {
			throw new IllegalArgumentException("Can not add child \'" + segment
					+ "\' to parent \'" + this.segment + "\'");
		}

		TreeNode node = new TreeNode(segment, hyphenation);

		if (segment.length() == this.segment.length() + 1) {
			children.put(segment.charAt(segment.length() - 1), node);
		} else {
			final char keyCharacter = segment.charAt(this.segment.length());
			getChild(keyCharacter).createChild(segment, hyphenation);
		}
	}

	public String getSegment() {
		return segment;
	}

	public char getLastCharacter() {
		return segment.charAt(segment.length() - 1);
	}

	public int[] getHyphenation() {
		return hyphenation;
	}

	public TreeNode getChild(char c) {
		return children.get(c);
	}

	/**
	 * Is this the root node onto which all other nodes should be added?
	 */
	public boolean isRoot() {
		return this.segment == "";
	}

	/**
	 * Node is only a place holder required by the tree structure.
	 */
	public boolean isBlank() {
		return this.blank;
	}
}

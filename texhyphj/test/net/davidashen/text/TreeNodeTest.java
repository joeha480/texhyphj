package net.davidashen.text;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertArrayEquals;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasProperty;

public class TreeNodeTest {

	@Test
	public void rootNodeHaAnEmptySegment() {
		TreeNode root = TreeNode.createRoot();

		assertThat("root should be root", root.isRoot());
		assertThat("root shold be blank", root.isBlank());
		assertThat("root segment", root.getSegment(), equalTo(""));
	}

	// TODO: Check that int[] is always one element longer than segment String.

	@Test
	public void shouldHaveWeakestPossibleHyphenationByDefault() {
		TreeNode node = new TreeNode("watson");

		int[] defaultHypenation = node.getHyphenation();
		for (int i = 0; i < defaultHypenation.length; i++) {
			assertThat("Default hypenation is all 0. ELement [" + i + "]",
					defaultHypenation[i], equalTo(0));
		}
		assertThat("Default hypernation is blank", node.isBlank());
	}

	@Test
	public void canAddChildToRoot() {
		TreeNode node = TreeNode.createRoot();

		node.createChild("a", new int[] { 0, 0 });

		TreeNode childNode = node.getChild('a');
		assertThat("child node", childNode,
				hasProperty("segment", equalTo("a")));
		assertThat("child node", childNode,
				hasProperty("lastCharacter", equalTo('a')));
		assertArrayEquals("child node hyphenation", new int[] { 0, 0 },
				childNode.getHyphenation());
	}

	@Test
	public void canAddDirectChild() {
		TreeNode node = new TreeNode("z");
		node.createChild("za", new int[] { 0, 0, 1 });

		TreeNode childNode = node.getChild('a');
		assertThat("child node", childNode,
				hasProperty("segment", equalTo("za")));
		assertThat("child node", childNode,
				hasProperty("lastCharacter", equalTo('a')));
		assertArrayEquals("child node hyphenation", new int[] { 0, 0, 1 },
				childNode.getHyphenation());
	}

	@Test
	public void grandChildShouldBeCreatedAsLeaf() {
		TreeNode node = new TreeNode("z");
		node.createChild("za", new int[] { 0, 0, 1 });
		node.createChild("zab", new int[] { 0, 5, 2, 0 });

		TreeNode childNode = node.getChild('a');
		TreeNode grandchildNode = childNode.getChild('b');

		assertThat("grandchild node", grandchildNode,
				hasProperty("segment", equalTo("zab")));
		assertThat("grandchild node", grandchildNode,
				hasProperty("lastCharacter", equalTo('b')));
		assertArrayEquals("grandchild hyphenation", new int[] { 0, 5, 2, 0 },
				grandchildNode.getHyphenation());
	}

	@Test(expected = RuntimeException.class)
	public void canNotAddChildToLongerSegment() {
		TreeNode node = new TreeNode("aa");
		node.createChild("a", new int[] { 0, 1 });
	}

	@Test(expected = RuntimeException.class)
	public void aChildCanNotBeCreatedOnTheWrongNode() {
		TreeNode node = new TreeNode("a");
		node.createChild("za", new int[] { 0, 0, 1 });
	}
	
	@Test
	public void addGrandchildCreatesChildren() {
		TreeNode root = TreeNode.createRoot();
		root.createChild("za", new int[] { 0, 0, 1 });
		
		TreeNode middleNode = root.getChild('z');
		assertThat(middleNode, hasProperty("segment", equalTo("z")));
		assertThat(middleNode, hasProperty("blank", equalTo(true)));
	}

	
	@Test 
	public void thatNewNodeCanBeCreatedFromPattern() {
		//Digit first
		TreeNode zb = TreeNode.createFromPattern("4zb");
		assertEquals("node segment", "zb", zb.getSegment());
		assertArrayEquals("hyphenation", new int[] { 4, 0, 0 }, zb.getHyphenation());

		//Digit in the middle
		TreeNode zero = TreeNode.createFromPattern("ze3ro");
		assertEquals("node segment", "zero", zero.getSegment());
		assertArrayEquals("hyphenation", new int[] { 0,0, 3,0,0 }, zero.getHyphenation());

		//Digit at the end
		TreeNode za1 = TreeNode.createFromPattern("za1");
		assertEquals("node segment", "za", za1.getSegment());
		assertArrayEquals("hyphenation", new int[] { 0, 0, 1 }, za1.getHyphenation());
	} 
	
	//TODO: Support control numbers higher than 9

	
	@Test 
	public void thatChildNodeCanBeCreatedFromPattern() {
		TreeNode root = TreeNode.createRoot();
		root.createChildFromPattern("a1");
		
		TreeNode child = root.getChild('a');
		assertThat( child,
				hasProperty("segment", equalTo("a")));
		assertArrayEquals("grandchild hyphenation", new int[] { 0, 1 },
				child.getHyphenation());
	}
	
	// TODO: Replace existing blank node with concrete node.
	// TODO: Exception on (non-blank) duplicate

	// TODO: Generate List structures for consumption in Hyphenator
}

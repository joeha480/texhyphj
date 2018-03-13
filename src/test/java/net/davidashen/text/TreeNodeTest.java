package net.davidashen.text;

import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.util.LinkedList;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import net.davidashen.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class TreeNodeTest {
	private static Logger log;
	private static TreeNodeTest.EavesdroppingLogHandler logHandler; 
	
	@Before
	public  void enableLogHandler() {
		log = Logger.getLogger(TreeNode.class.getCanonicalName());
		logHandler = new EavesdroppingLogHandler();
		log.addHandler(logHandler);
	}
	
	@After
	public void disableLogHandler(){
		log.removeHandler(logHandler);
	}
	
	//// Tests ////

	@Test
	public void rootNodeHaAnEmptySegment() {
		TreeNode root = TreeNode.createRoot();

		assertThat("root should be root", root.isRoot());
		assertThat("root shold be blank", root.isBlank());
		assertThat("root segment", root.getSegment(), equalTo(""));
	}

	@Test(expected = RuntimeException.class)
	public void shouldNotAllowHyphenIntArrayToBeToShort() {
		new TreeNode("short", new int[]{1,2,3,4,5});
	}

	@Test(expected = RuntimeException.class)
	public void shouldNotAllowHyphenIntArrayToBeToLong() {
		new TreeNode("long", new int[]{1,2,3,4,5,6});
	}

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

	/**
	 * Log cases where there is more than one digit of hyphenation between two characters.
	 * This may be a sign of an incorrect patterns file.
	 */
	@Test 
	public void logWarningIfHyphenationIsHigherThan9() {
		log.setLevel(Level.WARNING);
		
		TreeNode zb = TreeNode.createFromPattern("z12b");
		assertEquals("node segment", "zb", zb.getSegment());
		assertArrayEquals("hyphenation", new int[] { 0, 12, 0 }, zb.getHyphenation());
		
		assertThat(logHandler.records, hasItem(hasProperty("message", containsString("z12b"))));
	} 

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
	
	@Test 
	public void shouldReplacePlaceholderNode() {
		TreeNode root = TreeNode.createRoot();
		root.createChildFromPattern("f2oo");
		root.createChildFromPattern("1f");
		
		assertEquals("(f [1, 0] (o [0, 0, 0] (o [0, 2, 0, 0])))",
				root.getChild('f').toList().describe());
	}


	/**
	 * Log warning on duplicate node.
	 * This may be a sign of an incorrect patterns file.
	 */
	@Test 
	@SuppressWarnings("unchecked")
	public void shouldWarnOnDuplicateNode() {
		TreeNode root = TreeNode.createRoot();
		root.createChildFromPattern("f1oo");
		root.createChildFromPattern("f2oo");
		
		assertEquals("(f [0, 0] (o [0, 0, 0] (o [0, 2, 0, 0])))",
				root.getChild('f').toList().describe());

		assertThat(logHandler.records, hasItem(hasProperty("message", allOf(containsString("f1oo"), containsString("f2oo")))));
	}


	@Test
	@SuppressWarnings("unchecked")
	public void canProduceListStructure() {
		TreeNode root = TreeNode.createFromPattern("z");
		root.createChildFromPattern("za1");

		//Expected: ('z' [0,0] ('a' [0,0,1]) )
		List list = root.toList();
		assertThat("z rule", list.head(), allOf(instanceOf(Character.class), equalTo('z')));
		assertArrayEquals("z rule", new int[]{0,0}, (int[])list.longTail().head());
		assertThat("z list length", list.length(), equalTo(3));
		
		List za = (List)list.last();
		assertThat("za rule", za.head(), allOf(instanceOf(Character.class), equalTo('a')));
		assertArrayEquals(new int[]{0,0,1}, (int[])za.longTail().head());
		assertThat("za list length", za.length(), equalTo(2));
	}

	@Test
	public void listStructureWithALphabeticalOrder() {
		TreeNode root = TreeNode.createRoot();
		root.createChildFromPattern("z1b");
		root.createChildFromPattern("z1a");
		root.createChildFromPattern("z1d");
		root.createChildFromPattern("z1c");
		
		assertEquals(
				"(z [0, 0] (a [0, 1, 0]) (b [0, 1, 0]) (c [0, 1, 0]) (d [0, 1, 0]))",
				root.getChild('z').toList().describe());
	}

	@Test
	public void canProduceListStructureFromRoot() {
		TreeNode root = TreeNode.createRoot();
		root.createChildFromPattern("x1a");
		root.createChildFromPattern("y2b");
		root.createChildFromPattern("z3a");
		root.createChildFromPattern("z4b");
		
		assertEquals(
				"((x [0, 0] (a [0, 1, 0])) (y [0, 0] (b [0, 2, 0])) (z [0, 0] (a [0, 3, 0]) (b [0, 4, 0])))",
				root.toList().describe());
	}	
	
	@Test
	public void generatedListStructureWorksWithHyphenator() {
		TreeNode root = TreeNode.createRoot();
		root.createChildFromPattern("1own");
		root.createChildFromPattern("v2e");
		root.createChildFromPattern("la3");
		
		RuleDefinition scanner = new TreeNodeScanner(root);
		
		Hyphenator hyphenator = new Hyphenator();
		hyphenator.setRuleSet(scanner);
		
		String actual = hyphenator.hyphenate("The quick brown fox jumps over the lazy dog.");
		String expected = "The quick br\u00adown fox jumps over the la\u00adzy dog.";
		assertEquals(expected, actual);
	}
	

	public static class TreeNodeScanner implements RuleDefinition  {
		private final TreeNode rootNode;
		
		public TreeNodeScanner(TreeNode root) {
			rootNode = root;
		}
		
		public int[] getException(String word) {
			return null;
		}
		
		public List getPatternTree(int c) {
			List list = new List();
			if(rootNode.hasChild((char)c)){
				list.snoc(rootNode.getChild((char)c).toList());
			} 
			return list;
		}
	}

	public class EavesdroppingLogHandler extends Handler {
		private java.util.List<Object> records =new LinkedList<Object>();

		@Override
		public void publish(LogRecord record) {
			records.add(record);
		}

		@Override
		public void flush() {/* Do nothing*/}

		@Override
		public void close() throws SecurityException { /* Do nothing */	}
	}

}

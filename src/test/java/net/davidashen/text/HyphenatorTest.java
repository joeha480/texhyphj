package net.davidashen.text;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.util.Enumeration;

import net.davidashen.util.List;

import org.junit.Ignore;
import org.junit.Test;

@SuppressWarnings("javadoc")
public class HyphenatorTest {

	@Test
	public void simplestPossibleTest() {
		Hyphenator hyphenator = new Hyphenator();
		hyphenator.setRuleSet(new RuleDefinition() {

			public int[] getException(String word) {
				//No exceptions
				return null;
			}

			public List getPatternTree(int c) {
				return new List();
			}
		});
		
		String result  = hyphenator.hyphenate("Continues the work by David Tolpin. Specifically, adding UTF-8 support for pattern files.");
		String expected = "Continues the work by David Tolpin. Specifically, adding UTF-\u200b8 support for pattern files.";
		assertEquals(expected, result);
	} 
	
	/**
	 * Test using smallest possible rule set
	 */
	@Test
	public void singleRule() {
		Hyphenator hyphenator = new Hyphenator();
		hyphenator.setRuleSet(new RuleDefinition() {

			public int[] getException(String word) {
				//No exceptions
				return null;
			}

			/**
			 * Returns a rules wrapped in an outer list.
			 */
			public List getPatternTree(int c) {
				List outerList = new List();
				List innerList = new List();
				innerList.snoc(new Character('i'));
				innerList.snoc(new int[]{1,0});
				outerList.snoc(innerList);
				return outerList;
			}
		});
		
		String result  = hyphenator.hyphenate("Continues the work by David Tolpin. Specifically, adding UTF-8 support for pattern files.");
		String expected = "Cont\u00adinues the work by Dav\u00adid Tolp\u00adin. Spec\u00adif\u00adically, add\u00ading UTF-\u200b8 support for pattern f\u00adiles.";
		assertEquals(expected, result);
	} 
	
	@Test
	public void useRealGrammer() throws FileNotFoundException, IOException {
		//u00ad is soft hyphen
		Hyphenator hyphenator = new Hyphenator();
		
		
		hyphenator.loadTable(this.getClass().getResource("/ushyph.tex").openStream());
		
		String result  = hyphenator.hyphenate("Continues the work by David Tolpin. Specifically, adding UTF-8 support for pattern files.");
		String expected = "Con\u00adtin\u00adues the work by David Tolpin. Specif\u00adi\u00adcal\u00adly, adding UT\u00adF-\u200b8 sup\u00adport for pat\u00adtern files.";
		assertEquals(expected, result);
	}
	
	@Test
	public void useRealGrammerWithLimit() throws FileNotFoundException, IOException {
		//u00ad is soft hyphen
		Hyphenator hyphenator = new Hyphenator();
		
		int leftHyphenMin = 4;
		int rightHyphenMin = 1;
		
		hyphenator.loadTable(this.getClass().getResource("/ushyph.tex").openStream());
	
//		System.out.println("Rule list for 'z':");
//		List list = hyphenator.getRuleSet().getList('z');
//		printObject(list);
		
		final String inputPhrase = "Continues the work by David Tolpin. Specifically, adding UTF-8 support for pattern files.";
		String result  = hyphenator.hyphenate(inputPhrase, leftHyphenMin, rightHyphenMin);
		String expected = "Contin\u00adues the work by David Tolpin. Specif\u00adi\u00adcal\u00adly, adding UTF-\u200b8 support for pattern files.";
		assertEquals(expected, result);
	}
	
	/**
	 * Hyphenates a large file ('The adventures of sherlock holmes') and check that the behaviour 
	 * has not changed since the last time the comparision file was created.   
	 */
	@Test
	public void useWithRealGrammerAndComparisionFile() throws FileNotFoundException, IOException {
		//u00ad is soft hyphen
		Hyphenator hyphenator = new Hyphenator();
		hyphenator.loadTable(this.getClass().getResource("/ushyph.tex").openStream());
		Charset utf8 = Charset.forName("UTF-8");
		
		InputStreamReader reader = new InputStreamReader(this.getClass().getResource("/sherlock.txt").openStream(), utf8);
		LineNumberReader input = new LineNumberReader(reader);
		
		InputStreamReader reader2 = new InputStreamReader(this.getClass().getResource("/sherlock-expected.txt").openStream(), utf8);
		LineNumberReader expected = new LineNumberReader(reader2);
		
		String inputLine;
		String expectedLine;
		
		int lineNumber = 1; 
		while ((inputLine=input.readLine())!=null & (expectedLine=expected.readLine())!=null) {
			String actualLine = hyphenator.hyphenate(inputLine);
			assertEquals("Line #" + (lineNumber++), expectedLine, actualLine);
		}
		assertEquals(inputLine, null);
		assertEquals(expectedLine, null);
		
		input.close();
		expected.close();
	}

	
	/**
	 * Check some samples lines with words from sv-dictionary-expected.txt
	 * 
	 * (This test is a lot faster than using the whole dictionary, but also less complete)  
	 */
	@Test
	public void useWithUtf8TexParser() throws Exception {
		//u00ad is soft hyphen
		Charset utf8 = Charset.forName("UTF-8");
		
		Hyphenator hyphenator = new Hyphenator();
		Utf8TexParser parser = new Utf8TexParser();
		final InputStreamReader ruleFileReader = new InputStreamReader(this.getClass().getResourceAsStream("/hyph-sv-utf8.tex"), utf8);
		RuleDefinition r = parser.parse(ruleFileReader);
		hyphenator.setRuleSet(r);
		ruleFileReader.close();

		assertHyphenation(hyphenator, "Blom­mi­ga­re bön­der en­vi­sa­des med att styv­fas­ter in-te rag-lar på torg.");
		assertHyphenation(hyphenator, "Kall-pra-ta om te­ma­in­rik­ta­de kon­junk­tur­väx­ling­ar med män­ni­sko­rätts­ak­ti­vis­ter.");
		assertHyphenation(hyphenator, "Röst­be­rät­ti­ga­de tvil­ling­föds­lar re­sul­te­rar i re­ha­bi­li­te­rings­pla­ne­ring.");
		assertHyphenation(hyphenator, "tvärs-över");
	}
	
	

	
	/**
	 * Hyphenates a large file ('Swedish list of words') and hyphenated using a reference implementation,
	 * and check that the behaviour has not changed when using the new utf-8 parser.   
	 * 
	 * This test takes roughly 15 seconds.
	 */
	@Test
	public void useWithRealGrammerAndUTF8ComparisionFile() throws Exception {
		//u00ad is soft hyphen
		Charset utf8 = Charset.forName("UTF-8");
		
		Hyphenator hyphenator = new Hyphenator();
		Utf8TexParser parser = new Utf8TexParser();
		final InputStreamReader ruleFileReader = new InputStreamReader(this.getClass().getResourceAsStream("/hyph-sv-utf8.tex"), utf8);
		RuleDefinition r = parser.parse(ruleFileReader);
		hyphenator.setRuleSet(r);
		ruleFileReader.close();

		InputStreamReader reader = new InputStreamReader(this.getClass().getResource("/sv-dictionary-input.txt").openStream(), utf8);
		LineNumberReader input = new LineNumberReader(reader);
		
		InputStreamReader reader2 = new InputStreamReader(this.getClass().getResource("/sv-dictionary-expected.txt").openStream(), utf8);
		LineNumberReader expected = new LineNumberReader(reader2);
		
		String inputLine;
		String expectedLine;
		
		int lineNumber = 1; 
		while ((inputLine=input.readLine())!=null & (expectedLine=expected.readLine())!=null) {
			String actualLine = hyphenator.hyphenate(inputLine, 2,2);
			assertEquals("Line #" + (lineNumber++), expectedLine, actualLine);
			
			if(lineNumber % 50000 == 0) {
				System.out.println("Line #" + (lineNumber) + ": " + actualLine);
			}
		}
		assertEquals(inputLine, null);
		assertEquals(expectedLine, null);
		
		input.close();
		expected.close();
	}

	/**
	 * This generates a new file to use as expected result in the test 'useRealGrammerFile' above.
	 *  
	 *  Do not run as a test!
	 */
	@Test
	@Ignore
	public void createComparisionFile() throws FileNotFoundException, IOException {
		//u00ad is soft hyphen
		Hyphenator hyphenator = new Hyphenator();
		hyphenator.loadTable(this.getClass().getResource("/ushyph.tex").openStream());
		Charset utf8 = Charset.forName("UTF-8");
		
		InputStreamReader reader = new InputStreamReader(this.getClass().getResource("/sherlock.txt").openStream(), utf8);
		LineNumberReader input = new LineNumberReader(reader);
		
		OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(new File("out.txt")), utf8);
		PrintWriter output = new PrintWriter(writer);

		String inputLine;
		while ((inputLine=input.readLine())!=null) {
			String hyphenatedLine = hyphenator.hyphenate(inputLine);
			output.println(hyphenatedLine);
		}
		input.close();
		output.close();
	}
	
	//// Helpers (debugging) ///

	/**
	 * Remove all hard hyphens, then hyphenate and assert that soft hyphens have 
	 * appeared where the hard hyphens used to be.  
	 */
	private void assertHyphenation(Hyphenator hyphenator, String hyphenatedText) {
		String input = hyphenatedText.replace("-", "");
		String expected = hyphenatedText.replace("-", "\u00ad");
		assertEquals(expected, hyphenator.hyphenate(input, 2, 2));
	}
	
	@SuppressWarnings("unused")
	private void printList(List list) {
		printList(list, 0);
	}
	
	@SuppressWarnings("rawtypes")
	private void printList(final List list, final int level) {
		Enumeration enumeration = list.elements();
		
		while (enumeration.hasMoreElements()) {
			Object o = enumeration.nextElement();
			printObject(o, level);
		}
		
	}
	
	@SuppressWarnings("unused")
	private void printObject(final Object o) {
		printObject(o, 0);
	}
	
	private void printObject(final Object o, final int level) {
		for (int i=0; i<level; i++) {
			System.out.print("\t");
		}
		System.out.print(o.getClass().toString() + ": ");
		
		if(o instanceof List) {
			System.out.println("( ");
			printList((List)o, level + 1);
			for (int i=0; i<level; i++) {
				System.out.print("\t");
			}
			System.out.println(")");

		} else if (o instanceof int[]){
			System.out.print("[");
			int[] ints = (int[]) o;
			String separator = "";
			for (int i : ints) {
				System.out.print(separator + i);
				separator = ", ";
			}
			System.out.println("]");
		} else {
			System.out.println(o.toString());
		}
	}
}

package net.davidashen.text;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

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

public class HyphenatorTest {

	@Test
	public void simplestPossibleTest() {
		Hyphenator hyphenator = new Hyphenator();
		hyphenator.setRuleSet(new Scanner() {

			public int[] getException(String word) {
				//No exceptions
				return null;
			}

			public List getList(int c) {
				return new List();
			}
		});
		
		String result  = hyphenator.hyphenate("Continues the work by David Tolpin. Specifically, adding UTF-8 support for pattern files.");
		String expected = "Continues the work by David Tolpin. Specifically, adding UTF-\u200b8 support for pattern files.";
		assertEquals(expected, result);
	} 
	
	/**
	 * TODO: Test using smallest possible rule set
	 */
	@Test
	@Ignore
	public void singleRule() {
		Hyphenator hyphenator = new Hyphenator();
		hyphenator.setRuleSet(new Scanner() {

			public int[] getException(String word) {
				//No exceptions
				return null;
			}

			public List getList(int c) {
				List list = new List();
				return list;
			}
		});
		
		fail("Not complete!");
		
		String result  = hyphenator.hyphenate("Continues the work by David Tolpin. Specifically, adding UTF-8 support for pattern files.");
		String expected = "Continues the work by David Tolpin. Specifically, adding UTF-\u200b8 support for pattern files.";
		assertEquals(expected, result);
	} 
	
	@Test
	public void useRealGrammer() throws FileNotFoundException, IOException {
		//u00ad is soft hyphen
		Hyphenator hyphenator = new Hyphenator();
		
		
		hyphenator.loadTable(this.getClass().getResource("resource-files/ushyph.tex").openStream());
		
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
		
		hyphenator.loadTable(this.getClass().getResource("resource-files/ushyph.tex").openStream());
		
		System.out.println("Rule list for 'z':");
		List list = hyphenator.getRuleSet().getList('z');
		printList(list);
		
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
		hyphenator.loadTable(this.getClass().getResource("resource-files/ushyph.tex").openStream());
		Charset utf8 = Charset.forName("UTF-8");
		
		InputStreamReader reader = new InputStreamReader(this.getClass().getResource("resource-files/sherlock.txt").openStream(), utf8);
		LineNumberReader input = new LineNumberReader(reader);
		
		InputStreamReader reader2 = new InputStreamReader(this.getClass().getResource("resource-files/sherlock-expected.txt").openStream(), utf8);
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
	 * This generates a new file to use as expected result in the test 'useRealGrammerFile' above.
	 *  
	 *  Do not run as a test!
	 */
	@Test
	@Ignore
	public void createComparisionFile() throws FileNotFoundException, IOException {
		//u00ad is soft hyphen
		Hyphenator hyphenator = new Hyphenator();
		hyphenator.loadTable(this.getClass().getResource("resource-files/ushyph.tex").openStream());
		Charset utf8 = Charset.forName("UTF-8");
		
		InputStreamReader reader = new InputStreamReader(this.getClass().getResource("resource-files/sherlock.txt").openStream(), utf8);
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
	
	//// Helpers ///
	private void printList(List list) {
		printList(list, 0);
	}
	
	private void printList(final List list, final int level) {
		Enumeration enumeration = list.elements();
		
		while (enumeration.hasMoreElements()) {
			Object o = enumeration.nextElement();
			
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

	
}

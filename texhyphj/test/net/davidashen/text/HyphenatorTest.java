package net.davidashen.text;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.nio.charset.Charset;
import java.util.Enumeration;

import net.davidashen.util.List;

import org.junit.Test;

public class HyphenatorTest {

	@Test
	public void simplestPossibleTest() {
		Hyphenator hyphenator = new Hyphenator();
		hyphenator.setScanner(new Scanner() {

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
	
	@Test
	public void singleRule() {
		Hyphenator hyphenator = new Hyphenator();
		hyphenator.setScanner(new Scanner() {

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
		
		List list = hyphenator.getScanner().getList('a');
		System.out.println(list);
		
		printList(list, 0);
		
		String result  = hyphenator.hyphenate("Continues the work by David Tolpin. Specifically, adding UTF-8 support for pattern files.", leftHyphenMin, rightHyphenMin);
		String expected = "Contin\u00adues the work by David Tolpin. Specif\u00adi\u00adcal\u00adly, adding UTF-\u200b8 support for pattern files.";
		assertEquals(expected, result);
	}
	
	@Test
	public void useRealGrammerFile() throws FileNotFoundException, IOException {
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
		
		//Uncomment to write new comparison file
		/*
		OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(new File("out.txt")), utf8);
		PrintWriter w = new PrintWriter(writer);

		while ((inputLine=input.readLine())!=null) {
			String actualLine = hyphenator.hyphenate(inputLine);
			w.println(actualLine);
		}
		w.close();*/

		//Comment out to write new comparison file
		while ((inputLine=input.readLine())!=null & (expectedLine=expected.readLine())!=null) {
			String actualLine = hyphenator.hyphenate(inputLine);
			assertEquals(expectedLine, actualLine);
		}
		assertEquals(inputLine, null);
		assertEquals(expectedLine, null);
		
		input.close();
		expected.close();
	}
	
	private void printList(List list, int level) {
		Enumeration enumeration = list.elements();
		
		while (enumeration.hasMoreElements()) {
			Object o = enumeration.nextElement();
			for (int i=0; i<level; i++) {
				System.out.print(" ");
			}
			if(o instanceof List) {
				System.out.println("- " + ((List)o).car());
				printList((List)o, level++);
			} else if (o instanceof int[]){
				int[] ints = (int[]) o;
				for (int i : ints) {
					System.out.print(i + ", ");
				}
				System.out.println("");

			} else {
				System.out.println(o.getClass().toString() + ": " + o.toString());
			}
		}
		
	}

	
}

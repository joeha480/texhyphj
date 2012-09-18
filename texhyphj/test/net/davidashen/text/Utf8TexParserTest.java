package net.davidashen.text;

import net.davidashen.util.List;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertArrayEquals;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.equalTo;


public class Utf8TexParserTest {

	@Test
	public void parseEmptySets() throws Exception {
		Utf8TexParser parser = new Utf8TexParser();
		
		RuleDefinition result = parser.parse("\\patterns{\n}\n\\hyphenation{\n}");
		
		assertThat("There are no exceptions" , result.getException(""), equalTo(null));
		assertThat("There are no patterns" , result.getPatternTree('a').isEmpty());
	} 

	
	@Test
	public void parseSinglePatterns() throws Exception {
		Utf8TexParser parser = new Utf8TexParser();
		
		RuleDefinition result = parser.parse("\\patterns{\nw2at\n}\n\\hyphenation{\n}");
		
		final List patterns = result.getPatternTree('w');
		assertEquals( "((w [0, 0] (a [0, 0, 0] (t [0, 2, 0, 0]))))", patterns.describe());
	} 


	@Test
	public void parseMultiplePatterns() throws Exception {
		Utf8TexParser parser = new Utf8TexParser();
		
		RuleDefinition result = parser.parse("\\patterns{\nbaz2\nb1a1r\n}\n\\hyphenation{\n}");
		
		final List patterns = result.getPatternTree('b');
		assertEquals( "((b [0, 0] (a [0, 0, 0] (r [0, 1, 1, 0]) (z [0, 0, 0, 2]))))", patterns.describe());
	} 

	
	@Test
	public void parseSingleException() throws Exception {
		Utf8TexParser parser = new Utf8TexParser();
		RuleDefinition result = parser.parse("\\patterns{}\n\\hyphenation{as-so-ciate}");
		
		final int[] exception = result.getException("associate");
		assertArrayEquals(new int[]{0,0,1,0,1,0,0,0,0,0}, exception);
	} 
	
	@Test
	public void parseMultipleExceptions() throws Exception {
		Utf8TexParser parser = new Utf8TexParser();
		RuleDefinition result = parser.parse("\\patterns{}\n\\hyphenation{as-so-ciate\noblig-a-tory}");
		
		final int[] associateException = result.getException("associate");
		assertArrayEquals(new int[]{0,0,1,0,1,0,0,0,0,0}, associateException);

		final int[] obligatoryException = result.getException("obligatory");
		assertArrayEquals(new int[]{0,0,0,0,0,1,1,0,0,0,0}, obligatoryException);
	}
	
	//TODO: Ignore Comments
	
	//TODO: Overlapping patterns stated longest first  baz4 b2a 1b
	
}

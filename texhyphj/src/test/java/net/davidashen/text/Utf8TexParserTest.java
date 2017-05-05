package net.davidashen.text;

import net.davidashen.util.List;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertNull;
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
		assertArrayEquals(new int[]{0,1,0,1,0,0,0,0,0,0}, exception);
	} 
	
	@Test
	public void parseMultipleExceptions() throws Exception {
		Utf8TexParser parser = new Utf8TexParser();
		RuleDefinition result = parser.parse("\\patterns{}\n\\hyphenation{as-so-ciate\noblig-a-tory}");
		
		final int[] associateException = result.getException("associate");
		assertArrayEquals(new int[]{0,1,0,1,0,0,0,0,0,0}, associateException);

		final int[] obligatoryException = result.getException("obligatory");
		assertArrayEquals(new int[]{0,0,0,0,1,1,0,0,0,0,0}, obligatoryException);
	}
	
	
	@Test
	public void ignoreComments() throws Exception {
		Utf8TexParser parser = new Utf8TexParser();
		
		RuleDefinition result = parser.parse("%line comment\n\\patterns{%patterns comment\nbaz2\nb1a1r%pattern comment\n}\n\\hyphenation%hypernations comment\n{\nas-so-ciate%hypernation comment\n}");
		
		final List patterns = result.getPatternTree('b');
		assertEquals( "((b [0, 0] (a [0, 0, 0] (r [0, 1, 1, 0]) (z [0, 0, 0, 2]))))", patterns.describe());

		final int[] exception = result.getException("associate");
		assertArrayEquals(new int[]{0,1,0,1,0,0,0,0,0,0}, exception);
	}
	
	@Test
	public void ignoreGroupsCommentedOut() throws Exception {
		Utf8TexParser parser = new Utf8TexParser();
		
		RuleDefinition result = parser.parse("%commented out \\patterns{baz2 b1a1r}}");
		
		final List patterns = result.getPatternTree('b');
		assertEquals( "()", patterns.describe());

		final int[] exception = result.getException("associate");
		assertNull(exception);
	} 
	
	@Test
	public void shouldHandleAnyPattenOrder() throws Exception {
		Utf8TexParser parser = new Utf8TexParser();
		RuleDefinition shortestFirst = parser.parse("\\patterns{1b b2a baz4}");
		RuleDefinition longestFirst = parser.parse("\\patterns{baz4 b2a 1b}");
		
		assertEquals(
				shortestFirst.getPatternTree('b').describe(),
				longestFirst.getPatternTree('b').describe()
				);
	}

	@Test(expected=Utf8TexParser.TexParserException.class)
	public void handleMissingStartBracket() throws Exception{
		Utf8TexParser parser = new Utf8TexParser();
		parser.parse("\\patterns foo bar baz}");
	}

	
	@Test(expected=Utf8TexParser.TexParserException.class)
	public void handleMissingEndingBracket() throws Exception{
		Utf8TexParser parser = new Utf8TexParser();
		parser.parse("\\patterns{foo bar baz");
	}
	

	@Test(expected=Utf8TexParser.TexParserException.class)
	public void handleCommentedOutEndingBracket() throws Exception{
		Utf8TexParser parser = new Utf8TexParser();
		parser.parse("\\patterns{foo bar baz %comment}");
	}

}

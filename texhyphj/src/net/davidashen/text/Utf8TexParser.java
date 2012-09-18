package net.davidashen.text;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Map;

import net.davidashen.util.List;

/**
 * Parses .tex files into sets of hyphenation patterns and exceptions.
 * 
 *  TODO: Throw own ParserException, not IOException
 */
public class Utf8TexParser {
	
	public RuleDefinition parse(String string) throws IOException {
		return this.parse(new StringReader(string));
	}
	
	public RuleDefinition parse(Reader reader) throws IOException  {
		TreeNode ruleRoot = TreeNode.createRoot();
		Map<String, int[]> exceptions = new Hashtable<String, int[]>();
		 
		int c = reader.read();
		while( c > -1) {
			char ch =(char)c;
			
			if(isStartOfComment(ch)) {
				ignoresRestOfLine(reader);
			} else if(ch == '\\') {
				String groupName = parseGroupName(reader);
				
				if(groupName.equals("patterns")) {
					for (String p : readWords(reader)) {
						ruleRoot.createChildFromPattern(p);
					}
				} else if (groupName.equals("hyphenation")) {
					for (String e : readWords(reader)) {
						String word = unhyphenWord(e); 
						int[] hyphenation = extractExceptionHyphenation(e);
						exceptions.put(word, hyphenation);
					}
				} else {
					throw new RuntimeException("Unknown keyword \'" + groupName + "\'");
				}
			}	
			c = reader.read();
		}

		return new TreeNodeScanner(ruleRoot, exceptions);
	}


	private static String parseGroupName(Reader reader) throws IOException {
		final StringBuffer buffer = new StringBuffer();
		
		//Read up until the next '{'
		int c = reader.read();
		while( c > -1 && ((char)c) != '{' ) {
			if(isStartOfComment((char)c)) {
				ignoresRestOfLine(reader);
			} else {
				buffer.append((char)c);
			}
			c = reader.read();
		}
 
		return buffer.toString();
	}

	/**
	 * Read a set of whitespace separated words 
	 */
	private static java.util.List<String> readWords(Reader reader) throws IOException {
		final java.util.List<String> list = new LinkedList<String>();
		StringBuffer buffer = new StringBuffer();
		
		//Read words up until the next '}'
		int c = reader.read();
		while( c > -1 &&  (char)c != '}') {
			char ch = (char) c;
			
			if(Character.isWhitespace(ch)) {
				if(buffer.length()>0) {
				list.add(buffer.toString());
				}
				buffer  = new StringBuffer();
			} else {
				if(isStartOfComment(ch)) {
					ignoresRestOfLine(reader);
				} else  {
					buffer.append(ch);
				}
			}
			c = reader.read();
		}

		if(buffer.length()>0) {
			list.add(buffer.toString());
		}
		
		return list;
	}

	private String unhyphenWord(String exceptedWord) {
		final StringBuffer buffer = new StringBuffer();
		
		for(int i=0; i < exceptedWord.length(); i++) {
			char ch = exceptedWord.charAt(i);
			if (Character.isLetter(ch)) {
				buffer.append(ch);
			}
		}
		
		return buffer.toString();
	}

	private int[] extractExceptionHyphenation(String exceptedWord) {
		int[] tmpHyphenations = new int[exceptedWord.length()];
		int characterCount = 0;
		
		//Collect hyphenation info 
		for(int i=0; i < exceptedWord.length(); i++) {
			char ch = exceptedWord.charAt(i);
			if (ch == '-') {
				tmpHyphenations[characterCount] = 1;
			} else {
				characterCount++;
			}
		}
		
		//Shorten array
		int[] trimmedHyphenations = new int[characterCount + 1];
		for(int i=0; i < trimmedHyphenations.length; i++) {
			trimmedHyphenations[i] = tmpHyphenations[i];
		}

		return trimmedHyphenations;
	}


	/**
	 * Is this character the start of a comment? 
	 * 
	 */
	private static boolean isStartOfComment(char c) {
		return c == '%';
	}

	/**
	 * Read until the end of the line, including the new line character. 
	 * 
	 * @param reader to fast forward through
	 */
	private static void ignoresRestOfLine(Reader reader) throws IOException {
		int c = reader.read();
		while(c != -1 && (char)c != '\n') {
			c = reader.read();
		}
	}


	
	private static class TreeNodeScanner implements RuleDefinition {
		final private TreeNode rulesRoot;
		final private Map<String, int[]> exceptions;
		final private Map<Character, net.davidashen.util.List> listCache = new Hashtable<Character, List>();
		
		public TreeNodeScanner(TreeNode root, Map<String, int[]> exceptions) {
			this.rulesRoot = root;
			this.exceptions = exceptions;
		}

		public int[] getException(String word) {
			return exceptions.get(word);
		}

		public List getPatternTree(int c) {
			char ch = (char) c;
			
			if(listCache.containsKey(ch)) {
				return listCache.get(ch);
			} else {
				//List creation is relatively heavy, so let's only do it once per character.
				net.davidashen.util.List list = new net.davidashen.util.List();
				
				if(rulesRoot.hasChild(ch)){
					list.snoc( rulesRoot.getChild(ch).toList());
				}
				
				listCache.put(ch, list);
				return list;
			}
		}
	}
}

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
			if( ((char)c) =='\\' ) {
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
			buffer.append((char)c);
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
				buffer.append(ch);
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
	
	private static class TreeNodeScanner implements RuleDefinition {
		final private TreeNode rulesRoot;
		final private Map<String, int[]> exceptions;
		
		public TreeNodeScanner(TreeNode root, Map<String, int[]> exceptions) {
			this.rulesRoot = root;
			this.exceptions = exceptions;
		}

		public int[] getException(String word) {
			return exceptions.get(word);
		}

		public List getPatternTree(int c) {
			net.davidashen.util.List list = new net.davidashen.util.List();
			
			if(rulesRoot.hasChild((char)c)){
				list.snoc( rulesRoot.getChild((char)c).toList());
			}
			
			return list;
		}
	}
}

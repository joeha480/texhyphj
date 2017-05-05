package net.davidashen.text;

import net.davidashen.util.List;


/**
 * Data describing how words should be hyphenated.  
 */
public interface RuleDefinition {
	

	/**
	 * Get pattern tree structure to match against character sequences.
	 *   
	 * @param c	The first character of the sequence to match 
	 * @return	Tree structure to match against
	 */
	List getPatternTree(int c);

	
	/**
	 * Get the hyphenation info for words where hyphenation patterns should not be applied. 
	 * 
	 * @param word	The word to check
	 * @return 	Hyphenation for the word or null if the word was not an exception 
	 */
	int[] getException(String word);

}

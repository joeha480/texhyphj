/* $Id: Hyphenator.java,v 1.17 2003/08/25 08:41:28 dvd Exp $ */

package net.davidashen.text;

import java.io.Reader;
import java.util.logging.Logger;

import net.davidashen.text.Utf8TexParser.TexParserException;
import net.davidashen.util.*;

/**
 * insert soft hyphens at all allowed locations uses TeX hyphenation tables
 */
public class Hyphenator {

	//Hyphens from the wikipedia article: https://en.wikipedia.org/wiki/Hyphen#Unicode
    public static final char HYPHEN =  '\u2010';
    public static final char HYPHEN_MINUS = '\u002d';
    public static final char SOFT_HYPHEN = '\u00ad';
    public static final char NON_BREAKING_HYPHEN = '\u2011';
	private static final char ZERO_WIDTH_SPACE = '\u200b';
	
	private final ForwardingErrorHandler errorHandler;
	private RuleDefinition ruleSet;
	private final ByteScanner b;

	/**
	 * creates an uninitialized instance of Hyphenator. The same instance can be
	 * reused for different hyphenation tables.
	 */
	public Hyphenator() {
		errorHandler = new ForwardingErrorHandler(new LoggingErrorHandler(Logger.getLogger(this.getClass().getCanonicalName())));
		b = new ByteScanner(errorHandler);
	}

	public RuleDefinition getRuleSet() {
		return ruleSet;
	}

	public void setRuleSet(RuleDefinition scanner) {
		this.ruleSet = scanner;
	}

	public ErrorHandler getErrorHandler() {
		return errorHandler.getTarget();
	}

	/**
	 * installs error handler.
	 * 
	 * @param eh
	 *            ErrorHandler used while parsing and hyphenating
	 * @see net.davidashen.util.ErrorHandler
	 */
	public void setErrorHandler(ErrorHandler eh) {
		errorHandler.setTarget(eh);
	}

	/**
	 * <p>Loads a hyphenation table with a reader. This enables the use of UTF-8 pattern files.
	 * Note that escape codes in the original tex-files are not supported, e.g. ^^f6.
	 * This method also differs in that multiple calls to loadTable are not joined, only the 
	 * most recent pattern file is used.</p>
	 * 
	 * <p>Only "\pattern{" and "\hyphenation{" groups are supported.</p>
	 * 
	 * @param reader a reader containing hyphenation patterns (most likely a file)
	 * @throws TexParserException if there are problems reading the input
	 */
	public void loadTable(Reader reader) throws TexParserException {
		Utf8TexParser parser = new Utf8TexParser();
		ruleSet = parser.parse(reader);
	}
	
	/**
	 * loads hyphenation table
	 * 
	 * @param in
	 *            hyphenation table
	 * @throws java.io.IOException if an I/O error occurs
	 */
	public void loadTable(java.io.InputStream in) throws java.io.IOException {
		int[] codelist = new int[256];
		{
			for (int i = 0; i != 256; ++i)
				codelist[i] = i;
		}
		loadTable(in, codelist);
	}

	/**
	 * loads hyphenation table and code list for non-ucs encoding
	 * 
	 * @param in
	 *            hyphenation table
	 * @param codelist
	 *            an array of 256 elements. maps one-byte codes to UTF codes
	 * @throws java.io.IOException if an I/O error occurs
	 */
	public void loadTable(java.io.InputStream in, int[] codelist)
			throws java.io.IOException {
		b.scan(in, codelist);
		ruleSet = b;
	}

	/**
	 * performs hyphenation
	 * 
	 * @param phrase
	 *            string to hyphenate
	 * @return the string with soft hyphens inserted
	 */
	public String hyphenate(String phrase) {
		return hyphenate(phrase, 1, 1);
	}

	/**
	 * performs hyphenation
	 * 
	 * @param phrase
	 *            string to hyphenate
	 * @param leftHyphenMin
	 *            unbreakable characters at the beginning of each word in the
	 *            phrase
	 * @param rightHyphenMin
	 *            unbreakable characters at the end of each word in the phrase
	 * @return the string with soft hyphens inserted
	 */
	public String hyphenate(String phrase, int leftHyphenMin, int rightHyphenMin) {

		// Check input
		leftHyphenMin = Math.max(leftHyphenMin, 1);
		rightHyphenMin = Math.max(rightHyphenMin, 1);

		// Ignore short phrases (early out)
		if (phrase.length() < rightHyphenMin + leftHyphenMin) {
			return phrase;
		}

		int processedOffset = Integer.MIN_VALUE; 
		int ich = 0; 
		char[] sourcePhraseChars = new char[phrase.length() + 1];
		sourcePhraseChars[sourcePhraseChars.length - 1] = (char) 0;
		phrase.getChars(0, phrase.length(), sourcePhraseChars, 0);

		
		char[] hyphenatedPhraseChars = new char[sourcePhraseChars.length * 2 - 1];
		int ihy = 0;
		
		boolean inword = false;
		while (true) {
			if (inword) {
				if (Character.isLetter(sourcePhraseChars[ich])) {
					ich++;
				} else { // last character will be reprocessed in the other
							// state
					int length = ich - processedOffset;
					String word = new String(sourcePhraseChars, processedOffset, length).toLowerCase();
					int[] hyphenQualificationPoints = ruleSet
							.getException(word);

					if (hyphenQualificationPoints == null) {
						char[] wordChars = extractWord(sourcePhraseChars, processedOffset, length);
						hyphenQualificationPoints = applyHyphenationRules(
								wordChars, length);
					}

					// now inserting soft hyphens
					if (leftHyphenMin + rightHyphenMin <= length) {
						for (int i = 0; i < leftHyphenMin - 1; i++){
							hyphenatedPhraseChars[ihy++] = sourcePhraseChars[processedOffset++];
						}
						
						for (int i = leftHyphenMin - 1; i < length
								- rightHyphenMin; i++) {
							hyphenatedPhraseChars[ihy++] = sourcePhraseChars[processedOffset++];
							if (hyphenQualificationPoints[i] % 2 == 1)
								hyphenatedPhraseChars[ihy++] = SOFT_HYPHEN;
						}
						
						for (int i = length - rightHyphenMin; i < length; i++){
							hyphenatedPhraseChars[ihy++] = sourcePhraseChars[processedOffset++];
						}
					} else {
						//Word is to short to hyphenate, so just copy
						for (int i = 0; i != length; ++i){
							hyphenatedPhraseChars[ihy++] = sourcePhraseChars[processedOffset++];
						}
					}
					inword = false;
				}
			} else {
				if (Character.isLetter(sourcePhraseChars[ich])) {
					processedOffset = ich;
					inword = true; // processedOffset remembers the start of the word
				} else {
					if (sourcePhraseChars[ich] == (char) 0)
						break; // zero is a guard inserted earlier
					hyphenatedPhraseChars[ihy++] = sourcePhraseChars[ich];
					if (sourcePhraseChars[ich] == HYPHEN_MINUS || sourcePhraseChars[ich] == HYPHEN) { 
						hyphenatedPhraseChars[ihy++] = ZERO_WIDTH_SPACE; 
					}
				}
				ich++;
			}
		}
		return new String(hyphenatedPhraseChars, 0, ihy);
	}

	/**
	 * Extract a word from a char array. The word is converted to lower case and
	 * a '.' character is appended to the beginning and end of the new array.
	 * 
	 * @param chars
	 *            The character array to extract a smaller section from
	 * @param wordStart
	 *            First character to include from the source array <b>chars</b>.
	 * @param wordLength
	 *            Number of characters to include from the source array
	 *            <b>chars</b>
	 * @return Word converted so lower case and surrounded by '.'
	 */
	private char[] extractWord(char[] chars, int wordStart, int wordLength) {
		char[] echars = new char[wordLength + 2];
		echars[0] = echars[echars.length - 1] = '.';
		for (int i = 0; i < wordLength; i++) {
			echars[1 + i] = Character.toLowerCase(chars[wordStart + i]);
		}
		return echars;
	}
	
	/**
	 * Generate a hyphen qualification points for a word by applying rules.
	 * 
	 * @param wordChars
	 *            Word surrounded by '.' characters
	 * @param length
	 *            Length of the word (excluding '.' characters)
	 * @return hyphen qualification points for the word
	 */
	@SuppressWarnings("rawtypes")
	private int[] applyHyphenationRules(final char[] wordChars, final int length) {
		int[] hyphenQualificationPoints = new int[wordChars.length + 1];

		for (int istart = 0; istart < length; istart++) {
			List rules = ruleSet.getPatternTree((int) wordChars[istart]);
			int i = istart;

			java.util.Enumeration rulesEnumeration = rules.elements();
			while(rulesEnumeration.hasMoreElements()) {
				rules = (List) rulesEnumeration.nextElement();

				if (((Character) rules.head()).charValue() == wordChars[i]) {
					rules = rules.longTail(); // values
					int[] nodevalues = (int[]) rules.head();
					for (int inv = 0; inv < nodevalues.length; inv++) {
						if (nodevalues[inv] > hyphenQualificationPoints[istart
								+ inv]){
							hyphenQualificationPoints[istart + inv] = nodevalues[inv];
							}
					}
					i++;

					if (i == wordChars.length) {
						break;
					}
					rulesEnumeration = rules.longTail().elements(); // child
														// nodes
				}
			}
		}

		int[] newvalues = new int[length];
		System.arraycopy(hyphenQualificationPoints, 2, newvalues, 0, length); // save
		// 12
		// bytes;
		// senseless
		hyphenQualificationPoints = newvalues;
		return hyphenQualificationPoints;
	}

	private class ForwardingErrorHandler implements ErrorHandler {
		private ErrorHandler target;
		
		public ForwardingErrorHandler(ErrorHandler target) {
			this.target = target;
		}

		public ErrorHandler getTarget() {
			return target;
		}

		public void setTarget(ErrorHandler target) {
			this.target = target;
		}

		public void debug(String domain, String message) {
			target.debug(domain, message);
		}
	
		public void info(String s) {
			target.info(s);
		}
	
		public void warning(String s) {
			target.warning(s);
		}
	
		public void error(String s) {
			target.error(s);
		}
	
		public void exception(String s, Exception e) {
			target.exception(s, e);
		}
	}

}

/*
 * $Log: Hyphenator.java,v $ Revision 1.17 2003/08/25 08:41:28 dvd 1. Added
 * symbolic accents: dot above, ring, ogonek, \i. 2. Updated hyphenation tables
 * for German, two tables are provided, with hexadecimal values and symbolic
 * accents. Revision 1.16 2003/08/21 16:03:52 dvd atilde added Revision 1.15
 * 2003/08/21 08:52:59 dvd pre-release 1.0 Revision 1.14 2003/08/21 05:50:30 dvd
 * *** empty log message *** Revision 1.13 2003/08/20 22:40:24 dvd bug fixes
 * Revision 1.12 2003/08/20 22:34:38 dvd bug fixes Revision 1.11 2003/08/20
 * 22:18:01 dvd *** empty log message *** Revision 1.10 2003/08/20 20:34:46 dvd
 * complete acctab Revision 1.9 2003/08/20 18:55:36 dvd Makefile makes Revision
 * 1.8 2003/08/20 18:07:07 dvd main() added to Hyphenator as invocation example
 * Revision 1.7 2003/08/20 17:31:55 dvd polish l and scandinavian o (accented)
 * are fixed Revision 1.6 2003/08/20 16:12:32 dvd java docs Revision 1.5
 * 2003/08/17 22:06:12 dvd *** empty log message *** Revision 1.4 2003/08/17
 * 21:55:24 dvd Hyphenator.java is a java program Revision 1.3 2003/08/17
 * 20:30:43 dvd CVS keywords added
 */

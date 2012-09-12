/* $Id: Hyphenator.java,v 1.17 2003/08/25 08:41:28 dvd Exp $ */

package net.davidashen.text;

import net.davidashen.util.*;

/**
 * insert soft hyphens at all allowed locations
 * uses TeX hyphenation tables
 */
public class Hyphenator {
	private ErrorHandler errorHandler = ErrorHandler.DEFAULT;
	private Scanner scanner;

	/**
	 * creates an uninitialized instance of Hyphenator.
	 * The same instance can be reused for different hyphenation tables.
	 */
	public Hyphenator() {
	}


	
	public Scanner getScanner() {
		return scanner;
	}



	public void setScanner(Scanner scanner) {
		this.scanner = scanner;
	}



	public ErrorHandler getErrorHandler() {
		return errorHandler;
	}



	/**
	 * installs error handler.
	 * 
	 * @param errorHandler
	 *            ErrorHandler used while parsing and hyphenating
	 * @see net.davidashen.util.ErrorHandler
	 */
	public void setErrorHandler(ErrorHandler eh) {
		this.errorHandler = eh;
	}

	/**
	 * loads hyphenation table
	 * 
	 * @param in
	 *            hyphenation table
	 * @throws java.io.IOException
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
	 * @throws java.io.IOException
	 */
	public void loadTable(java.io.InputStream in, int[] codelist) throws java.io.IOException {
		ByteScanner b = new ByteScanner(errorHandler);
		b.scan(in, codelist);
		scanner = b;
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
	 * @param remain_count
	 *            unbreakable characters at the beginning of the string
	 * @param push_count
	 *            unbreakable characters at the end of the string
	 * @return the string with soft hyphens inserted
	 */
	public String hyphenate(String phrase, int remain_count, int push_count) {
		if (remain_count < 1) remain_count = 1;
		if (push_count < 1) push_count = 1;
		if (phrase.length() >= push_count + remain_count) {
			int jch = Integer.MIN_VALUE, ich = 0, ihy = 0;
			char[] chars = new char[phrase.length() + 1], hychars = new char[chars.length * 2 - 1];
			chars[chars.length - 1] = (char) 0;
			phrase.getChars(0, phrase.length(), chars, 0);
			boolean inword = false;
			for (;;) {
				if (inword) {
					if (Character.isLetter(chars[ich])) {
						ich++;
					} else { // last character will be reprocessed in the other state
						int length = ich - jch;
						String word = new String(chars, jch, length).toLowerCase();
						int[] values =  scanner.getException(word);
						if (values == null) {
							char[] echars = new char[length + 2];
							values = new int[echars.length + 1];
							echars[0] = echars[echars.length - 1] = '.';
							for (int i = 0; i != length; ++i)
								echars[1 + i] = Character.toLowerCase(chars[jch + i]);
							for (int istart = 0; istart != length; ++istart) {
								List entry = scanner.getList((int)echars[istart]);
								int i = istart;
								for (java.util.Enumeration eentry = entry.elements(); eentry.hasMoreElements();) {
									entry = (List) eentry.nextElement();
									if (((Character) entry.car()).charValue() == echars[i]) {
										entry = entry.cdr(); // values
										int[] nodevalues = (int[]) entry.car();
										for (int inv = 0; inv != nodevalues.length; ++inv) {
											if (nodevalues[inv] > values[istart + inv]) values[istart + inv] = nodevalues[inv];
										}
										++i;
										if (i == echars.length) break;
										eentry = entry.cdr().elements(); // child nodes
									}
								}
							}
							int[] newvalues = new int[length];
							System.arraycopy(values, 2, newvalues, 0, length); //save 12 bytes; senseless
							values = newvalues;
						}

						// now inserting soft hyphens
						if (remain_count + push_count <= length) {
							for (int i = 0; i != remain_count - 1; ++i)
								hychars[ihy++] = chars[jch++];
							for (int i = remain_count - 1; i != length - push_count; ++i) {
								hychars[ihy++] = chars[jch++];
								if (values[i] % 2 == 1) hychars[ihy++] = '\u00ad';
							}
							for (int i = length - push_count; i != length; ++i)
								hychars[ihy++] = chars[jch++];
						} else {
							for (int i = 0; i != length; ++i)
								hychars[ihy++] = chars[jch++];
						}
						inword = false;
					}
				} else {
					if (Character.isLetter(chars[ich])) {
						jch = ich;
						inword = true; // jch remembers the start of the word
					} else {
						if (chars[ich] == (char) 0) break; // zero is a guard inserted earlier
						hychars[ihy++] = chars[ich];
						if (chars[ich] == '\u002d' || chars[ich] == '\u2010') { // dash or hyphen
							hychars[ihy++] = '\u200b'; // zero-width space
						}
					}
					ich++;
				}
			}
			return new String(hychars, 0, ihy);
		} else {
			return phrase;
		}
	}

}

/*
 * $Log: Hyphenator.java,v $
 * Revision 1.17 2003/08/25 08:41:28 dvd
 * 1. Added symbolic accents: dot above, ring, ogonek, \i.
 * 2. Updated hyphenation tables for German, two tables are provided, with
 * hexadecimal values and symbolic accents.
 * Revision 1.16 2003/08/21 16:03:52 dvd
 * atilde added
 * Revision 1.15 2003/08/21 08:52:59 dvd
 * pre-release 1.0
 * Revision 1.14 2003/08/21 05:50:30 dvd
 * *** empty log message ***
 * Revision 1.13 2003/08/20 22:40:24 dvd
 * bug fixes
 * Revision 1.12 2003/08/20 22:34:38 dvd
 * bug fixes
 * Revision 1.11 2003/08/20 22:18:01 dvd
 * *** empty log message ***
 * Revision 1.10 2003/08/20 20:34:46 dvd
 * complete acctab
 * Revision 1.9 2003/08/20 18:55:36 dvd
 * Makefile makes
 * Revision 1.8 2003/08/20 18:07:07 dvd
 * main() added to Hyphenator as invocation example
 * Revision 1.7 2003/08/20 17:31:55 dvd
 * polish l and scandinavian o (accented) are fixed
 * Revision 1.6 2003/08/20 16:12:32 dvd
 * java docs
 * Revision 1.5 2003/08/17 22:06:12 dvd
 * *** empty log message ***
 * Revision 1.4 2003/08/17 21:55:24 dvd
 * Hyphenator.java is a java program
 * Revision 1.3 2003/08/17 20:30:43 dvd
 * CVS keywords added
 */

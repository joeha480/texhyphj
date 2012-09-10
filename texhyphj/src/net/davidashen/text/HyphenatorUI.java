package net.davidashen.text;

import net.davidashen.util.ErrorHandler;

public class HyphenatorUI {

	/** simple command-line invocation -- serves as example */
	public static void main(String[] args) {
		Hyphenator hyphenator = new Hyphenator();
		hyphenator.setErrorHandler(new ErrorHandler() {
			public void debug(String guard, String s) {
			}

			public void info(String s) {
				System.err.println(s);
			}

			public void warning(String s) {
				System.err.println("WARNING: " + s);
			}

			public void error(String s) {
				System.err.println("ERROR: " + s);
			}

			public void exception(String s, Exception e) {
				System.err.println("ERROR: " + s);
				e.printStackTrace();
			}

			public boolean isDebugged(String guard) {
				return false;
			}
		});
		if (args.length != 2 && args.length != 3) {
			System.err.println("call: java net.davidashen.text.Hyphenator word table.tex [codes.txt]");
			System.exit(1);
		}
		java.io.InputStream table = null;
		try {
			table = new java.io.BufferedInputStream(new java.io.FileInputStream(args[1]));
		} catch (java.io.IOException e) {
			System.err.println("cannot open hyphenation table " + args[1] + ": " + e.toString());
			System.exit(1);
		}
		int[] codelist = new int[256];
		for (int i = 0; i != 256; ++i)
			codelist[i] = i;
		if (args.length == 3) {
			java.io.BufferedReader codes = null;
			try {
				codes = new java.io.BufferedReader(new java.io.FileReader(args[2]));
			} catch (java.io.IOException e) {
				System.err.println("cannot open code list" + args[2] + ": " + e.toString());
				System.exit(1);
			}
			try {
				String line;
				while ((line = codes.readLine()) != null) {
					java.util.StringTokenizer tokenizer = new java.util.StringTokenizer(line);
					String token;
					if (tokenizer.hasMoreTokens()) { // skip empty lines
						token = tokenizer.nextToken();
						if (!token.startsWith("%")) { // lines starting with %
														// are comments
							int key = Integer.decode(token).intValue(), value = key;
							if (tokenizer.hasMoreTokens()) {
								token = tokenizer.nextToken();
								value = Integer.decode(token).intValue();
							}
							codelist[key] = value;
						}
					}
				}
				codes.close();
			} catch (java.io.IOException e) {
				System.err.println("error reading code list: " + e.toString());
				System.exit(1);
			}
		}

		try {
			hyphenator.loadTable(table, codelist);
			table.close();
		} catch (java.io.IOException e) {
			System.err.println("error loading hyphenation table: " + e
					.toString());
			System.exit(1);
		}

		System.out.println(args[0] + " -> " + hyphenator.hyphenate(args[0]));
	}
}

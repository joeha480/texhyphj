package net.davidashen.util;

import java.util.logging.Level;
import java.util.logging.Logger;

public final class LoggingErrorHandler implements ErrorHandler {
	public Logger log;

	public LoggingErrorHandler(Logger log) {
		this.log = log;
	}

	public void debug(String domain, String message) {
		log.fine(domain  + " - " + message);
	}

	public void info(String s) {
		log.info(s);
	}

	public void warning(String s) {
		log.warning(s);
	}

	public void error(String s) {
		log.severe(s);
	}

	public void exception(String s, Exception e) {
		log.log(Level.SEVERE, s, e);
	}
	  
  }
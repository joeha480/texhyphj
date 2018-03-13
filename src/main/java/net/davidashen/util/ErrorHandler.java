/* $Id: ErrorHandler.java,v 1.2 2003/08/20 16:12:32 dvd Exp $ */
package net.davidashen.util;


/** Generic Error Handler interface */
public interface ErrorHandler {
 /** debug 
  @param domain a string used to display debugging information selectively
  @param message debugging information */
  public void debug(String domain, String message);
 /** say something
  @param s the thing to say */
  public void info(String s);
 /** report a warning
  @param s explanation */
  public void warning(String s);
 /** report an error
  @param s explanation */
  public void error(String s);
 /** report an error caused by a caught exception;
  @param s explanation
  @param e exception */
  public void exception(String s,Exception e);
}

/*
* $Log: ErrorHandler.java,v $
* Revision 1.2  2003/08/20 16:12:32  dvd
* java docs
*
* Revision 1.1  2003/08/17 21:55:24  dvd
* Hyphenator.java is a java program
*
*/

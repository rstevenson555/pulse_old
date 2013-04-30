package com.bos.applets.arch.jcookies;

/**
 * Thrown when failed to parse a cookie
 *@author Yoav Zur & Amit Caspi
 */
public class CookieParseException extends java.io.IOException{
  /**
   * Create a new exception after failing to parse the given cookie
   */
  public CookieParseException(String cookieName) {
    super("Failed to parse cookie: "+cookieName);
  }
} 


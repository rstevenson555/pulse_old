package com.bos.applets.arch.jcookies;

import java.io.*;
/**
 * Thrown when trying to use  CookiesJar in a browser that does not support
 * cookies caching or if caching is disabled
 *@author Yoav Zur & Amit Caspi
 */
public class CookieNotSupportedException extends IOException {
  private Exception exception;

  /**
   * Create a new exception with the given message, wrapping the given
   * internal exception
   */
  public CookieNotSupportedException(String message,Exception nestedException) {
    super(message);
    exception=nestedException;
  }

  /**
   * Returns the internal (nested) exception that caused this exception
   */
  public Exception getNestedException(){
    return exception;
  }
} 


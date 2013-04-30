package com.bos.applets.arch.jcookies;

import java.io.*;
/**
 * Thrown when a cookie is not found in the browser's cache
 *@author Yoav Zur & Amit Caspi
 */
public class CookieNotFoundException extends IOException {
  /**
   * Create a new exception after not finding the given cookie
   */
  public CookieNotFoundException(String cookieName) {
    super("Cookie not found: "+cookieName);
  }
} 


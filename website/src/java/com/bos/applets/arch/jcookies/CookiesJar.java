package com.bos.applets.arch.jcookies;
import java.util.Properties;
import java.util.Date;
import java.applet.Applet;
import netscape.javascript.*;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.Enumeration;

/**
 * This class provides an interface for reading & writing an Applet's state. <p>
 * The state is represented as a Properties object which is a map of key,value string pairs.
 * The Properties object is saved as a cookie in the browser's local cookies cache. <p>
 * For limitations of saving data in cookies see related documentation. <p>
 * Both the key and value strings in each pair must not include the following characters: <p>
 * ',' (comma) ';' (semi colon) and '=' (equality sign).<p>
 * NOTE: Cookie names are case sensitive
 *@see java.util.Properties
 *@author Yoav Zur & Amit Caspi
 */
 public class CookiesJar {
  private static final String COOKIE_NAME_PREFIX="cookie";

  private static final char[] illegalChars={';',',','='};

  /**
   * The current applet
   */
  private Applet applet;



  /**
   * Creates a cookies jar that handles cookies for the given applet
   */
  public CookiesJar(Applet applet){
    this.applet=applet;
  }

  /**
   * Returns an array of names of available cookies. <p>
   * Returns an array with size 0 if none found
   *@throws CookieNotSupportedException if the browser does not support cookie caching or
   * caching is disabled.
   */
  public String[] getAllNames() throws CookieNotSupportedException{
    Vector namesVec=new Vector();
    try {
        String cookies=(String)JSObject.getWindow(applet).eval("document.cookie");
        if(cookies!=null){
          StringTokenizer st = new StringTokenizer(cookies, ";", false);
          while (st.hasMoreTokens()) {
            String cookie=st.nextToken().trim();
            namesVec.addElement(getCookieName(cookie));
          }
        }
        String[] namesArr=new String[namesVec.size()];
        namesVec.copyInto(namesArr);
        return namesArr;
     }catch(JSException h){
      throw new CookieNotSupportedException("Failed to get cookie names",h);
     }
  }

  /**
   * Returns true if a cookie with name = 'cookieName' exists  otherwise returns false
   *@throws CookieNotSupportedException if the browser does not support cookie caching or
   * caching is disabled.
   */
  public  boolean isExist(String cookieName) throws CookieNotSupportedException{
      try {
        String cookies=(String)JSObject.getWindow(applet).eval("document.cookie");
        StringTokenizer st = new StringTokenizer(cookies, ";", false);
        while (st.hasMoreTokens()) {
         String cookie=st.nextToken().trim();
         if(getCookieName(cookie).equals(cookieName)){
          return true;
         }
        }
        return false;
     }catch(JSException h){
      throw new CookieNotSupportedException("Failed to compare cookies",h);
     }

  }

  /**
   * Creates a new Properties object with the (key,value) pairs stored in the given cookie. <p>
   *@throws CookieNotFoundException if the cookie is not found
   *@throws CookieParseException if the cookie can't be parsed
   *@throws CookieNotSupportedException if the browser does not support cookie caching or
   * caching is disabled.
   */
  public  Properties read(String cookieName)  throws CookieParseException,CookieNotFoundException ,CookieNotSupportedException{
    try {
      String cookies=(String)JSObject.getWindow(applet).eval("document.cookie");
      System.out.println("cookies: " + cookies);
      if(cookies==null){
        throw new CookieNotFoundException(cookieName);
      }
      StringTokenizer st = new StringTokenizer(cookies, ";", false);
      while (st.hasMoreTokens()) {
        String cookie=st.nextToken().trim();
        System.out.println("next cookie: " + cookie);
        if(getCookieName(cookie).equals(cookieName)){
          try{
            return parseCookie(cookie);
          }catch(Exception j){
            throw new CookieParseException(cookieName);
          }
        }
      }
      throw new CookieNotFoundException(cookieName);
    }catch(JSException h){
      throw new CookieNotSupportedException("Failed to read "+cookieName,h);
    }
  }

  /**
   * Writes the Properties object as a cookie into the browser's cache. <p>
   * The cookie is saved under the given cookie name. <p>
   * The browser will automatically delete the cookie at the given expire date.
   *@throws CookieNotSupportedException if the browser does not support cookie caching or
   * caching is disabled.
   *@throws CookieParseException if a key or value inside the properties object is not legal
   * (see above).
   */
  public  void write(Properties props,String cookieName,Date expireDate) throws CookieParseException,CookieNotSupportedException{
      if(!this.validateString(cookieName)){
          throw new CookieParseException(cookieName);
      }
      StringBuffer buffer=new StringBuffer("document.cookie ='");
      buffer.append(COOKIE_NAME_PREFIX);
      buffer.append(cookieName);
      buffer.append("=");
      Enumeration keys=props.keys();
      while(keys.hasMoreElements()){
        String key=(String)keys.nextElement();
        String value=props.getProperty(key);
        if(!this.validateString(key)||!this.validateString(value)){
          throw new CookieParseException(cookieName);
        }
        buffer.append(key);
        buffer.append(",");
        buffer.append(value);
        if(keys.hasMoreElements()){
         buffer.append(",");
        }
      }
      buffer.append(";expires=");

    try{
      buffer.append(getUTCString(expireDate));
      buffer.append("'");
      JSObject.getWindow(applet).eval(buffer.toString());
    }catch(Exception j){
      throw new CookieNotSupportedException("Failed to write: "+cookieName,j);
    }
  }

  /**
   * This method returns the cookie name without the prefix.
   *@param cookieStr the whole cookie string
   */
  private String getCookieName(String cookieStr){
    int index=cookieStr.lastIndexOf('=');
    return cookieStr.substring(COOKIE_NAME_PREFIX.length(),index);
  }

  private Properties parseCookie(String cookieStr){
      Properties props=new Properties();
      int index=cookieStr.lastIndexOf('=');
      String data=cookieStr.substring(index+1,cookieStr.length());
      //data+="%23cf%23";
      //StringTokenizer st = new StringTokenizer(data, ",", false);
      //while (st.hasMoreTokens()) {
        //String key=st.nextToken().trim();
        //String value=st.nextToken().trim();
        //props.put(value,key);
      //}
      //System.out.println("trying to parse: " + data);
      int from = 0,pos = 0;
      while( (pos = data.indexOf("%23cf%23",from))!=-1) {
          String key = data.substring(0,pos);
          //System.out.println("key: " + key);
          String value = data.substring(pos+"%23cf%23".length());
          //System.out.println("value: " + value);
          props.put(value,key);
          break;
      }
      return props;
  }

  /**
   * Convert this date to the string form that JavaScript understands
   */
  private String getUTCString(Date date)throws JSException{
    JSObject jsdate=(JSObject)JSObject.getWindow(applet).eval("new Date");
    jsdate.call("setDate",new Object[]{new Integer(date.getDate())});
    jsdate.call("setMonth",new Object[]{new Integer(date.getMonth())});
    jsdate.call("setYear",new Object[]{new Integer(date.getYear()+1900)});
    return ((String)jsdate.call("toGMTString",new Object[]{}));
  }

  /**
   * returns true iff the string is a valid key or value we can insert into the cookie.
   */
  private boolean validateString(String str){
    for(int i=0;i<illegalChars.length;i++){
      if(str.indexOf(illegalChars[i])>=0){
        return false;
      }
    }
    return true;
  }
}


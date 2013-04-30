

package com.bos.art.logParser.tools;

import java.util.Properties;
import java.net.URL;

public class URIResourceLoader {
    
    public static final Properties loadPropertiesFile(String fileName){
        boolean usedSystemClassLoader = false;
        Properties p = new Properties();
        ClassLoader cl = URIResourceLoader.class.getClassLoader();
        if(cl == null){
            usedSystemClassLoader = true;
            System.out.println("Class Loader is NULL!!  This means that your bootstrap class loader is loading this class, and that is a bad thing!!");
            System.out.println("However, I will try to get the bootstrap class loader to load this uri resource.  here we go...");
            cl  = ClassLoader.getSystemClassLoader();
        }
        URL propertiesUrl = cl.getResource(fileName);
        try{
            p.load(propertiesUrl.openStream());
        }catch(java.io.IOException e){
            e.printStackTrace();
        }
        return p;
    }
}

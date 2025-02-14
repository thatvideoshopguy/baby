package com.ccs.baby.utils;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Enumeration;
import java.util.jar.Manifest;

public class MiscUtils {
    
    public static String getStackTrace(final Throwable throwable) {
        final StringWriter sw = new StringWriter();
        final PrintWriter pw = new PrintWriter(sw, true);
        throwable.printStackTrace(pw);
        return sw.getBuffer().toString();
    }

    /** Return date/time the JAR was built, taken from the JAR manifest */
    public static String getBuildTime() {
        // TODO: inside cheerpj this method returns null, suspect no manifests found but TBC.
        
        String buildTime = "";
        try
        {                                   
            Enumeration<java.net.URL> manifests = MiscUtils.class.getClassLoader().getResources("META-INF/MANIFEST.MF");

            // should only return 1 but...
            while(manifests.hasMoreElements())
            {
                java.net.URL url = manifests.nextElement();
                Manifest manifest = new Manifest(url.openStream());
                java.util.jar.Attributes attributes = manifest.getMainAttributes();
                buildTime = attributes.getValue("Build-Time");
                break;
            }
                
        }
        catch (IOException ex)
        {            
            System.out.println("error getting buildtime: " + MiscUtils.getStackTrace(ex));
        } 
        return buildTime;
    }
        

}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package spk.algo.support;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;
import java.net.URLEncoder;

/**
 * Idea pulled from: https://stackoverflow.com/questions/861500/url-to-load-resources-from-the-classpath-in-java
 * @author L2EDDMAN
 */
public class Resource {
    public static InputStream fromURI(URI uri ) throws FileNotFoundException, Exception{
        String scheme = uri.getScheme();
        if( scheme != null && scheme.equalsIgnoreCase("classpath")){
            InputStream is = Resource.class.getResourceAsStream(uri.getPath());
            if( is == null ){
                throw new FileNotFoundException("Resource: " + uri + " cannot be found");            
            }
            return is;
        }
        else if( (scheme == null||scheme.equalsIgnoreCase("file")) && uri.getPath() != null ){
            return new FileInputStream(decodeURIComponent( uri.getPath() ) );
        }         
        else {
            throw new Exception("scheme not implemented");
        }
        
    }
    
    
    public static InputStream fromURI( String uri )throws FileNotFoundException, Exception {
        
        return Resource.fromURI(URI.create(encodeURIComponent(uri)));
    }
    /**
     * taken from https://stackoverflow.com/questions/14321873/java-url-encoding-urlencoder-vs-uri
     * 
     * 
     * @param s
     * @return 
    */
    public static String encodeURIComponent(String s) {
        String result;
        result = s.replace(" ", "%20").replace("%", "%25").replace("@", "%40");
        /*
        try {
            result = URLEncoder.encode(s, "UTF-8")
                    .replaceAll("\\+", "%20")
                    .replaceAll("\\%21", "!")
                    .replaceAll("\\%27", "'")
                    .replaceAll("\\%28", "(")
                    .replaceAll("\\%29", ")")
                    .replaceAll("\\%7E", "~");
        } catch (UnsupportedEncodingException e) {
            result = s;
        }
        */
        return result;
    }
    public static String decodeURIComponent(String s) {
        String result;
        result = s.replace("%20", " ").replace("%25", "%").replace("%40", "@");
    
        return result;
    }
}

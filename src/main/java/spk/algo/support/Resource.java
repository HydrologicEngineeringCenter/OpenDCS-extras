/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package spk.algo.support;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URI;

/**
 * Idea pulled from: https://stackoverflow.com/questions/861500/url-to-load-resources-from-the-classpath-in-java
 * @author L2EDDMAN
 */
public class Resource {
    public static InputStream fromURI(URI uri ) throws FileNotFoundException, Exception{
        String scheme = uri.getScheme();
        if( scheme != null && scheme.equalsIgnoreCase("classpath")){
            InputStream is = Resource.class.getResourceAsStream(uri.getPath());
            return is;
        }
        else if( (scheme == null||scheme.equalsIgnoreCase("file")) && uri.getPath() != null ){
            return new FileInputStream(uri.getPath());
        } 
        else {
            throw new Exception("scheme not implemented");
        }
        
    }
}

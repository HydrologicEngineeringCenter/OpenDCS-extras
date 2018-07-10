/*
 *  As a work of the United States Government, this package 
 *  is in the public domain within the United States. Additionally, 
 *  We waive copyright and related rights in the work worldwide
 *  through the CC0 1.0 Universal Public Domain Dedication 
 *  (which can be found at https://creativecommons.org/publicdomain/zero/1.0/).

 */
package spk.apps;

import ilex.util.AuthException;
import ilex.util.UserAuthFile;
import java.io.IOException;

/**
 * Shows the username and password on YOUR system.
 * @author Michael Neilson <michael.a.neilson@usace.army.mil>
 */
public class getpasswd {
    private static Object args;
    public static void main( String args[] ) throws IOException, AuthException{
        UserAuthFile authFile;		
	authFile = new UserAuthFile( args[0]); 
        authFile.read();
        System.out.println( "Username: " + authFile.getUsername() );
        System.out.println( "Password: " + authFile.getPassword() );
        
    }
}

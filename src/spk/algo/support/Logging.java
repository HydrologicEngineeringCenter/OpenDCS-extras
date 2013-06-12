/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package spk.algo.support;

import ilex.util.Logger;
/**
 
 *  debug logging, shamelessly copied from DbAlgorithmExecutive.java created by Ilex/Sutron
 *  for use in support classes not derived from the above.
 
 * @author L2EDDMAN
 */
public class Logging {
        
        public static void warning(String msg ) {            
            
            Logger.instance().warning(new StringBuilder().append("Library '").append
				      ("").append
				      ("' ").append
				      (msg).toString());
        }

        public static void info(String msg) {
            Logger.instance().info(new StringBuilder().append("Library '").append
				   (" ").append
				   ("' ").append
				   (msg).toString());
        }

        public static void debug1(String msg) {
            Logger.instance().debug1(new StringBuilder().append("Library '").append
				     (" ").append
				     ("' ").append
				     (msg).toString());
        }

        public static void debug2(String msg) {
            Logger.instance().debug2(new StringBuilder().append("Library '").append
				     ("").append
				     ("' ").append
				     (msg).toString());
        }

        public static void debug3(String msg) {
            Logger.instance().debug3(new StringBuilder().append("Library '").append
				     ("").append
				     ("' ").append
				     (msg).toString());
        }

}

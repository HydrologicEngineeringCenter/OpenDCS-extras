/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package spk.algo.support;

import java.io.InputStream;
import java.net.URI;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author L2EDDMAN
 */
public class ResourceTest {
    
    public ResourceTest() {
    }

    /**
     * Test of fromURI method, of class Resource.
     */
    @Test
    public void testFromURI_with_Class_path() throws Exception {
        InputStream is = Resource.fromURI( URI.create("classpath:/shared/stations/TEST.Stage.station") );
        assertNotNull("Unable to retrieve file", is);
    }
    
    
}

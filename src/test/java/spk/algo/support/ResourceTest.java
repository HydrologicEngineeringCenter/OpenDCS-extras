/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package spk.algo.support;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import org.junit.Test;
import static org.junit.Assert.*;
import org.lobobrowser.util.OS;

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
        InputStream is = Resource.fromURI(URI.create("classpath:/shared/stations/TEST.Stage.station"));
        assertNotNull("Unable to retrieve file", is);
    }

    /**
     * Test of fromURI, of file system Resource
     */
    @Test
    public void testFromURI_with_filesystem_path() throws Exception {
        File tmp = File.createTempFile("spkag_test", ".txt");
        URI uri = tmp.toURI();
        InputStream is = Resource.fromURI(uri);
        assertNotNull("Unable to retrieve file", is);
        is.close();

        tmp.deleteOnExit();
    }

}

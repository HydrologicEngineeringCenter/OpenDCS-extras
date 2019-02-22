/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package decodes.db;

import decodes.db.DataSource;
import decodes.db.DataSourceList;
import decodes.db.DataTypeSet;
import decodes.db.DatabaseException;
import decodes.db.DatabaseIO;
import decodes.db.EngineeringUnitList;
import decodes.db.EnumList;
import decodes.db.EquipmentModel;
import decodes.db.EquipmentModelList;
import decodes.db.NetworkList;
import decodes.db.NetworkListList;
import decodes.db.NetworkListSpec;
import decodes.db.Platform;
import decodes.db.PlatformConfig;
import decodes.db.PlatformConfigList;
import decodes.db.PlatformList;
import decodes.db.PresentationGroup;
import decodes.db.PresentationGroupList;
import decodes.db.RoutingSpec;
import decodes.db.RoutingSpecList;
import decodes.db.Site;
import decodes.db.SiteList;
import decodes.db.SiteName;
import decodes.db.UnitConverterSet;
import decodes.sql.DbKey;
import decodes.xml.XmlDatabaseIO;
import ilex.util.Counter;
import ilex.util.Logger;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.logging.Level;
import javax.xml.parsers.ParserConfigurationException;
import opendcs.dai.LoadingAppDAI;
import opendcs.dai.PlatformStatusDAI;
import opendcs.dai.ScheduleEntryDAI;
import org.xml.sax.SAXException;
import spk.algo.support.Resource;

/**
 *
 * @author L2EDDMAN
 */
public class TestDatabaseIO extends XmlDatabaseIO {

    public TestDatabaseIO(String xmldir) throws SAXException, ParserConfigurationException {
        super(xmldir);
    }

    @Override
    protected String[] listDirectory(String dir) throws IOException {

        InputStream is = null;
        try {
            String listing_file = xmldir + "/" + dir + "/listing";
            is = Resource.fromURI(URI.create(listing_file));
            if( is == null ){
                return null;
            }
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            ArrayList<String> listing = new ArrayList<>();
            String tmp = null;
            while ((tmp = reader.readLine()) != null) {
                listing.add(tmp.trim());
            }
            return listing.toArray(new String[listing.size()]);

        } catch( FileNotFoundException ex) {
            return null;
        }catch (Exception ex) {
            throw new IOException("Could not access resource", ex);
        } finally {

            try {
                if (is != null) {
                    is.close();
                }
            } catch (IOException ex) {
                throw new IOException("Could close resource", ex);
            }
        }
    }

    /**
     * Open an output stream for writing to the passed file name.
     *
     * @param dir the directory
     * @param name the file name
     * @return an OutputStream
     */
    @Override
    protected OutputStream getOutputStream(String dir, String name) throws IOException {
        throw new IOException("Output Streams not supported on this database at this time.");
    }

    /**
     * Opens an input stream to read the passed file name. The root directory
     * for the database is pre-pended.
     *
     * @param dir the directory
     * @param name the file name
     * @return @throws IOException if can't read file
     */
    @Override
    protected InputStream getInputStream(String dir, String name) throws IOException {

        String fn = makePath(dir, name);
        Logger.instance().log(Logger.E_DEBUG1,
                "XmlDatabaseIO: Opening '" + fn + "'");
        try {
            return Resource.fromURI(URI.create(fn));
        } catch (Exception ex) {
            throw new IOException("Could not access resource", ex);
        }

    }

    /**
     * Returns the last modify time for an object in this database. The read
     * methods can use this to determine if an already-loaded copy is
     * up-to-date.
     *
     * @param dir the directory
     * @param name the file name
     * @return msec last modify time
     * @throws IOException if IO error.
     */
    @Override
    protected long getLastModifyTime(String dir, String name) throws IOException {
        return Long.MAX_VALUE;
    }

    /**
     * @param dir the directory
     * @param name the file name, which may or may not end with ".xml"
     * @return the path
     */
    @Override
    public String makePath(String dir, String name) {
        String sep = File.pathSeparator;
        if (xmldir.startsWith("classpath")) {
            sep = "/";
        }

        String fn = xmldir + sep + dir + sep + name;
        if (!fn.endsWith(".xml")) {
            fn += ".xml";
        }
        return fn;
    }

    /**
     * @param dir the directory
     * @param name the file name, which may or may not end with ".xml"
     * @return the path
     */
    public String makeDir(String dir) {
        String sep = File.pathSeparator;
        if (xmldir.startsWith("classpath")) {
            sep = "/";
        }

        String fn = xmldir + sep + dir + sep;

        return fn;
    }

}

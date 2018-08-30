package spk.algo.support;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
import spk.algo.support.WaterControlDiagram;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import ilex.util.Logger;
import ilex.util.StderrLogger;

/**
 *
 * @author L2EDDMAN
 */
public class WaterControlDiagramTest {

    String basic_graph = "classpath:/shared/tables/tcs/basic";
    String fbo_graph = "classpath:/shared/tables/tcs/fbo";

    WaterControlDiagram instance;

    double expResult;
    double result;

    public WaterControlDiagramTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
        StderrLogger l = new StderrLogger("test");

        Logger.instance().setLogger(l);
        Logger.instance().setMinLogPriority(0);
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
        expResult = -1;
        result = -1;
        instance = null;
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of load_graph method, of class WaterControlDiagram.
     */
    @Test
    public void testLoadBasic_graph() throws Exception {
        System.out.println("load_graph");
        String graph_file = "C:\\shared\\production\\config\\topcon\\blackbutte";
        WaterControlDiagram instance = new WaterControlDiagram();

        instance.load_graph(basic_graph);
    }

    @Test
    public void testLoadFBO_graph() throws Exception {
        WaterControlDiagram instance = new WaterControlDiagram();
        instance.load_graph(fbo_graph);
    }

    /**
     *
     * TODO: break into multiple tests Test of get_allowed_storage method, of
     * class WaterControlDiagram.
     */
    @Test
    public void testGet_allowed_storage() throws Exception {
        int day = 0;
        double fcp = 0.0;
        instance = new WaterControlDiagram();
        instance.load_graph(basic_graph);
        expResult = 112253.0;
        result = instance.get_allowed_storage(day, fcp);
        assertEquals(expResult, result, 1.0);

    }

    @Test
    public void testBounding() throws Exception {
        instance = new WaterControlDiagram();
        instance.load_graph(basic_graph);
        int day = 0;
        double fcp = 0;
        result = instance.get_allowed_storage(day, fcp);
        assertEquals(112253, result, 1.0);

        result = instance.get_allowed_storage(80, 0.0);
        assertEquals(29800.0, result, 1.0);

        result = instance.get_allowed_storage(80, 16.0);
        assertEquals(0.0, result, 1.0);
    }

    @Test
    public void testControlPoints() throws Exception {
        instance = new WaterControlDiagram();
        instance.load_graph(basic_graph);
        int day = 0;
        double fcp = 0;
        System.out.println("Control Points");
        result = instance.get_allowed_storage(76, 3.0);
        assertEquals(29800.0, result, 1.0);

        result = instance.get_allowed_storage(76, 14.0);
        assertEquals(0.0, result, 1.0);

        System.out.println("Between Control Points");
        result = instance.get_allowed_storage(0, 3.5);
        assertEquals(112253.0, result, 1.0);

        result = instance.get_allowed_storage(76, 12.5);
        assertEquals(8400.0, result, 1.0);

        System.out.println("Full interpoaltion");

        result = instance.get_allowed_storage(214, 7.5);
        assertEquals(107573.75, result, 1.0);

    }

    /**
     * Test of interpolate_graph_line method, of class WaterControlDiagram.
     */
    @Test
    public void testInterpolate_graph_line() throws Exception {
        System.out.println("interpolate_graph_line");
        int day = 0;
        double fcp = 0.0;
        WaterControlDiagram instance = new WaterControlDiagram();

        instance.load_graph(basic_graph);
        double expResult = 0.0;
        System.out.println("Control Points");
        double result = instance.interpolate_graph_line(day, 3.0, false);
        assertEquals(112253.0, result, 0.5);
        result = instance.interpolate_graph_line(56, 3.0, false);
        assertEquals(29800, result, 0.5);
        result = instance.interpolate_graph_line(144, 4.0, false);
        assertEquals(45222, result, 0.5);

        System.out.println("Interpolated Points");

        result = instance.interpolate_graph_line(25, 3.0, false);
        assertEquals(75443.625, result, 1.0);

        result = instance.interpolate_graph_line(59, 3.0, false);
        assertEquals(29800, result, 1.0);

        result = instance.interpolate_graph_line(214, 7.0, false);
        assertEquals(112827.5, result, 1.0);
        //assertEquals(expResult)

    }

    /**
     * Test of get_allowed_storage method, of class WaterControlDiagram.
     */
    @Test
    public void testbound() throws Exception {
        int day = 144;
        double fcp = 0.0;
        double high_bound_test = 49650;
        double low_bound_test = -200;
        double good_value = 36000;

        WaterControlDiagram instance = new WaterControlDiagram();
        instance.load_graph(basic_graph);
        double expResult = 0.0;
        System.out.println("Bounding");

        double _val = instance.bound(day, high_bound_test);
        assertEquals(49650, _val, 10);

        _val = instance.bound(day, low_bound_test);
        assertEquals(0, _val, 0);

        _val = instance.bound(day, good_value);
        assertEquals(good_value, _val, 0);

        day = 0;
        _val = instance.bound(day, 110000);
        assertEquals(112253, _val, 0);

    }
}

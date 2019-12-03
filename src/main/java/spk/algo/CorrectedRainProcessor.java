package spk.algo;

import com.sun.tools.corba.se.idl.constExpr.GreaterEqual;
import decodes.cwms.CwmsFlags;
import decodes.tsdb.BadTimeSeriesException;
import decodes.tsdb.algo.*;
import java.util.Date;

import ilex.var.NamedVariableList;
import ilex.var.NamedVariable;
import decodes.tsdb.DbAlgorithmExecutive;
import decodes.tsdb.DbCompException;
import decodes.tsdb.DbIoException;
import decodes.tsdb.VarFlags;
import decodes.tsdb.algo.AWAlgoType;
import decodes.tsdb.CTimeSeries;
import decodes.tsdb.ParmRef;
import ilex.var.TimedVariable;
import decodes.tsdb.TimeSeriesIdentifier;
import ilex.var.NoConversionException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;
import opendcs.dai.TimeSeriesDAI;

//AW:IMPORTS
//AW:IMPORTS_END
//AW:JAVADOC
/**
 * This computation includes it's output in its input. This input MUST be
 * specified to be 1 interval accumulator or you WILL get stuck in and endless
 * loop.
 */
//AW:JAVADOC_END
public class CorrectedRainProcessor
        extends decodes.tsdb.algo.AW_AlgorithmBase {
//AW:INPUTS

    public double raw_in;   //AW:TYPECODE=i
    public double orig_delta;	//AW:TYPECODE=id
    public double rev_in;	//AW:TYPECODE=i
    String _inputNames[] = {"raw_in", "orig_delta", "rev_in"};
//AW:INPUTS_END

//AW:LOCALVARS
    private SimpleDateFormat df = new SimpleDateFormat("ddMMMyyyy HHmm"); // basic standard HEC Format; // used to dispay dates in the debug output    
    private GregorianCalendar calStartOfWaterYear; // we don't care about what year we are in, just that we are at the start time (day/month/hour) of the new water year
    private GregorianCalendar slice = null;
    double accumulator;
    CTimeSeries output_ts;
//AW:LOCALVARS_END
//AW:OUTPUTS
    public NamedVariable rev_out = new NamedVariable("rev_out", 0);
    String _outputNames[] = {"rev_out"};
//AW:OUTPUTS_END

//AW:PROPERTIES
    private String waterYearStartUTC = "01Oct2019 0700";
    String _propertyNames[] = {"waterYearStartUTC"};
//AW:PROPERTIES_END

    // Allow javac to generate a no-args constructor.
    /**
     * Algorithm-specific initialization provided by the subclass.
     */
    protected void initAWAlgorithm()
            throws DbCompException {
//AW:INIT
        _awAlgoType = AWAlgoType.TIME_SLICE;
//AW:INIT_END
        accumulator = Double.NEGATIVE_INFINITY;
        try {
            Date startWY = this.df.parse(this.waterYearStartUTC);
            calStartOfWaterYear = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
            calStartOfWaterYear.setTime(startWY);
        } catch (ParseException ex) {
            debug3("unable to parse water year start date: " + ex.getLocalizedMessage());
            throw new DbCompException(ex.getLocalizedMessage());
        }

//AW:USERINIT
//AW:USERINIT_END
    }

    /**
     * This method is called once before iterating all time slices.
     */
    protected void beforeTimeSlices()
            throws DbCompException {
        accumulator = Double.NEGATIVE_INFINITY;
        try {
            //AW:BEFORE_TIMESLICES
            ParmRef p = this.getParmRef("rev_out");
            output_ts = p.timeSeries;
            Date start = this.baseTimes.first();
            Date end = this.baseTimes.last();
            TimeSeriesDAI dai = this.tsdb.makeTimeSeriesDAO();
            dai.fillTimeSeries(output_ts, end, end, true, true, true);
        } catch (BadTimeSeriesException | DbIoException ex) {
            warning(ex.getLocalizedMessage());
            throw new DbCompException(ex.getLocalizedMessage());
        }
//AW:BEFORE_TIMESLICES_END

    }

    /**
     * Do the algorithm for a single time slice. AW will fill in user-supplied
     * code here. Base class will set inputs prior to calling this method. User
     * code should call one of the setOutput methods for a time-slice output
     * variable.
     *
     * @throws DbCompException (or subclass thereof) if execution of this
     * algorithm is to be aborted.
     */
    protected void doAWTimeSlice()
            throws DbCompException {
//AW:TIMESLICE
        if (!isMissing(orig_delta)) {
            orig_delta = Math.max(0,orig_delta);
            // logic to check flags
            // logic to check for start of water year
            if (isStartOfWaterYear()) { // force reset
                accumulator = 0.0;
                setOutput(rev_out, accumulator);
                return;
            } else if (!isMissing(rev_in) && isMissing(accumulator)) { // e.g. the accumulator hasn't been initialized yet
                accumulator = rev_in; // TODO: this should also be a water year start check
            } else if (isMissing(accumulator)) {
                accumulator = 0.0;
            }

            int flags = this.getInputFlagBits("raw_in");
            debug3("Flags are " + flags);
            int cflags = CwmsFlags.flag2CwmsQualityCode(flags);
            if ((cflags & (CwmsFlags.PROTECTED | CwmsFlags.REPLACEMENT_MASK)) > 0 && (raw_in >= accumulator)) {
                accumulator = raw_in; // the user set a value, use it.
            } else {
                accumulator = accumulator + orig_delta; // new data, just use difference
            }

            TimedVariable tv = output_ts.findWithin(_timeSliceBaseTime, roundSec);
            double cur_out = Double.NEGATIVE_INFINITY;
            try {
                cur_out = tv.getDoubleValue();
            } catch (NullPointerException | NoConversionException ex) {
                debug3("not current output value");
            }

            if (!isMissing(cur_out) && nearlyEqual(cur_out, accumulator, .001)) {
                // do nothing
            } else {
                setOutput(rev_out, accumulator);
            }

        } else {
            debug3("a value is missing in the window, that shouldn't happen");
            throw new DbCompException("gap in window");
        }
        // else do nothing, we are either and the end or someone needs to get in an edit data.
//AW:TIMESLICE_END
    }

    /**
     * This method is called once after iterating all time slices.
     */
    protected void afterTimeSlices()
            throws DbCompException {
//AW:AFTER_TIMESLICES
//AW:AFTER_TIMESLICES_END
    }

    /**
     * Required method returns a list of all input time series names.
     */
    public String[] getInputNames() {
        return _inputNames;
    }

    /**
     * Required method returns a list of all output time series names.
     */
    public String[] getOutputNames() {
        return _outputNames;
    }

    /**
     * Required method returns a list of properties that have meaning to this
     * algorithm.
     */
    public String[] getPropertyNames() {
        return _propertyNames;
    }

    /**
     * Proper floating point comparison Taken from:
     * https://stackoverflow.com/questions/4915462/how-should-i-do-floating-point-comparison
     *
     * @param a
     * @param b
     * @param epsilon
     * @return
     */
    public boolean nearlyEqual(double a, double b, double epsilon) {
        final double absA = Math.abs(a);
        final double absB = Math.abs(b);
        final double diff = Math.abs(a - b);

        if (a == b) { // shortcut, handles infinities
            return true;
        } else if (a == 0 || b == 0 || diff < Float.MIN_NORMAL) {
            // a or b is zero or both are extremely close to it
            // relative error is less meaningful here
            return diff < (epsilon * Float.MIN_NORMAL);
        } else { // use relative error
            return diff / (absA + absB) < epsilon;
        }
    }

    private boolean isStartOfWaterYear() {
        if( slice == null ){
            slice = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
        }
        slice.setTime(_timeSliceBaseTime);
        return (calStartOfWaterYear.get(Calendar.MONTH) == slice.get(Calendar.MONTH ) )
                && (calStartOfWaterYear.get(Calendar.DATE) == slice.get(Calendar.DATE ) )
                && (calStartOfWaterYear.get(Calendar.HOUR_OF_DAY) == slice.get(Calendar.HOUR_OF_DAY ) )
                && (calStartOfWaterYear.get(Calendar.MINUTE) == slice.get(Calendar.MINUTE ) ) ;
                
    }

}

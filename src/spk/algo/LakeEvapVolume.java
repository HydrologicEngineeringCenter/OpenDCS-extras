package spk.algo;

import java.util.Date;

import ilex.var.NamedVariableList;
import ilex.var.NamedVariable;
import decodes.tsdb.DbAlgorithmExecutive;
import decodes.tsdb.DbCompException;
import decodes.tsdb.DbIoException;
import decodes.tsdb.VarFlags;
import decodes.tsdb.algo.AWAlgoType;

//AW:IMPORTS
// Place an import statements you need here.
import java.util.Calendar;
import java.util.GregorianCalendar;
//AW:IMPORTS_END

//AW:JAVADOC
/**
 * Calculate the volume of evap given measured evap number, evap coeffiecents ( defined as parameters ), and average lake area ( 0.5*[start of day + end of day] )
 * The default constants are in English Standard units, convert them to metric if you want your calculations done in metric.
 * @author L2EDDMAN
 */
//AW:JAVADOC_END
public class LakeEvapVolume
	extends decodes.tsdb.algo.AW_AlgorithmBase
{
//AW:INPUTS
	public double evap;	//AW:TYPECODE=i
	public double avg_lake_area;	//AW:TYPECODE=i
	String _inputNames[] = { "evap", "avg_lake_area" };
//AW:INPUTS_END

//AW:LOCALVARS
	// Enter any local class variables needed by the algorithm.
        GregorianCalendar cal = null; // used to figure out the current month so we can adjusted the evap value
//AW:LOCALVARS_END

//AW:OUTPUTS
	public NamedVariable evap_volume = new NamedVariable("evap_volume", 0);
	String _outputNames[] = { "evap_volume" };
//AW:OUTPUTS_END

//AW:PROPERTIES
        // these constants are the estimates used in HADA
        // for the average monthly values shown in the
        // water control manuals
	public double January = 0.067;
	public double February = 0.058;
	public double March = 0.056;
	public double April = 0.056;
	public double May = 0.057;
	public double June = 0.06;
	public double July = 0.064;
	public double August = 0.068;
	public double September = 0.073;
	public double October = 0.078;
	public double November = 0.080;
	public double December = 0.077;
	String _propertyNames[] = { "January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December" };
//AW:PROPERTIES_END

	// Allow javac to generate a no-args constructor.

	/**
	 * Algorithm-specific initialization provided by the subclass.
	 */
	protected void initAWAlgorithm( )
		throws DbCompException
	{
//AW:INIT
		_awAlgoType = AWAlgoType.TIME_SLICE;
//AW:INIT_END

//AW:USERINIT
		// Code here will be run once, after the algorithm object is created.
                cal = new GregorianCalendar();
                
//AW:USERINIT_END
	}
	
	/**
	 * This method is called once before iterating all time slices.
	 */
	protected void beforeTimeSlices()
		throws DbCompException
	{
//AW:BEFORE_TIMESLICES
		// This code will be executed once before each group of time slices.
		// For TimeSlice algorithms this is done once before all slices.
		// For Aggregating algorithms, this is done before each aggregate
		// period.
//AW:BEFORE_TIMESLICES_END
	}

	/**
	 * Do the algorithm for a single time slice.
	 * AW will fill in user-supplied code here.
	 * Base class will set inputs prior to calling this method.
	 * User code should call one of the setOutput methods for a time-slice
	 * output variable.
	 *
	 * @throws DbCompException (or subclass thereof) if execution of this
	 *        algorithm is to be aborted.
	 */
	protected void doAWTimeSlice()
		throws DbCompException
	{
//AW:TIMESLICE
                double volume;		
                cal.setTime(_timeSliceBaseTime); // set the calendar to the current date/time so we can get the month
                                                 // of this time slice.
                switch( cal.get( Calendar.MONTH ) )
                {
                    case Calendar.JANUARY:  evap = evap*January;  break;
                    case Calendar.FEBRUARY: evap = evap*February; break;
                    case Calendar.MARCH:    evap = evap*March;    break;
                    case Calendar.APRIL:    evap = evap*April;    break;
                    case Calendar.MAY:      evap = evap*May;      break;
                    case Calendar.JUNE:     evap = evap*June;     break;
                    case Calendar.JULY:     evap = evap*July;     break;
                    case Calendar.AUGUST:   evap = evap*August;   break;
                    case Calendar.SEPTEMBER:evap = evap*September;break;
                    case Calendar.OCTOBER:  evap = evap*October;  break;
                    case Calendar.NOVEMBER: evap = evap*November; break;
                    case Calendar.DECEMBER: evap = evap*December; break;
                    default:{
                        debug3("If you are reading this you are either looking at the code or somehow aren't within the standard 12 months");
                        debug3("  On a system that is only defined with 12 months; if so, you're probably on your own figuring this problem out.");
                    }
                }
                volume = evap*avg_lake_area;
                if( !isMissing( volume ) )
                {
                    setOutput( evap_volume, volume );
                }

//AW:TIMESLICE_END
	}

	/**
	 * This method is called once after iterating all time slices.
	 */
	protected void afterTimeSlices()
		throws DbCompException
	{
//AW:AFTER_TIMESLICES
		// This code will be executed once after each group of time slices.
		// For TimeSlice algorithms this is done once after all slices.
		// For Aggregating algorithms, this is done after each aggregate
		// period.
//AW:AFTER_TIMESLICES_END
	}

	/**
	 * Required method returns a list of all input time series names.
	 */
	public String[] getInputNames()
	{
		return _inputNames;
	}

	/**
	 * Required method returns a list of all output time series names.
	 */
	public String[] getOutputNames()
	{
		return _outputNames;
	}

	/**
	 * Required method returns a list of properties that have meaning to
	 * this algorithm.
	 */
	public String[] getPropertyNames()
	{
		return _propertyNames;
	}
}

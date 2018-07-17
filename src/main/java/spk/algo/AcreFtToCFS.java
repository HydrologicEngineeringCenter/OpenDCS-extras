package spk.algo;



import java.util.Date;

import ilex.var.NamedVariableList;
import ilex.var.NamedVariable;
import decodes.tsdb.DbAlgorithmExecutive;
import decodes.tsdb.DbCompException;
import decodes.tsdb.DbIoException;
import decodes.tsdb.VarFlags;
import decodes.tsdb.algo.AWAlgoType;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

//AW:IMPORTS
// Place an import statements you need here.
//AW:IMPORTS_END

//AW:JAVADOC
/**
 * Converts ac-ft for a day to CFS
 * takes into account daylight savings
 */
//AW:JAVADOC_END
public class AcreFtToCFS
	extends spk.algo.support.AlgBaseNew
{
//AW:INPUTS
        // input values, declare a variable, and add the string of the variable name to the _inputNames array
        public double acreft;    //AW:TYPECODE=i
        
	String _inputNames[] = { "acreft" };
//AW:INPUTS_END

//AW:LOCALVARS
	// Enter any local class variables needed by the algorithm.
	
//AW:LOCALVARS_END

//AW:OUTPUTS
	public NamedVariable cfs = new NamedVariable("cfs", 0.0); // the ouput
	String _outputNames[] = { "cfs" };
//AW:OUTPUTS_END

//AW:PROPERTIES
	String _propertyNames[] = {  };
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
		// Enter code to be executed at each time-slice.
                /*
                 * we shouldn't need to check for close to zero. we aren't
                 * doing physcis calculations with small number and everything is generally
                 * out to two decimal places only anyways
                 */
                 GregorianCalendar t1 = new GregorianCalendar(this.aggTZ);
                 GregorianCalendar t2 = new GregorianCalendar(this.aggTZ);
                 t1.setTime( _timeSliceBaseTime );
                 t2.setTime(_timeSliceBaseTime);
                 t1.add( Calendar.DATE, -1);
                 double conversion = 0.0;
                 long diff =  (t2.getTimeInMillis() - t1.getTimeInMillis())/(1000*60*60);
                 
                 if( diff == 24 ){
                     debug3("normal day");
                     conversion = 0.50417;
                 }
                 else if( diff == 23 ){
                     debug3("short day");
                     conversion = 0.52609;
                 }
                 else if( diff == 25){
                     debug3("long day");
                     conversion = 0.48400;
                 }
                 else{
                     debug3("probably the end of day...or you have a really wierd daylight savings shift");
                     throw new DbCompException( "You have entered some weird daylight savings that adjusts by more than an hour...if this is true, 1st: sorry. 2nd: see the programmer about expanding this section of the code" );
                 }
                 double flow = acreft*conversion;
                 setOutput(cfs, flow);
                 
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

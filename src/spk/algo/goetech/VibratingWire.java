package spk.algo.goetech;



import spk.algo.*;
import java.util.Date;

import ilex.var.NamedVariableList;
import ilex.var.NamedVariable;
import decodes.tsdb.DbAlgorithmExecutive;
import decodes.tsdb.DbCompException;
import decodes.tsdb.DbIoException;
import decodes.tsdb.VarFlags;
import decodes.tsdb.algo.AWAlgoType;
import java.util.logging.Level;
import java.util.logging.Logger;
import decodes.cwms.CwmsFlags;

//AW:IMPORTS
// Place an import statements you need here.
//AW:IMPORTS_END

//AW:JAVADOC
/**
*Converts digits to PSI using the numbers from the vibrating wire calibration sheet.
*Calibration factors will be stored in a spread sheet.
 */
//AW:JAVADOC_END
public class VibratingWire
	extends decodes.tsdb.algo.AW_AlgorithmBase
{
//AW:INPUTS
        public double digits;   //AW:TYPECODE=i
        public double temp; //AW:TYPECODE=i
        // input values, declare a variable, and add the string of the variable name to the _inputNames array
	String _inputNames[] = { "digits", "temp" };
//AW:INPUTS_END

//AW:LOCALVARS
	// Enter any local class variables needed by the algorithm.
	VibratingWireData vwdata;
        String location;
//AW:LOCALVARS_END

//AW:OUTPUTS
	public NamedVariable psi = new NamedVariable("psi", 0);
	String _outputNames[] = { "psi" };
//AW:OUTPUTS_END

//AW:PROPERTIES	
        public boolean use_polynomial = false;
        public boolean temp_compensation = true;
        public String file = "fillthis";
	String _propertyNames[] = { "use_polynomial", "temp_compensation", "file", };
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
            try {
                //AW:BEFORE_TIMESLICES
                // This code will be executed once before each group of time slices.
                // For TimeSlice algorithms this is done once before all slices.
                // For Aggregating algorithms, this is done before each aggregate
                // period.
                vwdata = new VibratingWireData(file);
                location = getParmTsId( "digits" ).getSiteName().toLowerCase();
                //AW:BEFORE_TIMESLICES_END
            } catch (Exception ex) {
                Logger.getLogger(VibratingWire.class.getName()).log(Level.SEVERE, null, ex);
                throw new DbCompException("Could not read vibrating wire data file, bailing on computations");
            }
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
		VibratingWireSheet s = vwdata.getVibratingWireSheet(_timeSliceBaseTime, location);
                if( s == null ){
                    warning("Sheet not available for this site at this date (" + location + "," + _timeSliceBaseTime);
                    return;
                }
                double P=Double.NEGATIVE_INFINITY;
                if( use_polynomial ){
                    P = s.A*Math.pow(digits, 2) + s.B*digits+s.C;
                } else{
                    P = s.G*(s.R0-digits);
                }             
                
                if( temp_compensation ){
                    if( !isMissing(temp)){
                        P += s.K*(temp-s.T0);
                    }
                    else{
                        warning("Tempurature Compensation Requested but temperature not available, flagging data as questionable");
                        setFlagBits(psi,CwmsFlags.VALIDITY_QUESTIONABLE);
                    }
                }
                                
                // should probably add baro compensation at some point
                setOutput(psi, P);
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

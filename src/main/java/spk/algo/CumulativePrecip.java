package spk.algo;

import java.io.IOException;
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
import java.text.SimpleDateFormat;
import decodes.tsdb.*;
import ilex.var.NoConversionException;
import java.util.GregorianCalendar;
import java.util.Calendar;
import ilex.var.TimedVariable;

//for getInputData function
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.Writer;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
//AW:IMPORTS_END

//AW:JAVADOC
/**
Calculates a total from an incremental value. If a reset date is provided the data will be summed from that date.
Other wise the current incremental will be added to the previous cumulative value.
 */
//AW:JAVADOC_END
public class CumulativePrecip
	extends spk.algo.support.AlgBaseNew
{
//AW:INPUTS
	public double IncrementalPrecip;	//AW:TYPECODE=i	
	String _inputNames[] = { "IncrementalPrecip" }; //, "PreviousFCP" };
//AW:INPUTS_END

//AW:LOCALVARS
	// Enter any local class variables needed by the algorithm.
	double prev_value;
	SimpleDateFormat df;
	Date resetDate = null;
        GregorianCalendar cal = null;
        GregorianCalendar tmp = null;
        BufferedWriter extralog = null;
//AW:LOCALVARS_END

//AW:OUTPUTS
	public NamedVariable CumulativePrecip = new NamedVariable("CumulativePrecip", 0);
	String _outputNames[] = { "CumulativePrecip" };
//AW:OUTPUTS_END

//AW:PROPERTIES	
	public String ResetDate = "";	
	String _propertyNames[] = { "ResetDate", };
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
		
		
		df = new SimpleDateFormat( "ddMMM" );
		try{
			if( !ResetDate.equals("") )
			{
				resetDate = df.parse( ResetDate );
                                cal = new GregorianCalendar();
                                cal.setTime(resetDate);
			}
                        tmp = new GregorianCalendar();
		}
		catch( java.text.ParseException e )
		{
			throw new DbCompException("Could not parse reset date, please use format: ddMMMyyyy HHmm" );
		}



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
                 * TODO: Consider changing this to always pull the previous value
                 *  this is faster, just...feels iffy....It is assumed that all values exist.
                 */	
                double precip = 0.0;
		if( resetDate != null )
		{
                        //cal.set( 
                        GregorianCalendar rst = (GregorianCalendar)cal.clone();
                        rst.set(Calendar.YEAR, _timeSliceBaseTime.getYear()+1900);
                                                
                        tmp.setTime(_timeSliceBaseTime);
                        
                        if( rst.compareTo( tmp ) > 0 ){
                            rst.add( Calendar.YEAR, -1); // our reset date is in front of us, add a year, we want the previous occurance of this date
                        }
                                                
			if( (tmp.get(Calendar.MONTH) == cal.get(Calendar.MONTH) ) && (tmp.get(Calendar.DAY_OF_MONTH) == cal.get(Calendar.DAY_OF_MONTH)) )
			{	
				precip = IncrementalPrecip;
			}
                        else{
                            
                            double data[] = this.getPrevValues("IncrementalPrecip", rst.getTime(), _timeSliceBaseTime);
                            for( int i = 0; i < data.length; i++){
                                precip+=data[i]; // this should include the current days precip
                            }
                        }
		}
                else{
                    // just grap the previous value and add
                    double value = this.getPrevValue("CumulativePrecip", _timeSliceBaseTime);
                    if( isMissing(value) && isMissing(prev_value)){
                        precip = IncrementalPrecip;
                        
                    }else if( isMissing(value) ){
                        precip = prev_value + IncrementalPrecip;                        
                        
                    }else{
                        precip = value + IncrementalPrecip;
                    }
                    prev_value = precip;
                }
                
                
		if( !isMissing( IncrementalPrecip ) )
		{
			
			setOutput( CumulativePrecip, precip );
		}
		else
		{
			deleteOutput( CumulativePrecip );
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

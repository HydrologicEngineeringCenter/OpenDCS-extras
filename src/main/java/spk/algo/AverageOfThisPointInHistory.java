package spk.algo;



import decodes.cwms.CwmsFlags;
import decodes.tsdb.BadTimeSeriesException;
import decodes.tsdb.CTimeSeries;
import java.util.Date;

import ilex.var.NamedVariableList;
import ilex.var.NamedVariable;
import decodes.tsdb.DbAlgorithmExecutive;
import decodes.tsdb.DbCompException;
import decodes.tsdb.DbIoException;
import decodes.tsdb.IntervalCodes;
import decodes.tsdb.VarFlags;
import decodes.tsdb.algo.AWAlgoType;
import ilex.var.NoConversionException;
import ilex.var.TimedVariable;
import java.util.GregorianCalendar;
import java.util.logging.Level;
import java.util.logging.Logger;
import opendcs.dai.TimeSeriesDAI;
import opendcs.opentsdb.Interval;

//AW:IMPORTS
// Place an import statements you need here.
//AW:IMPORTS_END

//AW:JAVADOC
/**
 * Intended primary for daily data, given a fixed day, go back in time to average that day over a period of time.
 * Algorithm was created to operate on generic intervals but has not been tested for them.
 * @author L2EDDMAN
 */
//AW:JAVADOC_END
public class AverageOfThisPointInHistory
	extends spk.algo.support.AlgBaseNew
{
//AW:INPUTS
        // input values, declare a variable, and add the string of the variable name to the _inputNames array
        public double input;   //AW:TYPECODE=i
	String _inputNames[] = { "input"};
//AW:INPUTS_END

//AW:LOCALVARS
	// Enter any local class variables needed by the algorithm.
        private int interval_cal_costant= -1;
        private GregorianCalendar cal = null;
//AW:LOCALVARS_END
        
//AW:OUTPUTS
	// created a NameVariable with the name you want, and add the string of that name to the array
        public NamedVariable average = new NamedVariable("average",0);
	String _outputNames[] = { "average" };
//AW:OUTPUTS_END

//AW:PROPERTIES	
        public String interval = "years";
        public int    number_of_intervals = 10;
	String _propertyNames[] = { "interval", "number_of_intervals" };
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
                // create an output variable and give it's name here
                // this variable will determine the output interval
		//_aggPeriodVarRoleName = "avg_lake_area";
                Interval i = IntervalCodes.getInterval(interval);
                interval_cal_costant = IntervalCodes.getInterval(interval).getCalConstant();
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
                cal = new GregorianCalendar( this.aggTZ );
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
                int num = 0;
                double storage =0.0;
                if( !isMissing( input )){
                    storage = input;
                    num++;
                }
                CTimeSeries ts = this.getParmRef("input").timeSeries;
                
                
                
                
                
                
                for( int i=1; i < number_of_intervals; i++){
                    cal.setTime(_timeSliceBaseTime);
                    int current = cal.get(interval_cal_costant);
                    cal.set(interval_cal_costant, current-i);
                    Date time = cal.getTime();
                    TimedVariable tv = null;
                    TimeSeriesDAI tdao = null;
                    try{
                        debug3("filling ts for this time");
                        tdao = this.tsdb.makeTimeSeriesDAO();                        
                        tdao.fillTimeSeries(ts, time, time);
                        
                
                        debug3( "Search for data at time: " + time.toString() );
                        tv = ts.findWithin(cal.getTime(), 1000);
                        
                        debug3("Value at " + tv.timeString() + " = " + tv.getStringValue() );
                        storage += tv.getDoubleValue();
                        num++;
                    
                    } catch (NoConversionException ex) {
                        debug3("Not valid value: " + tv.getStringValue());
                        setFlagBits(average, CwmsFlags.VALIDITY_QUESTIONABLE);
                    } catch( NullPointerException ex) {
                        debug3("Missing Value in ten year average");
                        setFlagBits(average, CwmsFlags.VALIDITY_QUESTIONABLE);                        
                    } catch (DbIoException ex) {
                        debug3("Missing Value in ten year average");
                        setFlagBits(average, CwmsFlags.VALIDITY_QUESTIONABLE);      
                    } catch (BadTimeSeriesException ex) {
                        debug3("Missing Value in ten year average");
                        setFlagBits(average, CwmsFlags.VALIDITY_QUESTIONABLE);      
                    } finally{
                        if( tdao != null){
                            tdao.close();
                        }
                    }                                       
                    
                }
            
                double ave = storage/num;
                debug3("Aveerage =" + ave + " based on " + num + " value(s)");
                setOutput(average, ave);
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

package spk.algo;

import decodes.cwms.CwmsFlags;
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
import java.util.TimeZone;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
//AW:IMPORTS_END

//AW:JAVADOC
/**
 * This implements the logic for taking an Hourly time series of regulated flow 
 * and calculating the 1,2,3, and 5 day accumulated volumes from it.
 * is assumes there must be hours_future data (defaulting to 240) available in 
 * the system to calculated a set of volumes. (e.g. we got a 10 day forecast, after
 * the first time step of the forecast don't calculate anything.
 * 
 * The forecast will convert the hourly flows to volume before summing so it should
 * be DST safe if configured for local time except that the summing function will
 * only count hours. (but really, just leave it as GMT)
 * 
 * only, leave the units blank in the conversion.
 * 
 * marker is the variable used to verify a value should be created for the day,
 * set the value to ignore, the program logic will skip any set of data
 * that doesn't include a value for the marker

 */
//AW:JAVADOC_END
public class FolsomFBOParameter
	extends spk.algo.support.AlgBaseNew
{
//AW:INPUTS
        public double marker;   //AW:TYPECODE=i
	public double forecast;	//AW:TYPECODE=i	
	String _inputNames[] = { "marker","forecast" }; //, "PreviousFCP" };
//AW:INPUTS_END

//AW:LOCALVARS
	// Enter any local class variables needed by the algorithm.
	
	
//AW:LOCALVARS_END

//AW:OUTPUTS
	public NamedVariable OneDay = new NamedVariable("OneDay", 0);
        public NamedVariable TwoDay = new NamedVariable("TwoDay", 0);
        public NamedVariable ThreeDay = new NamedVariable("ThreeDay",0);
        public NamedVariable FiveDay = new NamedVariable("FiveDay",0);
	String _outputNames[] = { "OneDay", "TwoDay", "ThreeDay", "FiveDay" };
//AW:OUTPUTS_END

//AW:PROPERTIES
	public long hours_future = 120;	
	String _propertyNames[] = { "hours_future" };
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
            
            Date future = new Date( _timeSliceBaseTime.getTime() + (hours_future)*3600*1000);
            
            try {
                
                if( isMissing(marker)){
                    info("No value present in marker, skipping this dataset");
                    return;
                }
                
                CTimeSeries ts = this.tsdb.makeTimeSeries(this.getParmTsId("forecast") );
                ts.setUnitsAbbr(this.getParmUnitsAbbr("forecast"));
                this.tsdb.fillTimeSeries(ts, _timeSliceBaseTime, future);
                
                double accumulation = 0.0;
                double oneday = 0.0;
                double twoday = 0.0;
                double threeday = 0.0;
                double fiveday = 0.0;
                double conversion = 1.9835/24;   
                String units = ts.getUnitsAbbr();
                if( units.equalsIgnoreCase("cms") || units.equalsIgnoreCase("m3/s") ){
                    conversion = 3600; // 1 cms * 3600 s => 3600.
                }
                
                if( ts.size() >= (hours_future+1) ){
                    for(int i = 0; i < 5*24; i++ ){
                        accumulation += ts.sampleAt(i).getDoubleValue()*conversion;
                        switch(i){
                            // NOTE: the indexes start at 0, e.g. 23 => hour 24, so we subtract one from each
                            case 23:
                                oneday = accumulation;
                                break;
                            case 47:
                                twoday = accumulation;
                                break;
                            case 71:
                                threeday = accumulation;
                                break;
                            case 119:
                                fiveday = accumulation;
                                break;
                        }
                    }
                    
                    setOutput(OneDay, oneday);
                    setOutput(TwoDay, twoday);
                    setOutput(ThreeDay, threeday);
                    setOutput(FiveDay, fiveday);
                    // keep from accidental override'
                    VarFlags.setNoOverwrite(OneDay);
                    VarFlags.setNoOverwrite(TwoDay);
                    VarFlags.setNoOverwrite(ThreeDay);
                    VarFlags.setNoOverwrite(FiveDay);
                } else{
                    info("We have already done the accumulation for this forecast, doing nothing");
                }
                
                
            } catch (DbIoException ex) {
                debug3("couldn't retireve future time series info");
            } catch (BadTimeSeriesException ex) {
                debug3("Somehow this time series doesn't exit");
            } catch (NoSuchObjectException ex) {
                debug3( ex.getMessage() );                
            } catch (NoConversionException ex) {
                debug3( ex.getMessage() );
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

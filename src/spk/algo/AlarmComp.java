package spk.algo;



import decodes.db.DbEnum;
import decodes.tsdb.BadTimeSeriesException;
import decodes.tsdb.CTimeSeries;
import java.util.Date;

import ilex.var.NamedVariableList;
import ilex.var.NamedVariable;
import decodes.tsdb.DbAlgorithmExecutive;
import decodes.tsdb.DbCompException;
import decodes.tsdb.DbIoException;
import decodes.tsdb.VarFlags;
import decodes.tsdb.algo.AWAlgoType;
import ilex.var.TimedVariable;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;
import lrgs.gui.DecodesInterface;
import opendcs.dai.EnumDAI;
import opendcs.dai.TimeSeriesDAI;
import spk.apps.support.Alarms.AlarmCondition;
import spk.apps.support.Alarms.AlarmList;
import spk.apps.support.Alarms.AlarmResponse;

//AW:IMPORTS
// Place an import statements you need here.
//AW:IMPORTS_END

//AW:JAVADOC
/**
* Process data and compare it to various alarm thresholds to see if a message
* should be sent on for further processing.
 */
//AW:JAVADOC_END
public class AlarmComp
	extends decodes.tsdb.algo.AW_AlgorithmBase
{
//AW:INPUTS
        // input values, declare a variable, and add the string of the variable name to the _inputNames array
        public double input;    //AW:TYPECODE=i
	String _inputNames[] = { "input" };
//AW:INPUTS_END

//AW:LOCALVARS
	// Enter any local class variables needed by the algorithm.
	public AlarmList alarms;
        public CTimeSeries ts;
//AW:LOCALVARS_END

//AW:OUTPUTS
	
	String _outputNames[] = { };
//AW:OUTPUTS_END

//AW:PROPERTIES	
        public String address = "spk-wmlocal1";
        public long    port    = 51900;
        public String alarm_file = "/shared/config/alarms/alarms.txt";
	String _propertyNames[] = { "address", "port", "alarm_file" };
        
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
		alarms = new AlarmList();
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
                
                // TODO: for testing the alarm list is short, long term this should be changed to only load on change, like DatChk
                alarms.load_alarms(AlarmCondition.CHECK_EQUALS | AlarmCondition.CHECK_GREATER | AlarmCondition.CHECK_LESS | AlarmCondition.CHECK_MISSING | AlarmCondition.CHECK_ROC | AlarmCondition.CHECK_STATIC, alarm_file);
                
                
                //ts = new CTimeSeries( getParmRef("input").compParm );
                ts = getParmRef("input").timeSeries;
                //TimeSeriesDAI tdai = tsdb.makeTimeSeriesDAO();
                //tdai.fillTimeSeriesMetadata(ts);
                // TODO: I'm quite sure not a single line below should actually BE needed...but it is.
                //ts.setTimeSeriesIdentifier(getParmTsId("input"));
                //ts.setUnitsAbbr(getParmUnitsAbbr("input"));
                debug3( getParmTsUniqueString("input"));
                //debug3( getParmTsId("input").getUniqueString());
                
                
//AW:BEFORE_TIMESLICES_END
            } catch (FileNotFoundException ex) {
                debug3( ex.toString() );
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
		// we don't want excessive messages sent, so if a
                // big block of data gets pushed only deal with last alarm
                TimedVariable tv = new TimedVariable();
                tv.setTime( _timeSliceBaseTime);
                tv.setValue(input);
		ts.addSample(tv);
                
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
                ts.sort();
                
                if( !DecodesInterface.isGUI() ){
                    debug3("Not GUI, will ignore data that isn't new.");
                    // something to ignore data after figuring out if said data is old.
                    /*
                    Perhaps search for data in the database that exists after and then assume this
                    data has already been processed. If equal, assume processing for first time, or data was corrected
                    */
                }
                
                AlarmResponse res = alarms.check_timeseries(ts);
                if( res != null){
                    debug3( ts.getNameString() );
                    debug3( res.toString() );
                            
                    Socket s;
                    try {
                        s = new Socket("spk-wmlocal1.spk.usace.army.mil",(int)51900);
                        s.getOutputStream().write(res.toString().getBytes() );
                        s.close();
                    } catch (IOException ex) {
                        warning("failed to send alarm message" + ex.toString() );
                    }
                    
                }
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

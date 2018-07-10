package spk.algo.reports;



import java.io.IOException;
import java.net.UnknownHostException;
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
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;
//AW:IMPORTS_END

//AW:JAVADOC
/**
 * Simple proxy to the report generation. Whenever data comes in and gets updated. we send a message to the
 * report runner. It will manage not having multiple runnings of each file
 * @author l2eddman
 */
//AW:JAVADOC_END
public class Report
	extends decodes.tsdb.algo.AW_AlgorithmBase
{
//AW:INPUTS
        // input values, declare a variable, and add the string of the variable name to the _inputNames array
        public double in1;	//AW:TYPECODE=i
	public double in2;	//AW:TYPECODE=i
	public double in3;	//AW:TYPECODE=i
	public double in4;	//AW:TYPECODE=i
	public double in5;	//AW:TYPECODE=i
	public double in6;	//AW:TYPECODE=i
        public double in7;	//AW:TYPECODE=i
        public double in8;	//AW:TYPECODE=i
        public double in9;	//AW:TYPECODE=i
        public double in10;	//AW:TYPECODE=i
        public double in11;	//AW:TYPECODE=i
        public double in12;	//AW:TYPECODE=i
        public double in13;	//AW:TYPECODE=i
        public double in14;	//AW:TYPECODE=i
        public double in15;	//AW:TYPECODE=i
        public double in16;	//AW:TYPECODE=i
        public double in17;	//AW:TYPECODE=i
        public double in18;	//AW:TYPECODE=i
        public double in19;	//AW:TYPECODE=i
        public double in20;	//AW:TYPECODE=i
        public double in21;	//AW:TYPECODE=i
        public double in22;	//AW:TYPECODE=i
        public double in23;	//AW:TYPECODE=i
        public double in24;	//AW:TYPECODE=i
        public double in25;	//AW:TYPECODE=i
        public double in26;	//AW:TYPECODE=i
        public double in27;	//AW:TYPECODE=i
        public double in28;	//AW:TYPECODE=i
        public double in29;	//AW:TYPECODE=i
        public double in30;	//AW:TYPECODE=i
	String _inputNames[] = { "in1",  "in2",  "in3",  "in4",  "in5",  "in6",
                                 "in7",  "in8",  "in9",  "in10", "in11", "in12",
                                 "in13", "in14", "in15", "in16", "in17", "in18",
                                 "in19", "in20", "in21", "in22", "in23", "in24",
                                 "in25", "in26", "in27", "in28", "in29", "in30" };
//AW:INPUTS_END

//AW:LOCALVARS
	// Enter any local class variables needed by the algorithm.
	
//AW:LOCALVARS_END

//AW:OUTPUTS
	
	String _outputNames[] = { };
//AW:OUTPUTS_END

//AW:PROPERTIES	
        public String report = "";
	String _propertyNames[] = { "report" };
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
        
            try {
                debug3( "Opening Connection");
                Socket s = new Socket("localhost", 2101);
                String msg = report + "\r\n";
                s.getOutputStream().write( msg.getBytes() );
                // wait for response
                s.close();
                
                
                
            } catch (UnknownHostException ex) {
                Logger.getLogger(Report.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(Report.class.getName()).log(Level.SEVERE, null, ex);
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
		// we do nothing here. we don't need to call this for the entire set of records, just once
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

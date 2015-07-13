package spk.algo.topcon;

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
import java.lang.Math;
//AW:IMPORTS_END

//AW:JAVADOC
/**
Calculated the Flood Control Parameter for the SAFCA version of the Folsom water
control diagram.

 */
//AW:JAVADOC_END
public class SAFCAFloodControlParameter
	extends decodes.tsdb.algo.AW_AlgorithmBase
{
//AW:INPUTS
        // input values, declare a variable, and add the string of the variable name to the _inputNames array
        public double french_meadow;   //AW:TYPECODE=i
        public double hell_hole;   //AW:TYPECODE=i
        public double union_valley;    //AW:TYPECODE=i
	String _inputNames[] = { "french_meadow", "hell_hole", "union_valley" };
//AW:INPUTS_END

//AW:LOCALVARS
	// Enter any local class variables needed by the algorithm.
	
//AW:LOCALVARS_END

//AW:OUTPUTS
	public NamedVariable safca_fcp = new NamedVariable("safca_fcp", 0);
	String _outputNames[] = { "safca_fcp" };
//AW:OUTPUTS_END

//AW:PROPERTIES
        public double french_meadow_crest = 110700;
        public double french_meadow_maxcredit = 45000;
        public double hell_hole_crest = 207600;
        public double hell_hole_maxcredit = 80000;
        public double union_valley_crest = 235100;
        public double union_valley_maxcredit = 75000;
	String _propertyNames[] = { "french_meadow_crest", "french_meadow_maxcredit", "hell_hole_crest", "hell_hole_maxcredit", "union_valley_crest","union_valley_maxcredit"  };
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
		double frenchmeadow_available = french_meadow_crest - french_meadow;
                double hellhole_available     = hell_hole_crest - hell_hole;
                double unionvalley_available = union_valley_crest - union_valley;

                double frenchmeadow_credit = Math.min( french_meadow_maxcredit, frenchmeadow_available );
                double hellhole_credit = Math.min( hell_hole_maxcredit, hellhole_available );
                double unionvalley_credit = Math.min( union_valley_maxcredit, unionvalley_available );

                double credit = frenchmeadow_credit + hellhole_credit + unionvalley_credit;
                debug3( "French Meadow: " );
                debug3( " storage: " + french_meadow );
                debug3( " space  : " + frenchmeadow_available );
                debug3( " credit : " + frenchmeadow_credit );

                debug3( "Hell Hole: " );
                debug3( " storage: " + hell_hole );
                debug3( " space  : " + hellhole_available );
                debug3( " credit : " + hellhole_credit );

                debug3( "Union Valley:" );
                debug3( " storage: " + union_valley );
                debug3( " space  : " + unionvalley_available );
                debug3( " credit : " + unionvalley_credit );

                debug3( "Credit: " + credit);


                setOutput(safca_fcp, credit);
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

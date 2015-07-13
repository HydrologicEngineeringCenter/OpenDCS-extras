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
import decodes.db.Constants;
import decodes.cwms.CwmsFlags;
import decodes.db.EngineeringUnit;
import decodes.db.UnitConverter;
import decodes.tsdb.*;
import java.io.*;
import ilex.util.EnvExpander;
//AW:IMPORTS_END

//AW:JAVADOC
/**
 * Flags data based on various provided limits. Limits will be a file called
 * <station_nmeumonic>.limits ( at SPK our CWMS path names are "<BASIN>
 * <station nmeumonic>-<full site name>"...etc limit files will just use the
 * station mnemonic. The limit file will contain the limits for all of the
 * stations parameters.
Limits Are
High Reject
High Question
Low Question
Low Reject

The file will have a structure like this
Parameter: "Low Reject" "Low Question" "High Question" "High Reject"

 * **************
 * **************
 * this was temporary until I had access to datchk, which was there
 * the whole time but not exposed to the CCP properly
 * **************
 * **************

 * @author L2EDDMAn
 * @deprecated 
 */
//AW:JAVADOC_END
public class DataCheck
	extends decodes.tsdb.algo.AW_AlgorithmBase
{
//AW:INPUTS
	public double input;	//AW:TYPECODE=i
	String _inputNames[] = { "input" };
//AW:INPUTS_END

//AW:LOCALVARS
	// Enter any local class variables needed by the algorithm.
	// These will be read from the limit file for a given station
	// if there is no limit file these defaults will be used and 
	// the value will be assume to always be valid
	double high_reject = Double.MAX_VALUE;
	double high_question = Double.MAX_VALUE;
	double low_question = Double.MIN_VALUE;
	double low_reject = Double.MIN_VALUE;

//AW:LOCALVARS_END

//AW:OUTPUTS
	public NamedVariable flagged = new NamedVariable("flagged", 0);
	String _outputNames[] = { "flagged" };
//AW:OUTPUTS_END

//AW:PROPERTIES
	public String limitFileDir = "$DECODES_INSTALL_DIR/limitfiles";
	String _propertyNames[] = { "limitFileDir" };
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
                 /*
		// Code here will be run once, after the algorithm object is created.
		// now we must file the correct limit file and extract the limits for our parameter
		String site_name = getSiteName( "input", "cwms" );
		String station_name = site_name.split("\\-")[0].split("\\ ")[1];
		ParmRef pr = getParmRef("input");
		DbCompParm parm = pr.compParm;
                //parm.get
		//SiteDatatype sdt = parm.getSiteDatatype();               
		String param = parm.getDataType().getCode();
		debug3( "Checking limits at: " + site_name + "(Station Name: " +station_name + ") with parameter: " + param );
		String filename = EnvExpander.expand( limitFileDir + "/" + station_name + ".limits" );
		debug3( "Using '" + filename + "' for limits" );
		try{
			BufferedReader in = new BufferedReader( new FileReader(filename) );
			String line;
			String parameter;
			while (( line = in.readLine()) != null ){
				if( line.trim().charAt(0) != '#' ) 
				{ // line is not a comment, process
					debug3( "Using line: " + line );
					parameter = line.split(":")[0];
					if( parameter.equalsIgnoreCase( param ) )
					{//this  is the limit set we want\
						String limits[] = line.split(":")[1].split("\\ ");
						debug3( "Length of line is " + limits.length );
						debug3( "limits(from string var) are: " + limits[1] + " " + limits[2] + " " + limits[3] + " " + limits[4] );
						low_reject = Double.parseDouble(limits[1]);
						low_question = Double.parseDouble(limits[2]);
						high_question = Double.parseDouble(limits[3]);
						high_reject = Double.parseDouble(limits[4] );
					}
				}
			}
			in.close();
		}
		catch( IOException err ){
			throw new DbCompException( "Failed to open and process limit file: " + filename);
		}
		debug3( "Limits are in lr lq rq rh order: " + low_reject + " " + low_question + " " + high_question + " " + high_reject );
                */
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
                decodes.db.EngineeringUnit eu = EngineeringUnit.getEngineeringUnit("mm");
                UnitConverter  uc = null;
                for( int i = 0; i < 100000; i++){

                    uc = decodes.db.CompositeConverter.build(eu, eu);
                    
                    if( uc != null){
                        debug3("Attempt " + i + " result: " + uc.toString() );
                    } else{
                            debug3("Attempt " + i + " result: failed" );
                    }
                  
                }
                debug3("Done Looping through unit converter creation");
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
		setOutput( flagged, input ); // we always copy the value over, all we are doing in this comp is setting flags
		// First bit is data was screened ( that would be this comp )
		// bit 5 is data is rejected
		// bit 4 is data is questioned
		// bit 6-7 range check
		//  00 value is less than/equal to low reject
		//  01 value is greater than low reject less than/equal to low question
		//  10 value is greater than/equal to or high question less than high reject
		//  11 value is greater than or equal to high reject
		//
		//  bit 16 a magnitude test has failed ( that would also be this comp )
		if( input <= low_reject )
		{
			debug3( "Data value classified as rejected, below lowest magnitude" );
			//setFlagBits( flagged, Integer.parseInt( "100000000010001", 2 ) );
			setFlagBits( flagged, CwmsFlags.SCREENED | CwmsFlags.TEST_ABSOLUTE_VALUE | CwmsFlags.RANGE_NO_RANGE | CwmsFlags.VALIDITY_REJECTED );
		     		}
		else if( input <= low_question && input > low_reject )
		{
			debug3( "Data value classified as questionable, value too low" );
			//setFlagBits( flagged, Integer.parseInt( "100000000101001", 2 ) );
			setFlagBits( flagged, CwmsFlags.SCREENED | CwmsFlags.TEST_ABSOLUTE_VALUE | CwmsFlags.RANGE_RANGE_1 | CwmsFlags.VALIDITY_QUESTIONABLE );
		}
		else if( input >= high_question && input < high_reject )
		{
			debug3( "Data value classified as questionable, value too high" );
			//setFlagBits( flagged, Integer.parseInt( "100000001001001", 2) );
			setFlagBits( flagged, CwmsFlags.SCREENED | CwmsFlags.TEST_ABSOLUTE_VALUE | CwmsFlags.RANGE_RANGE_2 | CwmsFlags.VALIDITY_QUESTIONABLE );
		}
		else if( input >= high_reject )
		{
			debug3( "Data value classified as rejected, value above highest magnitude" );
			//setFlagBits( flagged, Integer.parseInt( "100000001110001", 2) );
			setFlagBits( flagged, CwmsFlags.SCREENED | CwmsFlags.TEST_ABSOLUTE_VALUE | CwmsFlags.RANGE_RANGE_3 | CwmsFlags.VALIDITY_REJECTED );
		}
		else
		{//the value has passed all tests
			//setFlagBits( flagged, Integer.parse( "11", 2 ) );
			setFlagBits( flagged, CwmsFlags.SCREENED | CwmsFlags.TEST_ALL_PASS | CwmsFlags.VALIDITY_OKAY );
		}
		// we do not set the protection bit in this comp, this is done when data is saved in the CAVI
		// based on the CopyNoOverwrite code from Ilex, if the protection is set data won't be overwritten
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

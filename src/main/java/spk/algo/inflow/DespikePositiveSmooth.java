/*
 *  As a work of the United States Government, this package 
 *  is in the public domain within the United States. Additionally, 
 *  We waive copyright and related rights in the work worldwide
 *  through the CC0 1.0 Universal Public Domain Dedication 
 *  (which can be found at https://creativecommons.org/publicdomain/zero/1.0/).

 */
package spk.algo.inflow;

import java.util.Date;

import ilex.var.NamedVariableList;
import ilex.var.NamedVariable;
import decodes.tsdb.DbAlgorithmExecutive;
import decodes.tsdb.DbCompException;
import decodes.tsdb.DbIoException;
import decodes.tsdb.IntervalCodes;
import decodes.tsdb.VarFlags;
import decodes.tsdb.algo.AWAlgoType;
import java.util.ArrayList;


//AW:IMPORTS
// Place an import statements you need here.

//AW:IMPORTS_END

//AW:JAVADOC
/**
 * Takes a window of data (containing an odd number of samples) and outputs a
 * despike, positived, and smoothed values based on a script provided by Mike
 * Perryman.
 * 
 * The output interval is taken to be the total window.
 * The smoothing will happen over a 5 hour window and will be a centered average.
 * 
 */
//AW:JAVADOC_END
/**
 *
 * @author Michael Neilson <michael.a.neilson@usace.army.mil>
 */
public class DespikePositiveSmooth
        extends decodes.tsdb.algo.AW_AlgorithmBase {
//AW:INPUTS
    // input values, declare a variable, and add the string of the variable name to the _inputNames array

    public double inflow;    //AW:TYPECODE=i
    String _inputNames[] = {"inflow"};
//AW:INPUTS_END

//AW:LOCALVARS
    // Enter any local class variables needed by the algorithm.
    private ArrayList<Double> data_list;
    private int expected_intervals;
//AW:LOCALVARS_END

//AW:OUTPUTS
    public NamedVariable smoothed_inflow = new NamedVariable("smoothed_inflow", 0);
    String _outputNames[] = {"smoothed_inflow"};
//AW:OUTPUTS_END

//AW:PROPERTIES	
    String _propertyNames[] = {};
//AW:PROPERTIES_END

	// Allow javac to generate a no-args constructor.
    /**
     * Algorithm-specific initialization provided by the subclass.
     */
    protected void initAWAlgorithm()
            throws DbCompException {
//AW:INIT
        _awAlgoType = AWAlgoType.RUNNING_AGGREGATE;
        _aggPeriodVarRoleName = "smoothed_inflow";        
//AW:INIT_END

//AW:USERINIT
        aggLowerBoundClosed = true;
        aggUpperBoundClosed = true;
        
        
        
//AW:USERINIT_END
    }

    /**
     * This method is called once before iterating all time slices.
     */
    protected void beforeTimeSlices()
            throws DbCompException {
//AW:BEFORE_TIMESLICES
        // This code will be executed once before each group of time slices.
        // For TimeSlice algorithms this is done once before all slices.
        // For Aggregating algorithms, this is done before each aggregate
        // period.
        data_list = new ArrayList<Double>();
        if( aggLowerBoundClosed != true && aggUpperBoundClosed != true){
            throw new DbCompException("The aggLowerBoundClosed and aggUpperBoundClosed should both be set to true for this algorithm. Please add them to the properties list");
        }
        debug3("we will be using the following data: ");
        debug3(" You should see an uneven number of values starting and ending on the aggregate period boundraries");
//AW:BEFORE_TIMESLICES_END
    }

    /**
     * Do the algorithm for a single time slice. AW will fill in user-supplied
     * code here. Base class will set inputs prior to calling this method. User
     * code should call one of the setOutput methods for a time-slice output
     * variable.
     *
     * @throws DbCompException (or subclass thereof) if execution of this
     * algorithm is to be aborted.
     */
    protected void doAWTimeSlice()
            throws DbCompException {
//AW:TIMESLICE
        // Enter code to be executed at each time-slice.
        data_list.add(inflow); // we don't care about the time, the system will figure it out
        debug3(" " + _timeSliceBaseTime + " " + inflow);
//AW:TIMESLICE_END
    }

    /**
     * This method is called once after iterating all time slices.
     */
    protected void afterTimeSlices()
            throws DbCompException {
//AW:AFTER_TIMESLICES
        // This code will be executed once after each group of time slices.
        // For TimeSlice algorithms this is done once after all slices.
        // For Aggregating algorithms, this is done after each aggregate
        // period.
        
        
        debug3("Processing data between " + _aggregatePeriodBegin + " and " + _aggregatePeriodEnd );
        
        // get data
        int interval_sec = IntervalCodes.getIntervalSeconds(this.getInterval("inflow"));
        int start_sec = (int)(_aggregatePeriodBegin.getTime()/1000L);
        int end_sec = (int)(_aggregatePeriodEnd.getTime()/1000L);
        
        int intervals = (end_sec-start_sec)/interval_sec + 1;
        
        if( intervals < 5 ){
            throw new DbCompException("This algorithm needs be called with a window creater than 5 hours");
        }
        
        debug3("Expecting " + intervals + " have " + data_list.size() );
        if( intervals != data_list.size()){
            return; //we're probably at the end
        }
        // verify amount
        
        double data[] = new double[data_list.size()];
        for( int i = 0; i < data_list.size(); i++ ){
            data[i] = data_list.get(i).doubleValue();
            
        }
        
        int middle = intervals/2;
        
        
        // despike (2 pass)
        for( int i = 0; i < 2; i++ ){
            for( int j = 0; j < data.length-1; j++){
                if( isMissing(data[j]) || isMissing( data[j+1]) ) continue;                
                
                int signJ = data[j] < 0.0 ? -1 : 1;
                int signJp1 = data[j+1] < 0.0 ? -1 : 1;
                double absJ = Math.abs(data[j]);
                double absJp1 = Math.abs(data[j+1]);
                double total = data[j] + data[j+1];
                boolean despiked = false;
                if( signJ != signJp1 ){
                    if( Math.abs(total)/ Math.max( absJ, absJp1 ) < .5 ){
                        data[j] = data[j+1] = total/2.0;
                        despiked = true;                                                               
                    }
                }
                
                if( !despiked && j > 1){
                    if( isMissing( data[j-1] )) continue;
                    
                    int signJm1 = data[j-1] < 0.0 ? -1 : 1;
                    if( signJ == signJm1 && signJ == signJp1 ) continue; 
                    
                    double absJm1 = Math.abs(data[j-1]);
                    if( Math.abs(total)/ Math.max( absJm1, Math.max(absJ, absJp1) )   < .67 ){
                        data[j-1]=data[j] = data[j+1] = total/3.0;
                        despiked=true;
                    }
                }
                
                
            }
        }
        
        
        
        // positive
        
        for( int i = 0; i < data.length; i++ ){            
            if( !isMissing(data[i]) && data[i] < 0 ){
                double total = data[i];
                double totalPositive = 0.0;
                int first = i-1;
                int last = i+1;
                int previousFirst=-1;
                int previousLast=data.length;
                int numNegative = 1;
                for( int k = 0; k < 1;k++){ // allowing this to go through ALL of the window drastically overdamps the reasponse in certain conditions.
                                            // This may depend a lot on the specific data set and I will like make this a changable property at some point.
                    
                    while( first > 0 && isMissing(data[first])) first -= 1; 
                    
                    if( first > -1 ){
                        if( total+data[first] >= 0.0 ){
                            totalPositive +=  data[first];
                            total += data[first];
                            if( previousLast == data.length){
                                last = i;
                            } else{
                                last = previousLast;
                            }
                            
                            //last = ( (previousLast==data.length) ? i : previousLast );    
                            break;
                        }
                    }
                    
                    while( last < data.length-1 && isMissing(data[last] ) ) last += 1;
                    
                    if( first == -1 && last == data.length) break; // give up, we've been about the whole array
                    
                    if( last < data.length ){
                        if( total+data[last] >= 0.0 ){
                            totalPositive += data[last];
                            total+=data[last];
                            
                            if( previousLast == data.length ){
                                last = i;
                            } else {
                                last = previousLast;
                            }                            
                            //first = ( (previousFirst==-1) ? i : previousFirst );
                            break;
                        }
                    } else{
                        if( first > -1){
                            if( data[first] >= 0.0 ) {
                                totalPositive += data[first];
                            }
                            else {
                                numNegative++;
                            }
                            total += data[first];
                        }
                        
                        if( last < data.length ){ // pretty sure this is unreachable
                            if( data[last] >= 0.0) {
                                totalPositive += data[last];
                            }
                            else numNegative++;
                            total += data[last];
                        }
                        
                    }
                    
                    if( total >= 0.0 ) break;
                    
                    if( first == -1 && last == data.length - 1 ) break ;
                    if( first > -1 ){
                        previousFirst = first;
                        first--;                                
                    }
                    if( last < data.length -1 ){
                        previousLast = last;
                        last++;
                    }
                }
                
                // the rest
                if( total > 0.0 ){
                    double ratio = total/totalPositive;
                    
                    if( first < 0) first = 0;
                                        
                    for( int j = first; j <= last; j++ ){
                        if( last == data.length ) break;
                        if( !isMissing(j)){
                            if( data[j] < 0.0) data[j] = 0.0;
                            else data[j] *= ratio;
                        }                        
                    }
                    //i = last; // this line in the python script, would do absolutely nothing.
                    // also, we've chosen a limited window so just process everything.
                } else{
                    data[i] = 0.0; // no better option, this is short interval data on a really small window.
                }
                
            }
            
            
        }
        
        
        
        // smooth
        double out[] = new double[data.length];
        // even though we only pick the center value of the window, process everything
        // so that when viewing in the debugger we can sanity check it.
        for( int i = 2; i < data.length-2 ; i++){
            double value = 0;
            int num = 0;
            
            for(int j = -2; j <= 2; j++){
                if( !isMissing(data[i+j]) ){
                    value += data[i+j];
                    num++;
                }
            }
            out[i] = value/num; // this should have a minNumCheck.
        }
        
        setOutput(smoothed_inflow, out[middle]);
        
        //output
        
        
        
//AW:AFTER_TIMESLICES_END
    }

    /**
     * Required method returns a list of all input time series names.
     */
    public String[] getInputNames() {
        return _inputNames;
    }

    /**
     * Required method returns a list of all output time series names.
     */
    public String[] getOutputNames() {
        return _outputNames;
    }

    /**
     * Required method returns a list of properties that have meaning to this
     * algorithm.
     */
    public String[] getPropertyNames() {
        return _propertyNames;
    }
}

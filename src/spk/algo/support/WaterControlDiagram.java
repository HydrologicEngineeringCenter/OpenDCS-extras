
package spk.algo.support;




import decodes.tsdb.DbCompException;
import spk.algo.support.exceptions.InterpolationException;
import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import static spk.algo.support.Logging.*;



/** 
 * Contains all of the support functions for calculating top of conservation storage
 * @author L2EDDMAN
 */
public class WaterControlDiagram
	
{
        
    
         
	// Enter any local class variables needed by the algorithm.
        // TODO: might be able to rework this to take advantage of TreeMap
	HashMap< Double, ArrayList<Double> > graph = null;
        HashMap< Double, ArrayList<Double> > snow_graph = null;
        ArrayList< Double > lower_bound = null;
        ArrayList< Double > upper_bound = null;
	ArrayList< Double >       times = null;
	ArrayList< Double>   irrigation = null;
        double               gross_pool = 0.0;
        double          restricted_pool = Double.NEGATIVE_INFINITY;
        double            max_snowspace = 0.0;
        ExponentialEquation           equation = null;

        
        public WaterControlDiagram()
        {
            
            graph = new HashMap< Double, ArrayList<Double> > ();
            snow_graph = new HashMap< Double, ArrayList<Double> > ();
            lower_bound = new ArrayList< Double >();
            upper_bound = new ArrayList< Double >();
            times = new ArrayList<Double>();
            irrigation = new ArrayList<Double>();
        }

        public WaterControlDiagram( String graph_file ) throws Exception
        {
            graph = new HashMap< Double, ArrayList<Double> > ();
            snow_graph = new HashMap< Double, ArrayList<Double> > ();
            lower_bound = new ArrayList< Double >();
            upper_bound = new ArrayList< Double >();
            times = new ArrayList<Double>();
            irrigation = new ArrayList<Double>();
            this.load_graph( graph_file );
        }
        /**
         * Load a graph from a file
         * @param graph_file
         *  full path to the file to load
         * @throws java.lang.Exception
         */
	public void load_graph( String graph_file ) throws java.lang.Exception
	{
            
            File f = new File( graph_file );
            if( !f.exists() )
            {
                throw new java.lang.Exception( "The requested graph file doesn't exist ");
            }

            BufferedReader reader = new BufferedReader( new FileReader(f)  );
            try{
                String line = null;
                String []parts = null;
                String section = null;
                while( (line = reader.readLine()) != null )
                {
                    if( line.trim().equals("") || line.trim().charAt(0) == '#' ) continue; // comment or blank line, skip
                    parts = line.split( "\\:");
                    section = parts[0].toLowerCase();
                    if( section.equals( "rain_times" ))
                    {
                        debug3( "load_graph: reading in times array");
                        String _times[] = parts[1].trim().split("\\s+");
                        for( String t: _times)
                        {
                            
                            this.times.add(Double.parseDouble(t));
                        }
                    }
                    else if( section.equals("rain_line")  )
                    {

                        String bits[] = parts[1].split(",");
                        Double fcp = Double.parseDouble(bits[0]);
                        debug3( "load_graph: reading in line: " + fcp);
                        String _values[] = bits[1].trim().split("\\s+");
                        ArrayList<Double> values = new ArrayList<Double>();
                        for( String v: _values)
                        {
                            values.add( Double.parseDouble(v));
                        }

                        graph.put( fcp, values);
                    }
                    else if( section.equals("snow_line")  )
                    {

                        String bits[] = parts[1].split(",");
                        Double fcp = Double.parseDouble(bits[0]);
                        debug3( "load_graph: reading in line: " + fcp);
                        String _values[] = bits[1].trim().split("\\s+");
                        ArrayList<Double> values = new ArrayList<Double>();
                        for( String v: _values)
                        {
                            values.add( Double.parseDouble(v));
                        }

                        snow_graph.put( fcp, values);
                    }
                    else if( section.equals( "upper_bound") )
                    {
                        debug3( "adding upper bound to graph" );
                        String _values[] = parts[1].trim().split("\\s+");
                        for( String v: _values)
                        {
                            upper_bound.add( Double.parseDouble(v) );
                        }
                    }
                    else if( section.equals( "lower_bound") )
                    {
                        debug3("adding lower bound to graph");
                        String _values[] = parts[1].trim().split("\\s+");
                        for( String v: _values)
                        {
                            lower_bound.add( Double.parseDouble(v) );
                        }
                    }
                    else if( section.equals("gross_pool"))
                    {
                        this.gross_pool = Double.parseDouble(parts[1].trim());
                    }
                    else if( section.equals("normal_irrigation"))
                    {
                        String _values[] = parts[1].trim().split("\\s+");
                        for( String v: _values)
                        {
                            irrigation.add( Double.parseDouble(v));
                        }
                    }
                    else if( section.equals("equation") ){
                        equation = new ExponentialEquation(parts[1].trim());
                    }
                    else if( section.equals("max_snowspace")){
                        this.max_snowspace = Double.parseDouble(parts[1]);
                    }
                    else if( section.equals("restricted_pool")){
                        this.restricted_pool = Double.parseDouble(parts[1]);
                
                    }
                }

                
                
                debug3( "load_graph: finished loading graph");
            }
            catch( java.io.IOException e )
            {
                debug3( "load_graph: Failed to read file, reason: " + e.getMessage() );
            }


            reader.close();

	}

        /**
         * Get the Gross Pool for the reservoir
         * @return the gross pool value for the loaded reservoir
         */
        public double gross_pool()
        {
            return this.gross_pool;
        }


        public double get_allowed_storage( int day, double fcp ) throws InterpolationException,ArrayIndexOutOfBoundsException
        {
            return get_allowed_storage( day, fcp, false);
        }

        /**
         * Calculate the Allowed storage based on the given values
         * @param day
         *  The day you are calculating Top of Conservation for
         *
         * @param fcp
         *  The Current Flood Control Paramenter
         * @param snow
         *  Are we calculating snow melt
         * @return
         *  The allowable storage in the reservoir for the day
         * @throws InterpolationException
         * @throws ArrayIndexOutOfBoundsException
         */
	public double get_allowed_storage( int day, double fcp, boolean snow ) throws InterpolationException,ArrayIndexOutOfBoundsException
	{
		//calculate the allowed storage based the the given water year julian day
		//and the give flood control parameter
		double fcp_lower=0.0, fcp_higher = 0.0;
		double lower, higher;
                double max_parameter = -1.0;
                double min_parameter = -1.0;
                TreeSet<Double> parameters;
                if( !snow )
                    parameters = new TreeSet<Double>( this.graph.keySet() );
                else
                    parameters = new TreeSet<Double>( this.snow_graph.keySet() );
                
                Double []fcp_values = parameters.toArray(new Double[parameters.size()]);
                //java.util.Collections.

		//step 1, bound the input fcp to the upper and lower lines provided in the diagram.
		if( min_parameter == -1 && max_parameter == -1 )
		{
			min_parameter = parameters.first();
			max_parameter = parameters.last();
			debug3( "get_allowed_storage: fcp will be bounded by min of " + min_parameter + " and max of " + max_parameter);
		}
                //bound input value
		debug3( "get_allowed_storage: Input FCP was " + fcp );
		fcp = Math.min( Math.max( min_parameter, fcp ), max_parameter );
		debug3( "FCP is now    " + fcp );

		if( parameters.contains(fcp))
                {
                    debug3( "get_allowed_storage: Asked for fcp value has a defined line, performing single interpolation");
                    return interpolate_graph_line( day, fcp, snow );
                }
                
                debug3( "get_allowed_storage: Asked for fcp value is between defined lines, performing double interpolation");

                for( int i = 0; i < fcp_values.length; i++ )
                {
                    if( fcp_values[i] > fcp )
                    {
                        fcp_lower = fcp_values[i-1];
                        fcp_higher = fcp_values[i];
                        break; // we've found the values we need
                    }
                }

                higher = this.interpolate_graph_line(day, fcp_higher,snow);
                lower = this.interpolate_graph_line(day, fcp_lower,snow);

                if( higher == lower)
                {
                    // this is a section where lines have merged, just return the interpolated value
                    // save a few cycles.
                    return higher;
                }
                else
                {
                    return (((higher-lower))/(fcp_higher-fcp_lower))*(fcp-fcp_lower)+lower;
                }
                     


                //debug3( "");
                //return Double.MIN_VALUE;

	}

        /**
         * Interpolate along a curve represented by one of the label lines on a water control diagram
         * @param day the water year julian day
         * @param fcp the current flood control parameter
         * @param snow are we calculating for snow melt
         * @return the allowed storage for the current day/fcp pair
         * @throws InterpolationException
         * @throws ArrayIndexOutOfBoundsException
         */
	public double interpolate_graph_line( int day, double fcp, boolean snow )
                throws InterpolationException, ArrayIndexOutOfBoundsException
	{
            HashMap<Double, ArrayList<Double> > mygraph =null;
            if( !snow )
            {
                mygraph = graph;
            }
            else
            {
                mygraph = snow_graph;
            }

            if ( !mygraph.containsKey( fcp ))
            {
                throw new InterpolationException( "interpolate graph line called with an fcp value that doesn't have a graph line" );
            }
            
            Double []curve = (Double[])mygraph.get(fcp).toArray(new Double[mygraph.get(fcp).size()]);
            double val = this.interpolate_curve(day, curve);
            debug3( "day: fcp line ->" + day + " : " + fcp );
            return val;
            /*
            // need to check this for leap year, will only be wrong for last day of WY in this case anyways
            if( day > 365 && day != 366 )
            {
                throw new java.lang.Exception( "day must be between 0 and 366" );
            }
            else if( day == 366 )
            {
                day = 365;
            }
                
            Double []curve = (Double[])this.graph.get(fcp).toArray(new Double[this.graph.get(fcp).size()]);
            
            Integer []_times = (Integer[])this.times.toArray( new Integer[this.times.size()]);
            if( curve.length != this.times.size() )
            {
                throw new java.lang.Exception( "Your list of times is not the same length of your list of values for the curve: " + fcp );
            }

            if( this.times.contains(day) )
            {
                debug3("A value exists for this day, returning that value");
                return curve[this.times.indexOf(day)];
            }

            debug3( "There does not exist a value for the provided day, interpolating data" );
            int index_day_before = 0;
            int index_day_after = 0;

            for( int i = 0; i < _times.length; i++)
            {
                if( _times[i] > day )
                {
                    index_day_before = i-1;
                    index_day_after = i;
                    break; // we have found the values we need
                }
            }
             
            double value = (
                             (curve[index_day_after] - curve[index_day_before] )
                             /
                             (_times[index_day_after] - _times[index_day_before] )
                           )
                           *
                           (day-_times[index_day_before])
                           +
                           curve[index_day_before];
                           ;
            debug3( " The value was: " + value );
            debug3( " The value was calculated using\n" +
                    "prevday:value ->" + _times[index_day_before] + " : " + curve[index_day_before] + "\n" +
                    "nextday:value ->"  + _times[index_day_after] + " : " + curve[index_day_after] + "\n" +
                    "day: fcp line ->" + day + " : " + fcp
                    );
            return value;
             *
             */
	}
        /**
         *  Bound the value of Conservation storage based on the extremums provided in the water control manual.
         *  If you are porting from HADA you should also check the DIFL file and confirm with the water manager
         *  for the reservoir
         * @param day water year julian day
         * @param value
         *  The value to check the bounds on
         * @return
         *  if the value is within the bounds of the graph, the value will be returned, otherwise the upper or lower bound value will be returned.
         * @throws InterplationException
         * @throws ArrayIndexOutOfBoundsException
         */
        public double bound( int day, double value ) throws InterpolationException, ArrayIndexOutOfBoundsException
        {
            // need to check this for leap year, will only be wrong for last day of WY in this case anyways
            if( day > 365 && day != 366 )
            {
                throw new ArrayIndexOutOfBoundsException( "day must be between 0 and 366" );
            }
            else if( day == 366 )
            {
                day = 365;
            }
            Double []lbound = (Double[])lower_bound.toArray(new Double[lower_bound.size()]);
            Double []ubound = (Double[])upper_bound.toArray(new Double[upper_bound.size()]);
            double lbval = this.interpolate_curve( day, lbound);
            double ubval = this.interpolate_curve( day, ubound);
            debug3( "Water Year Day is " + day );
            debug3( "lower bound value is " + lbval);
            debug3( "upper bound value is " + ubval);
            debug3( "value " + value + " will be contrained within those bounds");
            double tmp = value;
            if( value > ubval)
            {
                tmp = ubval;
            }
            else if( value < lbval)
            {
                tmp = lbval;
            }
            /*
             * Absolute final bounds check
             */
            if( this.restricted_pool != Double.NEGATIVE_INFINITY ){
                tmp = Math.min( tmp, this.restricted_pool );
            }
            return tmp;
            
        }
        
        /** get the upper bound value
         * @param day the water year julian day
         */
        public double get_upper_bound( int day ) throws InterpolationException, ArrayIndexOutOfBoundsException
        {
            // need to check this for leap year, will only be wrong for last day of WY in this case anyways
            if( day > 365 && day != 366 )
            {
                throw new ArrayIndexOutOfBoundsException( "day must be between 0 and 366" );
            }
            else if( day == 366 )
            {
                day = 365;
            }
            
            Double []ubound = (Double[])upper_bound.toArray(new Double[upper_bound.size()]);
            
            return this.interpolate_curve( day, ubound);
        }
        
        /** get the lower bound value
         * @param day the water year julian day
         */
        public double get_lower_bound( int day ) throws InterpolationException, ArrayIndexOutOfBoundsException
        {
            if( day > 365 && day != 366 )
            {
                throw new ArrayIndexOutOfBoundsException( "day must be between 0 and 366" );
            }
            else if( day == 366 )
            {
                day = 365;
            }
            Double []lbound = (Double[])lower_bound.toArray(new Double[lower_bound.size()]);
            return this.interpolate_curve( day, lbound);
        }
        
        

        /**
         * Interpolates along a single curve
         * @param day the water year julian day
         * @param curve array of doubles that contain the actual values, must match with times
         * @return the allowed storage for that particular curve on the given day
         * @throws InterpolationException
         * @throws ArrayIndexOutOfBoundsException
         */
        public double interpolate_curve( int day, Double []curve ) throws InterpolationException, ArrayIndexOutOfBoundsException
        {
            // need to check this for leap year, will only be wrong for last day of WY in this case anyways
            if( day > 365 && day != 366 )
            {
                throw new ArrayIndexOutOfBoundsException( "day must be between 0 and 366" );
            }
            else if( day == 366 )
            {
                day = 365;
            }

            Double []_times = (Double[])this.times.toArray( new Double[1]);
            if( curve.length != this.times.size() )
            {
                throw new InterpolationException( "Your list of times is not the same length of your list of values for the curve " + curve.length + " !=" + _times.length );
            }

            if( this.times.contains( (double)day ) )
            {
                debug3("A value exists for this day, returning that value");
                return curve[this.times.indexOf( (double)day) ];
            }

            debug3( "There does not exist a value for the provided day, interpolating data" );
            int index_day_before = 0;
            int index_day_after = 0;

            for( int i = 0; i < _times.length; i++)
            {
                if( _times[i] > day )
                {
                    index_day_before = i-1;
                    index_day_after = i;
                    break; // we have found the values we need
                }
            }

            double value = (
                             (curve[index_day_after] - curve[index_day_before] )
                             /
                             (_times[index_day_after] - _times[index_day_before] )
                           )
                           *
                           (day-_times[index_day_before])
                           +
                           curve[index_day_before];
                           ;
            debug3( " The value was: " + value );
            debug3( " The value was calculated using\n" +
                    "prevday:value ->" + _times[index_day_before] + " : " + curve[index_day_before] + "\n" +
                    "nextday:value ->"  + _times[index_day_after] + " : " + curve[index_day_after] + "\n"
                    /*"day: fcp line ->" + day + " : " + fcp*/
                    );
            return value;
        }

        /*
         * TODO: rebuild for new full water year array format
         */
        public double normal_irrigation( Date basetime)
        {
            GregorianCalendar cal = new GregorianCalendar();
            cal.setTime(basetime);
            int month = cal.get( Calendar.MONTH);
            int cur_day = cal.get(Calendar.DAY_OF_MONTH);
            double demand = 0.0;
            if( month < Calendar.FEBRUARY || month > Calendar.JULY )
                return 0.0; // we don't worry about irrigation at this time
                                   
            int day_remaining = cal.getActualMaximum(cal.DAY_OF_MONTH) - cur_day;
            demand = day_remaining*irrigation.get(month)*1.9835;
            // TODO: check that this is valid for everything
            for( int i=month+1; i <= Calendar.JUNE; i++ ) // go to 30 june                
            {
                cal.set( cal.MONTH, i);
                demand += irrigation.get( cal.get(Calendar.MONTH))*cal.getActualMaximum(Calendar.DAY_OF_MONTH)*1.9835;
            }

            return demand;
        }
        
        public static IrrigationDemands get_irrigation_data( String filename ) throws DbCompException, FileNotFoundException, IOException, ParseException{
            try {
                return new IrrigationDemands( filename );
            } catch (org.json.simple.parser.ParseException ex) {
                Logger.getLogger(WaterControlDiagram.class.getName()).log(Level.SEVERE, null, ex);
                throw new DbCompException( "Could not load irrigation demand data");
            }
        }
        
        public static TreeMap< Date, ArrayList< Double > > get_normal_irrigation_data( String filename ) throws DbCompException, FileNotFoundException, IOException, ParseException{
        
            TreeMap< Date, ArrayList< Double > > irr_data = new TreeMap< Date, ArrayList< Double > >();
            File f = new File(filename);
            if(!f.exists())
                throw new DbCompException("No iriigation demand file, this project requires one");
            BufferedReader reader = new BufferedReader( new FileReader(f) );
            String line=null;
            SimpleDateFormat df = new SimpleDateFormat("MM/dd/yyyy");
            while( (line = reader.readLine()) != null )
            {
                String parts[] = line.split(",");
                Date d = df.parse(parts[1]);
                ArrayList<Double> tmp = new ArrayList<Double>();
                for( int i=2; i <parts.length; i++)
                {
                    tmp.add( Double.parseDouble(parts[i])); // values are stored in thousands of ac-ft
                }
                irr_data.put(d, tmp);
            }
            reader.close();
            return irr_data;
        }

    


        public double get_allowed_storage_equation(int wy_day, double runoff) {
            return equation.calculate(wy_day)*runoff;
        }
        
        public double get_max_snowspace(){
            return this.max_snowspace;
        }
        
}

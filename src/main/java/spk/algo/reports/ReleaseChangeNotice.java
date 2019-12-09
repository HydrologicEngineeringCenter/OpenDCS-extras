package spk.algo.reports;



import java.util.Date;

import ilex.var.NamedVariableList;
import ilex.var.NamedVariable;
import decodes.tsdb.DbAlgorithmExecutive;
import decodes.tsdb.DbCompException;
import decodes.tsdb.DbIoException;
import decodes.tsdb.NoSuchObjectException;
import decodes.tsdb.ParmRef;
import decodes.tsdb.TimeSeriesIdentifier;
import decodes.tsdb.VarFlags;
import decodes.tsdb.algo.AWAlgoType;
import ilex.var.TimedVariable;
import java.io.StringReader;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.logging.Level;
import java.util.logging.Logger;



//AW:IMPORTS
// Place an import statements you need here.
import java.util.*;
import javax.mail.*;
import javax.mail.internet.*;
//import javax.mail.activation.*;
//AW:IMPORTS_END

//AW:JAVADOC
/**
 * Now that data has come into the database for release changes lets build an email
 * 
 */
//AW:JAVADOC_END
public class ReleaseChangeNotice
	extends spk.algo.support.AlgBaseNew
{
//AW:INPUTS
        // input values, declare a variable, and add the string of the variable name to the _inputNames array
        public double from;    //AW:TYPECODE=i
        public double to;    //AW:TYPECODE=i
        public double div1; //AW:TYPECODE=i
        public double div2; //AW:TYPECODE=i
        public double div3; //AW:TYPECODE=i
        public double div4; //AW:TYPECODE=i
        public double div5; //AW:TYPECODE=i
        public double div6; //AW:TYPECODE=i
        public double div7; //AW:TYPECODE=i
        
	String _inputNames[] = { "from", "to", "div1", "div2", "div3", "div4", "div5", "div6", "div7" };
//AW:INPUTS_END

//AW:LOCALVARS
	// Enter any local class variables needed by the algorithm.
        Session session;
        String project_name;
	Connection conn; // this function currently needs its own database connection
        SimpleDateFormat message_date_format;
        //PreparedStatement stmt; // the 
        CallableStatement dbcall; // statement that will actually execute the procedure
//AW:LOCALVARS_END

//AW:OUTPUTS
	public NamedVariable from_val = new NamedVariable("from_val", 0.0); // the output
        public NamedVariable to_val = new NamedVariable("to_val", 0.0); // the output
        public NamedVariable div1_val = new NamedVariable("div1_val", 0.0); // the output
        public NamedVariable div2_val = new NamedVariable("div2_val", 0.0); // the output
        public NamedVariable div3_val = new NamedVariable("div3_val", 0.0); // the output
        public NamedVariable div4_val = new NamedVariable("div4_val", 0.0); // the output
        public NamedVariable div5_val = new NamedVariable("div5_val", 0.0); // the output
        public NamedVariable div6_val = new NamedVariable("div6_val", 0.0); // the output
        public NamedVariable div7_val = new NamedVariable("div7_val", 0.0); // the output
	String _outputNames[] = { "from_val", "to_val", "div1_val", "div2_val", "div3_val", "div4_val", "div5_val", "div6_val", "div7_val"  };
//AW:OUTPUTS_END

//AW:PROPERTIES
        public String send_to_list = "list of emails";   
        public String from_address = "fill in";
        public String smtp_host = "localhost";
        public String smtp_port = "25";
        public String div1_MISSING = "ignore";
        public String div2_MISSING = "ignore";
        public String div3_MISSING = "ignore";
        public String div4_MISSING = "ignore";
        public String div5_MISSING = "ignore";
        public String div6_MISSING = "ignore";
        public String div7_MISSING = "ignore";    
	String _propertyNames[] = { "send_to_list", "from_address", "smtp_host", "smtp_port", "div1_MISSING", "div2_MISSING", "div3_MISSING", "div4_MISSING", "div5_MISSING", "div6_MISSING", "div7_MISSING" };
//AW:PROPERTIES_END
        /*
         * public String div1_name = "diversion1";
        public String div2_name = "diversion2";
        public String div3_name = "diversion3";
        public String div4_name = "diversion4";
        public String div5_name = "diversion5";
        public String div6_name = "diversion6";
        public String div7_name = "diversion7";        
        
         */
        
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
            
//		conn = tsdb.getConnection();                
//                try {
//                    dbcall = conn.prepareCall("{call Cwms_Text.Store_TS_Text( :1, :2, :3, Null, Null, Null, 'T', 'T', 'F', 'F', Null, Null )}");
//               } catch (SQLException ex) {
//                    Logger.getLogger(ReleaseChangeNotice.class.getName()).log(Level.SEVERE, null, ex);
//                }
            
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
            Properties properties = System.getProperties();
            properties.setProperty("mail.smtp.host", smtp_host);
            properties.setProperty("mail.smtp.port", smtp_port);
            message_date_format = new SimpleDateFormat( "MM/dd/yyyy HH:mm" );
            session = Session.getDefaultInstance( properties );
            
            String site_name = getSiteName( "from", "cwms" );
            project_name = site_name; //.split("\\s+")[0];
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
                 * we shouldn't need to check for close to zero. we aren't
                 * doing physcis calculations with small number and everything is generally
                 * out to two decimal places only anyways
                 */
            
                 // for initial testing we'll just handle the inflow and outflow
                 boolean send_message = false;
                 boolean needed_value_missing = false;
                 
                 
                 // we always have to have a FROM AND TO, the others are informational
                 if( !isMissing(from) )
                 {
                     double cur_from = this.getCurrentValue("from_val", _timeSliceBaseTime);
                     debug3( "Current from value is " + cur_from );                     
                     debug3( "New     from value is " + from );                     
                     if( ! fequals( cur_from, from, 0.0001 ) )
                     {
                         send_message = true;
                     }
                     setOutput(from_val, from );
                     
                 }
                 
                 if( ! isMissing(to) ){
                     double cur_to = this.getCurrentValue("to_val", _timeSliceBaseTime);
                     debug3( "Current to   value is " + cur_to);
                     debug3( "New     to   value is " + to );                         
                     if( ! fequals( cur_to, to, 0.0001 ) )
                     {
                         send_message = true;
                     }
                     setOutput(to_val, to );
                 }
                 
                 if ( isAssigned( "div1" ) && !isMissing( div1 )){
                     
                        
                    double cur_div1 = this.getCurrentValue("div1_val", _timeSliceBaseTime );
                    if( ! fequals( cur_div1, div1, 0.0001 ) ){
                        send_message = true;
                    }
                    setOutput(div1_val, div1);
                 }
                 
                 if ( isAssigned( "div2" ) && !isMissing( div2 )){
                     
                        
                    double cur_div2 = this.getCurrentValue("div2_val", _timeSliceBaseTime );
                    if( ! fequals( cur_div2, div2, 0.0001 ) ){
                        send_message = true;
                    }
                    setOutput(div2_val, div2);
                 }

                 if ( isAssigned( "div3" ) && !isMissing( div3 )){
                     
                        
                     double cur_div3 = this.getCurrentValue("div3_val", _timeSliceBaseTime );
                     if( ! fequals( cur_div3, div3, 0.0001 ) ){
                         send_message = true;
                     }
                     setOutput(div3_val, div3);
                 }
                 
                 
                 if ( isAssigned( "div4" ) && !isMissing( div4 )){
                     
                        
                    double cur_div4 = this.getCurrentValue("div4_val", _timeSliceBaseTime );
                    if( ! fequals( cur_div4, div4, 0.0001 ) ){
                        send_message = true;
                    }
                    setOutput(div4_val, div4);
                 }
                 
                 if ( isAssigned( "div5" ) && !isMissing( div5 )){
                    
                    double cur_div5 = this.getCurrentValue("div5_val", _timeSliceBaseTime );
                    if( ! fequals( cur_div5, div5, 0.0001 ) ){
                        send_message = true;
                    }
                    setOutput(div5_val, div5);
                 }
                 
                 if ( isAssigned( "div6" ) && !isMissing( div6 )){
                     
                        
                    double cur_div6 = this.getCurrentValue("div6_val", _timeSliceBaseTime );
                    if( ! fequals( cur_div6, div6, 0.0001 ) ){
                        send_message = true;
                    }
                     setOutput(div6_val, div6);
                 }
                 
                 if ( isAssigned( "div7" ) && !isMissing( div7 )){
                     
                        
                    double cur_div7 = this.getCurrentValue("div7_val", _timeSliceBaseTime );
                    if( ! fequals( cur_div7, div7, 0.0001 ) ){
                        send_message = true;
                    }
                     setOutput(div7_val, div7);
                 }
                 
                 if( send_message == true )
                 {
                     String email_from = from_address;
                     String host = smtp_host;
                     
                     
                     try {
                         MimeMessage message = new MimeMessage(session);
                         message.setFrom( new InternetAddress(email_from ) );
                         
                         message.addRecipients(Message.RecipientType.TO, send_to_list);
                         String link = "https://spk-wmlocal2.spk.usace.army.mil/auth_required/release/?project=" + project_name + "&amp;datetime=" + message_date_format.format(_timeSliceBaseTime);
                         message.setSubject("New Release from " + project_name);
                         
                         String msg = "New Release information is availabled from " +project_name + " + please click <a href=\"" + link + "\">Here</a> To check and approve the message";
                                 
                         message.setText(msg, "utf-8", "html");
                                 
                         
                         Transport.send(message);
                                                  
                         debug3( "Message sent for " + project_name + " for time slice " + _timeSliceBaseTime);
                         
                     } catch (AddressException ex) {
                         Logger.getLogger(ReleaseChangeNotice.class.getName()).log(Level.SEVERE, null, ex);
                     } catch (MessagingException ex) {
                         Logger.getLogger(ReleaseChangeNotice.class.getName()).log(Level.SEVERE, null, ex);
                     }
                 }
                 else{
                     debug3("no message sent");
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
/*                
            try {    
                debug3("**************");
                String role = message.getName();
                ParmRef parmRef = getParmRef(message.getName());
                TimeSeriesIdentifier tsi = tsdb.getTimeSeriesIdentifier(parmRef.timeSeries.getSDI());
                String tsid = tsi.getUniqueString();
                TimedVariable tv;
                //java.sql.Clob clob = conn.createClob();
                for( int i=0; i < parmRef.timeSeries.size(); i++ ){
                    dbcall.clearParameters();
                    tv = parmRef.timeSeries.sampleAt(i);
                    java.sql.Timestamp d = new java.sql.Timestamp( tv.getTime().getTime() );                    
                    String msg = tv.getStringValue();
                    debug3( d.toString() + " " + msg );
                    dbcall.setString(1, tsid);
                    dbcall.setClob(2, new StringReader(msg));
                    dbcall.setTimestamp(3,  d);                    
                    
                    dbcall.execute();
                    conn.commit();
                }
            } catch (DbIoException ex) {
                Logger.getLogger(ReleaseChangeNotice.class.getName()).log(Level.SEVERE, null, ex);
            } catch (NoSuchObjectException ex) {
                Logger.getLogger(ReleaseChangeNotice.class.getName()).log(Level.SEVERE, null, ex);
            } catch (SQLException ex) {
                Logger.getLogger(ReleaseChangeNotice.class.getName()).log(Level.SEVERE, null, ex);
            }
*/          
                
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
        
        
        public static boolean fequals( double f1, double f2, double epsilon)
        {
            return Math.abs(f2-f1  )< epsilon;
        }
}

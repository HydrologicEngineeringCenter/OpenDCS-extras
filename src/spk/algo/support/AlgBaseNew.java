
package spk.algo.support;
import java.lang.reflect.Field;
import java.util.Date;
import java.util.Iterator;
import java.util.TimeZone;
import java.util.TreeSet;

import decodes.hdb.HdbFlags;
import decodes.sql.DbKey;
import decodes.tsdb.BadTimeSeriesException;
import decodes.tsdb.DbAlgorithmExecutive;
import decodes.tsdb.DbCompException;
import decodes.tsdb.DbIoException;
import decodes.tsdb.IntervalCodes;
import decodes.tsdb.IntervalIncrement;
import decodes.tsdb.MissingAction;
import decodes.tsdb.ParmRef;
import decodes.tsdb.VarFlags;

import ilex.util.Logger;
import ilex.util.TextUtil;
import ilex.var.NamedVariable;
import ilex.var.NamedVariableList;
import ilex.var.NoConversionException;
import ilex.var.TimedVariable;

import decodes.tsdb.algo.AWAlgoType;
import decodes.tsdb.algo.AggregatePeriod;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;


/**
 * Slightly modified version of CCP Algorithm base
 * to avoid the DST issue (An issue with Inc Precip). Also includes additional helper functions
 * @author L2EDDMAN
 */
public abstract class AlgBaseNew extends DbAlgorithmExecutive
{
    protected NamedVariableList _timeSliceVars;
    protected Date _timeSliceBaseTime;
    private boolean _inTimeSlice = false;
    protected Date _aggregatePeriodBegin;
    protected Date _aggregatePeriodEnd;
    protected AWAlgoType _awAlgoType = AWAlgoType.TIME_SLICE;
    protected String _aggPeriodVarRoleName = null;
    public static final long MS_PER_HOUR = 3600000L;
    public static final long MS_PER_DAY = 86400000L;
    private boolean _saveOutputCalled;
    protected boolean _sliceInputsDeleted = false;
    protected boolean _aggInputsDeleted = false;
    private boolean _deleteOutputCalled = false;
    protected TreeSet baseTimes = null; // changed to protected so that algorithms can look at this if needed, a Test of SubSample required it.
    protected boolean noAggregateFill = false;
    protected String aggPeriodInterval = null;
    protected int debugLevel = 0;
    
    protected void initAlgorithm() throws DbCompException {
	_inTimeSlice = false;
	String t_string = comp.getProperty("debugLevel");
	if (t_string != null) {
	    try {
		debugLevel = Integer.parseInt(t_string.trim());
	    } catch (NumberFormatException ex) {
		debugLevel = 0;
		warning
		    ("Invalid 'debugLevel' property. May be 1, 2, or 3 only.");
	    }
	}
	t_string = comp.getProperty("noAggregateFill");
	if (t_string != null)
	    noAggregateFill = TextUtil.str2boolean(t_string);
	t_string = comp.getProperty("aggPeriodInterval");
	if (t_string != null)
	    aggPeriodInterval = t_string;
	t_string = comp.getProperty("aggregateTimeZone");
	if (t_string != null) {
	    TimeZone tz = TimeZone.getTimeZone(t_string);
	    if (tz == null)
		warning(new StringBuilder().append
			    ("Invalid aggregateTimeZone property '").append
			    (t_string).append
			    ("' -- ignored.").toString());
	    else {
		aggregateTimeZone = t_string;
		aggTZ = tz;
		aggCal.setTimeZone(aggTZ);
		debugSdf.setTimeZone(TimeZone.getTimeZone(aggregateTimeZone));
		debug3(new StringBuilder().append
			   ("Setting aggregate TimeZone to '").append
			   (aggregateTimeZone).append
			   ("'").append
			   (" current time=").append
			   (debugSdf.format(new Date())).toString());
	    }
	}
	Class cls = this.getClass();
	String[] arr$ = getPropertyNames();
	int len$ = arr$.length;
	for (int i$ = 0; i$ < len$; i$++) {
	    String propName = arr$[i$];
	    setCompProperty(cls, propName);
	}
	initAWAlgorithm();
	t_string = comp.getProperty("aggLowerBoundClosed");
	if (t_string != null)
	    aggLowerBoundClosed = TextUtil.str2boolean(t_string);
	else
	    aggLowerBoundClosed = _awAlgoType != AWAlgoType.RUNNING_AGGREGATE;
	t_string = comp.getProperty("aggUpperBoundClosed");
	if (t_string != null)
	    aggUpperBoundClosed = TextUtil.str2boolean(t_string);
	else
	    aggUpperBoundClosed = _awAlgoType == AWAlgoType.RUNNING_AGGREGATE;
	t_string = comp.getProperty("interpDeltas");
	if (t_string != null)
	    interpDeltas = TextUtil.str2boolean(t_string);
	t_string = comp.getProperty("maxInterpIntervals");
	if (t_string != null) {
	    try {
		maxInterpIntervals = Integer.parseInt(t_string);
	    } catch (Exception ex) {
		warning(new StringBuilder().append
			    ("Bad maxInterpIntervals property '").append
			    (maxInterpIntervals).append
			    ("' -- ignored.").toString());
	    }
	}
    }
    
    private Field getField(Class cls, String varName)
	throws NoSuchFieldException {
	Field field;
	try {
	    field = cls.getDeclaredField(varName);
	} catch (NoSuchFieldException ex) {
	    return cls.getField(varName);
	}
	return field;
    }
    
    private void setCompProperty(Class cls, String propName) {
	String propVal = comp.getProperty(propName);
	if (propVal == null)
	    debug1(new StringBuilder().append("Received property '").append
		       (propName).append
		       ("' with null value -- ignored.").toString());
	else {
	    if (propVal.equals("\"\""))
		propVal = "";
	    String ftyp = "unkown";
	    try {
		Field field = getField(cls, propName);
		ftyp = field.getType().getName();
		if (ftyp.equals("java.lang.String"))
		    field.set(this, propVal);
		else if (ftyp.equals("double")) {
		    if (propVal.equalsIgnoreCase("Double.MAX_VALUE"))
			field.setDouble(this, 1.7976931348623157E308);
		    else if (propVal.equalsIgnoreCase("Double.MIN_VALUE")
			     || (propVal.equalsIgnoreCase
				 ("Double.NEGATIVE_INIFINITY")))
			field.setDouble(this, Double.NEGATIVE_INFINITY);
		    else
			field.setDouble(this, Double.parseDouble(propVal));
		} else if (ftyp.equals("long")) {
		    if (propVal.equalsIgnoreCase("Long.MAX_VALUE"))
			field.setDouble(this, 9.223372036854776E18);
		    else if (propVal.equalsIgnoreCase("Long.MIN_VALUE"))
			field.setDouble(this, -9.223372036854776E18);
		    field.setLong(this, Long.parseLong(propVal));
		} else if (ftyp.equals("boolean"))
		    field.setBoolean(this, TextUtil.str2boolean(propVal));
		else
		    warning(new StringBuilder().append("Property '").append
				(propName).append
				("' has invalid local type -- ignored.")
				.toString());
	    } catch (NumberFormatException ex) {
		warning(new StringBuilder().append("Property '").append
			    (propName).append
			    ("' could not be parsed. ").append
			    ("Required type is ").append
			    (ftyp).toString());
	    } catch (Exception ex) {
		warning(new StringBuilder().append("Property '").append
			    (propName).append
			    ("' with no matching ").append
			    ("local variable -- ignored: ").append
			    (ex).toString());
	    }
	}
    }
    
    protected void applyAlgorithm() throws DbCompException, DbIoException {
	int defLogPriority = Logger.instance().getMinLogPriority();
	if (debugLevel != 0) {
	    switch (debugLevel) {
	    case 1:
		Logger.instance().setMinLogPriority(2);
		break;
	    case 2:
		Logger.instance().setMinLogPriority(1);
		break;
	    case 3:
		Logger.instance().setMinLogPriority(0);
		break;
	    }
	}
	try {
	    if (_awAlgoType == AWAlgoType.AGGREGATING
		|| _awAlgoType == AWAlgoType.RUNNING_AGGREGATE)
		doAggregatePeriods();
	    else {
		baseTimes = getAllInputData();
		if (baseTimes.size() > 0) {
		    beforeTimeSlices();
		    _inTimeSlice = true;
		    iterateTimeSlices(baseTimes);
		    _inTimeSlice = false;
		    afterTimeSlices();
		}
	    }
	} finally{
	    Logger.instance().setMinLogPriority(defLogPriority);
	   // throw object;
	}
	Logger.instance().setMinLogPriority(defLogPriority);
    }
    
    protected void doAggregatePeriods() throws DbCompException, DbIoException {
	String intervalS = aggPeriodInterval;
	if (intervalS == null) {
	    if (_aggPeriodVarRoleName == null) {
		warning
		    ("Cannot do aggregating algorithm without a controlling output variable.");
		return;
	    }
	    ParmRef parmRef = getParmRef(_aggPeriodVarRoleName);
	    if (parmRef == null) {
		warning(new StringBuilder().append
			    ("Unknown aggregate control output variable '")
			    .append
			    (_aggPeriodVarRoleName).append
			    ("'").toString());
		return;
	    }
	    intervalS = parmRef.compParm.getInterval();
	    if (aggPeriodInterval != null)
		intervalS = aggPeriodInterval;
	}
	TreeSet inputBaseTimes = determineInputBaseTimes();
	debug2(new StringBuilder().append("Aggregating period is '").append
		   (intervalS).append
		   ("', found ").append
		   (inputBaseTimes.size()).append
		   (" base times in input data.").toString());
	if (inputBaseTimes.size() != 0) {
	    if (_awAlgoType == AWAlgoType.RUNNING_AGGREGATE) {
		Date t = (Date) inputBaseTimes.last();
		if (t == null)
		    return;
		aggCal.setTime(t);
		IntervalIncrement calIncr
		    = IntervalCodes.getIntervalCalIncr(intervalS);
		aggCal.add(calIncr.getCalConstant(), calIncr.getCount());
		Date end = aggCal.getTime();
		String varName = getInputNames()[0];
		ParmRef inpParmRef = getParmRef(varName);
		calIncr = IntervalCodes.getIntervalCalIncr(inpParmRef
							       .compParm
							       .getInterval());
		aggCal.setTime(t);
		aggCal.add(calIncr.getCalConstant(), calIncr.getCount());
		while (aggCal.getTime().before(end)
		       || aggUpperBoundClosed && aggCal.equals(end)) {
		    t = aggCal.getTime();
		    inputBaseTimes.add(t);
		    aggCal.add(calIncr.getCalConstant(), calIncr.getCount());
		}
	    }
	    _aggregatePeriodBegin = null;
	    Date baseTime = null;
	    Iterator timesIterator = inputBaseTimes.iterator();
	    for (;;) {
		AggregatePeriod aggPer = null;
		if (baseTime != null && aggUpperBoundClosed
		    && aggLowerBoundClosed
		    && baseTime.equals(_aggregatePeriodEnd)) {
		    debug3
			(new StringBuilder().append
			     ("Special processing for double-closed boundaries. Just did period ending ")
			     .append
			     (debugSdf.format(baseTime)).toString());
                    debug3("    ****spk mod****");
		    if( !timesIterator.hasNext() )
			break;
		    baseTime = (Date) timesIterator.next();
		    long msec = baseTime.getTime();
		    baseTime.setTime(msec + 1000L);
		    aggPer = determineAggPeriod(baseTime, intervalS);
		    baseTime.setTime(msec);
		    debug3(new StringBuilder().append("New agg per: ").append
			       (debugSdf.format(aggPer.getBegin())).append
			       (" to ").append
			       (debugSdf.format(aggPer.getEnd())).toString());
		} else {
		    if (!timesIterator.hasNext())
			break;
		    baseTime = (Date) timesIterator.next();
		    aggPer = determineAggPeriod(baseTime, intervalS);
		}
		if (_aggregatePeriodBegin == null
		    || !aggPer.getBegin().equals(_aggregatePeriodBegin)) {
		    _aggregatePeriodBegin = aggPer.getBegin();
		    _aggregatePeriodEnd = aggPer.getEnd();
		    debug2(new StringBuilder().append
			       ("Doing aggregate period (").append
			       (debugSdf.format(_aggregatePeriodBegin)).append
			       (", ").append
			       (debugSdf.format(_aggregatePeriodEnd)).append
			       (")").toString());
		    if (!noAggregateFill)
			baseTimes = getAllInputData(_aggregatePeriodBegin,
						    _aggregatePeriodEnd);
		    beforeTimeSlices();
		    _inTimeSlice = true;
		    _aggInputsDeleted = false;
		    if (!noAggregateFill)
			iterateTimeSlices(baseTimes);
		    _inTimeSlice = false;
		    _saveOutputCalled = false;
		    _deleteOutputCalled = false;
		    afterTimeSlices();
		    debug2(new StringBuilder().append
			       ("Finished aggregate period that started at ")
			       .append
			       (debugSdf.format(_aggregatePeriodBegin))
			       .toString());
		    if (_awAlgoType == AWAlgoType.AGGREGATING
			&& !_saveOutputCalled && !_deleteOutputCalled
			&& getOutputNames().length == 1 && _aggInputsDeleted) {
			debug2("Auto-deleting output.");
			deleteAllOutputs();
		    }
		}
	    }
	}
    }
    
    private AggregatePeriod determineAggPeriod(Date baseTime,
					       String interval) {
	long lower = baseTime.getTime();
	long orig = baseTime.getTime();
	long upper = lower;
	IntervalIncrement calIncr = IntervalCodes.getIntervalCalIncr(interval);
	if (calIncr == null)
	    warning(new StringBuilder().append
			("Aggregate control output variable '").append
			(_aggPeriodVarRoleName).append
			("' is instantaneous ").append
			("-- cannot determine aggregate period.").toString());
	else if (interval.equalsIgnoreCase("wy")) {
	    aggCal.setTime(baseTime);
	    aggCal.set(11, 0);
	    aggCal.set(12, 0);
	    aggCal.set(13, 0);
	    aggCal.set(5, 1);
	    int month = aggCal.get(2);
	    aggCal.set(2, 9);
	    if (month < 9)
		aggCal.add(1, -1);
	    lower = aggCal.getTimeInMillis();
	    if (aggUpperBoundClosed && lower == orig) {
		aggCal.add(1, -1);
		lower = aggCal.getTimeInMillis();
	    }
	    aggCal.add(1, 1);
	    upper = aggCal.getTimeInMillis();
	} else if (aggPeriodInterval != null) {
	    aggCal.setTime(baseTime);
	    upper = baseTime.getTime();
	    aggCal.add(calIncr.getCalConstant(), -calIncr.getCount());
	    lower = aggCal.getTimeInMillis();
	} else {
	    aggCal.setTime(baseTime);
	    int[] cis = { 13, 12, 11, 5, 2 };
	    if (calIncr.getCalConstant() == 3)
		cis = new int[] { 13, 12, 11 };
	    for (int x = 0;
		 x < cis.length && cis[x] != calIncr.getCalConstant(); x++) {
		int n = cis[x] == 5 ? 1 : 0;
		aggCal.set(cis[x], n);
	    }
	    int x = (aggCal.get(calIncr.getCalConstant()) / calIncr.getCount()
		     * calIncr.getCount());
	    aggCal.set(calIncr.getCalConstant(), x);
	    lower = aggCal.getTimeInMillis();
	    Date dlower = new Date(lower);
	    boolean lowerInDaylight
		= aggCal.getTimeZone().inDaylightTime(dlower);
	    debug3(new StringBuilder().append("lower=").append
		       (debugSdf.format(dlower)).append
		       (", lowerInDaylight = ").append
		       (lowerInDaylight).toString());
	    if (aggUpperBoundClosed && lower == orig) {
		aggCal.add(calIncr.getCalConstant(), -calIncr.getCount());
		lower = aggCal.getTimeInMillis();
	    }
	    aggCal.add(calIncr.getCalConstant(), calIncr.getCount());
	    upper = aggCal.getTimeInMillis();
	    Date dupper = new Date(upper);
	    boolean upperInDaylight
		= aggCal.getTimeZone().inDaylightTime(dupper);
	    if (lowerInDaylight && !upperInDaylight) {
		aggCal.add(11, 1);
		upper = aggCal.getTimeInMillis();
		debug2("Added 1 hour to upper because of daylight change.");
	    } else if (!lowerInDaylight && upperInDaylight) {
		aggCal.add(11, -1);
		upper = aggCal.getTimeInMillis();
		debug2
		    ("Subtracted 1 hour to upper because of daylight change.");
	    }
	    debug3(new StringBuilder().append("upper=").append
		       (debugSdf.format(dupper)).append
		       (", upperInDaylight = ").append
		       (upperInDaylight).toString());
	}
	AggregatePeriod ret
	    = new AggregatePeriod(new Date(lower), new Date(upper));
	return ret;
    }
    
    protected void doTimeSlice(NamedVariableList timeSlice, Date baseTime)
	throws DbCompException {
	_timeSliceVars = timeSlice;
	_timeSliceBaseTime = baseTime;
	boolean _missing_found = false;
	getSliceInputs();
	String[] arr$ = getInputNames();
	int len$ = arr$.length;
	for (int i$ = 0; i$ < len$; i$++) {
	    String varName = arr$[i$];
	    ParmRef parmRef = getParmRef(varName);
	    if (parmRef != null
		&& parmRef.missingAction == MissingAction.FAIL) {
		NamedVariable v = _timeSliceVars.findByName(varName);
		if (v == null) {
		    _missing_found = true;
		    break;
		}
		if (VarFlags.wasDeleted(v)) {
		    _missing_found = true;
		    deleteAllOutputs();
		    break;
		}
	    }
	}
        
	if (!_missing_found) {
	    if (_sliceInputsDeleted)
		_aggInputsDeleted = true;
	    _saveOutputCalled = false;
	    _deleteOutputCalled = false;
	    doAWTimeSlice();
	    if (_awAlgoType == AWAlgoType.TIME_SLICE && !_saveOutputCalled
		&& getOutputNames().length == 1 && _sliceInputsDeleted
		&& !_deleteOutputCalled)
		deleteAllOutputs();
	}
    }
    
    public void setOutput(NamedVariable v, double d) {
	if (d == Double.NEGATIVE_INFINITY || d == 4.9E-324)
	    deleteOutput(v);
	else {
	    v.setValue(d);
	    saveOutput(v);
	}
    }
    
    protected void setOutput(NamedVariable v, double d, Date t) {
	if (d == Double.NEGATIVE_INFINITY || d == 4.9E-324)
	    deleteOutput(v, t);
	else {
	    v.setValue(d);
	    saveOutput(v, new Date(t.getTime()));
	}
    }
    
    protected void setOutput(NamedVariable v, long li) {
	if (li == -9223372036854775808L)
	    deleteOutput(v);
	else {
	    v.setValue(li);
	    saveOutput(v);
	}
    }
    
    protected void setOutput(NamedVariable v, String s) {
	if (s == null)
	    deleteOutput(v);
	else {
	    v.setValue(s);
	    saveOutput(v);
	}
    }
    
    public void setFlagBits(NamedVariable v, int bits) {
	v.setFlags(v.getFlags() | bits);
	saveOutput(v);
    }
    
    public void clearFlagBits(NamedVariable v, int bits) {
	v.setFlags(v.getFlags() & (bits ^ 0xffffffff));
	saveOutput(v);
    }
    
    protected int getInputFlagBits(String name) {
	if (!_inTimeSlice) {
	    warning(new StringBuilder().append("Cannot get '").append(name)
			.append
			("' flag bits outside a time-slice.").toString());
	    return 0;
	}
	NamedVariable v = _timeSliceVars.findByName(name);
	if (v == null) {
	    warning(new StringBuilder().append("Cannot get '").append(name)
			.append
			("' flag bits -- no variable with that name.")
			.toString());
	    return 0;
	}
	return v.getFlags();
    }
    
    protected void setInputFlagBits(String name, int bits) {
	if (!_inTimeSlice)
	    warning(new StringBuilder().append("Cannot set '").append(name)
			.append
			("' flag bits outside a time-slice.").toString());
	else {
	    NamedVariable v = _timeSliceVars.findByName(name);
	    if (v == null)
		warning(new StringBuilder().append("Cannot set '").append
			    (name).append
			    ("' flag bits -- no variable with that name.")
			    .toString());
	    else {
		_saveOutputCalled = true;
		int oldFlags = v.getFlags();
		if ((oldFlags | bits) != oldFlags)
		    v.setFlags(v.getFlags() | bits | 0x4);
	    }
	}
    }
    
    protected void clearInputFlagBits(String name, int bits) {
	if (!_inTimeSlice)
	    warning(new StringBuilder().append("Cannot clear '").append
			(name).append
			("' flag bits outside a time-slice.").toString());
	else {
	    NamedVariable v = _timeSliceVars.findByName(name);
	    if (v == null)
		warning(new StringBuilder().append("Cannot clear '").append
			    (name).append
			    ("' flag bits -- no variable with that name.")
			    .toString());
	    else {
		_saveOutputCalled = true;
		int oldFlags = v.getFlags();
		if ((oldFlags & (bits ^ 0xffffffff)) != oldFlags)
		    v.setFlags(v.getFlags() & (bits ^ 0xffffffff) | 0x4);
	    }
	}
    }
    
    private void saveOutput(NamedVariable v) {
	VarFlags.setToWrite(v);
	_saveOutputCalled = true;
	v.setSourceId(comp.getDataSourceId());
	if (_inTimeSlice)
	    _timeSliceVars.add(v);
	else if (_aggregatePeriodBegin == null)
	    warning(new StringBuilder().append("Cannot save '").append
			(v.toString()).append
			("' Not an aggregating algorithm.").toString());
	else {
	    ParmRef parmRef = getParmRef(v.getName());
	    if (parmRef == null)
		warning(new StringBuilder().append("Cannot save '").append
			    (v.toString()).append
			    ("' no output parameter role defined!")
			    .toString());
	    else {
		Date varDate = (_awAlgoType == AWAlgoType.AGGREGATING
				? _aggregatePeriodBegin : _aggregatePeriodEnd);
		Date aggD
		    = parmRef.compParm.baseTimeToParamTime(varDate, aggCal);
		debug1(new StringBuilder().append
			   ("Storing aggregate value=").append
			   (v.getStringValue()).append
			   (" basetime=").append
			   (debugSdf.format(varDate)).append
			   (", parmtime=").append
			   (debugSdf.format(aggD)).append
			   (" parm deltaT=").append
			   (parmRef.compParm.getDeltaT()).append
			   (" (").append
			   (parmRef.compParm.getDeltaTUnits()).append
			   (")").toString());
		TimedVariable tv = new TimedVariable(v, aggD);
		TimedVariable oldTv
		    = parmRef.timeSeries.findWithin(varDate, 10);
		try {
		    if (oldTv != null) {
			double diff
			    = v.getDoubleValue() - oldTv.getDoubleValue();
			if (diff >= -1.0E-7 && diff <= 1.0E-7) {
			    int f = oldTv.getFlags() | 0x4;
			    tv.setFlags(f);
			}
		    }
		} catch (NoConversionException ex) {
		    warning(new StringBuilder().append
				("Error comparing existing aggregate output '")
				.append
				(oldTv).append
				(": ").append
				(ex).toString());
		}
		parmRef.timeSeries.addSample(tv);
	    }
	}
    }
    
    private void saveOutput(NamedVariable v, Date t) {
	_saveOutputCalled = true;
	ParmRef parmRef = getParmRef(v.getName());
	if (parmRef == null)
	    warning(new StringBuilder().append("Cannot save '").append
			(v.toString()).append
			("' no output parameter role defined!").toString());
	else {
	    TimedVariable tv = new TimedVariable(v, t);
	    tv.setSourceId(comp.getDataSourceId());
	    VarFlags.setToWrite(tv);
	    parmRef.timeSeries.addSample(tv);
	}
    }
    
    protected void deleteOutput(NamedVariable v) {
	VarFlags.setToDelete(v);
	_deleteOutputCalled = true;
	if (_inTimeSlice)
	    _timeSliceVars.add(v);
	else if (_aggregatePeriodBegin == null)
	    warning(new StringBuilder().append("Cannot delete '").append
			(v.toString()).append
			("' Not an aggregating algorithm.").toString());
	else {
	    ParmRef parmRef = getParmRef(v.getName());
	    if (parmRef == null)
		warning(new StringBuilder().append("Cannot delete '").append
			    (v.toString()).append
			    ("' no output parameter role defined!")
			    .toString());
	    else {
		Date paramTime
		    = parmRef.compParm
			  .baseTimeToParamTime(_aggregatePeriodBegin, aggCal);
		TimedVariable tv = new TimedVariable(v, paramTime);
		VarFlags.setToDelete(tv);
		parmRef.timeSeries.addSample(tv);
	    }
	}
    }
    
    protected void deleteOutput(NamedVariable v, Date t) {
	VarFlags.setToDelete(v);
	_deleteOutputCalled = true;
	ParmRef parmRef = getParmRef(v.getName());
	if (parmRef == null)
	    warning(new StringBuilder().append("Cannot delete '").append
			(v.toString()).append
			("' no output parameter role defined!").toString());
	else {
	    TimedVariable tv = new TimedVariable(v, t);
	    VarFlags.setToDelete(tv);
	    parmRef.timeSeries.addSample(tv);
	}
    }
    
    protected void deleteAllOutputs() {
	Class cls = this.getClass();
	String[] arr$ = getOutputNames();
	int len$ = arr$.length;
	for (int i$ = 0; i$ < len$; i$++) {
	    String role = arr$[i$];
	    try {
		Field field = getField(cls, role);
		String ftyp = field.getType().getName();
		if (ftyp.equals("ilex.var.NamedVariable")) {
		    NamedVariable nv = (NamedVariable) field.get(this);
		    deleteOutput(nv);
		}
	    } catch (Exception ex) {
		warning(new StringBuilder().append
			    ("Error in deleteAllOutputs: ").append
			    (ex).toString());
	    }
	}
    }
    
    protected void getSliceInputs() {
	String[] arr$ = getInputNames();
	int len$ = arr$.length;
	for (int i$ = 0; i$ < len$; i$++) {
	    String varName = arr$[i$];
	    ParmRef parmRef = getParmRef(varName);
	    if (parmRef != null && parmRef.compParm != null) {
		_sliceInputsDeleted = false;
		Class cls = null;
		try {
		    cls = this.getClass();
		    Field field = getField(cls, varName);
		    String nm = varName;
		    String typ
			= parmRef.compParm.getAlgoParmType().toLowerCase();
		    if (typ.length() > 1 && typ.charAt(1) == 'd')
			nm = new StringBuilder().append(nm).append("_d")
				 .toString();
		    NamedVariable v = _timeSliceVars.findByNameIgnoreCase(nm);
		    String ftyp = field.getType().getName();
		    if (ftyp.equals("double")) {
			if (v == null)
			    field.setDouble(this, Double.NEGATIVE_INFINITY);
			else if (VarFlags.wasDeleted(v)) {
			    field.setDouble(this, Double.NEGATIVE_INFINITY);
			    _sliceInputsDeleted = true;
			} else
			    field.setDouble(this, v.getDoubleValue());
		    } else if (ftyp.equals("long")) {
			if (v == null)
			    field.setLong(this, -9223372036854775808L);
			else if (VarFlags.wasDeleted(v)) {
			    field.setLong(this, -9223372036854775808L);
			    _sliceInputsDeleted = true;
			} else
			    field.setLong(this, v.getLongValue());
		    } else if (ftyp.equals("java.lang.String")) {
			if (v == null)
			    field.set(this, null);
			else if (VarFlags.wasDeleted(v)) {
			    field.set(this, null);
			    _sliceInputsDeleted = true;
			} else
			    field.set(this, v.getStringValue());
		    } else
			warning(new StringBuilder().append
				    ("Invalid input variable type '").append
				    (ftyp).append
				    ("' -- ignored.").toString());
		} catch (IllegalAccessException ex) {
		    String msg
			= new StringBuilder().append
			      ("Inconsistent class -- cannot access input field named '")
			      .append
			      (varName).append
			      ("'").toString();
		    System.err.println(msg);
		    ex.printStackTrace(System.err);
		    warning(msg);
		} catch (NoSuchFieldException ex) {
		    warning
			(new StringBuilder().append
			     ("Inconsistent class -- no input field named '")
			     .append
			     (varName).append
			     ("'").toString());
		} catch (NoConversionException ex) {
		    warning(new StringBuilder().append
				("Cannot convert input '").append
				(varName).append
				("' to correct input type.").toString());
		}
	    }
	}
    }
    
    protected boolean isMissing(double var) {
	return var == 4.9E-324 || var == Double.NEGATIVE_INFINITY;
    }
    
    protected boolean isMissing(long var) {
	return var == -9223372036854775808L;
    }
    
    protected boolean isMissing(String var) {
	return var == null;
    }
    
    protected abstract void initAWAlgorithm() throws DbCompException;
    
    protected abstract void beforeTimeSlices() throws DbCompException;
    
    protected abstract void doAWTimeSlice() throws DbCompException;
    
    protected abstract void afterTimeSlices() throws DbCompException;
    
    public abstract String[] getPropertyNames();
    
    protected double getCoeff(String rolename) throws DbCompException {
	DbKey sdi = getSDI(rolename);
        
	String interval = getInterval(rolename);
	String ts = getTableSelector(rolename);
	double coeff;
	try {
	    if (_inTimeSlice) {
		if (_timeSliceBaseTime == null)
		    throw new DbCompException
			      (new StringBuilder().append
				   ("Cannot find Coeff for null time, role=")
				   .append
				   (rolename).toString());
		coeff = tsdb.getCoeff(sdi, ts, interval, _timeSliceBaseTime);
	    } else {
		if (_aggregatePeriodBegin == null)
		    throw new DbCompException
			      (new StringBuilder().append
				   ("Cannot find Coeff for null time., role")
				   .append
				   (rolename).toString());
		coeff
		    = tsdb.getCoeff(sdi, ts, interval, _aggregatePeriodBegin);
	    }
	} catch (DbIoException ex) {
	    throw new DbCompException(new StringBuilder().append
					  ("Cannot find Coeff for role ")
					  .append
					  (rolename).append
					  (":").append
					  (ex).toString());
	}
	return coeff;
    }
    
    protected char getInputHdbValidationFlag(String name) {
	return HdbFlags.flag2HdbValidation(getInputFlagBits(name));
    }
    
    protected String getInputHdbDerivationFlag(String name) {
	return HdbFlags.flag2HdbDerivation(getInputFlagBits(name));
    }
    
    protected void setHdbValidationFlag(NamedVariable v,
					char hdbValidationFlag) {
	int f = v.getFlags();
	f &= ~0xf0;
	f |= HdbFlags.hdbValidation2flag(hdbValidationFlag);
	v.setFlags(f);
	saveOutput(v);
    }
    
    protected void setInputHdbValidationFlag(String name,
					     char hdbValidationFlag) {
	clearInputFlagBits(name, 240);
	setInputFlagBits(name, HdbFlags.hdbValidation2flag(hdbValidationFlag));
    }
    
    protected void setHdbDerivationFlag(NamedVariable v,
					String hdbDerivationFlags) {
	int f = v.getFlags();
	f &= ~0xff00;
	f |= HdbFlags.hdbDerivation2flag(hdbDerivationFlags);
	v.setFlags(f);
	saveOutput(v);
    }
    
    protected void setInputHdbDerivationFlag(String name,
					     String hdbDerivationFlags) {
	clearInputFlagBits(name, 65280);
	setInputFlagBits(name,
			 HdbFlags.hdbDerivation2flag(hdbDerivationFlags));
    }

    /**
     * Retrieve the previous value in time for the given base time and time series
     * @param parmRefName The Time Series for which to get data
     * @param tsbasetime The time one period after the one we want
     * @return The value one period previous to the provided base time
     */
    public double getPrevValue( String parmRefName, Date tsbasetime)
    {
        double value = Double.NEGATIVE_INFINITY; // initialize to missing
        double val2 = Double.NEGATIVE_INFINITY;
        Date previous_fcp_date = tsbasetime;
        GregorianCalendar cal = new GregorianCalendar();
        cal.setTime(previous_fcp_date);        
        debug3( "Getting output parameter reference");        
        ParmRef pr = getParmRef(parmRefName);
        String interval = pr.timeSeries.getInterval();
        int seconds_back = -1*(decodes.tsdb.IntervalCodes.getIntervalSeconds(interval));
        debug3( "interval = " + seconds_back + " seconds");
        cal.add( Calendar.SECOND, seconds_back); // get the previous day
        previous_fcp_date = cal.getTime();
        debug3("intial date" + tsbasetime);
        debug3("calculated date" + previous_fcp_date);
        decodes.tsdb.CTimeSeries ts = new decodes.tsdb.CTimeSeries(pr.compParm);

        try{
                ts.setUnitsAbbr(pr.timeSeries.getUnitsAbbr());
                debug3("Querying database");
                    // tsdb is the generic interface to the database backend of the CCP
                    // having the start date and end date the time gets us one value


                    if( tsdb.fillTimeSeries( ts, previous_fcp_date, tsbasetime) == 0 )
                    {
                        warning( "Could not access output timeseries, assuming it doesn't exist and we are starting at 0");                        
                    }
                    
                    // this gets us an individual value from within the retreived time series
                    TimedVariable tv = tsdb.getPreviousValue(ts, tsbasetime);
                    //TimedVariable tv = ts.findWithin(previous_fcp_date, roundSec);
                    
                    
                    try{
                        if( tv != null )
                        {
                            value = tv.getDoubleValue();                            
                        }
                        else
                        {
                            warning( "no existing data, starting at 0.0" );                            
                        }
                    }
                    catch( NoConversionException e)
                    {
                        warning( "could not convert data from provided  time series");
                    
                    }
                }
                catch( DbIoException e)
                {
                    warning( "Could not access timeseries, returning missing val");
                    
                }
                catch( BadTimeSeriesException e )
                {
                    warning( "Could not access timeseries, returning missing val");
                    
                }


                
                return value;
    }
    /**
     * Get all of the values previous to this one up to a certain base time
     * @param parmRefName the value for which we want the data
     *
     * @param start the current time ( we have this value )
     * @param end the time from which ( inclusive ) to get data
     * @return array of the previous values. this version doesn't care about the dates
     */
    public double[] getPrevValues( String parmRefName, Date start, Date end)
    {
        double[] data = { 0 };
        double value = Double.MIN_VALUE;
        
        debug3( "Getting output parameter reference");        
        ParmRef pr = getParmRef(parmRefName);
        ArrayList<Date> dates = new ArrayList<Date>();
        // ParmRef time series aren't changeable. so we have to create a fresh time series to fill
        decodes.tsdb.CTimeSeries ts = new decodes.tsdb.CTimeSeries(pr.compParm);
        try{
                ts.setUnitsAbbr(pr.timeSeries.getUnitsAbbr());
                debug3("Querying database");
                dates.add(start);
                dates.add(end);
                    // tsdb is the generic interface to the database backend of the CCP                    
                    if( tsdb.fillTimeSeries( ts, start,end) == 0 )
                    {
                        warning( "Could not access timeseries, assuming it doesn't exist and we are starting at 0 (returning empty array)");                        
                        return data;
                    }
                    debug3( "database returned " + ts.size() + " entries");
                    
                    data = new double[ts.size()];
                    for( int i =0; i < ts.size(); i++ )
                    {
                    //TimedVariable tv = pr.timeSeries.findWithin(previous_fcp_date, roundSec);
                        TimedVariable tv = ts.sampleAt(i);

                        

                        try{
                            if( tv != null )
                            {
                                debug3( " Date" + tv.timeString() + "  value " + tv.getDoubleValue());
                                data[i] = tv.getDoubleValue();
                                
                            }
                            else
                            {
                                data[i] = 0.0f;
                                warning( "A missing value was set to 0");
                            }
        
                        }
                        catch( NoConversionException e)
                        {
                            warning( "could not convert data from provided  time series");

                        }
                    }
                }
                catch( DbIoException e)
                {
                    warning( "Could not access timeseries, returning empty array");

                }
                catch( BadTimeSeriesException e )
                {
                    warning( "Could not access timeseries, returning empty array");

                }



                return data;
    }
    /**
     * Compares two dates to see if they are equal. A difference of 3 seconds is allowed
     * @param d1
     * @param d2
     * @return true if the dates are within 3 seconds, false otherwise
     */
    public boolean date_equal( Date d1, Date d2 )
    {

        long delta = d1.getTime() - d2.getTime();
        //debug3( "Delta = " + delta);
        return ( Math.abs( delta) < 3000);
    }

    /**
     * Retrieve the current value (the provided tsbasetime ) of a given time series
     * @param parmRefName The Time Series for which to get data
     * @param tsbasetime The time period  we want
     * @return The value one period previous to the provided base time
     */
    public double getCurrentValue( String parmRefName, Date tsbasetime)
    {
        double value = Double.NEGATIVE_INFINITY; // initialize to missing
        double val2 = Double.NEGATIVE_INFINITY;
        
        debug3( "Getting output parameter reference");        
        ParmRef pr = getParmRef(parmRefName);
                       
        
        decodes.tsdb.CTimeSeries ts = new decodes.tsdb.CTimeSeries(pr.compParm);

        try{
                ts.setUnitsAbbr(pr.timeSeries.getUnitsAbbr());
                debug3("Querying database");
                    // tsdb is the generic interface to the database backend of the CCP
                    // having the start date and end date the time gets us one value


                    if( tsdb.fillTimeSeries( ts, tsbasetime, tsbasetime) == 0 )
                    {
                        warning( "Could not access output timeseries, assuming it doesn't exist and we are starting at 0");                        
                    }
                    
                    // this gets us an individual value from within the retreived time series
                    //TimedVariable tv = tsdb.getPreviousValue(ts, tsbasetime);
                    
                    TimedVariable tv = ts.findWithin(tsbasetime, roundSec);
                    
                    
                    try{
                        if( tv != null )
                        {
                            value = tv.getDoubleValue();                            
                        }
                        else
                        {
                            warning( "no existing data, starting at 0.0" );                            
                        }
                    }
                    catch( NoConversionException e)
                    {
                        warning( "could not convert data from provided  time series");
                    
                    }
                }
                catch( DbIoException e)
                {
                    warning( "Could not access timeseries, returning missing val");
                    
                }
                catch( BadTimeSeriesException e )
                {
                    warning( "Could not access timeseries, returning missing val");
                    
                }


                
                return value;
    }
    
    /**
     * Is the variable assigned
     * @param parmRefName The time series we're checking     
     * @return true if the time series is configured
     */
    public boolean isAssigned( String parmRefName ){
        ParmRef pr = getParmRef(parmRefName);
        if( pr == null ){
            return false;
        }
        else{
            return true;
        }                        
    }
    
    public Date getStartDate(){
        return (Date)this.baseTimes.first();
    }
    
    public Date getEndDate(){
        return (Date)this.baseTimes.last();
    }
    
    
    
}


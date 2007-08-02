/* ============================================================== */
/*                                                              */
/* UK Astronomy Technology Centre */
/* Royal Observatory, Edinburgh */
/* Joint Astronomy Centre, Hilo */
/* Copyright (c) PPARC 2001 */
/*                                                              */
/* ============================================================== */

package gemini.sp;

import gemini.sp.obsComp.SpInstObsComp;
import gemini.sp.obsComp.SpSurveyObsComp;
import java.util.Enumeration;

/**
 * OMP class.
 * 
 * @author Martin Folger (M.Folger@roe.ac.uk)
 */
public class SpMSB extends SpObsContextItem
{

	/** This attribute records the observation priority. */
	public static final String ATTR_PRIORITY = "priority";
	public static final String ATTR_LIBRARY_VERSION = "library_version";
	public static final String KEYWORD_IDENTIFIER = "$";
	public static final String LIBRARY_VERSION = "Revision";

	/**
     * High observation priority, relative to the other observations in the
     * science program.
     */
	public static final int PRIORITY_HIGH = 1;

	/**
     * Medium observation priority, relative to the other observations in the
     * science program.
     */
	public static final int PRIORITY_MEDIUM = 49;

	/**
     * Low observation priority, relative to the other observations in the
     * science program.
     */
	public static final int PRIORITY_LOW = 99;
	public static String[] PRIORITIES = { "High" , "Medium" , "Low" };
	private static final int numPriorities = 99;

	/** This attribute records the number of remaining MSBs. */
	public static final String ATTR_REMAINING = ":remaining";
	public static final String ATTR_CHECKSUM = ":checksum";

	/** This attribute records the estimated duration of the MSB. */
	public static final String ATTR_ELAPSED_TIME = "estimatedDuration";

	/** This attribute records the estimated total duration of the MSB. */
	public static final String ATTR_TOTAL_TIME = "totalDuration";

	/**
     * String used to indecate that the MSB has been removed.
     * 
     * @see #REMOVED_CODE
     */
	public static final String REMOVED_STRING = "REMOVED";
	public static final String REMOVE_STRING = "(UN)REMOVE";

	/** This attribute records whether an MSB has been suspended */
	public static final String ATTR_SUSPEND = ":suspend";

	/**
     * The databases uses this number to indecate that an MSB has been removed.
     * 
     * Usage: {@link #setNumberRemaining(int) (REMOVED_CODE)}
     * 
     * @deprecated Removed MSB's and now indicated using -remaing
     */
	public static final int REMOVED_CODE = -999;

	/**
     * Default constructor.
     */
	protected SpMSB()
	{
		super( SpType.MSB_FOLDER );
		_avTable.noNotifySet( ATTR_REMAINING , "1" , 0 );
		_avTable.noNotifySet( ATTR_PRIORITY , "99" , 0 );
	}

	/**
     * Constructor with the specific SpType.
     * 
     * This constructor can be called from subclasses. Note that it deliberately
     * does not initialise _avTable values. This should be done in the
     * constructor of the subclass. This is usefull because the
     * {@link gemini.sp.SpObs} constructor initialises its _avTable values
     * dependend on whether the OT runs in OMP mode or not. The {@link #SpMSB}
     * constructor however always assumes OMP mode.
     */
	protected SpMSB( SpType spType )
	{
		super( spType );
	}

	/**
     * Override getTitle so that it simply returns the "title" attribute if set.
     */
	public String getTitle()
	{
		String title = getTitleAttr();
		if( ( title == null ) || title.equals( "" ) )
			title = type().getReadable();

		if( getNumberRemaining() < 0 )
			return title + " (" + REMOVED_STRING + ")";
		else
			return title + " (" + getNumberRemaining() + "X)";
	}

	/**
     * Get the observation priority.
     */
	public int getPriority()
	{
		String str = _avTable.get( ATTR_PRIORITY );
		if( str == null )
			return PRIORITY_LOW;

		for( int i = 0 ; i < PRIORITIES.length ; ++i )
		{
			if( str.equals( PRIORITIES[ i ] ) )
			{
				if( i == 0 )
					return PRIORITY_HIGH;
				if( i == 1 )
					return PRIORITY_MEDIUM;
				if( i == 2 )
					return PRIORITY_LOW;
			}
		}
		return( ( new Integer( str ) ).intValue() );
	}

	/**
     * Get the observation priority as a human readable String.
     */
	public int getPriorityString()
	{
		String str = _avTable.get( ATTR_PRIORITY );
		if( str == null )
			return PRIORITY_LOW;
		return( ( new Integer( str ) ).intValue() );
	}

	/**
     * Set the Observation type.
     */
	public void setPriority( int priority )
	{
		if( ( priority < 0 ) || ( priority > numPriorities ) )
			return;

		_avTable.set( ATTR_PRIORITY , priority );
	}

	/**
     * Set the library verion to the default String.
     * 
     */
	public void setLibraryRevision()
	{
		_avTable.set( ATTR_LIBRARY_VERSION , KEYWORD_IDENTIFIER + LIBRARY_VERSION + KEYWORD_IDENTIFIER );
	}

	/**
     * Get the version of the library.
     */
	public String getLibraryRevision()
	{
		return _avTable.get( ATTR_LIBRARY_VERSION );
	}

	/**
     * Get the number of observations remaining to be observed.
     * 
     * Added for OMP (MFO, 9 August 2001)
     * 
     * @return number of observations remaining to be observed or 1 if the
     *         attribute has not been set.
     */
	public int getNumberRemaining()
	{
		return _avTable.getInt( ATTR_REMAINING , 1 );
	}

	/**
     * Set status attribute.
     * 
     * Added for OMP (MFO, 9 August 2001)
     */
	public void setNumberRemaining( int remaining )
	{
		_avTable.set( ATTR_REMAINING , remaining );
	}

	public String getChecksum()
	{
		if( _avTable.exists( ATTR_CHECKSUM ) )
			return _avTable.get( ATTR_CHECKSUM );
		else
			return "";
	}

	/**
     * Returns a boolean if the suspend attribute exists
     */
	public boolean isSuspended()
	{
		boolean suspended = false;
		if( _avTable.exists( ATTR_SUSPEND ) )
			suspended = true;
		return suspended;
	}

	/**
     * Unsuspend an MSB
     */
	public void unSuspend()
	{
		_avTable.rm( ATTR_SUSPEND );
	}

	/**
     * Calculates the duration of this MSB.
     * 
     * Note that this method does <B>not</B> return ATTR_ELAPSED_TIME. The
     * return value is re-calculated whenever the method is called. This method
     * includes times for optional and calibration observations.
     */
	public double getTotalTime()
	{
		double elapsedTime = 0.0;
		Enumeration children = children();
		SpItem spItem = null;
		boolean containsSurvey = false;

		while( children.hasMoreElements() )
		{
			spItem = ( SpItem )children.nextElement();

			if( spItem instanceof SpObs )
			{
				elapsedTime += ( ( SpObs )spItem ).getElapsedTime();
			}
			else if( spItem instanceof SpSurveyContainer )
			{
				containsSurvey = true;
				elapsedTime += ( ( SpSurveyContainer )spItem ).getTotalTime();
			}
		}

		SpInstObsComp spInstObsComp = SpTreeMan.findInstrument( this );

		// Add a slew time if we don't have a Survey child - the Survey child already adds a slew
		if( spInstObsComp != null && !containsSurvey )
			elapsedTime += spInstObsComp.getSlewTime();
		return elapsedTime;
	}

	/**
     * Calculates the duration of this MSB.
     * 
     * Note that this method does <B>not</B> return ATTR_ELAPSED_TIME. The
     * return value is re-calculated whenever the method is called. This method
     * does not include times for calibration and optional observations
     */

	protected static boolean isUKIRT = false;

	protected static boolean cachedTelescope = false;

	public double getElapsedTime()
	{
		double elapsedTime = 0.0;
		Enumeration children = children();
		SpItem spItem = null;

		if( !cachedTelescope )
		{
			isUKIRT = "UKIRT".equalsIgnoreCase( System.getProperty( "TELESCOPE" ) );
			cachedTelescope = true;
		}

		while( children.hasMoreElements() )
		{
			spItem = ( SpItem )children.nextElement();

			if( spItem instanceof SpObs && ( !( ( ( SpObs )spItem ).isOptional() ) || isUKIRT ) )
				elapsedTime += ( ( SpObs )spItem ).getElapsedTime();
			else if( spItem instanceof SpSurveyContainer )
				elapsedTime += ( ( SpSurveyContainer )spItem ).getElapsedTime();
		}

		SpInstObsComp spInstObsComp = SpTreeMan.findInstrument( this );

		/*
         * The targetCount is set assuming that there is either a target list or
         * a survey component in the scope but not both.
         */
		int targetCount = 1;

		SpSurveyObsComp surveyObsComp = SpTreeMan.findSurveyComp( this );

		if( surveyObsComp != null )
			targetCount = surveyObsComp.size();

		if( spInstObsComp != null )
			return ( elapsedTime + spInstObsComp.getSlewTime() ) * targetCount;
		else
			return elapsedTime * targetCount;
	}

	/**
     * Sets the ATTR_TOTAL_TIME attribute.
     * 
     * I.e. "saves" total time to the SpAvTable of this item.
     */
	public void saveTotalTime()
	{
		_avTable.set( ATTR_TOTAL_TIME , getTotalTime() );
	}

	/**
     * Sets the ATTR_ELAPSED_TIME attribute.
     * 
     * I.e. "saves" elapsed time to the SpAvTable of this item.
     */
	public void saveElapsedTime()
	{
		_avTable.set( ATTR_ELAPSED_TIME , getElapsedTime() );
	}

	protected void processAvAttribute( String avAttr , String indent , StringBuffer xmlBuffer )
	{
		if( avAttr.equals( ATTR_TOTAL_TIME ) )
			xmlBuffer.append( "\n  " + indent + "<" + ATTR_TOTAL_TIME + " units=\"seconds\">" + getTotalTime() + "</" + ATTR_TOTAL_TIME + ">" );
		else if( avAttr.equals( ATTR_ELAPSED_TIME ) )
			xmlBuffer.append( "\n  " + indent + "<" + ATTR_ELAPSED_TIME + " units=\"seconds\">" + getElapsedTime() + "</" + ATTR_ELAPSED_TIME + ">" );
		else
			super.processAvAttribute( avAttr , indent , xmlBuffer );
	}

	public void processXmlAttribute( String elementName , String attributeName , String value )
	{
		// Do not save units = seconds to the AV table because the XML attribute units="seconds"
		// is added in processAvAttribute. So ignore units = seconds.
		if( attributeName.equals( "units" ) && value.equals( "seconds" ) )
			return;

		super.processXmlAttribute( elementName , attributeName , value );
	}
}

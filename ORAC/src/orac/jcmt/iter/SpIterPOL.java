/*==============================================================*/
/*                                                              */
/*                UK Astronomy Technology Centre                */
/*                 Royal Observatory, Edinburgh                 */
/*                 Joint Astronomy Centre, Hilo                 */
/*                   Copyright (c) PPARC 2000                   */
/*                                                              */
/*==============================================================*/
// $Id$
package orac.jcmt.iter ;

import gemini.sp.SpFactory ;
import gemini.sp.SpType ;
import gemini.sp.iter.IterConfigItem ;
import gemini.sp.iter.SpIterConfigObs ;

// This is basically a crude copy of the UKIRT IRPOL iterator.
// The hard wired values might need changing. (Added by MFO, 11 January 2002)
/**
 * The POL configuration iterator for JCMT (SCUBA).
 */
public class SpIterPOL extends SpIterConfigObs
{
	public static final SpType SP_TYPE = SpType.create( SpType.ITERATOR_COMPONENT_TYPE , "instPOL" , "POL" ) ;
	
	public static String CONTINUOUS_SPIN = "continuousSpin" ;

	// Register the prototype.
	static
	{
		SpFactory.registerPrototype( new SpIterPOL() ) ;
	}

	// Hardwire the allowed angles, so that they are accessible to all instruments.
	static final String ALLOWED_ANGLES[] = 
	{ 
		"0" , 
		"22.5" , 
		"45" , 
		"67.5" , 
		"90" , 
		"112.5" , 
		"135" , 
		"157.5" , 
		"180.0" , 
		"202.5" , 
		"225.0" , 
		"247.5" , 
		"270.0" , 
		"292.5" , 
		"315.0" , 
		"337.5" 
	} ;

	/**
	 * Default constructor.
	 */
	public SpIterPOL()
	{
		super( SP_TYPE ) ;
	}

	/**
	 * Get the name of the item being iterated over.  Subclasses must
	 * define.
	 */
	public String getItemName()
	{
		return "POL" ;
	}

	/**
	 * Override adding a configuration item to use the no default version.
	 */
	public void addConfigItem( IterConfigItem ici , int size )
	{
		super.addConfigItem( ici , size ) ;

		// Then set a default value
		setConfigStep( ici.attribute , ALLOWED_ANGLES[ 0 ] , 0 ) ;
	}

	/**
	 * Get the array containing the IterConfigItems offered by POL.
	 */
	public IterConfigItem[] getAvailableItems()
	{
		// Hardwire the allowed angles.
		IterConfigItem iciWPLAngle = new IterConfigItem( "Waveplate Angle" , "POLIter" , ALLOWED_ANGLES ) ;

		IterConfigItem[] iciA = { iciWPLAngle } ;

		return iciA ;
	}
	
	public void setContinuousSpin( boolean enabled )
	{
		if( enabled )
		{
			getTable().rm( SpIterPOL.ATTR_ITER_ATTRIBUTES ) ;
			getTable().set( SpIterPOL.CONTINUOUS_SPIN , 0. ) ;
		}
		else
		{
			getTable().rm( SpIterPOL.CONTINUOUS_SPIN ) ;
		}
	}

	public boolean hasContinuousSpin()
	{
		return getTable().exists( SpIterPOL.CONTINUOUS_SPIN ) ;
	}	
}

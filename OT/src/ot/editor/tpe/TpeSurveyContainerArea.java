/*==============================================================*/
/*                                                              */
/*                UK Astronomy Technology Centre                */
/*                 Royal Observatory, Edinburgh                 */
/*                 Joint Astronomy Centre, Hilo                 */
/*                   Copyright (c) PPARC 2002                   */
/*                                                              */
/*==============================================================*/

// Copyright 1997 Association for Universities for Research in Astronomy, Inc.,
// Observatory Control System, Gemini Telescopes Project.
// See the file COPYRIGHT for complete details.
//
// $Id$
//
package ot.editor.tpe ;

import jsky.app.ot.tpe.TpeSciArea ;
import jsky.app.ot.fits.gui.FitsImageInfo ;
import gemini.sp.obsComp.SpInstObsComp ;
import gemini.sp.SpSurveyContainer ;

/**
 * Describes a Scan Area and facilitates drawing, rotating it.
 *
 * @see orac.jcmt.iter.SpIterRasterObs
 *
 * @author Martin Folger (M.Folger@roe.ac.uk), based on jsky.app.ot.tpe.TpeSciArea
 */
public class TpeSurveyContainerArea extends TpeSciArea
{
	/**
	 * Update the ScanArea fields, returning true iff changes were made.
	 */
	public boolean update( SpInstObsComp spInst , FitsImageInfo fii )
	{
		throw new UnsupportedOperationException( "TpeScanArea.update(orac.jcmt.iter.SpIterRasterObs, " + "jsky.app.ot.fits.gui.FitsImageInfo) should be used" ) ;
	}

	/**
	 * Update the ScanArea fields, returning true iff changes were made.
	 */
	public boolean update( SpSurveyContainer surveyContainer , FitsImageInfo fii )
	{
		double w , h , posAngle , sky ;

		w = 300 * fii.pixelsPerArcsec ;
		h = 300 * fii.pixelsPerArcsec ;

		posAngle = ( Math.PI * 0 ) / 180. ;
		sky = fii.theta ;

		// Update the instance variables if necessary.
		if( ( w != this.width ) || ( h != this.height ) || ( posAngle != this.posAngleRadians ) || ( sky != this.skyCorrection ) )
		{

			this.width = w ;
			this.height = h ;
			this.posAngleRadians = posAngle ;
			this.skyCorrection = sky ;
			return true ;
		}

		return false ;
	}
}

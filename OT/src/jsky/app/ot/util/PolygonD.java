// Copyright 1997 Association for Universities for Research in Astronomy, Inc.,
// Observatory Control System, Gemini Telescopes Project.
// See the file COPYRIGHT for complete details.
//
// $Id$
//
package jsky.app.ot.util;

import java.awt.Polygon;

/**
 * Like a java.awt.Polygon, but instead of integer coordinates, doubles are
 * used.
 */
public class PolygonD implements Cloneable
{
	public int npoints = 0;
	public double xpoints[] = new double[ 4 ];
	public double ypoints[] = new double[ 4 ];
	private Polygon _awtPolygon;

	/**
	 * Creates an empty polygon.
	 */
	public PolygonD(){}

	/**
	 * Creates a new PolygonD copying from the given PolygonD.
	 */
	public PolygonD( PolygonD srcPD )
	{
		this.npoints = srcPD.npoints;
		this.xpoints = new double[ this.npoints ];
		this.ypoints = new double[ this.npoints ];
		System.arraycopy( srcPD.xpoints , 0 , this.xpoints , 0 , this.npoints );
		System.arraycopy( srcPD.ypoints , 0 , this.ypoints , 0 , this.npoints );
	}

	/**
	 * Initializes a PolygonD from the specified parameters.
	 * @param xpoints the array of x coordinates
	 * @param ypoints the array of y coordinates
	 * @param npoints the total number of points in the Polygon
	 */
	public PolygonD( double[] xpoints , double[] ypoints , int npoints )
	{
		this.npoints = npoints;
		this.xpoints = new double[ npoints ];
		this.ypoints = new double[ npoints ];
		System.arraycopy( xpoints , 0 , this.xpoints , 0 , npoints );
		System.arraycopy( ypoints , 0 , this.ypoints , 0 , npoints );
	}

	public Polygon getAWTPolygon()
	{
		if( ( _awtPolygon == null ) || ( _awtPolygon.npoints != npoints ) )
			_awtPolygon = new Polygon( new int[ npoints ] , new int[ npoints ] , npoints );

		for( int i = 0 ; i < npoints ; ++i )
		{
			if( _awtPolygon.xpoints[ i ] < 0. )
				_awtPolygon.xpoints[ i ] = ( int )( xpoints[ i ] - 0.5 );
			else
				_awtPolygon.xpoints[ i ] = ( int )( xpoints[ i ] + 0.5 );

			if( _awtPolygon.ypoints[ i ] < 0. )
				_awtPolygon.ypoints[ i ] = ( int )( ypoints[ i ] - 0.5 );
			else
				_awtPolygon.ypoints[ i ] = ( int )( ypoints[ i ] + 0.5 );
		}
		return _awtPolygon;
	}

	public String toString()
	{
		return getClass().getName() + "[x=" + xpoints + ", y=" + ypoints + "]";
	}

	public Object clone()
	{
		PolygonD pd;
		try
		{
			pd = ( PolygonD )super.clone();
		}
		catch( CloneNotSupportedException ex )
		{
			return null;
		}
		pd.xpoints = ( double[] )xpoints.clone();
		pd.ypoints = ( double[] )ypoints.clone();

		return pd;
	}
}

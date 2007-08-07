// Copyright 1997 Association for Universities for Research in Astronomy, Inc.,
// Observatory Control System, Gemini Telescopes Project.
// See the file COPYRIGHT for complete details.
//
// $Id$
//
package jsky.app.ot.gui;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;

/**
 * A utility class for drawing routines.
 */
public final class DrawUtil
{
	/**
	 * Drag a string in the given foreground and background colors.
	 */
	public static void drawString( Graphics g , String str , Color fg , Color bg , int x , int y )
	{
		FontMetrics fm = g.getFontMetrics();
		int w = fm.stringWidth( str );
		int h = fm.getAscent();
		g.setColor( bg );
		g.fillRect( x , y - h , w + 1 , h + 1 ); // String loc + padding
		g.setColor( fg );
		g.drawString( str , x , y );
	}
}

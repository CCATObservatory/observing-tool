// Copyright 1997 Association for Universities for Research in Astronomy, Inc.,
// Observatory Control System, Gemini Telescopes Project.
// See the file COPYRIGHT for complete details.
//
// $Id$
//
package jsky.app.ot.tpe.feat;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Polygon;

import jsky.app.ot.gui.DrawUtil;

import gemini.sp.SpItem;
import gemini.sp.SpAvEditState;
import gemini.sp.obsComp.SpInstObsComp;

import jsky.app.ot.fits.gui.FitsImageInfo;
import jsky.app.ot.fits.gui.FitsMouseEvent;

import jsky.app.ot.util.Angle;
import jsky.app.ot.util.PolygonD;

import jsky.app.ot.tpe.TpeImageFeature;
import jsky.app.ot.tpe.TpeDraggableFeature;
import jsky.app.ot.tpe.TpeImageWidget;
import jsky.app.ot.tpe.TpeSciArea;
import java.awt.geom.Point2D;

/**
 * An implementation class used to simplify the job of rotating science
 * areas.
 */
final class TpeSciAreaDragObject
{
   int    _xb, _yb;
   double _angle = 0;
 
/**
 * Construct with the base position at (xb,yb).
 */
public TpeSciAreaDragObject(int xb, int yb, int x, int y)
{
   //System.out.println("BASE.........: (" + _xb + ", " + _yb + ")");
 
   _xb = xb;
   _yb = yb;
   _angle = getAngle(x,y);
 
   //System.out.println("INITIAL ANGLE: " + _angle);
}
 
public double
nextAngleDiff(int x, int y)
{
   double angle = getAngle(x, y);
 
   //System.out.println("NEXT ANGLE...: " + angle);
 
   double val   = angle - _angle;
   _angle = angle;
 
   //System.out.println("DIFF.........: " + val);
 
   return val;
}
 
public double
getAngle(int x, int y)
{
   double angle;
 
   // All the points are in screen coordinates, which means y increases down
   // This makes x and y relative to the origin in a right side up frame.
   int xp = x - _xb;
   int yp = _yb - y;
 
   int xa = Math.abs( xp );
   int ya = Math.abs( yp );
 
   if (xa == 0) {
      if (yp >= 0) {
         return Math.PI * 0.5;
      } else {
         return Math.PI * 1.5;
      }
   }
 
   angle = Angle.atanRadians( ((double) ya) / ((double) xa) );
 
   if ((xp > 0)  && (yp >= 0)) {
      return angle;
   }
 
   if ((xp < 0) && (yp >= 0)) {
      return Math.PI - angle;
   }
 
   if ((xp < 0) && (yp < 0)) {
      return Math.PI + angle;
   }
 
   return Math.PI * 2.0 - angle;
}
 
}


/**
 * Draws the Science Area, the detector or slit.
 */
public class TpeSciAreaFeature extends TpeImageFeature
                            implements TpeDraggableFeature
{
   private TpeSciArea    _sciArea;
   private PolygonD      _sciAreaPD;
   private PolygonD      _tickMarkPD;
   private SpInstObsComp _instItem;

   private boolean       _valid = false;

   private TpeSciAreaDragObject _dragObject;
   private boolean _dragging = false;
   private int     _dragX;
   private int     _dragY;

/**
 * Construct the feature with its name and description. 
 */
public TpeSciAreaFeature()
{
   super("Sci Area", "Science area.");
}

/**
 * Reinit.
 */
public void
reinit(TpeImageWidget iw, FitsImageInfo fii)
{
   //System.out.println("TpeSciAreaFeature.reinit");
   super.reinit(iw, fii);
   _valid = false;
   return;
}

/**
 * The position angle has changed.
 */
public void
posAngleUpdate(FitsImageInfo fii)
{
   _valid = false; 
}

/**
 * Calculate the polygon describing the screen location of the science area.
 */
private boolean
_calc(FitsImageInfo fii)
{
   //System.out.println("TpeSciAreaFeature._calc(): " + fii);

   // Need the instrument to get the science area
   SpInstObsComp spInst = _iw.getInstrumentItem();
   if (spInst == null) {
      return false;
   }

   if (_sciArea == null) {
      _sciArea = _iw.getSciArea();
      if (_sciArea == null) {
         return false;
      }
   }

   boolean updated = _sciArea.update(spInst, fii);
 
   if (_valid && !updated && (_sciAreaPD != null) && (spInst == _instItem)) {
      // Already have the current values
      return true;
   }
 
   _instItem = spInst;

   double xBase = (double) fii.baseScreenPos.x;
   double yBase = (double) fii.baseScreenPos.y;
   _sciAreaPD   = _sciArea.getPolygonDAt(xBase, yBase);

   // Init the _tickMarkPD
   if (_tickMarkPD == null) {
      double[] xpoints = new double[4];
      double[] ypoints = new double[4];
      _tickMarkPD = new PolygonD(xpoints, ypoints, 4);
   }

   double x = xBase;
   double y = yBase - _sciArea.height/2.0;

   _tickMarkPD.xpoints[0] = x;
   _tickMarkPD.ypoints[0] = y - MARKER_SIZE*2;

   _tickMarkPD.xpoints[1] = x - MARKER_SIZE;
   _tickMarkPD.ypoints[1] = y - 2;

   _tickMarkPD.xpoints[2] = x + MARKER_SIZE;
   _tickMarkPD.ypoints[2] = y - 2;

   _tickMarkPD.xpoints[3] = _tickMarkPD.xpoints[0];
   _tickMarkPD.ypoints[3] = _tickMarkPD.ypoints[0];

   _iw.skyRotate(_tickMarkPD, _sciArea.posAngleRadians);

   _valid = true;
   return true;
}

/**
 * Draw the feature.
 */
public void
draw(Graphics g, FitsImageInfo fii)
{
   if (!_calc(fii)) {
      return;
   }

   g.setColor(Color.cyan);
   g.drawPolygon(_sciAreaPD.getAWTPolygon());

   // If the shape is circular then forget about tick mark and dragging.
   if(_sciArea.getShape() == TpeSciArea.CIRCULAR) {
      return;
   }

   g.fillPolygon(_tickMarkPD.getAWTPolygon());

   if (_dragging) {
      // Draw a little above the mouse
      int baseX = _dragX;
      int baseY = _dragY - 10;

      // Draw a string displaying the rotation angle
      String s = _instItem.getPosAngleDegreesStr();
      DrawUtil.drawString(g, s, Color.cyan, Color.black, baseX, baseY);
   }
}

/**
 * Start dragging the object.
 */
public boolean
dragStart(FitsMouseEvent fme, FitsImageInfo fii)
{
   // If the shape is circular then forget about dragging.
   if(_sciArea.getShape() == TpeSciArea.CIRCULAR) {
      return false;
   }

   if ((_sciAreaPD == null) || (_tickMarkPD == null)) {
      return false;
   }

   _dragObject = null;

   // See if dragging by the corner
   for (int i=0; i<(_sciAreaPD.npoints-1); ++i) {
      int cornerx = (int) (_sciAreaPD.xpoints[i] + 0.5);
      int cornery = (int) (_sciAreaPD.ypoints[i] + 0.5);
 
     int dx = Math.abs(cornerx - fme.xWidget);
      if (dx > MARKER_SIZE) {
         continue;
      }
      int dy = Math.abs(cornery - fme.yWidget);
      if (dy > MARKER_SIZE) {
         continue;
      }

      Point2D.Double p = fii.baseScreenPos;
      _dragObject = new TpeSciAreaDragObject((int)p.x, (int)p.y, cornerx, cornery);
   }

   // See if dragging by the tick mark (give a couple extra pixels to make it
   // easier to grab)
   if (_dragObject == null) {
      int x  = (int) (_tickMarkPD.xpoints[0] + 0.5);
      int dx = Math.abs(x - fme.xWidget);
      if (dx <= MARKER_SIZE + 2) {
         int y0 = (int) (_tickMarkPD.ypoints[0] + 0.5);
         int y1 = (int) (_tickMarkPD.ypoints[1] + 0.5);
         int y = (y0 + y1)/2;
         int dy = Math.abs(y - fme.yWidget);
         if (dy <= MARKER_SIZE + 2) {
            Point2D.Double p = fii.baseScreenPos;
            _dragObject = new TpeSciAreaDragObject((int)p.x, (int)p.y, x, y);
         }
      }
   }

   _dragging = (_dragObject != null);
   if (_dragging) {
      _dragX    = fme.xWidget;
      _dragY    = fme.yWidget;
      _instItem.getAvEditFSM().setEachEditNotifies(false);
   }

   return _dragging;
}
 
/**
 * Drag to a new location.
 */
public void drag(FitsMouseEvent fme)
{
   // If the shape is circular then forget about dragging.
   if(_sciArea.getShape() == TpeSciArea.CIRCULAR) {
      return;
   }

   if (_dragObject != null) {
      _dragX = fme.xWidget;
      _dragY = fme.yWidget;

      double diff = _dragObject.nextAngleDiff(fme.xWidget, fme.yWidget);
      _instItem.addPosAngleRadians(diff);
   }
}
 
/**
 * Stop dragging.
 */
public void dragStop(FitsMouseEvent fme)
{
   // If the shape is circular then forget about dragging.
   if(_sciArea.getShape() == TpeSciArea.CIRCULAR) {
      return;
   }

   if (_dragObject != null) {
      _instItem.getAvEditFSM().setEachEditNotifies(true);
      _dragging = false;
      drag(fme);
      _dragObject = null;
   }
}

}


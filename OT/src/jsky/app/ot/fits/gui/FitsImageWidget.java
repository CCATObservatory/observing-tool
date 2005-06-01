// Copyright 1997 Association for Universities for Research in Astronomy, Inc.,
// Observatory Control System, Gemini Telescopes Project.
// See the file COPYRIGHT for complete details.
//
// $Id$
//
package jsky.app.ot.fits.gui;

import diva.canvas.GraphicsPane;
import diva.canvas.event.EventLayer;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.geom.*;
import java.awt.geom.Point2D;
import java.util.*;
import javax.swing.event.*;
import jsky.app.ot.gui.image.ImageView;
import jsky.app.ot.gui.image.ViewportImageWidget;
import jsky.app.ot.gui.image.ViewportMouseEvent;
import jsky.app.ot.util.Angle;
import jsky.app.ot.util.DDMMSS;
import jsky.app.ot.util.HHMMSS;
import jsky.app.ot.util.PolygonD;
import jsky.app.ot.util.RADecMath;
import jsky.app.ot.util.ScreenMath;
import gemini.util.TelescopePos;
import jsky.catalog.TableQueryResult;
import jsky.catalog.gui.SymbolSelectionEvent;
import jsky.catalog.gui.SymbolSelectionListener;
import jsky.catalog.gui.TablePlotter;
import jsky.coords.Coordinates;
import jsky.coords.WorldCoords;
import jsky.coords.NamedCoordinates;
import jsky.coords.CoordinateConverter;
import jsky.image.ImageChangeEvent;
import jsky.navigator.Navigator;
import jsky.util.gui.StatusPanel;

/**
 * A ViewportImageWidget that understands coordinate systems.
 */
public class FitsImageWidget extends ViewportImageWidget {

    protected FitsImageInfo      _imgInfo      = new FitsImageInfo();
    protected boolean            _imgInfoValid = false;
    protected boolean            _unsupportedProjection = false;

    private Vector _infoObs = new Vector();

    public FitsImageWidget(Component parent) { //, StatusPanel statusPanel) {
	super(parent); //, statusPanel);
	
	// We need to use the Diva classes to get the correct mouse events 
	// (see ancestor class DivaGraphicsImageDisplay)
	EventLayer layer = ((GraphicsPane)getCanvasPane()).getBackgroundEventLayer();
    }


    /** 
     * If the given screen coordinates point is within a displayed catalog symbol, set it to
     * point to the center of the symbol and return the world coordinates position
     * from the catalog table row. Otherwise, return null and do nothing.
     */
    protected NamedCoordinates getCatalogPosition(Point2D.Double p) {
	TablePlotter plotter = getNavigator().getPlotter();
	if (plotter == null)
	    return null;
	return plotter.getCatalogPosition(p);
    }


    /**
     * Make a NavigatorFrame or NavigatorInternalFrame, depending
     * on what type of frames are being used.
     * (Redefined here to arrange to snap new object positions to catalog symbol locations).
     */
    protected void makeNavigatorFrame() {
	super.makeNavigatorFrame();
    }

    /**
     * Set the instance of the catalog navigator to use with this image display.
     * (Redefined here to arrange to snap new object positions to catalog symbol locations).
     */
    public void setNavigator(Navigator navigator) {
	super.setNavigator(navigator);
    }


    /**
     * Free any resources used by this image widget.
     */
    public void	free() {
	_infoObs.removeAllElements();
	super.free();
    }

    /**
     * Is the FitsImageWidget initialized?
     */
    public boolean isInitialized() {
	return _imgInfoValid;
    }

    public synchronized void addInfoObserver(FitsImageInfoObserver obs) {
	if (!_infoObs.contains(obs)) {
	    _infoObs.addElement(obs);
	}
    }
 
    public synchronized void deleteInfoObserver(FitsImageInfoObserver obs) {
	_infoObs.removeElement(obs);
    }
 
    public synchronized void deleteInfoObservers() {
	_infoObs.removeAllElements();
    }
 
    private void _notifyInfoObs() {
	Vector v;
	synchronized (_infoObs) {
	    v = (Vector) _infoObs.clone();
	}

	for (int i=0; i<v.size(); ++i) {
	    FitsImageInfoObserver fiio = (FitsImageInfoObserver) v.elementAt(i);
	    fiio.imageInfoUpdate(this, _imgInfo);
	}
    }


    /**
     * Set the base position.
     */
    public boolean setBasePos(double ra, double dec) {
//         System.out.println("Called FitsImageWidget::setBasePos(" + HHMMSS.valStr(ra) + ", " + DDMMSS.valStr(dec) + ") from...");
//         new Exception().printStackTrace();
	if (! getCoordinateConverter().isWCS()) {
	    _imgInfoValid = false;
	    return false;
	}

	try {
	    // coords might be out of range
	    _imgInfo.baseScreenPos	= raDecToImageWidget(ra, dec);
	}
	catch(Exception e) {
	    return false;
	}

	_imgInfo.ra		= ra;
	_imgInfo.dec		= dec;

	// Get two points, one at the base and one an arcmin north of the base
	Point2D.Double temp1 = raDecToImageView(ra, dec);
	Point2D.Double temp2 = raDecToImageView(ra, dec + 0.01666667);

	// Get the difference in x,y and the distance between the two points
	double xdprime = temp2.x - temp1.x;
	double ydprime = temp2.y - temp1.y;

	// Measure theta from the y axis:  ie. a cartisian coordinate system
	// rotated by 90 degrees.
	_imgInfo.theta = Angle.atanRadians( xdprime/ydprime );
	if (ydprime > 0) {
	    _imgInfo.theta = Angle.normalizeRadians(_imgInfo.theta + Math.PI);
	}

	//System.out.println("----------");
	//System.out.println("xdprime = " + xdprime);
	//System.out.println("ydprime = " + ydprime);
	//System.out.println("----------");

	if (Angle.almostZeroRadians(_imgInfo.theta)) {
	    _imgInfo.theta = 0;
	}

	//System.out.println(" THETA = " + _imgInfo.theta);

	// Convert the two points to pixel coordinates on the screen
	Point2D.Double temp3 = imageViewToImageWidget(temp1.x, temp1.y);
	Point2D.Double temp4 = imageViewToImageWidget(temp2.x, temp2.y);

	// Get the difference in x,y pixels between the two points
	double xiprime = temp4.x - temp3.x;
	double yiprime = temp4.y - temp3.y;
	double r = Math.sqrt(xiprime*xiprime + yiprime*yiprime);

	// Divide the 1 min distance by 60 arcsec to get pixels/perArcsec
	_imgInfo.pixelsPerArcsec = r/60.0;
	//System.out.println("---> pixelsPerArcsec = " + _imgInfo.pixelsPerArcsec);

	_imgInfoValid = true;
	_notifyInfoObs();
	return true;
    }


    /**
     * Set the position angle.
     */
    public boolean setPosAngle(double posAngleDegrees) {
	if (! getCoordinateConverter().isWCS()) {
	    _imgInfoValid = false;
	    return false;
	}
	_imgInfo.posAngleDegrees = posAngleDegrees;

	_notifyInfoObs();
	return true;
    }


    /**
     * Convert an image x,y location to RA and Dec.
     */
    public double[] imageViewToRaDec(double x, double y) {
	Point2D.Double p = new Point2D.Double(x, y);
	getCoordinateConverter().userToWorldCoords(p, false);
	return new double[]{p.x, p.y};
    }

    /**
     * Convert RA and Dec (in degrees) to an image x,y.
     */
    private Point2D.Double raDecToImageView(double ra, double dec) {
	Point2D.Double p = new Point2D.Double(ra, dec);
	getCoordinateConverter().worldToUserCoords(p, false);
	return p;
    }

    /**
     * Convert an image widget pixel location to RA and Dec (in degrees).
     */
    public double[] imageWidgetToRaDec(int x, int y) {
	Point2D.Double p = new Point2D.Double(x, y);
	getCoordinateConverter().screenToWorldCoords(p, false);
	return new double[]{p.x, p.y};
    }

    /**
     * Convert RA and Dec (in degrees) to an image widget pixel location.
     */
    public Point2D.Double raDecToImageWidget(double ra, double dec) {
	Point2D.Double p = new Point2D.Double(ra, dec);
	getCoordinateConverter().worldToScreenCoords(p, false);
	return p;
    }

    /**
     * Convert an offset to an image widget pixel location.
     */
    public Point2D.Double offsetToImageWidget(double xOff, double yOff) {
	if (!_imgInfoValid) {
	    return null;
	}
	double ppa  = _imgInfo.pixelsPerArcsec;

	double xPix = _imgInfo.baseScreenPos.x - (xOff * ppa);
	double yPix = _imgInfo.baseScreenPos.y - (yOff * ppa);
	double posAngleRad = Angle.degreesToRadians(_imgInfo.posAngleDegrees);
	
// 	Point2D.Double pd   = skyRotate(xPix, yPix, posAngleRad);
	Point2D.Double pd   = skyRotate(xPix, yPix, 0.0);

	//int x = (int) Math.round(pd.x);
	// int y = (int) Math.round(pd.y);
	// return new Point2D.Double(x, y);
	return pd;
    }

    /**
     * Convert an image widget x/y to an offset.
     */
    public double[] imageWidgetToOffset(double x, double y) {
	if (!_imgInfoValid) {
	    return null;
	}
	// unrotate - must correct for the fact that skyRotate will try to adjust
	// for due north in the sky.  We ultimately want to rotate by
	// -(posAngle + theta).  skyRotate will add theta to whatever is passed
	// to it, so to unrotate we need -(posAngle + 2*theta)
	double posAngleRad = Angle.degreesToRadians(_imgInfo.posAngleDegrees);
	double angle       = -(posAngleRad + 2.0*_imgInfo.theta);
	Point2D.Double pd = skyRotate((double) x, (double) y, angle);

	double ppa  = _imgInfo.pixelsPerArcsec;
	double xOff = (_imgInfo.baseScreenPos.x - pd.x)/ppa;
	double yOff = (_imgInfo.baseScreenPos.y - pd.y)/ppa;
	xOff = Math.round(xOff * 1000.0)/1000.0;
	yOff = Math.round(yOff * 1000.0)/1000.0;
	double[] t = { xOff, yOff };
	return t;
    }

    /**
     * Convert a TelescopePos to an ImageWidget Point.
     */
    public Point2D.Double telescopePosToImageWidget(TelescopePos tp) {
	if (!tp.isValid()) {
	    return null;
	}

	double x = tp.getXaxis();
	double y = tp.getYaxis();

	if (tp.isOffsetPosition()) {
	    return offsetToImageWidget(x, y);
	}

	return raDecToImageWidget(x, y);
    }


    /**
     * Rotate a point through the given angle (in radians), relative to
     * the base position, correcting for sky rotation.
     */
    public Point2D.Double skyRotate(double x, double y, double phi) {
	if (!_imgInfoValid) {
	    return null;
	}

	double angle = _imgInfo.theta + phi;
	double xBase = (double) _imgInfo.baseScreenPos.x;
	double yBase = (double) _imgInfo.baseScreenPos.y;

	return ScreenMath.rotateRadians(x, y, angle, xBase, yBase);
    }

    /**
     * Rotate a polygon through the given angle (in radians), relative to
     * the base position, correcting for sky rotation.
     */
    public void skyRotate(PolygonD p, double phi) {
	if (!_imgInfoValid) {
	    return;
	}

	double angle = _imgInfo.theta + phi;
	double xBase = (double) _imgInfo.baseScreenPos.x;
	double yBase = (double) _imgInfo.baseScreenPos.y;

	ScreenMath.rotateRadians(p, angle, xBase, yBase);
    }


    protected void _notifyViewObs(ImageView iv) {
	setBasePos(_imgInfo.ra, _imgInfo.dec);
	super._notifyViewObs(iv);
    }

    protected ViewportMouseEvent _createMouseEvent() {
	return new FitsMouseEvent();
    }

    protected boolean _initMouseEvent(MouseEvent evt, ViewportMouseEvent vme) {
	//if (! _checkImgInfo()) 
	//    return false;

	FitsMouseEvent fme = (FitsMouseEvent) vme;
	Point2D.Double mp = new Point2D.Double(evt.getX(), evt.getY());

	NamedCoordinates catalogPosition = null;

	// snap to catalog symbol position, if user clicked on one
	if (evt.getID() == MouseEvent.MOUSE_CLICKED) {
	  catalogPosition = getCatalogPosition(mp);
	  if(catalogPosition != null) {
	    fme.ra  = catalogPosition.getCoordinates().getX();
	    fme.dec = catalogPosition.getCoordinates().getY();
	  }  
	}

	Point2D.Double p = new Point2D.Double(mp.x, mp.y);
	getCoordinateConverter().screenToUserCoords(p, false);
	if (catalogPosition == null) {
	    WorldCoords worldCoords = userToWorldCoords(p.x, p.y);
	    fme.ra  = worldCoords.getX();
	    fme.dec = worldCoords.getY();
	}    

	vme.id      = evt.getID();
	vme.source  = this;
	vme.xView   = p.x;
	vme.yView   = p.y;
	vme.xWidget = (int)Math.round(mp.x);
	vme.yWidget = (int)Math.round(mp.y);

	double[] d = screenCoordsToOffset(mp.x, mp.y);
	fme.xOffset    = d[0];
	fme.yOffset    = d[1];

	return true;

    }

    /** 
     * This method is called before and after a new image is loaded, each time
     * with a different argument.
     *
     * @param before set to true before the image is loaded and false afterwards
     */
    protected void newImage(boolean before) {
	super.newImage(before);
	if (! before) {
	    setBasePos(_imgInfo.ra, _imgInfo.dec);
	}
	updateImage();
    }

    /** Override base class version to create a blank image */
    public void clear() {
	blankImage(_imgInfo.ra, _imgInfo.dec);
    }


    /**
     * Convert the given user coordinates location to world coordinates.
     */
    public WorldCoords userToWorldCoords(double x, double y) {
	Point2D.Double p = new Point2D.Double(x, y);
	CoordinateConverter converter = getCoordinateConverter();
	converter.userToWorldCoords(p, false);
	return new WorldCoords(p.x, p.y, converter.getEquinox());
    }


    /**
     * Convert the given screen coordinates to an offset from the base position (in arcsec).
     */
    public double[] screenCoordsToOffset(double x, double y) {
	//if (! _checkImgInfo()) 
	//    return null;
	
	// unrotate - must correct for the fact that skyRotate will try to adjust
	// for due north in the sky.  We ultimately want to rotate by
	// -(posAngle + theta).  skyRotate will add theta to whatever is passed
	// to it, so to unrotate we need -(posAngle + 2*theta)
	double posAngleRad = Angle.degreesToRadians(_imgInfo.posAngleDegrees);
	double angle       = -(posAngleRad + 2.0*_imgInfo.theta);
	Point2D.Double pd = skyRotate((double) x, (double) y, angle);

	double ppa  = _imgInfo.pixelsPerArcsec;
	double xOff = (_imgInfo.baseScreenPos.x - pd.x)/ppa;
	double yOff = (_imgInfo.baseScreenPos.y - pd.y)/ppa;
	xOff = Math.round(xOff * 1000.0)/1000.0;
	yOff = Math.round(yOff * 1000.0)/1000.0;
	double[] t = { xOff, yOff };
	return t;
    }

}

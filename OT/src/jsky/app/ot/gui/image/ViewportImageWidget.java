// Copyright 1997 Association for Universities for Research in Astronomy, Inc.,
// Observatory Control System, Gemini Telescopes Project.
// See the file COPYRIGHT for complete details.
//
// $Id$
//
package jsky.app.ot.gui.image;

import java.awt.Component ;
import javax.swing.event.MouseInputListener ;
import java.awt.geom.Rectangle2D ;
import java.awt.geom.Point2D;
import java.util.Vector ;
import java.awt.event.MouseEvent ;
import jsky.navigator.NavigatorImageDisplay;

/**
 * An ImageWidget that maintains a concept of a viewport on a base image.
 * The image that the viewport displays is taken to be a view of or window
 * on the real base image.
 * <p>
 * The ViewportImageWidget supports two types of observers: mouse observers
 * and view change observers.  Mouse observers are kept informed of where
 * the mouse is at all times and when it is pressed and released.   View
 * change observers are notified whenever the view on the base image changes.
 */
public class ViewportImageWidget extends NavigatorImageDisplay implements MouseInputListener
{
	private Vector _mouseObs = new Vector();
	private Vector _viewObs = new Vector();

	public ViewportImageWidget( Component parent )
	{
		super( parent );
		addMouseListener( this );
		addMouseMotionListener( this );
	}

	/**
	 * Free any resources used by this widget.
	 */
	public void free(){}

	/**
	 * Translate an (x,y) position on the image view (which is relative to the
	 * the base image being viewed) to the ImageWidget itself.  Since the scale
	 * of the viewport may be greater than 1, double pixel values are required.
	 */
	public synchronized Point2D.Double imageViewToImageWidget( double x , double y )
	{
		Point2D.Double p = new Point2D.Double( x , y );
		getCoordinateConverter().userToScreenCoords( p , false );
		return p;
	}

	/**
	 * Translate an (x,y) position on the ImageWidget to the image view.  Since
	 * the view may be scaled, a Point2D.Double is returned.
	 */
	public synchronized Point2D.Double imageWidgetToImageView( int x , int y )
	{
		Point2D.Double p = new Point2D.Double( x , y );
		getCoordinateConverter().screenToUserCoords( p , false );
		return p;
	}

	protected ViewportMouseEvent _createMouseEvent()
	{
		return new ViewportMouseEvent();
	}

	protected synchronized boolean _initMouseEvent( MouseEvent evt , ViewportMouseEvent vme )
	{
		Point2D.Double p = new Point2D.Double( evt.getX() , evt.getY() );
		getCoordinateConverter().screenToUserCoords( p , false );

		vme.id = evt.getID();
		vme.source = this;
		vme.xView = p.x;
		vme.yView = p.y;
		vme.xWidget = evt.getX();
		vme.yWidget = evt.getY();

		return true;
	}

	public synchronized void addMouseObserver( ViewportMouseObserver obs )
	{
		if( !_mouseObs.contains( obs ) )
			_mouseObs.addElement( obs );
	}

	public synchronized void deleteMouseObserver( ViewportMouseObserver obs )
	{
		_mouseObs.removeElement( obs );
	}

	/**
	 * Tell all the mouse observers about the new mouse event.
	 */
	protected void _notifyMouseObs( MouseEvent e )
	{
		ViewportMouseEvent vme = _createMouseEvent();
		_initMouseEvent( e , vme );
		for( int i = 0 ; i < _mouseObs.size() ; ++i )
		{
			ViewportMouseObserver vmo = ( ViewportMouseObserver )_mouseObs.elementAt( i );
			vmo.viewportMouseEvent( this , vme );
		}
	}

	/**
	 * Tell all the mouse observers about the new ViewportMouseEvent event.
	 */
	protected void _notifyMouseObs( ViewportMouseEvent vme )
	{
		for( int i = 0 ; i < _mouseObs.size() ; ++i )
		{
			ViewportMouseObserver vmo = ( ViewportMouseObserver )_mouseObs.elementAt( i );
			vmo.viewportMouseEvent( this , vme );
		}
	}

	/** called when the image has changed to update the display */
	public synchronized void updateImage()
	{
		super.updateImage();

		Rectangle2D.Double r = getVisibleArea();
		try
		{
			// might throw an exception if changing images
			_notifyViewObs( new ImageView( ( int )r.x , ( int )r.y , ( int )( r.x + r.width ) , ( int )( r.y + r.height ) , 1.0 ) );
			/* last arg was scale which no longer exists in current jsky*/
		}
		catch( Exception e ){}
	}

	/**
	 * Tell all the view observers that the view has changed.
	 */
	protected void _notifyViewObs( ImageView iv )
	{
		for( int i = 0 ; i < _viewObs.size() ; ++i )
		{
			ViewportViewObserver vvo = ( ViewportViewObserver )_viewObs.elementAt( i );
			vvo.viewportViewChange( this , iv );
		}
	}

	public synchronized void addViewObserver( ViewportViewObserver obs )
	{
		if( !_viewObs.contains( obs ) )
			_viewObs.addElement( obs );
	}

	public synchronized void deleteViewObserver( ViewportViewObserver obs )
	{
		_viewObs.removeElement( obs );
	}

	// -- These implement the MouseInputListener interface --

	public void mousePressed( MouseEvent e )
	{
		_notifyMouseObs( e );
	}

	public void mouseDragged( MouseEvent e )
	{
		_notifyMouseObs( e );
	}

	public void mouseReleased( MouseEvent e )
	{
		_notifyMouseObs( e );
	}

	public void mouseMoved( MouseEvent e )
	{
		_notifyMouseObs( e );
	}

	public void mouseClicked( MouseEvent e )
	{
		_notifyMouseObs( e );
	}

	public void mouseEntered( MouseEvent e )
	{
		_notifyMouseObs( e );
	}

	public void mouseExited( MouseEvent e )
	{
		_notifyMouseObs( e );
	}
}

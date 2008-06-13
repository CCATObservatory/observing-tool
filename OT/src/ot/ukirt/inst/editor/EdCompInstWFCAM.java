/*==============================================================*/
/*                                                              */
/*                UK Astronomy Technology Centre                */
/*                 Royal Observatory, Edinburgh                 */
/*                 Joint Astronomy Centre, Hilo                 */
/*                   Copyright (c) PPARC 2003                   */
/*                                                              */
/*==============================================================*/
//
// Copyright 1997 Association for Universities for Research in Astronomy, Inc.,
// Observatory Control System, Gemini Telescopes Project.
// See the file COPYRIGHT for complete details.
//
// author: Alan Pickup = dap@roe.ac.uk         2003 March
//
package ot.ukirt.inst.editor ;

import orac.ukirt.inst.SpInstWFCAM ;

import gemini.sp.SpItem ;
import jsky.app.ot.gui.TextBoxWidgetExt ;
import jsky.app.ot.gui.TextBoxWidgetWatcher ;
import jsky.app.ot.gui.DropDownListBoxWidgetExt ;
import jsky.app.ot.gui.DropDownListBoxWidgetWatcher ;

import java.awt.event.ActionListener ;
import java.awt.event.ActionEvent ;

/**
 * This is the editor for the WFCAM instrument
 */
public final class EdCompInstWFCAM extends EdCompInstBase implements ActionListener
{
	private SpInstWFCAM _instWFCAM ;
	private boolean haveInitialised = false ;
	private WfcamGUI _w ;

	/**
	 * This flag is set true while _init is executed to prevent actionPerformed() to do react to
	 * action events caused by initializing widgets.
	 */

	/**
	 * The constructor initializes the title, description, and presentation source.
	 */
	public EdCompInstWFCAM()
	{
		_title = "WFCAM" ;
		_presSource = _w = new WfcamGUI() ;
		_description = "The WFCAM instrument is configured with this component." ;

		// ReadMode drop down list box
		_w.ReadMode.addWatcher( new DropDownListBoxWidgetWatcher()
		{
			public void dropDownListBoxSelect( DropDownListBoxWidgetExt dd , int i , String val ){}

			public void dropDownListBoxAction( DropDownListBoxWidgetExt dd , int i , String val )
			{
				_instWFCAM.setReadMode( val ) ;
				_updateReadMode() ;
			}
		} ) ;

		// Filter drop down list box
		_w.Filter.addWatcher( new DropDownListBoxWidgetWatcher()
		{
			public void dropDownListBoxSelect( DropDownListBoxWidgetExt dd , int i , String val ){}

			public void dropDownListBoxAction( DropDownListBoxWidgetExt dd , int i , String val )
			{
				_instWFCAM.setFilter( val ) ;
				_updateFilter() ;
			}
		} ) ;

		// Coadds text box
		_w.Coadds.addWatcher( new TextBoxWidgetWatcher()
		{
			public void textBoxKeyPress( TextBoxWidgetExt tbw )
			{
				try
				{
					String coaddsString = tbw.getText() ;
					int coadds = Integer.parseInt( coaddsString ) ;
					if( coadds > 0 )
						_instWFCAM.setCoadds( coadds ) ;
				}
				catch( Exception ex )
				{
					// ignore
				}
			}

			public void textBoxAction( TextBoxWidgetExt tbw ){} // ignore
		} ) ;

		//
		// Exposure time
		//
		_w.ExpTime.addWatcher( new TextBoxWidgetWatcher()
		{
			public void textBoxKeyPress( TextBoxWidgetExt tbw )
			{
				try
				{
					String ets = tbw.getText() ;
					double et = Double.parseDouble( ets ) ;
					if( et > 0.00001 )
						_instWFCAM.setExpTime( Double.toString( et ) ) ;
				}
				catch( Exception ex )
				{
					// ignore
				}
			}

			public void textBoxAction( TextBoxWidgetExt tbw ){} // ignore
		} ) ;
	}

	/** Return the position angle text box */
	public TextBoxWidgetExt getPosAngleTextBox()
	{
		// WFCAM does not have a position angle text box.
		return new TextBoxWidgetExt() ;
	}

	/** Return the exposure time text box */
	public TextBoxWidgetExt getExposureTimeTextBox()
	{
		return _w.ExpTime ;
	}

	/** Return the coadds text box, or null if not available. */
	public TextBoxWidgetExt getCoaddsTextBox()
	{
		return _w.Coadds ;
	}

	/**
	 * Override method in super class to avoid exposure time and position angle text box watchers
	 * being added twice.
	 */
	protected void _init(){}

	/**
	 * Override setup to store away a reference to the SpInstWFCAM item.
	 */
	public void setup( SpItem spItem )
	{
		_instWFCAM = ( SpInstWFCAM )spItem ;
		// Added by RDK
		_instWFCAM.avTableUpdate() ;
		//Edn of added by RDK
		haveInitialised = false ;
		super.setup( spItem ) ;
	}

	/**
	 * Implements the _updateWidgets method from OtItemEditor in order to
	 * setup the widgets to show the current values of the item.
	 */
	protected void _updateWidgets()
	{
		_updateWidgets( null ) ;
	}

	protected void _updateWidgets( Object source )
	{
		if( !haveInitialised )
		{
			// Load drop down lists only first time in
			_updateReadModeChoices() ;
			_updateReadMode() ;
			_updateFilterChoices() ;
			_updateFilter() ;
			_updateExpTime() ;
			_updateCoadds() ;
			haveInitialised = true ;
		}
	}

	//NEW...

	// Update the readMode choices
	private void _updateReadModeChoices()
	{
		String choices[] = new String[ _instWFCAM.getReadModeList().length ] ;
		choices = _instWFCAM.getReadModeList() ;
		_w.ReadMode.setChoices( choices ) ;
	}

	// Update the readMode
	private void _updateReadMode()
	{
		_w.ReadMode.setValue( _instWFCAM.getReadMode() ) ;
	}

	// Update the filter choices
	private void _updateFilterChoices()
	{
		String choices[] = new String[ _instWFCAM.getFilterList().length ] ;
		choices = _instWFCAM.getFilterList() ;
		_w.Filter.setChoices( choices ) ;
	}

	// Update the filter
	private void _updateFilter()
	{
		_w.Filter.setValue( _instWFCAM.getFilter() ) ;
	}

	// Update the exposure time
	private void _updateExpTime()
	{
		String expts = _instWFCAM.getExposureTimeString() ;
		_w.ExpTime.setText( expts ) ;
	}

	// Update the coadds
	private void _updateCoadds()
	{
		_w.Coadds.setText( _instWFCAM.getCoaddsString() ) ;
	}

	/**
	 *
	 */
	public void actionPerformed( ActionEvent evt )
	{
		Object w = evt.getSource() ;
		_updateWidgets( w ) ;
	}
}

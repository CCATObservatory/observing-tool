// Copyright 1997 Association for Universities for Research in Astronomy, Inc.,
// Observatory Control System, Gemini Telescopes Project.
// See the file COPYRIGHT for complete details.
//
// $Id$
//
package jsky.app.ot;

import jsky.app.ot.gui.CommandButtonWidgetExt;
import jsky.app.ot.gui.CommandButtonWidgetWatcher;
import jsky.app.ot.gui.ListBoxWidgetExt;
import jsky.app.ot.gui.OptionWidgetExt;
import jsky.app.ot.gui.StopActionWatcher;
import jsky.app.ot.gui.StopActionWidget;
import jsky.app.ot.gui.TextBoxWidgetExt;
import jsky.app.ot.gui.TextBoxWidgetWatcher;

import ot.util.DialogUtil;
import ot.gui.PasswordWidgetExt;

import java.awt.Component;
import java.awt.Frame;
import java.util.Vector;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.ButtonGroup;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JLayeredPane;

/**
 * A window that presents an interface used to obtain program listings and fetch programs from the ODB. This is a singleton class so at most one ProgListWindow will ever exist.
 */
public final class ProgListWindow extends RemoteGUI implements TextBoxWidgetWatcher , StopActionWatcher , ActionListener
{

	// The singleton ProgListWindowFrame or InternalFrame instance.
	private static Component _instance;

	public static final String LOGIN_PAGE = "loginPage";

	public static final String PROG_LIST_PAGE = "progListPage";

	public static final String LOGIN_PATH = "folderWidget.loginPage";

	public static final String PROG_LIST_PATH = "folderWidget.progListPage";

	private String _username;

	private Vector _progs;

	private ListBoxWidgetExt _progList;

	/**
	 * Get the singleton ProgListWindow JFrame or JInternalFrame.
	 */
	public static synchronized Component instance()
	{
		if( _instance == null )
		{
			if( OT.getDesktop() == null )
			{
				_instance = new ProgListWindowFrame();
			}
			else
			{
				_instance = new ProgListWindowInternalFrame();
				OT.getDesktop().add( ( JInternalFrame ) _instance , JLayeredPane.MODAL_LAYER );
			}
		}

		// make window popup in case it was iconified.
		if( _instance instanceof ProgListWindowFrame )
		{
			( ( ProgListWindowFrame ) _instance ).setState( java.awt.Frame.NORMAL );
		}

		return _instance;
	}

	//
	// Default constructor (not public). ProgListWindows are created by
	// calling the static "instance()" method.
	//
	public ProgListWindow()
	{
		// Select the PhaseII database by default
		OptionWidgetExt ow = phaseIIOption;
		ow.setValue( true );

		// Remember a reference to the "progList" ListBoxWidgetExt
		_progList = progList;

		// handle the login, fetch and close buttons
		loginButton.addWatcher( new CommandButtonWidgetWatcher()
		{
			public void commandButtonAction( CommandButtonWidgetExt cbwe )
			{
				if( stopAction.isBusy() )
					return;
				fetchListing();
			}
		} );
		fetchButton.addWatcher( new CommandButtonWidgetWatcher()
		{
			public void commandButtonAction( CommandButtonWidgetExt cbwe )
			{
				if( stopAction.isBusy() )
					return;
				fetchProg();
			}
		} );
		closeButton.addWatcher( new CommandButtonWidgetWatcher()
		{
			public void commandButtonAction( CommandButtonWidgetExt cbwe )
			{
				if( stopAction.isBusy() )
					return;
				_instance.setVisible( false );
			}
		} );

		ButtonGroup grp = new ButtonGroup();
		grp.add( phaseIIOption );
		grp.add( activeOption );
		activeOption.setEnabled( false );

		_init();
	}

	/**
	 * Show a particular page in this widget.
	 */
	public void gotoPage( String pageName )
	{
		int index = 0;
		if( pageName.equals( PROG_LIST_PAGE ) )
			index++;
		folderWidget.setSelectedIndex( index );
	}

	//
	// Return true if we need to fetch a new program list for the given
	// login information.
	//
	boolean _needToFetchList()
	{
		return _updateLoginInfo();
	}

	//
	// Initialize the textbox watchers.
	//
	private void _init()
	{
		// MFO
		passwordTextBox.addActionListener( this );

		keyTextBox.addActionListener( this );

		stopAction.addWatcher( this );
	}

	// Get the text in the username textbox.
	private String _getUsernameEntry()
	{
		TextBoxWidgetExt tb;
		tb = loginTextBox;
		return tb.getText().trim();
	}

	//
	// Get the most up-to-date login information. Returns true if never called
	// before or if anything changed since the last time called.
	//
	private boolean _updateLoginInfo()
	{
		boolean result = false;

		// Check the username
		if( _username == null )
		{
			_username = _getUsernameEntry();
			result = true;
		}
		else
		{
			String username = _getUsernameEntry();
			if( !_username.equals( username ) )
			{
				_username = username;
				result = true;
			}
		}

		return result;
	}

	//
	// Freeze user interactions with the window.
	//
	private void _freeze()
	{
		// For now, just set busy
		stopAction.setBusy();
		stopAction.actionsStarted();
	}

	/**
	 * Implementation of the StopActionWatcher interface.
	 * 
	 * @see StopActionWatcher
	 */
	public void stopAction( StopActionWidget saw )
	{
	}

	/**
	 * Contact the ODB and obtain a listing of available programs.
	 */
	public synchronized void fetchListing()
	{
		// Show the window if it isn't visible, and update the login info.
		if( !_instance.isVisible() )
		{
			_instance.setVisible( true );
		}
		if( _instance instanceof JFrame )
			( ( JFrame ) _instance ).setState( Frame.NORMAL );

		_updateLoginInfo();

		_freeze();
	}

	/**
	 * Fetch the selected program from the ODB.
	 */
	public synchronized void fetchProg()
	{
		// Get the selected name.
		String progName = _progList.getStringValue();
		if( progName == null )
		{
			DialogUtil.error( this , "You must select a program to fetch." );
			return;
		}

		// Now unformat it to get the spXXX id. The programs are listed either
		// as "spXXX" alone or "Arbitrary Title (spXXX)"
		if( progName.charAt( progName.length() - 1 ) == ')' )
		{
			int index = progName.lastIndexOf( '(' );
			progName = progName.substring( index + 1 , progName.length() - 1 );
		}

		// Get the key.
		// MFO
		PasswordWidgetExt tbw;
		tbw = keyTextBox;
		String key = new String( tbw.getPassword() ).trim();
		if( ( key == null ) || ( key.equals( "" ) ) )
		{
			DialogUtil.error( this , "You must specify the program's key." );
			return;
		}

		_freeze();
	}

	/**
	 * fill in login fields if necessary and show the proper page;
	 */
	public void updateWindow()
	{
		if( _progs == null )
		{
			gotoPage( LOGIN_PAGE );
		}
	}

	/**
	 * Part of the TextBoxWidgetWatcher interface that must be implemented, but we aren't interested in.
	 */
	public void textBoxKeyPress( TextBoxWidgetExt tbwe )
	{
	}

	/**
	 * This method implements part of the TextBoxWidgetWatcher interface from gemini.gui.
	 */
	public void textBoxAction( TextBoxWidgetExt tbwe )
	{
	}

	/**
	 * Receive notification when a PasswordWidgetExt that we are interrested in has the return key pressed.
	 * 
	 * This functionality used to be in textBoxAction. MFO: May 28, 2001
	 */
	public void actionPerformed( ActionEvent e )
	{
		if( e.getSource() == passwordTextBox )
		{
			fetchListing();
		}
		else if( e.getSource() == keyTextBox )
		{
			fetchProg();
		}
	}
}

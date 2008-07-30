/*
 * Copyright 2000 Association for Universities for Research in Astronomy, Inc.,
 * Observatory Control System, Gemini Telescopes Project.
 *
 * $Id$
 */

package jsky.app.ot ;

import javax.swing.JButton ;
import javax.swing.ImageIcon ;
import jsky.util.gui.GenericToolBar ;

import jsky.app.ot.gui.DrawUtil ;

/** 
 * A tool bar for the OT tree window.
 */
public class OtTreeToolBar extends GenericToolBar
{
	/** The target science program editor */
	protected OtWindow editor ;

	// toolbar buttons
	protected JButton obsFolderButton ;
	protected JButton obsGroupButton ;
	protected JButton observationButton ;
	protected JButton componentMenuButton ;
	protected JButton noteButton ;
	protected JButton libFolderButton ;
	protected JButton iterCompMenuButton ;
	protected JButton iterObsMenuButton ;

	// OMP buttons
	// added by MFO (06 July 2001)
	protected JButton msbFolderButton ;
	protected JButton andFolderButton ;
	protected JButton orFolderButton ;
	protected JButton surveyButton ;

	/**
	 * Create a toolbar with tree related actions for the given OT window.
	 */
	public OtTreeToolBar( OtWindow editor )
	{
		super( editor , false , VERTICAL ) ;
		setFloatable( false ) ;
		this.editor = editor ;
		showPictures = true ;
		showText = true ;
		addToolBarItems() ;
	}

	/** 
	 * Add the items to the tool bar.
	 */
	protected void addToolBarItems()
	{
		// added by MFO (06 July 2001)
		add( makeOrFolderButton() ) ;
		addSeparator() ;
		add( makeAndFolderButton() ) ;
		addSeparator() ;
		add( makeSurveyButton() ) ;
		addSeparator() ;
		add( makeMsbFolderButton() ) ;

		addSeparator() ;
		add( makeObservationButton() ) ;
		addSeparator() ;
		add( makeNoteButton() ) ;
		addSeparator() ;
		add( makeLibFolderButton() ) ;
		addSeparator() ;

		add( makeComponentMenuButton() ) ;
		addSeparator() ;
		add( makeIterCompMenuButton() ) ;
		addSeparator() ;
		add( makeIterObsMenuButton() ) ;
	}

	/**
	 * Make the Observation Folder button, if it does not yet exists. Otherwise update the display
	 * using the current options for displaying text or icons.
	 *
	 * @return the ObsFolder button
	 */
	protected JButton makeObsFolderButton()
	{
		if( obsFolderButton == null )
			obsFolderButton = makeButton( "Create an observation folder." , editor.getObsFolderAction() , false ) ;

		updateButton( obsFolderButton , "Folder" , new ImageIcon( DrawUtil.getResourceURL( "jsky/app/ot/images/obsFolder.gif" ) ) ) ;
		return obsFolderButton ;
	}

	/**
	 * Make the Observation Group button, if it does not yet exists. Otherwise update the display
	 * using the current options for displaying text or icons.
	 *
	 * @return the ObsGroup button
	 */
	protected JButton makeObsGroupButton()
	{
		if( obsGroupButton == null )
			obsGroupButton = makeButton( "Create an observation group." , editor.getObsGroupAction() , false ) ;

		updateButton( obsGroupButton , "Group" , new ImageIcon( DrawUtil.getResourceURL( "jsky/app/ot/images/obsGroup.gif" ) ) ) ;
		return obsGroupButton ;
	}

	/**
	 * Make the Observation button, if it does not yet exists. Otherwise update the display
	 * using the current options for displaying text or icons.
	 *
	 * @return the Observation button
	 */
	protected JButton makeObservationButton()
	{
		if( observationButton == null )
			observationButton = makeButton( "Create an observation." , editor.getObservationAction() , false ) ;

		updateButton( observationButton , "Observation" , new ImageIcon( DrawUtil.getResourceURL( "jsky/app/ot/images/observation.gif" ) ) ) ;
		return observationButton ;
	}

	/**
	 * Make the Observation Component menu button, if it does not yet exists. Otherwise update the display
	 * using the current options for displaying text or icons.
	 *
	 * @return the Observation Component menu button
	 */
	protected JButton makeComponentMenuButton()
	{
		if( componentMenuButton == null )
			componentMenuButton = makeMenuButton( "Create an observation component." , new OtCompPopupMenu( editor.getTreeWidget() ) ) ;

		updateButton( componentMenuButton , "Component" , new ImageIcon( DrawUtil.getResourceURL( "jsky/app/ot/images/component.gif" ) ) ) ;
		return componentMenuButton ;
	}

	/**
	 * Make the Note button, if it does not yet exists. Otherwise update the display
	 * using the current options for displaying text or icons.
	 *
	 * @return the Note button
	 */
	protected JButton makeNoteButton()
	{
		if( noteButton == null )
			noteButton = makeButton( "Create a note." , editor.getNoteAction() , false ) ;

		updateButton( noteButton , "Note" , new ImageIcon( DrawUtil.getResourceURL( "jsky/app/ot/images/note-tiny.gif" ) ) ) ;
		return noteButton ;
	}

	/**
	 * Make the LibFolder button, if it does not yet exists. Otherwise update the display
	 * using the current options for displaying text or icons.
	 *
	 * @return the LibFolder button
	 */
	protected JButton makeLibFolderButton()
	{
		if( libFolderButton == null )
			libFolderButton = makeButton( "Create a library folder." , editor.getLibFolderAction() , false ) ;

		updateButton( libFolderButton , "Library" , new ImageIcon( DrawUtil.getResourceURL( "jsky/app/ot/images/libFolder.gif" ) ) ) ;
		return libFolderButton ;
	}

	/**
	 * Make the Iterator Component menu button, if it does not yet exists. Otherwise update the display
	 * using the current options for displaying text or icons.
	 *
	 * @return the Iterator Component menu button
	 */
	protected JButton makeIterCompMenuButton()
	{
		if( iterCompMenuButton == null )
			iterCompMenuButton = makeMenuButton( "Create an iterator component." , new OtIterCompPopupMenu( editor.getTreeWidget() ) ) ;

		updateButton( iterCompMenuButton , "Iterator" , new ImageIcon( DrawUtil.getResourceURL( "jsky/app/ot/images/iterComp.gif" ) ) ) ;
		return iterCompMenuButton ;
	}

	/**
	 * Make the Observe Iterator menu button, if it does not yet exists. Otherwise update the display
	 * using the current options for displaying text or icons.
	 *
	 * @return the Observe Iterator menu button.
	 */
	protected JButton makeIterObsMenuButton()
	{
		if( iterObsMenuButton == null )
			iterObsMenuButton = makeMenuButton( "Create an observation iterator." , new OtIterObsPopupMenu( editor.getTreeWidget() ) ) ;

		updateButton( iterObsMenuButton , "Observe" , new ImageIcon( DrawUtil.getResourceURL( "jsky/app/ot/images/iterObs.gif" ) ) ) ;
		return iterObsMenuButton ;
	}

	// The following three functions were added fir the OMP project.
	// (MFO, 09 July 2001)

	/**
	 * Make the MSB Folder button (OMP project), if it does not yet exists.
	 * Otherwise update the display
	 * using the current options for displaying text or icons.
	 *
	 * @return the MSB Folder button
	 */
	protected JButton makeMsbFolderButton()
	{
		if( msbFolderButton == null )
			msbFolderButton = makeButton( "Create an MSB folder." , editor.getMsbFolderAction() , false ) ;

		updateButton( msbFolderButton , "MSB Folder" , new ImageIcon( DrawUtil.getResourceURL( "ot/images/msbFolder.gif" ) ) ) ;
		return msbFolderButton ;
	}

	/**
	 * Make the AND Folder button (OMP project), if it does not yet exists.
	 * Otherwise update the display
	 * using the current options for displaying text or icons.
	 *
	 * @return the AND Folder button
	 */
	protected JButton makeAndFolderButton()
	{
		if( andFolderButton == null )
			andFolderButton = makeButton( "Create an AND folder." , editor.getAndFolderAction() , false ) ;

		updateButton( andFolderButton , "AND Folder" , new ImageIcon( DrawUtil.getResourceURL( "ot/images/andFolder.gif" ) ) ) ;
		return andFolderButton ;
	}

	/**
	 * Make the OR Folder button (OMP project), if it does not yet exists.
	 * Otherwise update the display
	 * using the current options for displaying text or icons.
	 *
	 * @return the OR Folder button
	 */
	protected JButton makeOrFolderButton()
	{
		if( orFolderButton == null )
			orFolderButton = makeButton( "Create an OR folder." , editor.getOrFolderAction() , false ) ;

		updateButton( orFolderButton , "OR Folder" , new ImageIcon( DrawUtil.getResourceURL( "ot/images/orFolder.gif" ) ) ) ;
		return orFolderButton ;
	}

	/**
	 * Make the Survey Folder button (OMP project), if it does not yet exists.
	 * Otherwise update the display
	 * using the current options for displaying text or icons.
	 *
	 * @return the OR Folder button
	 */
	protected JButton makeSurveyButton()
	{
		if( surveyButton == null )
			surveyButton = makeButton( "Create a Survey Container." , editor.getSurveyFolderAction() , false ) ;

		updateButton( surveyButton , "Survey Container" , new ImageIcon( DrawUtil.getResourceURL( "ot/images/surveyContainer.gif" ) ) ) ;
		return surveyButton ;
	}

	/**
	 * Update the toolbar display using the current text/pictures options.
	 * (redefined from the parent class).
	 */
	public void update()
	{
		// added by MFO (06 July 2001)
		makeMsbFolderButton() ;
		makeAndFolderButton() ;
		makeOrFolderButton() ;

		makeObservationButton() ;
		makeComponentMenuButton() ;
		makeNoteButton() ;
		makeLibFolderButton() ;
		makeIterCompMenuButton() ;
		makeIterObsMenuButton() ;
	}
}

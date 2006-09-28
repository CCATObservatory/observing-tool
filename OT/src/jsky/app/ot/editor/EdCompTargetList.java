// Copyright 1997 Association for Universities for Research in Astronomy, Inc.,
// Observatory Control System, Gemini Telescopes Project.
// See the file COPYRIGHT for complete details.
//
//
package jsky.app.ot.editor;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.Component;
import java.util.Arrays;
import javax.swing.JLabel;
import javax.swing.JTabbedPane;
import javax.swing.JPanel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JOptionPane;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;
import jsky.app.ot.gui.DropDownListBoxWidgetExt;
import jsky.app.ot.gui.DropDownListBoxWidgetWatcher;
import jsky.app.ot.gui.TableWidgetExt;
import jsky.app.ot.gui.TableWidgetWatcher;
import jsky.app.ot.gui.TextBoxWidgetExt;
import jsky.app.ot.gui.TextBoxWidgetWatcher;
import gemini.sp.SpTelescopePos;
import gemini.sp.SpTelescopePosList;
import gemini.sp.obsComp.SpTelescopeObsComp;
import jsky.app.ot.tpe.TelescopePosEditor;
import jsky.app.ot.tpe.TpeManager;
import jsky.app.ot.util.Assert;
import gemini.util.CoordSys;
import gemini.util.TelescopePos;
import gemini.util.TelescopePosWatcher;
import jsky.util.gui.ProgressException;
import orac.util.TelescopeUtil;
import ot.util.DialogUtil;
import ot.OtConstants;
import ot.util.NameResolver;
import jsky.app.ot.OtCfg;

import ot.util.Horizons ;
import java.util.TreeMap ;
import java.util.Vector ;
import ot.ReportBox ; 


// MFO, June 06, 2002:
//   At the moment the only supported type is MAJOR. So the DropDownListBoxWidgetExt namedSystemType
//   has been commented out for now. I have not removed it completely from the code in case it is
//   needed in the future.
//   A DropDownListBoxWidgetExt with the available named target choices has been added.

// Conic System (Orbital Elements), Named System (named target such as planets, sun , moon etc)
// and Target specification using offsets added.
// Martin Folger (M.Folger@roe.ac.uk) February 27, 2002
/**
 * This is the editor for the target list component.
 */
public class EdCompTargetList extends OtItemEditor
    implements TelescopePosWatcher, TableWidgetWatcher, ActionListener,
               ChangeListener, TextBoxWidgetWatcher, DropDownListBoxWidgetWatcher, OtConstants {

    /** String used in DropDownListBoxWidgetExt namedTarget. */
    private static final String SELECT_TARGET = "<Select Target>";

    public static final String LABEL_RA     = "Ra";
    public static final String LABEL_DEC    = "Dec";

    // Frequently used widgets
    private DropDownListBoxWidgetExt _tag;	// Object ID/Type
    private DropDownListBoxWidgetExt _type;     // Object type
    protected TextBoxWidgetExt       _name;	// Object Name
    protected TextBoxWidgetExt       _xaxis;	// RA/Az
    protected TextBoxWidgetExt       _yaxis;	// Del/El
    protected DropDownListBoxWidgetExt _system;	// Coordinate System

    private TelescopePosTableWidget _tpTable;

    protected TelescopeGUI          _w;         // the GUI layout panel

    protected SpTelescopePos     _curPos;	// Position being edited
    private SpTelescopePosList _tpl;	// List of positions being edited

    private String [] _guideTags;

    private NameResolverFeedback _nameResolverFeedback;

    private boolean _targetSystemsChange = false;

    private boolean _resolving = false ;
    
    private boolean jcmtot = false ;
    
    /**
     * The constructor initializes the title, description, and presentation source.
     */
    public EdCompTargetList()
	{
		_title = "Target Information";
		_presSource = _w = new TelescopeGUI();
		_description = "Use this editor to enter the target information.";
		
		jcmtot = OtCfg.telescopeUtil instanceof orac.jcmt.util.JcmtUtil ;

		// Init name resolver drop down (MFO, May29, 2001)
		String[] catalogs = OtCfg.getNameResolvers();

		// Adding catalogs as follows:
		// available catalog 1
		// available catalog 2
		// ...
		// ---------------------
		// unavailable catalog 1
		// unavailable catalog 2
		// ...
		if( catalogs != null )
		{
			for( int i = 0 ; i < catalogs.length ; i++ )
			{
				if( NameResolver.isAvailableAsCatalog( catalogs[ i ] ) )
				{
					_w.nameResolversDDLBW.addItem( catalogs[ i ] );
				}
			}
		}
		
		if( !OtCfg.telescopeUtil.supports( TelescopeUtil.FEATURE_TARGET_INFO_PROP_MOTION ) )
		{
			_w.extrasFolder.setEnabledAt( 0 , false );
			// Disable the contents as well
			JPanel jp = ( JPanel ) _w.extrasFolder.getComponentAt( 0 );
			for( int i = 0 ; i < jp.getComponentCount() ; i++ )
			{
				if( jp.getComponent( i ) instanceof TextBoxWidgetExt )
				{
					( ( TextBoxWidgetExt ) jp.getComponent( i ) ).setEditable( false );
				}
				else
				{
					jp.getComponent( i ).setEnabled( false );
				}
			}
			_w.extrasFolder.setSelectedIndex( 1 );
		}

		if( !OtCfg.telescopeUtil.supports( TelescopeUtil.FEATURE_TARGET_INFO_TRACKING ) )
		{
			_w.extrasFolder.setEnabledAt( 1 , false );
			// Disable the contents as well
			JPanel jp = ( JPanel ) _w.extrasFolder.getComponentAt( 1 );
			for( int i = 0 ; i < jp.getComponentCount() ; i++ )
			{
				if( jp.getComponent( i ) instanceof TextBoxWidgetExt )
				{
					( ( TextBoxWidgetExt ) jp.getComponent( i ) ).setEditable( false );
				}
				else
				{
					jp.getComponent( i ).setEnabled( false );
				}
			}
			_w.extrasFolder.setSelectedIndex( 2 );
		}

		if( !OtCfg.telescopeUtil.supports( TelescopeUtil.FEATURE_TARGET_INFO_CHOP ) )
		{
			_w.extrasFolder.setEnabledAt( 2 , false );
			// Disable the contents as well
			JPanel jp = ( JPanel ) _w.extrasFolder.getComponentAt( 2 );
			for( int i = 0 ; i < jp.getComponentCount() ; i++ )
			{
				if( jp.getComponent( i ) instanceof TextBoxWidgetExt )
				{
					( ( TextBoxWidgetExt ) jp.getComponent( i ) ).setEditable( false );
				}
				else
				{
					jp.getComponent( i ).setEnabled( false );
				}
			}
			_w.extrasFolder.setSelectedIndex( 0 );
		}

		_w.setBaseButton.setText( "Set " + SpTelescopePos.BASE_TAG + " To Image Centre" );

		if( _w.setBaseButton.getText().length() > 24 )
		{
			_w.setBaseButton.setFont( new java.awt.Font( "Dialog" , 0 , 10 ) );
		}

		// UKIRT does not need the chopSystem choice and JCMT does not use the
		// Chop Settings tab. (MFO, 21 January 2002)
		_w.chopSystemLabel.setVisible( false );
		_w.chopSystem.setVisible( false );

		// Set Tool Tip text.
		_w.anode.setToolTipText( "Longitude of the ascending node" );
		_w.aorq.setToolTipText( "Mean distance (a) or perihelion distance (q)" );
		_w.e.setToolTipText( "Orbital Eccentricity" );
		_w.perih.setToolTipText( "Argument or Longitude of perihelion" );
		_w.orbinc.setToolTipText( "Inclination of the orbit " );
		_w.epoch.setToolTipText( "Epoch of the orbital elements" );
		_w.epochPerih.setToolTipText( "Time of perihelion passage" );
		_w.l_or_m.setToolTipText( "Longitude or mean anomaly" );
		_w.dm.setToolTipText( "Daily motion" );

		_w.conicSystemType.setChoices( SpTelescopePos.CONIC_SYSTEM_TYPES_DESCRIPTION );
		_w.namedTarget.setChoices( OtCfg.getNamedTargets() );
		_w.namedTarget.addChoice( SELECT_TARGET );

		// *** buttons
		_w.newButton.addActionListener( this );
		_w.removeButton.addActionListener( this );
		_w.plotButton.addActionListener( this );
		_w.setBaseButton.addActionListener( this );
		_w.resolveButton.addActionListener( this );

		// chop mode tab added by MFO (3 August 2001)
		_w.chopping.addActionListener( this );
		_w.offsetCheckBox.addActionListener( this );

		//
		_w.targetSystemsTabbedPane.addChangeListener( this );

		_w.epoch.addWatcher( this );
		_w.epochPerih.addWatcher( this );
		_w.orbinc.addWatcher( this );
		_w.anode.addWatcher( this );
		_w.perih.addWatcher( this );
		_w.aorq.addWatcher( this );
		_w.e.addWatcher( this );
		_w.l_or_m.addWatcher( this );
		_w.dm.addWatcher( this );
		_w.conicSystemType.addWatcher( this );
		_w.namedTarget.addWatcher( this );

		_w.resolveOrbitalElementButton.addActionListener( this );

		_type = _w.targetTypeDDList;
		for( int i = 0 ; i < _w.targetSystemsTabbedPane.getTabCount() ; i++ )
		{
			_type.addChoice( _w.targetSystemsTabbedPane.getTitleAt( i ) );
		}
		_type.addWatcher( new DropDownListBoxWidgetWatcher()
		{
			public void dropDownListBoxSelect( DropDownListBoxWidgetExt dd , int i , String val )
			{
				for( int tab = 0 ; tab < _w.targetSystemsTabbedPane.getTabCount() ; tab++ )
				{
					if( tab == i )
					{
						_w.targetSystemsTabbedPane.setEnabledAt( tab , true );
						_w.targetSystemsTabbedPane.setSelectedIndex( tab );
						Component[] component = ( ( JPanel ) _w.targetSystemsTabbedPane.getComponentAt( tab ) ).getComponents();
						for( int count = 0 ; count < component.length ; count++ )
						{
							component[ count ].setEnabled( true );
						}
					}
					else
					{
						_w.targetSystemsTabbedPane.setEnabledAt( tab , false );
					}
				}
			}

			public void dropDownListBoxAction( DropDownListBoxWidgetExt dd , int i , String newTag )
			{
				for( int tab = 0 ; tab < _w.targetSystemsTabbedPane.getTabCount() ; tab++ )
				{
					if( tab == i )
					{
						_w.targetSystemsTabbedPane.setEnabledAt( tab , true );
						_w.targetSystemsTabbedPane.setSelectedIndex( tab );
						Component[] component = ( ( JPanel ) _w.targetSystemsTabbedPane.getComponentAt( tab ) ).getComponents();
						for( int count = 0 ; count < component.length ; count++ )
						{
							component[ count ].setEnabled( true );
						}
					}
					else
					{
						_w.targetSystemsTabbedPane.setEnabledAt( tab , false );
					}
				}
			}
		} );

		// Get a reference to the "Tag" drop down, and initialize its choices
		_tag = _w.tagDDLBW;
		_tag.addChoice( SpTelescopePos.BASE_TAG );

		_guideTags = SpTelescopePos.getGuideStarTags();

		if( _guideTags != null && _guideTags.length > 0 )
		{
			_w.newButton.removeActionListener( this );
			_w.newButton.setModel( new DefaultComboBoxModel( _guideTags ) );
			_w.newButton.addActionListener( this );
		}

		// User tags are not used at the moment. (MFO, 19 Decemtber 2001)

		_tag.addWatcher( new DropDownListBoxWidgetWatcher()
		{
			public void dropDownListBoxSelect( DropDownListBoxWidgetExt dd , int i , String val )
			{
			}

			public void dropDownListBoxAction( DropDownListBoxWidgetExt dd , int i , String newTag )
			{
				_changeTag( newTag );
			}
		} );

		_name = _w.nameTBW;
		_name.addWatcher( new TextBoxWidgetWatcher()
		{
			public void textBoxKeyPress( TextBoxWidgetExt tbwe )
			{
				_curPos.deleteWatcher( EdCompTargetList.this );
				_curPos.setName( tbwe.getText() );
				_curPos.deleteWatcher( EdCompTargetList.this );
			}

			public void textBoxAction( TextBoxWidgetExt tbwe )
			{
			}
		} );

		_xaxis = _w.xaxisTBW;
		_xaxis.addWatcher( new TextBoxWidgetWatcher()
		{
			public void textBoxKeyPress( TextBoxWidgetExt tbwe )
			{
				_curPos.deleteWatcher( EdCompTargetList.this );

				if( ( _curPos.isOffsetPosition() && !_curPos.isBasePosition() ) || ( ( _curPos.getCoordSys() != CoordSys.FK5 ) && ( _curPos.getCoordSys() != CoordSys.FK4 ) && ( _curPos.getCoordSys() != CoordSys.HADEC ) ) )
				{
					double xAxis = 0.0;
					double yAxis = 0.0;

					try
					{
						xAxis = Double.parseDouble( _xaxis.getText() );
					}
					catch( Exception e )
					{
						System.out.println( "Could not parse x axis: " + _xaxis.getText() );
					}

					try
					{
						yAxis = Double.parseDouble( _yaxis.getText() );
					}
					catch( Exception e )
					{
						System.out.println( "Could not parse y axis: " + _yaxis.getText() );
					}

					_curPos.setXY( xAxis , yAxis );
				}
				else
				{
					// Make sure it matches the pattern D:DD:DD.DDD
					String pattern = "\\d{1,2}(:\\d{0,2}(:\\d{0,2}(\\.\\d*)?)?)?";
					if( tbwe.getText().matches( pattern ) )
					{
						String currentXString = _curPos.getXaxisAsString();
						String newXString = tbwe.getText();
						if( !newXString.equals( currentXString ) )
							_curPos.setXYFromString( newXString , null );
					}
					else
					{
						// Show an error if the field is not blank and does not end in a :
						if( tbwe.getText().length() > 0 )
						{
							JOptionPane.showMessageDialog( _w , "x-axis must only contain numbers and fields must be : separated" , "Bad format" , JOptionPane.ERROR_MESSAGE );
						}
					}
				}

				_curPos.addWatcher( EdCompTargetList.this );

				_resetPositionEditor();
			}

			public void textBoxAction( TextBoxWidgetExt tbwe )
			{
			}
		} );

		_yaxis = _w.yaxisTBW;
		_yaxis.addWatcher( new TextBoxWidgetWatcher()
		{
			public void textBoxKeyPress( TextBoxWidgetExt tbwe )
			{
				_curPos.deleteWatcher( EdCompTargetList.this );

				if( ( _curPos.isOffsetPosition() && !_curPos.isBasePosition() ) || ( ( _curPos.getCoordSys() != CoordSys.FK5 ) && ( _curPos.getCoordSys() != CoordSys.FK4 ) && ( _curPos.getCoordSys() != CoordSys.HADEC ) ) )
				{
					double xAxis = 0.0;
					double yAxis = 0.0;

					try
					{
						xAxis = Double.parseDouble( _xaxis.getText() );
					}
					catch( Exception e )
					{
						System.out.println( "Could not parse x axis: " + _xaxis.getText() );
					}

					try
					{
						yAxis = Double.parseDouble( _yaxis.getText() );
					}
					catch( Exception e )
					{
						System.out.println( "Could not parse y axis :" + _yaxis.getText() );
					}

					_curPos.setXY( xAxis , yAxis );

				}
				else
				{
					// Make sure it matches the pattern D:DD:DD.DDD
					String pattern = "^(\\+|-)?\\d{1,2}((:| )\\d{0,2}((:| )\\d{0,2}(\\.\\d*)?)?)?";
					if( tbwe.getText().matches( pattern ) )
					{
						_curPos.setXYFromString( null , tbwe.getText() );
					}
					else
					{
						// Show an error if the field is not blank and does not end in a :
						if( tbwe.getText().length() > 0 )
						{
							JOptionPane.showMessageDialog( _w , "y-axis must only contain numbers and fields must be : separated" , "Bad format" , JOptionPane.ERROR_MESSAGE );
						}
					}
				}

				_curPos.addWatcher( EdCompTargetList.this );

				_resetPositionEditor();
			}

			public void textBoxAction( TextBoxWidgetExt tbwe )
			{
			}
		} );

		_system = _w.systemDDLBW;
		_system.setChoices( OtCfg.telescopeUtil.getCoordSys() );
		_system.addWatcher( new DropDownListBoxWidgetWatcher()
		{
			public void dropDownListBoxSelect( DropDownListBoxWidgetExt dd , int i , String val )
			{
			}

			public void dropDownListBoxAction( DropDownListBoxWidgetExt dd , int i , String val )
			{

				_w.RA_Az_STW.setText( CoordSys.X_AXIS_LABEL[ i ] );
				_w.Dec_El_STW.setText( CoordSys.Y_AXIS_LABEL[ i ] );

				_updateCoordSystem();

				// The call _curPos.setXY() triggers a base position observer
				// notification so that the position editor gets updated
				// and it causes the String representation in the SpAvTable
				// of _curPos to be reformatted depending on the coordinate system
				// and whether _curPos is an offset position.
				_curPos.setXY( _curPos.getXaxis() , _curPos.getYaxis() );

				_updateXYUnitsLabels();

				_resetPositionEditor();
			}
		} );

		_w.velDefn.setChoices( TelescopeUtil.TCS_RV_DEFINITIONS );
		_w.velDefn.addWatcher( new DropDownListBoxWidgetWatcher()
		{
			public void dropDownListBoxSelect( DropDownListBoxWidgetExt dd , int i , String val )
			{
			}

			public void dropDownListBoxAction( DropDownListBoxWidgetExt dd , int i , String newTag )
			{
				_curPos.setTrackingRadialVelocityDefn( newTag );
				if( jcmtot )
				{
					SpTelescopePos refPos = ( SpTelescopePos )_tpl.getPosition( "REFERENCE" ) ;
					if( refPos != null )
						refPos.setTrackingRadialVelocityDefn( newTag ) ;
				}
			}
		} );

		_w.velFrame.setChoices( TelescopeUtil.TCS_RV_FRAMES );
		_w.velFrame.addWatcher( new DropDownListBoxWidgetWatcher()
		{
			public void dropDownListBoxSelect( DropDownListBoxWidgetExt dd , int i , String val )
			{
			}

			public void dropDownListBoxAction( DropDownListBoxWidgetExt dd , int i , String newTag )
			{
				_curPos.setTrackingRadialVelocityFrame( newTag );
				if( jcmtot )
				{
					SpTelescopePos refPos = ( SpTelescopePos )_tpl.getPosition( "REFERENCE" ) ;
					if( refPos != null )
						refPos.setTrackingRadialVelocityFrame( newTag ) ;
				}
			}
		} );

		JTabbedPane fwe;
		TextBoxWidgetExt tbwe;

		// *** The "extras" folder
		fwe = _w.extrasFolder;

		// --- Proper Motion Page
		tbwe = _w.propMotionRATBW;
		tbwe.addWatcher( new TextBoxWidgetWatcher()
		{
			public void textBoxKeyPress( TextBoxWidgetExt tbwe )
			{
				_curPos.deleteWatcher( EdCompTargetList.this );
				_curPos.setPropMotionRA( tbwe.getText() );
				_curPos.deleteWatcher( EdCompTargetList.this );
			}

			public void textBoxAction( TextBoxWidgetExt tbwe )
			{
			}
		} );

		tbwe = _w.propMotionDecTBW;
		tbwe.addWatcher( new TextBoxWidgetWatcher()
		{
			public void textBoxKeyPress( TextBoxWidgetExt tbwe )
			{
				_curPos.deleteWatcher( EdCompTargetList.this );
				_curPos.setPropMotionDec( tbwe.getText() );
				_curPos.deleteWatcher( EdCompTargetList.this );
			}

			public void textBoxAction( TextBoxWidgetExt tbwe )
			{
			}
		} );

		// --- Tracking Details Page

		tbwe = _w.detailsEpochTBW;
		tbwe.addWatcher( new TextBoxWidgetWatcher()
		{
			public void textBoxKeyPress( TextBoxWidgetExt tbwe )
			{
				_curPos.deleteWatcher( EdCompTargetList.this );
				_curPos.setTrackingEpoch( tbwe.getText() );
				_curPos.deleteWatcher( EdCompTargetList.this );
			}

			public void textBoxAction( TextBoxWidgetExt tbwe )
			{
			}
		} );

		tbwe = _w.detailsParallaxTBW;
		tbwe.addWatcher( new TextBoxWidgetWatcher()
		{
			public void textBoxKeyPress( TextBoxWidgetExt tbwe )
			{
				_curPos.deleteWatcher( EdCompTargetList.this );
				_curPos.setTrackingParallax( tbwe.getText() );
				_curPos.deleteWatcher( EdCompTargetList.this );
			}

			public void textBoxAction( TextBoxWidgetExt tbwe )
			{
			}
		} );

		tbwe = _w.velValue;
		tbwe.addWatcher( new TextBoxWidgetWatcher()
		{
			public void textBoxKeyPress( TextBoxWidgetExt tbwe )
			{
				_curPos.deleteWatcher( EdCompTargetList.this );
				String newValue = tbwe.getText() ;
				_curPos.setTrackingRadialVelocity( tbwe.getText() );
				if( jcmtot )
				{
					SpTelescopePos refPos = ( SpTelescopePos )_tpl.getPosition( "REFERENCE" ) ;
					if( refPos != null )
						refPos.setTrackingRadialVelocity( newValue ) ;
				}
				_curPos.deleteWatcher( EdCompTargetList.this );
			}

			public void textBoxAction( TextBoxWidgetExt tbwe )
			{
			}
		} );

		if( jcmtot )
		{
			_w.XYOffsetPanel.setVisible( false ) ;
		}
		else
		{
			tbwe = _w.baseXOff;
			tbwe.addWatcher( new TextBoxWidgetWatcher()
			{
				public void textBoxKeyPress( TextBoxWidgetExt tbwe )
				{
					_curPos.deleteWatcher( EdCompTargetList.this );
					_curPos.setBaseXOffset( tbwe.getText() );
					_curPos.deleteWatcher( EdCompTargetList.this );
				}
	
				public void textBoxAction( TextBoxWidgetExt tbwe )
				{
				}
			} );
	
			tbwe = _w.baseYOff;
			tbwe.addWatcher( new TextBoxWidgetWatcher()
			{
				public void textBoxKeyPress( TextBoxWidgetExt tbwe )
				{
					_curPos.deleteWatcher( EdCompTargetList.this );
					_curPos.setBaseYOffset( tbwe.getText() );
					_curPos.deleteWatcher( EdCompTargetList.this );
				}
	
				public void textBoxAction( TextBoxWidgetExt tbwe )
				{
				}
			} );
		}

		// *** Position Table
		_tpTable = _w.positionTable;

		// If there are more then 2 systems (FK5/FK4) then display the system of a target
		// in a separate column in the target list table.
		if( ( OtCfg.telescopeUtil.getCoordSys() != null ) && ( OtCfg.telescopeUtil.getCoordSys().length > 2 ) )
		{
			_tpTable.setColumnHeaders( new String[]
			{ "Tag" , "Name" , "X Axis" , "Y Axis" , "System" } );
			_tpTable.setColumnWidths( new int[]
			{ 45 , 85 , 90 , 90 , 90 } );
		}
		else
		{
			_tpTable.setColumnHeaders( new String[]
			{ "Tag" , "Name" , "X Axis" , "Y Axis" } );
			_tpTable.setColumnWidths( new int[]
			{ 45 , 85 , 90 , 90 } );
		}
		_tpTable.setRowSelectionAllowed( true );
		_tpTable.setColumnSelectionAllowed( false );
		_tpTable.addWatcher( this );

		// chop mode tab added by MFO (3 August 2001)
		_w.chopThrow.addWatcher( new TextBoxWidgetWatcher()
		{
			public void textBoxKeyPress( TextBoxWidgetExt tbwe )
			{
				// MFO TODO: see TODO in SpTelescopeObsComp.
				( ( SpTelescopeObsComp ) _spItem ).setChopThrow( _w.chopThrow.getText() );
				// MFO TODO: see TODO in SpTelescopeObsComp. Check whether deleteWatcher or addWatcher
				// is needed. deleteWatcher is used twice in all the cases but that is probably a bug.
			}

			public void textBoxAction( TextBoxWidgetExt tbwe )
			{
			}
		} );

		_w.chopAngle.addWatcher( new TextBoxWidgetWatcher()
		{
			public void textBoxKeyPress( TextBoxWidgetExt tbwe )
			{
				// MFO TODO: see TODO in SpTelescopeObsComp.

				// chop angle range is checked here rather than in
				// gemini.sp.obsComp.SpTelescopeObsComp.setChopAngle() because it is telescope specific.
				// SpTelescopeObsComp however is generic and does not contain telescope specific code.
				// EdCompTargetList on the other hand contains telescope specific code already.
				( ( SpTelescopeObsComp ) _spItem ).setChopAngle( validateChopAngle( _w.chopAngle.getText() ) );

				// MFO TODO: see TODO in SpTelescopeObsComp.
			}

			public void textBoxAction( TextBoxWidgetExt tbwe )
			{
			}
		} );
	}


    /**
	 * Show the given SpTelescopePos.
	 */
    public void showPos( SpTelescopePos tp ) {
	if (tp.getTag().startsWith(SpTelescopePos.USER_TAG)) {
	    _tag.setValue( SpTelescopePos.USER_TAG );
	} else {
	    _tag.setValue( tp.getTag() );
	}


	_name.setValue(   tp.getName()          );

	if(tp.isOffsetPosition()) {
            if ( tp.isBasePosition() ) {
                _xaxis.setValue(tp.getRealXaxisAsString());
                _yaxis.setValue(tp.getRealYaxisAsString());
            }
            else {
                System.out.println("Setting xaxis to " + tp.getXaxisAsString() );
                _xaxis.setValue(tp.getXaxisAsString());
                _yaxis.setValue(tp.getYaxisAsString());
            }
	}
	else {
	  _xaxis.setValue(tp.getXaxisAsString());
	  _yaxis.setValue(tp.getYaxisAsString());
	}

        if ( tp.isBasePosition() ) {
            _w.baseXOff.setValue(""+tp.getBaseXOffset());
            _w.baseYOff.setValue(""+tp.getBaseYOffset());
        }

        if ( !tp.isBasePosition() ) {
            _w.offsetCheckBox.setValue(tp.isOffsetPosition());
        }

	//_configureWidgets(tp);
	_setCoordSys(tp);

	if(tp.getSystemType() != SpTelescopePos.SYSTEM_SPHERICAL) {
          return;
	}

	// *** The "extras" folder
	TextBoxWidgetExt         tbwe;

	// --- Proper Motion Page
	tbwe = _w.propMotionRATBW;
	tbwe.setValue( tp.getPropMotionRA() );

	tbwe = _w.propMotionDecTBW;
	tbwe.setValue( tp.getPropMotionDec() );

	// --- Tracking Page
	tbwe = _w.detailsEpochTBW;
	tbwe.setValue( tp.getTrackingEpoch() );

	tbwe = _w.detailsParallaxTBW;
	tbwe.setValue( tp.getTrackingParallax() );

	tbwe = _w.velValue;
	tbwe.setValue( tp.getTrackingRadialVelocity() );
	
	_w.velDefn.setValue ( tp.getTrackingRadialVelocityDefn() );
	_w.velFrame.setValue ( tp.getTrackingRadialVelocityFrame() );

    }

    //
    // Configure the text prompts on the x and y axis boxes.
    //
    private void _setXYAxisBoxPrompts(String xPrompt, String yPrompt) {
	JLabel stw;

	stw = _w.RA_Az_STW;
	stw.setText(xPrompt);

	stw = _w.Dec_El_STW;
	stw.setText(yPrompt);
    }

    //
    // Show the coordinate system and update the other widgets based upon it.
    //
    private void _setCoordSys(SpTelescopePos tp) {
	int sysIndex = tp.getCoordSys();
	_system.setValue( sysIndex );

	if(tp.isOffsetPosition() && !tp.isBasePosition() ) {
	  _setXYAxisBoxPrompts(CoordSys.X_AXIS_LABEL[_system.getIntegerValue()],
	                       CoordSys.Y_AXIS_LABEL[_system.getIntegerValue()]);
	  return;
	}

	switch (sysIndex) {
	/*case CoordSys.APPARENT:  */
	case CoordSys.FK5:
	case CoordSys.FK4:
	    _setXYAxisBoxPrompts(LABEL_RA, LABEL_DEC);

	    // Enable the folder widget
	    JTabbedPane fwe;
	    fwe = _w.extrasFolder;
	    

	    // Set the Equinox and Proper Motion
	    TextBoxWidgetExt tbw;

	    if (sysIndex == CoordSys.FK4) {
		tbw = _w.propMotionRATBW;
		tbw.setText("fictitious");
		tbw = _w.propMotionDecTBW;
		tbw.setText("fictitious");
	    } else {
		tbw = _w.propMotionRATBW;
		tbw.setText("0");
		tbw = _w.propMotionDecTBW;
		tbw.setText("0");
	    }
	    tbw = _w.velValue;
	    tbw.setText("0");
	    break;
	case CoordSys.AZ_EL: 
	    _setXYAxisBoxPrompts("Az", "El");
	    // Enable the folder widget
	    fwe = _w.extrasFolder;

	    //fwe.setEnabledAt(1, false);
	    fwe.setEnabledAt(2, false);
	    break;
	}
    }


    /**
     * Implements the _updateWidgets method from OtItemEditor in order to
     * setup the widgets to show the current values of the item.
     */
    protected void _updateWidgets() {
        if(!_targetSystemsChange) {
	   _tpl = ((SpTelescopeObsComp) _spItem).getPosList();
	   _tpTable.reinit(_tpl);

	   String seltag = _avTab.get(".gui.selectedTelescopePos");
	   _tpTable.selectPos(seltag);

           _selectTargetSystemTab(_curPos);
	}

        if(_curPos.getSystemType() == SpTelescopePos.SYSTEM_SPHERICAL) {
          _w.offsetCheckBox.setValue(_curPos.isOffsetPosition());
	}
	else {
          // update the conic or named system widgets depending on selection.
	  _updateTargetSystemPane(_curPos);
	}

	TelescopePosEditor tpe = TpeManager.get(_spItem);
	if (tpe != null) tpe.reset(_spItem);


        // chop mode tab added by MFO (3 August 2001)
	_w.chopping.setSelected( ((SpTelescopeObsComp)_spItem).isChopping() );
	_w.chopThrow.setValue(   ((SpTelescopeObsComp)_spItem).getChopThrowAsString() );
	_w.chopAngle.setValue(   ((SpTelescopeObsComp)_spItem).getChopAngleAsString() );
	_w.chopThrow.setEnabled(_w.chopping.isSelected());
	_w.chopAngle.setEnabled(_w.chopping.isSelected());

	_updateXYUnitsLabels();

	// Set the type based on the current contents of the component
	_type.setSelectedIndex ( _curPos.getSystemType() );
    }

    /**
	 * Watch TableWidget row selections to make the selected row the currently displayed position.
	 * 
	 * @see TableWidgetWatcher
	 */
	public void tableRowSelected( TableWidgetExt twe , int rowIndex )
	{
		if( twe != _tpTable )
		{
			return; // shouldn't happen
		}

		// Show the selected position
		if( _curPos != null )
			_curPos.deleteWatcher( this );
		
		_curPos = _tpTable.getSelectedPos();
		
		if( _curPos != _tpl.getBasePosition() )
		{
			if( jcmtot )
			{
				_w.velDefn.setEnabled( false ) ;
				_w.velValue.setEnabled( false ) ;
				_w.velFrame.setEnabled( false ) ;	
			}
			else
			{
				_w.baseXOffLabel.setVisible( false );
				_w.baseYOffLabel.setVisible( false );
				_w.baseXOff.setVisible( false );
				_w.baseYOff.setVisible( false );
				_w.baseXOffUnits.setVisible( false );
				_w.baseYOffUnits.setVisible( false );
			}
		}
		else
		{
			if( jcmtot )
			{
				_w.velDefn.setEnabled( true ) ;
				_w.velValue.setEnabled( true ) ;
				_w.velFrame.setEnabled( true ) ;
			}
			else
			{
				_w.baseXOffLabel.setVisible( true );
				_w.baseYOffLabel.setVisible( true );
				_w.baseXOff.setVisible( true );
				_w.baseYOff.setVisible( true );
				_w.baseXOffUnits.setVisible( true );
				_w.baseYOffUnits.setVisible( true );
			}
		}
		
		_curPos.addWatcher( this );
		showPos( _curPos );
		_selectTargetSystemTab( _curPos );
		_updateTargetSystemPane( _curPos );
		_avTab.set( ".gui.selectedTelescopePos" , _curPos.getTag() );

		if( OtCfg.telescopeUtil.isOffsetTarget( _curPos.getTag() ) && !_curPos.isBasePosition() )
		{
			_w.offsetCheckBox.setValue( _curPos.isOffsetPosition() );
			_w.offsetCheckBox.setVisible( true );
		}
		else
		{
			_w.offsetCheckBox.setVisible( false );
		}

		_updateXYUnitsLabels();

		if( _curPos.getTag().equals( SpTelescopePos.BASE_TAG ) )
			_w.removeButton.setEnabled( false );
		else
			_w.removeButton.setEnabled( true );

		// Make sure the tag selection is disabled while the base position is selected.
		// The base position tag cannot be changed.
		if( _tag.getValue().equals( SpTelescopePos.BASE_TAG ) || _nextTag() == null )
			_tag.setEnabled( false );
		else
			_tag.setEnabled( true );

		String nextTag = _nextTag();

		// Make sure the tag selection and newButton are disabled when all possible tags
		// have been added.
		if( nextTag != null )
			_w.newButton.setEnabled( true );
		else
			_w.newButton.setEnabled( false );

		if( _tpl.size() < 2 )
			_w.removeButton.setEnabled( false );
	}

    /**
	 * As part of the TableWidgetWatcher interface, we must watch table actions, though not interested in them.
	 * 
	 * @see TableWidgetWatcher
	 */
    public void	tableAction(TableWidgetExt twe, int colIndex, int rowIndex) {}


    /**
     * The current position location has changed.
     * @see TelescopePosWatcher
     */
    public void	telescopePosLocationUpdate(TelescopePos tp) {
	telescopePosGenericUpdate(tp);
    }
 
    /**
     * The current position has changed in some way.
     * @see TelescopePosWatcher
     */
    public void	telescopePosGenericUpdate(TelescopePos tp) {
	if (tp != _curPos) {
	    // This shouldn't happen ...
	    System.out.println(getClass().getName() + ": received a position " +
			       " update for a position other than the current one: " + tp);
	    return;
	}
	showPos(_curPos);
    }
 
    /*
	*  Update the state based upon the current coordinate system.
	*/
	private void _updateCoordSystem()
	{
		String coordSys = _system.getStringValue();

		int sysInt = CoordSys.getSystem( coordSys );
		Assert.notFalse( sysInt != -1 );

		_curPos.setCoordSys( sysInt );
		_curPos.setXY( 0. , 0. ) ;

		showPos( _curPos );
	}

    /**
     * Set the labels for the x and y coordinate text boxes.
     *
     * <pre><tt>
     * Coordinate System FK5/FK4     : "" (no label)
     * Other Coordinate Systems      : "(degrees)"
     * Offset (any Coordinate System): "(arcsecs)"
     * </tt></pre>
     */
    private void _updateXYUnitsLabels() {
	if(_curPos.isOffsetPosition() && !_curPos.isBasePosition() ) {
	    _w.xUnitsLabel.setText("(arcsecs)");
	    _w.yUnitsLabel.setText("(arcsecs)");
	}
	else {
	    if((_curPos.getCoordSys() != CoordSys.FK5) && (_curPos.getCoordSys() != CoordSys.FK4) && (_curPos.getCoordSys() != CoordSys.HADEC)) {
	        _w.xUnitsLabel.setText("(degrees)");
	        _w.yUnitsLabel.setText("(degrees)");
	    }
	    else {
	        _w.xUnitsLabel.setText("");
	        _w.yUnitsLabel.setText("");
	    }
	}
    }

    /**
     * Updates the labels of Orbital Elements on the Conic System Tab.
     *
     * The labels are updated according to the selected Conic System type
     * (Major, Minor, Comet).
     *
     *  @param conicSystemType Use SpTelescopePos.TYPE_MAJOR,
     *                             SpTelescopePos.TYPE_MINOR,
     *                             SpTelescopePos.TYPE_COMET
     */
    private void _setConicSystemType(int conicSystemType) {
	// Reset all non displayed elements to 0
      switch(conicSystemType) {
        case SpTelescopePos.TYPE_MAJOR:
	       // Epoch of perihelion
	       _w.epochPerih.setVisible(false);
// 	       _curPos.setConicSystemEpochPerih("0.0");
	       _w.epochPerihLabel.setVisible(false);
	       _w.epochPerihUnitsLabel.setVisible(false);

	       // Mean distance
	       _w.aorqLabel.setText("a");

               // Longitude of perihelion
	       _w.perihLabel.setText("\u03D6");

	       // Longitude and daily motion
	       _w.l_or_m.setValue(_curPos.getConicSystemLorM());
	       _w.l_or_mLabel.setText("L");
	       _w.l_or_m.setVisible(true);
	       _w.l_or_mLabel.setVisible(true);
	       _w.l_or_mUnitsLabel.setVisible(true);

	       _w.dm.setValue(_curPos.getConicSystemDailyMotion());
	       _w.dm.setVisible(true);
	       _w.dmLabel.setVisible(true);
	       _w.dmUnitsLabel.setVisible(true);

	       break;

        case SpTelescopePos.TYPE_MINOR:
	       // Epoch of perihelion
	       _w.epochPerih.setVisible(false);
	       _w.epochPerihLabel.setVisible(false);
	       _w.epochPerihUnitsLabel.setVisible(false);
// 	       _curPos.setConicSystemEpochPerih("0.0");

	       // Mean distance
	       _w.aorqLabel.setText("a");

               // Argument of perihelion
	       _w.perihLabel.setText("\u03C9");

	       // Mean Anomaly
	       _w.l_or_m.setValue(_curPos.getConicSystemLorM());
	       _w.l_or_mLabel.setText("M");
	       _w.l_or_m.setVisible(true);
	       _w.l_or_mLabel.setVisible(true);
	       _w.l_or_mUnitsLabel.setVisible(true);

	       // No daily motion
	       _w.dm.setVisible(false);
	       _w.dmLabel.setVisible(false);
	       _w.dmUnitsLabel.setVisible(false);
// 	       _curPos.setConicSystemDailyMotion("0.0");

               break;

        // Comet
	default:
	       // Epoch of perihelion
	       _w.epochPerih.setVisible(true);
	       _w.epochPerihLabel.setVisible(true);
	       _w.epochPerihUnitsLabel.setVisible(true);

	       // Perihelion distance
	       _w.aorqLabel.setText("q");

               // Argument of perihelion
	       _w.perihLabel.setText("\u03C9");

               // No longitude, mean anomaly, daily motion
	       _w.l_or_m.setVisible(false);
	       _w.l_or_mLabel.setVisible(false);
	       _w.l_or_mUnitsLabel.setVisible(false);
// 	       _curPos.setConicSystemLorM("0.0");
	       _w.dm.setVisible(false);	       
	       _w.dmLabel.setVisible(false);	       
	       _w.dmUnitsLabel.setVisible(false);	       
// 	       _curPos.setConicSystemDailyMotion("0.0");
      }
    }


    /**
     * Updates the the conic or named system widgets
     * depending on the target system of the selected target.
     */
    private void _updateTargetSystemPane(SpTelescopePos tp) {
      switch(tp.getSystemType()) {
        case SpTelescopePos.SYSTEM_CONIC:
          _w.epoch.setValue(tp.getConicSystemEpochAsString());
	  _w.epoch.setCaretPosition(0);
          _w.epochPerih.setValue(tp.getConicSystemEpochPerihAsString());
	  _w.epochPerih.setCaretPosition(0);
          _w.orbinc.setValue(tp.getConicSystemInclination());
          _w.anode.setValue(tp.getConicSystemAnode());
          _w.perih.setValue(tp.getConicSystemPerihelion());
          _w.aorq.setValue(tp.getConicSystemAorQ());
          _w.e.setValue(tp.getConicSystemE());
          _w.l_or_m.setValue(tp.getConicSystemLorM());
          _w.dm.setValue(tp.getConicSystemDailyMotion());
          _w.conicSystemType.setValue(tp.getConicOrNamedTypeDescription());

	  _setConicSystemType(_w.conicSystemType.getIntegerValue());
	  break;

        case SpTelescopePos.SYSTEM_NAMED:
          //_w.namedSystemType.setValue(tp.getConicOrNamedTypeDescription());

	  if(Arrays.asList(OtCfg.getNamedTargets()).contains(tp.getName())) {
	    _w.namedTarget.setValue(tp.getName());
	  }
	  else {
	    _w.namedTarget.setValue(SELECT_TARGET);
	  }

	  break;
      }
    }

    /**
     * Updates the target system pane selection
     * depending on the target system of the selected target.
     */
    private void _selectTargetSystemTab(SpTelescopePos tp) {
      _w.targetSystemsTabbedPane.removeChangeListener(this);

      // Added by SdW 
      // When base is a named system or orbital elements, and the user selects
      // REFERENCE - then we need to to display the RA/DEC tab, but with the
      // offset checkbox selected and disabled to stop users putting in
      // absolute positions
      SpTelescopePos base = null;
      if ( _tpl != null ) {
	  base = _tpl.getBasePosition();
      }
      if ( tp.getTag().equals("REFERENCE") &&
	   base.getSystemType() != SpTelescopePos.SYSTEM_SPHERICAL ) {
	   tp.setOffsetPosition(true);
	   _w.targetSystemsTabbedPane.setSelectedComponent(_w.objectGBW);
	   _w.targetTypeDDList.setSelectedIndex(0);
	   _w.offsetCheckBox.setEnabled(false);
           _w.targetSystemsTabbedPane.addChangeListener(this);
	   _w.offsetCheckBox.setValue(true);
	   return;
      }
      else {
	  _w.offsetCheckBox.setEnabled(true);
      }
      
      switch(tp.getSystemType()) {
        case SpTelescopePos.SYSTEM_CONIC:
          _w.targetSystemsTabbedPane.setSelectedComponent(_w.conicSystemPanel);
	  _w.targetTypeDDList.setSelectedIndex(1);
	  break;

        case SpTelescopePos.SYSTEM_NAMED:
          _w.targetSystemsTabbedPane.setSelectedComponent(_w.namedSystemPanel);
	  _w.targetTypeDDList.setSelectedIndex(2);
	  break;

        // SpTelescopePos.SYSTEM_SPHERICAL:
	default:
          _w.targetSystemsTabbedPane.setSelectedComponent(_w.objectGBW);
	  _w.targetTypeDDList.setSelectedIndex(0);
	  break;
      }

      _w.targetSystemsTabbedPane.addChangeListener(this);
    }


    private void _resetPositionEditor() {
	// MFO: copied from ot-1.0.1 (18 June 2001)
	// If this is the base position, update the position editor
	if (_curPos.isBasePosition()) {
	    TelescopePosEditor tpe = TpeManager.get(_spItem);
	    if (tpe != null) {
	        tpe.loadImage(TelescopePosEditor.ANY_SERVER);
	    }
	}
    }


	/**
	 * Method to handle button actions.
	 */
	public void actionPerformed( ActionEvent evt )
	{
		Object w = evt.getSource();

		if( w == _w.newButton )
		{
			SpTelescopePos base = _tpl.getBasePosition();
			if( base == null )
			{
				return;
			}

			String nextTag = ( String ) _w.newButton.getSelectedItem();

			/*
			 * If the user selects SKY, we create a new indexed tag starting at 0
			 */
			if( "SKY".equalsIgnoreCase( nextTag ) || "SKYGUIDE".equalsIgnoreCase( nextTag ) )
			{
				int index = 0;
				while( true )
				{
					TelescopePos p = _tpl.getPosition( nextTag + index );
					if( p == null )
						break;
					index++;
				}
				nextTag = nextTag + index;
			}

			SpTelescopePos tp = _tpl.createPosition( nextTag , base.getXaxis() , base.getYaxis() );

			if( jcmtot )
			{
				SpTelescopePos basePos = _tpl.getBasePosition() ;
				tp.setTrackingRadialVelocity( basePos.getTrackingRadialVelocity() ) ;
				tp.setTrackingRadialVelocityDefn( basePos.getTrackingRadialVelocityDefn() ) ;
				tp.setTrackingRadialVelocityFrame( basePos.getTrackingRadialVelocityFrame() ) ;
			}

			if( tp.getSystemType() == SpTelescopePos.TYPE_MAJOR )
			{
				tp.setName( base.getName() );
			}
			_w.removeButton.setEnabled( true );

			if( OtCfg.telescopeUtil.isOffsetTarget( tp.getTag() ) )
			{
				_w.offsetCheckBox.setValue( tp.isOffsetPosition() );
				_w.offsetCheckBox.setVisible( true );
			}
			else
			{
				_w.offsetCheckBox.setVisible( false );
			}

			// Select HMSDEG/DEGDEG pane.
			_w.targetSystemsTabbedPane.setSelectedComponent( _w.objectGBW );

			return;
		}
		else if( w == _w.removeButton )
		{
			if( _curPos.getTag().equals( SpTelescopePos.BASE_TAG ) )
			{
				DialogUtil.error( _w , "You can't remove the Base Position." );
				return;
			}

			_tpl.removePosition( _curPos );

			return;
		}
		else if( w == _w.plotButton )
		{
			try
			{
				TpeManager.open( _spItem );
			}
			catch( Exception e )
			{
				DialogUtil.error( e );
			}

			return;
		}
		else if( w == _w.setBaseButton )
		{
			TelescopePosEditor tpe = TpeManager.get( _spItem );
			if( tpe == null )
			{
				DialogUtil.message( _w , "The Position Editor must be opened for this feature to work." );
				return;
			}

			double[] raDec = tpe.getImageCenterLocation();
			if( raDec == null )
			{
				DialogUtil.message( _w , "Couldn't determine the image center." );
				return;
			}

			SpTelescopePos base = _tpl.getBasePosition();
			if( base == null )
			{
				return;
			}

			base.setXY( raDec[ 0 ] , raDec[ 1 ] );
			return;
		}
		else if( w == _w.resolveButton )
		{
//			 Added by MFO
			TelescopePosEditor tpe = TpeManager.get( _spItem );
			if( tpe != null && tpe.isVisible() )
			{
				JOptionPane.showMessageDialog( _w , "The position editor must be closed for this to work" , "Close Position Editor" , JOptionPane.INFORMATION_MESSAGE );
				return;
			}
			_nameResolverFeedback = new NameResolverFeedback();
			_nameResolverFeedback.start();
		}
		else if( w == _w.chopping )
		{
//			 chop mode tab added by MFO (3 August 2001)
			_w.chopThrow.setEnabled( _w.chopping.isSelected() );
			_w.chopAngle.setEnabled( _w.chopping.isSelected() );

			( ( SpTelescopeObsComp ) _spItem ).setChopping( _w.chopping.isSelected() );
			( ( SpTelescopeObsComp ) _spItem ).setChopThrow( _w.chopThrow.getText() );
			( ( SpTelescopeObsComp ) _spItem ).setChopAngle( _w.chopAngle.getText() );
		}
		else if( w == _w.offsetCheckBox )
		{
			if( _w.offsetCheckBox.getBooleanValue() )
			{
				_curPos.setOffsetPosition( true );
			}
			else
			{
				_curPos.setOffsetPosition( false );
			}

			_curPos.setXY( 0.0 , 0.0 );

			_updateXYUnitsLabels();
		}
		else if( w == _w.resolveOrbitalElementButton )
		{
			if( _resolving )
				return ;
			HorizonsThread thread = new HorizonsThread() ;
			thread.start() ;
		}
	}

    public void stateChanged(ChangeEvent e) {

      if(e.getSource() == _w.targetSystemsTabbedPane) {

        if(_w.targetSystemsTabbedPane.getSelectedComponent() == _w.objectGBW) {
          _curPos.setSystemType(SpTelescopePos.SYSTEM_SPHERICAL);
	  // See if the user has entered the name of a planet.  If so, warn him that
	  // spherical coordinates are probably not sensible.  Pop up a warning box
	  // with cancel and continue options, and handle the response.
	  String [] targetList = OtCfg.getNamedTargets();
	  for (int i=0; i<targetList.length; i++) {
	      if (_name.getValue().equalsIgnoreCase(targetList[i])) {
		  // We have a winner!
		  int selected = JOptionPane.showConfirmDialog(null,
							       "Do you REALLY want to specify the RA and DEC for this object?",
							       "Name is Planet/Moon/Sun",
							       JOptionPane.YES_NO_OPTION,
							       JOptionPane.WARNING_MESSAGE);
		  if (selected == JOptionPane.NO_OPTION) {
// 		      _w.targetSystemsTabbedPane.setSelectedComponent(_w.namedSystemPanel);
		      // Which pane is the named SystemPanel anyway?
		      for (int tab=0; tab<_w.targetSystemsTabbedPane.getTabCount(); tab++) {
			  if (_w.targetSystemsTabbedPane.getComponentAt(tab) ==  _w.namedSystemPanel) {
			      _type.setValue(_w.targetSystemsTabbedPane.getTitleAt(tab));
			      break;
			  }
		      }
		  }
		  else {
		      // Oh well, at least we warned the user.  
		      // Maybe this will be validated somewhere down the line...
		  }
	      }
	  }
	}

        if(_w.targetSystemsTabbedPane.getSelectedComponent() == _w.conicSystemPanel) {
          _curPos.setSystemType(SpTelescopePos.SYSTEM_CONIC);
	}

        if(_w.targetSystemsTabbedPane.getSelectedComponent() == _w.namedSystemPanel) {
          _curPos.setSystemType(SpTelescopePos.SYSTEM_NAMED);
	  // See if the name is one of the predefined ones.  If it is still empty, fine,
	  // if it matches, set the choice appropriately, if it doesn't, warn the user and
	  // clear the field if this is what they want to do.
	  if (_name.getValue().equals("")) {
	      // OK, carry on
	  }
	  else {
	      boolean targetFound = false;
	      String [] namedTargets = OtCfg.getNamedTargets();
	      for (int targetCount=0; targetCount < namedTargets.length; targetCount++) {
		  if (_name.getValue().equalsIgnoreCase((String)namedTargets[targetCount])) {
		      // Another winner!
		      targetFound = true;
		      _w.namedTarget.setSelectedItem(namedTargets[targetCount]);
		      break;
		  }
	      }
	      if (targetFound == false) {
		  // Oops, the name is not in the list.  Warn the user and if he aborts, set
		  // the selected tab to conic since this is probably what he wanted to do
		  // anyway.
		  int status = JOptionPane.showConfirmDialog(null,
							     "Continuing will remove your named object.\nDo you REALLY want to do this?",
							     "Name does not match list",
							     JOptionPane.YES_NO_OPTION,
							     JOptionPane.WARNING_MESSAGE);
		  if (status == JOptionPane.YES_OPTION) {
		      // You were warned...
		      _curPos.setName("");
		      _name.setValue("");
		  }
		  else {
// 		      _w.targetSystemsTabbedPane.setSelectedComponent(_w.conicSystemPanel);
		      // Which pane is the conicSystemPanel anyway?
		      for (int tab=0; tab<_w.targetSystemsTabbedPane.getTabCount(); tab++) {
			  if (_w.targetSystemsTabbedPane.getComponentAt(tab) ==  _w.conicSystemPanel) {
			      _type.setValue(_w.targetSystemsTabbedPane.getTitleAt(tab));
			      break;
			  }
		      }
		  }
	      }
	  }
	}

        _targetSystemsChange = true;
        _updateWidgets();

        _curPos.deleteWatcher(EdCompTargetList.this);
        
	if(_curPos.getSystemType() == SpTelescopePos.SYSTEM_CONIC) {
	  _curPos.setConicOrNamedType(SpTelescopePos.NAMED_SYSTEM_TYPES[SpTelescopePos.TYPE_COMET]);
	}
	else {
	  _curPos.setConicOrNamedType(SpTelescopePos.NAMED_SYSTEM_TYPES[SpTelescopePos.TYPE_MAJOR]);
	}

        _curPos.addWatcher(EdCompTargetList.this);

	_targetSystemsChange = false;
      }
    }

    public void textBoxKeyPress(TextBoxWidgetExt tbwe) {
      if(tbwe == _w.epoch) {
        _curPos.setConicSystemEpoch(_w.epoch.getValue());
        return;
      }

      if(tbwe == _w.epochPerih) {
        _curPos.setConicSystemEpochPerih(_w.epochPerih.getValue());
        return;
      }

      if(tbwe == _w.orbinc) {
        _curPos.setConicSystemInclination(_w.orbinc.getValue());
        return;
      }

      if(tbwe == _w.anode) {
        _curPos.setConicSystemAnode(_w.anode.getValue());
        return;
      }

      if(tbwe == _w.perih) {
        _curPos.setConicSystemPerihelion(_w.perih.getValue());
        return;
      }

      if(tbwe == _w.aorq) {
        _curPos.setConicSystemAorQ(_w.aorq.getValue());
        return;
      }

      if(tbwe == _w.e) {
        _curPos.setConicSystemE(_w.e.getValue());
        return;
      }

      if(tbwe == _w.l_or_m) {
        _curPos.setConicSystemLorM(_w.l_or_m.getValue());
        return;
      }

      if(tbwe == _w.dm) {
        _curPos.setConicSystemDailyMotion(_w.dm.getValue());
        return;
      }
    }

    public void textBoxAction(TextBoxWidgetExt tbwe) { }


    public void dropDownListBoxSelect(DropDownListBoxWidgetExt dd, int i, String val) { }

    public void dropDownListBoxAction(DropDownListBoxWidgetExt dd, int i, String val) {
      if(dd == _w.conicSystemType) {
	  _curPos.setSystemType(SpTelescopePos.SYSTEM_CONIC);
	  _curPos.setConicOrNamedType(SpTelescopePos.CONIC_SYSTEM_TYPES[i]);

        _updateTargetSystemPane(_curPos);
//         _setConicSystemType(i);

	return;
      }

//      if(dd == _w.namedSystemType) {
//        _curPos.setConicOrNamedType(SpTelescopePos.NAMED_SYSTEM_TYPES[i]);
//	return;
//      }

      if(dd == _w.namedTarget) {
        _curPos.deleteWatcher(EdCompTargetList.this);
        _curPos.setConicOrNamedType(SpTelescopePos.NAMED_SYSTEM_TYPES[SpTelescopePos.TYPE_MAJOR]);

        if(val.equals(SELECT_TARGET)) {
          _curPos.setName("");
	  _name.setValue("");
        }
        else {
          _curPos.setName(val);
	  _name.setValue(val);
	  if ( (_tpl != null) && (_tpl.exists("REFERENCE"))  ) {
		  // Set the object name field to the currently selected planet
		  _name.setValue( val );
		  ((SpTelescopePos)_tpl.getPosition("REFERENCE")).setName(val);
	  }
	      
        }

        _curPos.addWatcher(EdCompTargetList.this);

	return;
      }
    }

    /**
     * Checks whether chop angle is in valid range.
     * 
     * The validity check applies to UKIRT. JCMT does not use the "Chop Settings" tab.
     *
     * Added by MFO (12 October 2001)
     */
    public String validateChopAngle(String chopAngleString) {
      double chopAngle = Double.valueOf(chopAngleString).doubleValue();
	
      if(chopAngle < -90) {
        chopAngle = -90;
        DialogUtil.error(_w, "Valid range of chop angles: -90.0..90.0");
      }

      if(chopAngle > 90) {
        chopAngle = 90;
        DialogUtil.error(_w, "Valid range of chop angles: -90.0..90.0");
      }	

      return Double.toString(chopAngle);
    }

    /**
     * Thread for name resolving.
     *
     * Added by MFO (10 Oct 2001).
     */
    class NameResolverFeedback extends Thread {
    
	public void run() {

          try {
	    NameResolver nameResolver = new NameResolver((String)_w.nameResolversDDLBW.getSelectedItem(), _w.nameTBW.getText());

	    _w.resolvedName.setText("Resolved Name: "+nameResolver.getId());
	    _w.xaxisTBW.setText(nameResolver.getRa());
	    _w.yaxisTBW.setText(nameResolver.getDec());
	    _w.systemDDLBW.setValue(CoordSys.FK5);

            _curPos.deleteWatcher(EdCompTargetList.this);
	    try {
	      _curPos.setXYFromString(_w.xaxisTBW.getText(), _w.yaxisTBW.getText());
	    }
	    // In case an exception is thrown here if the new position is out of view (in the
	    // position editor)
	    // The new position will be "in view" after the call _resetPositionEditor.
	    // But _resetPositionEditor can only be called after _cur has been set to the new
	    // position. Otherwise. it would not pick up the right position.
	    catch(Exception exception) {
              // print stack trace for debugging.
	      exception.printStackTrace();
	    }
	    _curPos.setName(_w.nameTBW.getText() );
	    _curPos.setCoordSys(CoordSys.FK5);
	    _curPos.addWatcher(EdCompTargetList.this);

	    // Added by SdW
	    if (nameResolver.getEquinox().equalsIgnoreCase("RJ")) {
		_curPos.setCoordSys(CoordSys.FK5);
		_w.systemDDLBW.setValue(CoordSys.FK5);
	    }
	    else if ( nameResolver.getEquinox().equalsIgnoreCase("RB")) {
		_curPos.setCoordSys(CoordSys.FK4);
		_w.systemDDLBW.setValue(CoordSys.FK4);
	    }
	    else if (nameResolver.getEquinox().equalsIgnoreCase("GA")) {
		_curPos.setCoordSys(CoordSys.GAL);
		_w.systemDDLBW.setValue(CoordSys.GAL);
	    }
	    // End of addition

            _resetPositionEditor();
	  }
	  catch(ProgressException e) {
            // User has interrupted query. Do nothing.
            System.out.println("Query interrupted by OT user.");
	  }
	  catch(RuntimeException e) {
            if(System.getProperty("DEBUG") != null) {
              e.printStackTrace();
	    }
e.printStackTrace();
            DialogUtil.error(_w, "Error while trying to resolve name \"" + _w.nameTBW.getText() + "\"\n" + e.getMessage());
	  }
	}
    }

    private void _changeTag(String newTag) {
      String oldTag = _curPos.getTag();

      if(_tpl.exists(newTag)) {
        DialogUtil.error(newTag + " exists.");
        _tag.setValue(oldTag);
        return;
      }

      if(oldTag.startsWith(SpTelescopePos.USER_TAG)) {
        oldTag = SpTelescopePos.USER_TAG;
      }

      if (oldTag.equals(newTag)) return;

      // Don't allow changes from BASE_TAG to anything else.  We always
      // want to have a Base.
      if(oldTag.equals(SpTelescopePos.BASE_TAG)) {
        DialogUtil.error("You can't change the type of the Base Position.");
        _tag.setValue(SpTelescopePos.BASE_TAG);
          return;
      }

      _tpl.changeTag(_curPos, newTag);

      // Update newButton

      String nextTag = _nextTag();

      if(nextTag != null) {
        //_w.newButton.setText("Add " + _nextTag());
      }
      else {
        _w.newButton.setEnabled(false);
      }
    }

    private String _nextTag() {
      if((_guideTags != null) && (_guideTags[0] != null)) {
        for(int i = 0; i < _guideTags.length; i++) {
          if(!_tpl.exists(_guideTags[i])) {
            return _guideTags[i];
          }
        }
      }	

      return null;
    }
   
    /* 
     * Why are we bothering with a thread ? 
     * So that the OT does not become unresponsive 
     */
    public class HorizonsThread extends Thread
    {
    	public synchronized void run()
    	{
    		_resolving = true ;
    		_w.resolveOrbitalElementButton.setText( "Resolving ..." ) ;
			Horizons horizons = Horizons.getInstance() ;
			String query = _w.nameTBW.getText() ;
			TreeMap treeMap = horizons.resolveName( query ) ;
			_resolving = false ;
			_w.resolveOrbitalElementButton.setText( "Resolve Name" ) ;
			
			if( treeMap.isEmpty() )
			{
				DialogUtil.error( null , "No result returned") ;
				return ;
			}
			
			Object tmp = treeMap.get( "NAME" ) ;			
			String value = "" ;
			if( tmp != null && tmp instanceof String )
			{
				value = ( String )tmp ;
				if( value.equals( "" ) )
				{
					if( !searchHorizons( query ) )
						DialogUtil.error( null , "No result returned") ;
					return ;
				}	
				_w.orbitalElementResolvedNameLabel.setText( value ) ;
			}
			else
			{
				if( !searchHorizons( query ) )
					DialogUtil.error( null , "No result returned") ;
				return ;

			}
			
			tmp = treeMap.get( "EPOCH" ) ;
			if( tmp != null && tmp instanceof Double )
			{
				value = ( ( Double )tmp ).toString() ;
				_curPos.setConicSystemEpoch( value ) ;
			}
			
			tmp = treeMap.get( "TP" ) ;
			if( tmp != null && tmp instanceof Double )
			{
				value = ( ( Double )tmp ).toString() ;
				_curPos.setConicSystemEpochPerih( value ) ;
			}

			tmp = treeMap.get( "IN" ) ;
			if( tmp != null && tmp instanceof Double )
			{
				value = ( ( Double )tmp ).toString() ;
				_curPos.setConicSystemInclination( value ) ;
			}

			tmp = treeMap.get( "W" ) ;
			if( tmp != null && tmp instanceof Double )
			{
				value = ( ( Double )tmp ).toString() ;
				_curPos.setConicSystemPerihelion( value ) ;
			}

			tmp = treeMap.get( "EC" ) ;
			if( tmp != null && tmp instanceof Double )
			{
				value = ( ( Double )tmp ).toString() ;
				_curPos.setConicSystemE( value ) ;
			}
			
			tmp = treeMap.get( "OM" ) ;
			if( tmp != null && tmp instanceof Double )
			{
				value = ( ( Double )tmp ).toString() ;
				_curPos.setConicSystemAnode( value ) ;
			}

			tmp = treeMap.get( "QR" ) ;
			if( tmp != null && tmp instanceof Double )
			{
				value = ( ( Double )tmp ).toString() ;
				_curPos.setConicSystemAorQ( value ) ;
			}

			tmp = treeMap.get( "MA" ) ;
			if( tmp != null && tmp instanceof Double )
			{
				value = ( ( Double )tmp ).toString() ;
				_curPos.setConicSystemLorM( value ) ;
			}

			tmp = treeMap.get( "N" ) ;
			if( tmp != null && tmp instanceof Double )
			{
				value = ( ( Double )tmp ).toString() ;
				_curPos.setConicSystemDailyMotion( value ) ;
			}
			
			_updateTargetSystemPane( _curPos ) ;
    		
    	}
    }
    
    public boolean searchHorizons( String name )
    {
		_resolving = true ;
		_w.resolveOrbitalElementButton.setText( "Searching ..." ) ;
		Horizons horizons = Horizons.getInstance() ;
		Vector results = horizons.searchName( name ) ;
		_resolving = false ;
		_w.resolveOrbitalElementButton.setText( "Resolve Name" ) ;
		
		if( results.size() == 0 )
			return false ;
		
		StringBuffer buffer = new StringBuffer() ;
		while( results.size() != 0 )
		{
			Object tmp = results.remove( 0 ) ;
			if( tmp instanceof String )
			{
				String line = ( String )tmp ;
				if( line.trim().matches( "^No matches found.$" ) )
					return false ;
				buffer.append( tmp.toString() + "\n" ) ;
			}
		}
		new ReportBox( buffer.toString() , "Search results for " + name ) ;
		return true ;
    }
    
}

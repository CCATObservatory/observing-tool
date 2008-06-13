/*==============================================================*/
/*                                                              */
/*                UK Astronomy Technology Centre                */
/*                 Royal Observatory, Edinburgh                 */
/*                 Joint Astronomy Centre, Hilo                 */
/*                   Copyright (c) PPARC 2003                   */
/*                                                              */
/*==============================================================*/
// $Id$
package ot.editor ;

import javax.swing.JPanel ;
import javax.swing.JTable ;
import javax.swing.JButton ;
import javax.swing.JCheckBox ;
import javax.swing.JComboBox ;
import javax.swing.JTextField ;
import javax.swing.JLabel ;
import javax.swing.JScrollPane ;
import javax.swing.JTabbedPane ;
import javax.swing.border.EtchedBorder ;
import java.awt.BorderLayout ;
import java.awt.FlowLayout ;
import java.awt.Color ;
import java.awt.Dimension ;

import jsky.app.ot.editor.TelescopeGUI ;

/**
 * @author Martin Folger (M.Folger@roe.ac.uk)
 */
public class SurveyGUI extends JPanel
{
	private TelescopeGUI _telescopeGUI = null ; //new TelescopeGUI() ;
	JComboBox remaining = new JComboBox() ;
	JComboBox priority = new JComboBox() ;
	JButton addButton = new JButton( "Add" ) ;
	JButton removeButton = new JButton( "Remove" ) ;
	JButton removeAllButton = new JButton( "Remove all" ) ;
	JButton loadButton = new JButton( "Load" ) ;
	JTabbedPane tabbedPane = new JTabbedPane() ;
	JCheckBox chooseButton = new JCheckBox( "Select" ) ;
	JLabel selectLabel = new JLabel( "from 1" ) ;
	JTextField selectField = new JTextField() ;
	JLabel titleLabel = new JLabel( "Title:" ) ;
	JTextField titleField = new JTextField() ;

	/**
	 * List of fiels.
	 *
	 * Each field is represented by a SpTelescopeObsComp item.
	 */
	JTable fieldTable = new JTable() ;

	public SurveyGUI( TelescopeGUI telescopeGUI )
	{
		_telescopeGUI = telescopeGUI ;

		priority.setFont( new java.awt.Font( "Dialog" , 0 , 12 ) ) ;
		remaining.setFont( new java.awt.Font( "Dialog" , 0 , 12 ) ) ;
		addButton.setFont( new java.awt.Font( "Dialog" , 0 , 12 ) ) ;
		removeButton.setFont( new java.awt.Font( "Dialog" , 0 , 12 ) ) ;
		removeAllButton.setFont( new java.awt.Font( "Dialog" , 0 , 12 ) ) ;
		loadButton.setFont( new java.awt.Font( "Dialog" , 0 , 12 ) ) ;
		chooseButton.setFont( new java.awt.Font( "Dialog" , 0 , 12 ) ) ;
		selectLabel.setFont( new java.awt.Font( "Dialog" , 0 , 12 ) ) ;
		tabbedPane.setFont( new java.awt.Font( "Dialog" , 0 , 12 ) ) ;
		titleLabel.setFont( new java.awt.Font( "Dialog" , 0 , 12 ) ) ;
		selectField.setColumns( 4 ) ;

		setLayout( new BorderLayout() ) ;

		JPanel buttonPanel = new JPanel() ;
		buttonPanel.add( addButton ) ;
		buttonPanel.add( removeButton ) ;
		buttonPanel.add( removeAllButton ) ;
		buttonPanel.add( loadButton ) ;

		JPanel panel = new JPanel() ;

		JLabel label = new JLabel( "Remaining" ) ;
		label.setFont( new java.awt.Font( "Dialog" , 0 , 12 ) ) ;
		label.setForeground( Color.black ) ;
		panel.add( label ) ;
		panel.add( remaining ) ;

		label = new JLabel( "    Priority" ) ;
		label.setFont( new java.awt.Font( "Dialog" , 0 , 12 ) ) ;
		label.setForeground( Color.black ) ;
		panel.add( label ) ;
		panel.add( priority ) ;

		JLabel strut = new JLabel( "     " ) ;
		strut.setFont( new java.awt.Font( "Dialog" , 0 , 12 ) ) ;
		panel.add( strut ) ;
		panel.add( chooseButton ) ;
		panel.add( selectField ) ;
		panel.add( selectLabel ) ;

		JPanel southPanel = new JPanel( new BorderLayout() ) ;
		southPanel.add( panel , BorderLayout.NORTH ) ;
		southPanel.add( buttonPanel , BorderLayout.SOUTH ) ;

		JPanel surveyPanel = new JPanel( new BorderLayout() ) ;
		surveyPanel.add( new JScrollPane( fieldTable ) , BorderLayout.CENTER ) ;
		surveyPanel.add( southPanel , BorderLayout.SOUTH ) ;
		surveyPanel.setBorder( new EtchedBorder() ) ;
		surveyPanel.setPreferredSize( new Dimension( 100 , 100 ) ) ;

		tabbedPane.add( "Survey Targets" , surveyPanel ) ;
		tabbedPane.add( "Target Information" , _telescopeGUI ) ;

		add( tabbedPane , BorderLayout.CENTER ) ;

		JPanel northPanel = new JPanel() ;
		( ( FlowLayout )northPanel.getLayout() ).setAlignment( FlowLayout.LEFT ) ;
		titleField.setColumns( 15 ) ;
		northPanel.add( titleLabel , BorderLayout.NORTH ) ;
		northPanel.add( titleField , BorderLayout.NORTH ) ;
		add( northPanel , BorderLayout.NORTH ) ;
	}

	public TelescopeGUI getTelescopeGUI()
	{
		return _telescopeGUI ;
	}
}

/*==============================================================*/
/*                                                              */
/*                UK Astronomy Technology Centre                */
/*                 Royal Observatory, Edinburgh                 */
/*                 Joint Astronomy Centre, Hilo                 */
/*                   Copyright (c) PPARC 2001                   */
/*                                                              */
/*==============================================================*/
// $Id$

package ot.jcmt.inst.editor;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.BoxLayout;
import java.awt.event.*;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.FlowLayout;
import java.awt.Container;
import java.awt.Font;
import java.awt.Insets;

/**
 * This GUI class is based on the GUI part of the edfreq.FrontEnd class which is not used anymore.
 *
 * @author Dennis Kelly ( bdk@roe.ac.uk ), modified by Martin Folger (M.Folger@roe.ac.uk)
 */
public class HeterodyneGUI extends JPanel {

  JComboBox feChoice;
  JComboBox feBandModeChoice;
  private String currentFE = "";
  private JPanel fePanel;
  private JPanel displayPanel;
  private JPanel overlapBandwidthPanel;
  private JPanel velocityPanel;
  private JPanel warningPanel;
//  private JPanel rangePanel;
//  private Box linePanel;
  private JPanel mol1Panel;
  private JPanel mol2Panel;
  private JPanel northPanel;
  private JPanel southPanel;
  JLabel lowFreq;
  JLabel highFreq;
  JTextField velocity;
  JComboBox velocityDefinition;
  JComboBox velocityFrame;
  JTextField overlap;
  JComboBox feBand;
  JComboBox feMode;
  JComboBox moleculeChoice;
  JComboBox moleculeChoice2;
  JTextField moleculeFrequency;
  JTextField moleculeFrequency2;
  JComboBox transitionChoice;
  JComboBox transitionChoice2;
  JComboBox bandWidthChoice;
  JLabel resolution;
    JButton acceptVF = new JButton ("Accept Values");
  JButton freqEditorButton = new JButton("Show Frequency Editor");
  JButton hideFreqEditorButton = new JButton("Hide Frequency Editor");
  JLabel label = null;

  private FlowLayout flowLayoutLeft  = new FlowLayout(FlowLayout.LEFT);
  private FlowLayout flowLayoutRight = new FlowLayout(FlowLayout.RIGHT);
  // Changed to avoid security exception in applet.
  private String defaultStoreDirectory = ".;"; //System.getProperty("user.dir");
  private boolean editFlag = false;

  // Commented out to avoid security exception in applet.
  // private JFileChooser fileChooser = new JFileChooser ( );

  //private JButton sideBandButton = new JButton("Show Side Band Display");

    JComboBox   feMixers;

    // ADDED by SDW to allow for Special DAS configurations
    JPanel    specialPanel;
    JLabel    specialConfigurationLabel;
    JComboBox specialConfigurations;


  public HeterodyneGUI() {

      setLayout(new BorderLayout());
/* Create the choice of frontends */

      fePanel = new JPanel();
      fePanel.setLayout ( new GridLayout(1, 0, 10, 0) );
      label = new JLabel("Front End");
      label.setFont(new Font("Dialog", 0, 12));
      label.setForeground(Color.black);
      fePanel.add(label);

      feChoice = new JComboBox (); //cfg.frontEnds );
      feChoice.setFont(new Font("Dialog", 0, 12));
      feChoice.setForeground(Color.black);
      lowFreq = new JLabel ( "215" );
      lowFreq.setFont(new Font("Dialog", 0, 12));
      lowFreq.setForeground(Color.black);
      lowFreq.setBorder ( new BevelBorder ( BevelBorder.LOWERED ) );
      lowFreq.setMinimumSize(lowFreq.getPreferredSize());
      highFreq = new JLabel ( "272" );
      highFreq.setFont(new Font("Dialog", 0, 12));
      highFreq.setForeground(Color.black);
      highFreq.setBorder ( new BevelBorder ( BevelBorder.LOWERED ) );
      highFreq.setMinimumSize(highFreq.getPreferredSize());

//      feChoice.addActionListener ( this );

      feMode = new JComboBox (); // cfg.frontEndModes );
      feMode.setFont(new Font("Dialog", 0, 12));
      feMode.setForeground(Color.black);
      feBandModeChoice = new JComboBox ( );
      feBandModeChoice.setFont(new Font("Dialog", 0, 12));
      feBandModeChoice.setForeground(Color.black);
      //feBandModeChoice.addActionListener ( this );

      feMixers = new JComboBox();
      feMixers.setFont(new Font("Dialog", 0, 12));
      feMixers.setForeground(Color.black);

      fePanel.add ( feChoice );
      fePanel.add ( feMode );
      fePanel.add ( feBandModeChoice );
      fePanel.add ( feMixers );

/* Create the display */

      displayPanel = new JPanel(new GridBagLayout());
      label = new JLabel("Low Limit (GHz)");
      label.setFont(new Font("Dialog", 0, 12));
      label.setForeground(Color.black);
      label.setMinimumSize ( label.getPreferredSize() );
      displayPanel.add ( label,  new GridBagConstraints (0 ,0, 1, 1,  1.0, 0.0,
							 GridBagConstraints.WEST,
							 GridBagConstraints.NONE,
							 new Insets (10, 10, 10, 5),
							 0, 0)
			 );
      displayPanel.add ( lowFreq,  new GridBagConstraints (1 ,0, 1, 1,  0.0, 0.0,
							   GridBagConstraints.WEST,
							   GridBagConstraints.NONE,
							   new Insets (10, 0, 10, 10),
							   0, 0)
			 );
      label = new JLabel("High Limit (GHz)");
      label.setFont(new Font("Dialog", 0, 12));
      label.setForeground(Color.black);
      label.setMinimumSize ( label.getPreferredSize() );
      displayPanel.add ( label ,  new GridBagConstraints (2 ,0, 1, 1,  1.0, 0.0,
							   GridBagConstraints.WEST,
							   GridBagConstraints.NONE,
							   new Insets (10, 10, 10, 5),
							   0, 0)
			 );
      displayPanel.add ( highFreq,  new GridBagConstraints (3 ,0, 1, 1,  0.0, 0.0,
							   GridBagConstraints.WEST,
							   GridBagConstraints.NONE,
							   new Insets (10, 0, 10, 10),
							   0, 0)
			 );

      // Bandwidth and Overlap
      overlapBandwidthPanel = new JPanel(new GridBagLayout());

      label = new JLabel("Side Band");
      label.setFont(new Font("Dialog", 0, 12));
      label.setForeground(Color.black);
      fePanel.add(label);

      feBand = new JComboBox(); // new String[] { "usb", "lsb", "optimum" } );
      feBand.setFont(new Font("Dialog", 0, 12));
      feBand.setForeground(Color.black);
//      feBand.addActionListener ( this );
      fePanel.add(feBand);

      label = new JLabel("    Bandwidth");
      label.setFont(new Font("Dialog", 0, 12));
      label.setForeground(Color.black);
      overlapBandwidthPanel.add(label, 
				new GridBagConstraints (0, 0, 1, 1, 0.0, 0.0, 
							GridBagConstraints.LINE_START,
							GridBagConstraints.NONE,
							new Insets (10, 0, 10, 10),
							0, 0
							)
				);
      
      bandWidthChoice = new JComboBox();
      bandWidthChoice.setFont(new Font("Dialog", 0, 12));
      bandWidthChoice.setForeground(Color.black);
      double height = bandWidthChoice.getPreferredSize().getHeight();
      bandWidthChoice.setMinimumSize ( new Dimension (1, (int)height) );
      overlapBandwidthPanel.add(bandWidthChoice,
				new GridBagConstraints (1, 0, 1, 1, 1.0, 0.0, 
							GridBagConstraints.WEST,
							GridBagConstraints.HORIZONTAL,
							new Insets (10, 10, 10, 10),
							0, 0
							)
				);

      label = new JLabel("    Resolution (KHz)");
      label.setFont(new Font("Dialog", 0, 12));
      label.setForeground(Color.black);
      label.setMinimumSize ( label.getPreferredSize() );
      resolution = new JLabel();
      resolution.setBorder ( new BevelBorder ( BevelBorder.LOWERED ) );
      resolution.setFont(new Font("Dialog", 0, 12));
      resolution.setForeground(Color.black);
      resolution.setMinimumSize( resolution.getPreferredSize() );
      displayPanel.add(label,  new GridBagConstraints (4 ,0, 1, 1,  1.0, 0.0,
						       GridBagConstraints.WEST,
						       GridBagConstraints.NONE,
						       new Insets (10, 10, 10, 5),
						       0, 0)
		       );
      displayPanel.add(resolution,  new GridBagConstraints (5 ,0, 1, 1,  0.0, 0.0,
							    GridBagConstraints.WEST,
							    GridBagConstraints.NONE,
							    new Insets (10, 0, 10, 10),
							    0, 0)
		       );


      label = new JLabel("    Overlap (MHz)");
      label.setFont(new Font("Dialog", 0, 12));
      label.setForeground(Color.black);
      overlapBandwidthPanel.add (label,
				 new GridBagConstraints (3, 0, 1, 1, 0.0, 0.0, 
							 GridBagConstraints.WEST,
							 GridBagConstraints.NONE,
							 new Insets (10, 10, 10, 10),
							 0, 0
							 )
				 );

      overlap = new JTextField();
      overlap.setColumns(10);
      overlap.setText ( "0.0" );
      overlap.setMinimumSize ( new Dimension (1, (int)height) );
//      overlap.addActionListener ( this );
      overlapBandwidthPanel.add(overlap,
				new GridBagConstraints (4, 0, 1, 1, 1.0, 0.0, 
							GridBagConstraints.WEST,
							GridBagConstraints.HORIZONTAL,
							new Insets (10, 10, 10, 10),
							0, 0
							)
				);

      // Velocity
      velocityPanel = new JPanel( new GridBagLayout() );

      label = new JLabel("Velocity (km/s), Redshift");
      label.setFont(new Font("Dialog", 0, 12));
      label.setForeground(Color.black);
      velocityPanel.add(label, new GridBagConstraints ( 0, 0, 2, 1, 1.0, 0.0, 
							GridBagConstraints.WEST, 
							GridBagConstraints.NONE,
							new Insets (10, 10, 10, 0),
							0, 0)
			);

      velocityDefinition = new JComboBox();
      velocityDefinition.setFont(new Font("Dialog", 0, 12));
      velocityPanel.add(velocityDefinition, new GridBagConstraints ( 2, 0, 1, 1, 1.0, 0.0, 
							GridBagConstraints.WEST, 
							GridBagConstraints.NONE,
							new Insets (10, 5, 10, 0),
							0, 0)
			);

      velocity = new JTextField();
      velocity.setColumns(10);
      velocity.setText ( "0.0" );
      velocity.setMinimumSize( velocity.getPreferredSize() );
//      velocity.addActionListener ( this );
      velocityPanel.add(velocity, new GridBagConstraints ( 3, 0, 1, 1, 1.0, 0.0, 
							GridBagConstraints.WEST, 
							GridBagConstraints.NONE,
							new Insets (10, 5, 10, 10),
							0, 0)
			);

      label = new JLabel("Frame");
      label.setFont(new Font("Dialog", 0, 12));
      label.setForeground(Color.black);
      velocityPanel.add(label, new GridBagConstraints ( 4, 0, 1, 1, 1.0, 0.0, 
							GridBagConstraints.WEST, 
							GridBagConstraints.NONE,
							new Insets (10, 10, 10, 0),
							0, 0)
			);

      velocityFrame = new JComboBox();
      velocityFrame.setFont(new Font("Dialog", 0, 12));
      velocityPanel.add(velocityFrame, new GridBagConstraints ( 5, 0, 1, 1, 1.0, 0.0, 
							GridBagConstraints.WEST, 
							GridBagConstraints.NONE,
							new Insets (10, 5, 10, 10),
							0, 0)
			);
      

//      rangePanel = new JPanel(flowLayoutLeft);


/* Main molecular line choice - used to set front-end LO1 to put the line
   in the nominated sideband */

      moleculeChoice = new JComboBox();
      moleculeChoice.setFont(new Font("Dialog", 0, 12));
      moleculeChoice.setForeground ( Color.red );
//      moleculeChoice.addActionListener ( this );
      transitionChoice = new JComboBox();
      transitionChoice.setFont(new Font("Dialog", 0, 12));
      transitionChoice.setForeground ( Color.red );
//      transitionChoice.addActionListener ( this );
      moleculeFrequency = new JTextField();
      moleculeFrequency.setColumns(12);
      //moleculeFrequency.setText ( "0.0000" );
      moleculeFrequency.setForeground ( Color.red );
//      moleculeFrequency.addActionListener(this);
//      bandWidthChoice.addActionListener( this );
      mol1Panel = new JPanel( new GridBagLayout() );
      mol1Panel.add ( moleculeChoice, new GridBagConstraints ( 0, 0, 1, 1, 0.0, 0.0, 
							GridBagConstraints.WEST, 
							GridBagConstraints.NONE,
							new Insets (10, 10, 10, 0),
							0, 0)
		      );
      mol1Panel.add ( transitionChoice, new GridBagConstraints ( 1, 0, 1, 1, 0.0, 0.0, 
							GridBagConstraints.WEST, 
							GridBagConstraints.NONE,
							new Insets (10, 5, 10, 0),
							0, 0)
		      );
      mol1Panel.add ( moleculeFrequency, new GridBagConstraints ( 2, 0, 1, 1, 0.0, 0.0, 
							GridBagConstraints.WEST, 
							GridBagConstraints.NONE,
							new Insets (10, 5, 10, 0),
							0, 0)
		      );
      label = new JLabel("GHz");
      label.setFont(new Font("Dialog", 0, 12));
      label.setForeground(Color.black);
      mol1Panel.add (label, new GridBagConstraints ( 3, 0, 1, 1, 1.0, 0.0, 
						     GridBagConstraints.WEST, 
						     GridBagConstraints.NONE,
						     new Insets (10, 5, 10, 10),
						     0, 0)
		     );
/* Secondary moleculare line choice - displayed just for convenience of
   astronomer */

      moleculeChoice2 = new JComboBox();
      moleculeChoice2.setFont(new Font("Dialog", 0, 12));
      moleculeChoice2.setForeground ( Color.magenta );
//      moleculeChoice2.addActionListener ( this );
      transitionChoice2 = new JComboBox();
      transitionChoice2.setFont(new Font("Dialog", 0, 12));
      transitionChoice2.setForeground ( Color.magenta );
//      transitionChoice2.addActionListener ( this );
      moleculeFrequency2 = new JTextField();
      moleculeFrequency2.setColumns(12);
      moleculeFrequency2.setText ( "0.0000" );
      moleculeFrequency2.setForeground ( Color.magenta );
//      moleculeFrequency2.addActionListener(this);
      mol2Panel = new JPanel(flowLayoutRight);
      mol2Panel.add ( moleculeChoice2 );
      mol2Panel.add ( transitionChoice2 );
      mol2Panel.add ( moleculeFrequency2 );
      label = new JLabel("MHz");
      label.setFont(new Font("Dialog", 0, 12));
      label.setForeground(Color.black);
      mol2Panel.add(label);

      // Combine the molecule and velocity panels, and add an accept button to them
      // This will mean the user will not have to remember to hit return to accept the fields
      JPanel vfPanel = new JPanel();
      vfPanel.setLayout( new BoxLayout(vfPanel, BoxLayout.Y_AXIS) );
      vfPanel.setBorder ( BorderFactory.createTitledBorder ( BorderFactory.createEtchedBorder(), "Velocity and Frequency" ) );

      acceptVF.setFont(new Font("Dialog", 0, 12));
      acceptVF.setForeground(Color.black);

      vfPanel.add(velocityPanel);
      vfPanel.add(mol1Panel);
      vfPanel.add(acceptVF);


      freqEditorButton.setFont(new Font("Dialog", 0, 12));
      hideFreqEditorButton.setFont(new Font("Dialog", 0, 12));

      warningPanel = new JPanel(new GridLayout(0,1));
      label = new JLabel ("Note");
      label.setFont(new Font(label.getFont().getName(), Font.BOLD, 16));
      label.setForeground(Color.red);
      warningPanel.add(label);
      
      label = new JLabel ("Use the \"Enter\" Key to confirm edited values of VELOCITY and FREQUENCY");
      label.setFont(new Font(label.getFont().getName(), Font.PLAIN, 14));
      label.setForeground(Color.red);
      warningPanel.add(label);

      // ADDED BY SDW to allow for special DAS modes
      specialPanel = new JPanel(flowLayoutLeft); // Just use standard flow layout
      specialConfigurationLabel = new JLabel();

      specialConfigurationLabel.setFont(new Font("Dialog", 0, 12));
      specialConfigurationLabel.setForeground(Color.black);
      specialConfigurationLabel.setText ("Special Configurations");

      specialConfigurations = new JComboBox();
      specialConfigurations.setFont(new Font("Dialog", 0, 12));
      specialConfigurations.setForeground(Color.black);

      specialPanel.add(specialConfigurationLabel);
      specialPanel.add(specialConfigurations);
      

/* Assemble the display */


      northPanel = new JPanel();
      northPanel.setLayout ( new BoxLayout ( northPanel, 
        BoxLayout.Y_AXIS ) );


      northPanel.add ( fePanel );
      northPanel.add ( displayPanel );
      northPanel.add ( overlapBandwidthPanel );
      northPanel.add(specialPanel);
      northPanel.add ( vfPanel );
      add ( northPanel, BorderLayout.NORTH );


      southPanel = new JPanel();
      southPanel.add(freqEditorButton);
      southPanel.add(hideFreqEditorButton);

      add(southPanel, BorderLayout.SOUTH);
  }



}


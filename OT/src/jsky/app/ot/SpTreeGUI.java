/*
 * Copyright 2000 Association for Universities for Research in Astronomy, Inc.,
 * Observatory Control System, Gemini Telescopes Project.
 *
 * $Id$
 */

package jsky.app.ot;

import java.awt.*;
import java.awt.Dimension;
import java.awt.event.*;
import java.net.*;
import javax.swing.*;
import javax.swing.border.*;
import jsky.app.ot.gui.CommandButtonWidgetExt;
import jsky.util.gui.BasicWindowMonitor;
import ot.util.DialogUtil;
import jsky.util.gui.GenericToolBarTarget;


/** 
 * Implements the GUI layout of the user interface for the main OT window
 * (was spTree.gui with Bongo).
 *
 * @version $Revision$
 * @author Allan Brighton (ported to Swing/JSky)
 */
public class SpTreeGUI extends JPanel implements GenericToolBarTarget {

    /** The top level parent frame (or internal frame). */
    protected Component parent;

    /** Set this to the JDesktopPane, if using internal frames. */
    protected JDesktopPane desktop = null;
    
    /** The tree widget used to display the science program. */
    protected OtTreeWidget tree;

    /** Contains the editor panels. */
    protected JPanel editorPane;

    /** Action to use for the "Open" menu and toolbar items */
    protected AbstractAction openAction = new AbstractAction("Open") {
	    public void actionPerformed(ActionEvent evt) {
		try {
		    open(false);
		}
		catch(Exception e) {
		    DialogUtil.error(SpTreeGUI.this, e);
		}
	    }
	};

    /** Action to use for the "Back" menu and toolbar items */
    protected AbstractAction backAction = new AbstractAction("Back") {
	    public void actionPerformed(ActionEvent evt) {
		try {
		    back();
		}
		catch(Exception e) {
		    DialogUtil.error(SpTreeGUI.this, e);
		}
	    }
	};

    /** Action to use for the "Forward" menu and toolbar items */
    protected AbstractAction forwAction = new AbstractAction("Forward") {
	    public void actionPerformed(ActionEvent evt) {
		try {
		    forward();
		}
		catch(Exception e) {
		    DialogUtil.error(SpTreeGUI.this, e);
		}
	    }
	};

    /** Action to use for the "Stop" menu and toolbar items */
    protected AbstractAction stopAction = new AbstractAction("Stop Loading") {
	    public void actionPerformed(ActionEvent evt) {
		try {
		    interrupt();
		}
		catch(Exception e) {
		    DialogUtil.error(SpTreeGUI.this, e);
		}
	    }
	    public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		setDownloadState(enabled);
	    }
	};

    /** Action to use for the "Status" menu and toolbar items */
    protected AbstractAction statusAction = new AbstractAction("Status") {
	    public void actionPerformed(ActionEvent evt) {
		// XXX to be done...
	    }
	};

    /** Action to use for the "Save" menu and toolbar items */
    protected AbstractAction saveAction = new AbstractAction("Save") {
	    public void actionPerformed(ActionEvent evt) {
		try {
		    save();
		}
		catch(Exception e) {
		    DialogUtil.error(SpTreeGUI.this, e);
		}
	    }
	};

    /** Action to use for the "Cut" menu and toolbar items */
    protected AbstractAction cutAction = new AbstractAction("Cut") {
	    public void actionPerformed(ActionEvent evt) {
		try {
		    cut();
		}
		catch(Exception e) {
		    DialogUtil.error(SpTreeGUI.this, e);
		}
	    }
	};

    /** Action to use for the "Copy" menu and toolbar items */
    protected AbstractAction copyAction = new AbstractAction("Copy") {
	    public void actionPerformed(ActionEvent evt) {
		try {
		    copy();
		}
		catch(Exception e) {
		    DialogUtil.error(SpTreeGUI.this, e);
		}
	    }
	};

    /** Action to use for the "Paste" menu and toolbar items */
    protected AbstractAction pasteAction = new AbstractAction("Paste") {
	    public void actionPerformed(ActionEvent evt) {
		try {
		    paste();
		}
		catch(Exception e) {
		    DialogUtil.error(SpTreeGUI.this, e);
		}
	    }
	};


    // The following three actions were added for the OMP project.
    // (MFO, 09 July 2001)

    /** Action to use for the "MsbFolder" menu and toolbar items */
    protected AbstractAction msbFolderAction = new AbstractAction("MsbFolder") {
	    public void actionPerformed(ActionEvent evt) {
		try {
		    addMsbFolder();
		}
		catch(Exception e) {
		    DialogUtil.error(SpTreeGUI.this, e);
		}
	    }
	};


    /** Action to use for the "AndFolder" menu and toolbar items */
    protected AbstractAction andFolderAction = new AbstractAction("AndFolder") {
	    public void actionPerformed(ActionEvent evt) {
		try {
		    addAndFolder();
		}
		catch(Exception e) {
		    DialogUtil.error(SpTreeGUI.this, e);
		}
	    }
	};


    /** Action to use for the "OrFolder" menu and toolbar items */
    protected AbstractAction orFolderAction = new AbstractAction("OrFolder") {
	    public void actionPerformed(ActionEvent evt) {
		try {
		    addOrFolder();
		}
		catch(Exception e) {
		    DialogUtil.error(SpTreeGUI.this, e);
		}
	    }
	};


    /** Action to use for the "ObsFolder" menu and toolbar items */
    protected AbstractAction obsFolderAction = new AbstractAction("Folder") {
	    public void actionPerformed(ActionEvent evt) {
		try {
		    addFolder();
		}
		catch(Exception e) {
		    DialogUtil.error(SpTreeGUI.this, e);
		}
	    }
	};

    /** Action to use for the "ObsGroup" menu and toolbar items */
    protected AbstractAction obsGroupAction = new AbstractAction("Group") {
	    public void actionPerformed(ActionEvent evt) {
		try {
		    addGroup();
		}
		catch(Exception e) {
		    DialogUtil.error(SpTreeGUI.this, e);
		}
	    }
	};


    /** Action to use for the "Observation" menu and toolbar items */
    protected AbstractAction observationAction = new AbstractAction("Observation") {
	    public void actionPerformed(ActionEvent evt) {
		try {
		    addObservation();
		}
		catch(Exception e) {
		    DialogUtil.error(SpTreeGUI.this, e);
		}
	    }
	};


    /** Action to use for the "Note" menu and toolbar items */
    protected AbstractAction noteAction = new AbstractAction("Note") {
	    public void actionPerformed(ActionEvent evt) {
		try {
		    addNote();
		}
		catch(Exception e) {
		    DialogUtil.error(SpTreeGUI.this, e);
		}
	    }
	};


    /** Action to use for the "LibFolder" menu and toolbar items */
    protected AbstractAction libFolderAction = new AbstractAction("LibFolder") {
	    public void actionPerformed(ActionEvent evt) {
		try {
		    addLibFolder();
		}
		catch(Exception e) {
		    DialogUtil.error(SpTreeGUI.this, e);
		}
	    }
	};


    /** Action to use for the "PosEditor" menu and toolbar items */
    protected AbstractAction posEditorAction = new AbstractAction("PosEditor") {
	    public void actionPerformed(ActionEvent evt) {
		try {
		    showPositionEditor();
		}
		catch(Exception e) {
		    DialogUtil.error(SpTreeGUI.this, e);
		}
	    }
	};

    /** Action to use for the Prioritize menu and toolbar items */
    protected AbstractAction prioritizeAction = new AbstractAction("Prioritize") {
	    public void actionPerformed (ActionEvent evt) {
		try {
		    prioritize();
		}
		catch (Exception e) {
		    DialogUtil.error(SpTreeGUI.this, e);
		}
	    }
	};


    /** Action to use for the "Validation" menu and toolbar items */
    protected AbstractAction validationAction = new AbstractAction("Validation") {
	    public void actionPerformed(ActionEvent evt) {
		try {
		    doValidation();
		}
		catch(Exception e) {
		    DialogUtil.error(SpTreeGUI.this, e);
		}
	    }
	};


    /**
     * Default constructor. If you use this version, you need to call
     * setParentFrame() to set the parent frame.
     */
    public SpTreeGUI() {
	setLayout(new BorderLayout());
	backAction.setEnabled(false);
	forwAction.setEnabled(false);
	stopAction.setEnabled(false);
	libFolderAction.setEnabled(false);

	tree = new OtTreeWidget();
	tree.setBorder(new BevelBorder(BevelBorder.LOWERED));
	tree.setMinimumSize(new Dimension(200, 200));

	editorPane = new JPanel();
	editorPane.setLayout(new BorderLayout());
	editorPane.setMinimumSize(new Dimension(400, 200));

	JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, tree, editorPane);
	splitPane.setOneTouchExpandable(false);
	add("Center", splitPane);
    }


    /**
     * Make a new SpTreeGUI widget.
     *
     * @param parent the top level parent frame (or internal frame)
     */
    public SpTreeGUI(Component parent) {
	this();
	this.parent = parent;
    }


    /** Return the top level parent frame (or internal frame) used to close the window */
    public Component getParentFrame() {return parent;}

    /** Set the top level parent frame (or internal frame) used to close the window */
    public void setParentFrame(Component p) {parent = p;}

    /** Set the frame's title. */
    public void setTitle(String s) {
	if (parent != null) {
	    if (parent instanceof JFrame)
		((JFrame)parent).setTitle(s);
	    else
		((JInternalFrame)parent).setTitle(s);
	}
    }


    /** Return the frame's title. */
    public String getTitle() {
	if (parent != null) {
	    if (parent instanceof JFrame)
		return ((JFrame)parent).getTitle();
	    else
		return ((JInternalFrame)parent).getTitle();
	}
	return "";
    }



    /** Return the JDesktopPane, if using internal frames, otherwise null */
    public JDesktopPane getDesktop() {
	return desktop;
    }


    /** Set the JDesktopPane to use for top level windows, if using internal frames */
    public void setDesktop(JDesktopPane desktop) {
	this.desktop = desktop;
    }

    // The following three methods were added for the OMP project.
    // (MFO, 09 July 2001)

    /** Create an observation folder. */
    public void addMsbFolder() {
    }

    /** Create an observation folder. */
    public void addAndFolder() {
    }

    /** Create an observation folder. */
    public void addOrFolder() {
    }

    /** Create an observation folder. */
    public void addFolder() {
    }

    /** Create an observation group. */
    public void addGroup() {
    }

    /**Create an observation. */
    public void addObservation() {
    }

    /** Add a note to the tree. */
    public void addNote() {
    }

    /** Add a library folder to the tree. */
    public void addLibFolder() {
    }

    /** Cut the selected item(s) to the clipboard. */
    public void cut() {
    }

    /** Copy the selected item(s) to the clipboard. */
    public void copy() {
    }

    /** Paste the selected item(s) from the clipboard. */
    public void paste() {
    }

    /** 
     * Pop up a dialog to open a new science program.
     *
     * @param newWindow if true, open a new window, otherwise reuse this one
     */
    public void open(boolean newWindow) {
    }

    /**
     * Go back to the previous item in the history list
     */
    public void back() {
    }

    /**
     * Go forward to the next component in the history list
     */
    public void forward() {
    }

    /**
     * Stop the background loading thread if it is running 
     */
    public void interrupt() {
    }

    /** 
     * Set the state of the menubar/toolbar actions
     *
     * @param downloading set to true if a file is being downloaded
     */
    protected void setDownloadState(boolean downloading) {
    }


    /** 
     * Save the current science program.
     */
    public void save() {
    }


    /** 
     * Display the position editor.
     */
    public void showPositionEditor() {
    }

    /**
     * Prioritize all the MSBs
     */
    public void prioritize() {
    }

    /** 
     * Validate selected Science Program or Observation.
     */
    public boolean doValidation() {
      return false;
    }

    // These are for the GenericToolBarTarget interface
    public AbstractAction getOpenAction() {return openAction;}
    public AbstractAction getBackAction() {return backAction;}
    public AbstractAction getForwAction() {return forwAction;}
    public AbstractAction getStopAction() {return stopAction;}
    public AbstractAction getStatusAction() {return statusAction;}

    // access other toolbar actions
    public AbstractAction getSaveAction() {return saveAction;}
    public AbstractAction getCutAction() {return cutAction;}
    public AbstractAction getCopyAction() {return copyAction;}
    public AbstractAction getPasteAction() {return pasteAction;}
    public AbstractAction getObsFolderAction() {return obsFolderAction;}
    public AbstractAction getObsGroupAction() {return obsGroupAction;}
    public AbstractAction getObservationAction() {return observationAction;}
    public AbstractAction getNoteAction() {return noteAction;}
    public AbstractAction getLibFolderAction() {return libFolderAction;}

    // The following three methods were added for the OMP project.
    // (MFO, 09 July 2001)
    public AbstractAction getMsbFolderAction() {return msbFolderAction;}
    public AbstractAction getAndFolderAction() {return andFolderAction;}
    public AbstractAction getOrFolderAction()  {return  orFolderAction;}

    public AbstractAction getPosEditorAction() {return posEditorAction;}
    public AbstractAction getPrioritizeAction() {return prioritizeAction;}
    public AbstractAction getValidationAction() {return validationAction;}


    /**
     * test main: usage: java SpTreeGUI
     * (Only tests the basic layout).
     */
    public static void main(String[] args) {
	JFrame frame = new JFrame("SpTreeGUI");
	SpTreeGUI spTree = new SpTreeGUI();
        spTree.setPreferredSize(new Dimension(360, 400));
        frame.getContentPane().add("Center", spTree);
        frame.pack();
        frame.setVisible(true);
	frame.addWindowListener(new BasicWindowMonitor());
    }
}


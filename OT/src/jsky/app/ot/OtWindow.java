/*
 * Copyright 2000 Association for Universities for Research in Astronomy, Inc.,
 * Observatory Control System, Gemini Telescopes Project.
 *
 * $Id$
 */

package jsky.app.ot;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.geom.Point2D;
import java.io.File;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Observable;
import java.util.Observer;
import java.util.Stack;
import java.util.Vector;
import javax.swing.AbstractAction;
import javax.swing.JDesktopPane;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JLayeredPane;
import javax.swing.JMenu;
import javax.swing.JOptionPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.EventListenerList;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;
import jsky.app.ot.ProgramInfo;
import jsky.app.ot.gui.CommandButtonWidgetExt;
import jsky.app.ot.gui.CommandButtonWidgetWatcher;
import jsky.app.ot.gui.MultiSelTreeWidgetWatcher;
import jsky.app.ot.gui.TreeNodeWidgetExt;
import jsky.app.ot.gui.TreeWidgetExt;
import jsky.app.ot.gui.TreeWidgetWatcher;
import gemini.sp.SpEditChangeObserver;
import gemini.sp.SpHierarchyChangeObserver;
import gemini.sp.SpAvTable;
import gemini.sp.SpEditState;
import gemini.sp.SpFactory;
import gemini.sp.SpInsertData;
import gemini.sp.SpItem;
import gemini.sp.SpMSB;
import gemini.sp.SpObs;
import gemini.sp.SpObsFolder;
import gemini.sp.SpObsGroup;
import gemini.sp.SpPhase1;
import gemini.sp.SpProg;
import gemini.sp.SpRootItem;
import gemini.sp.SpTreeMan;
import gemini.sp.SpType;
import orac.util.OracUtilities;
import ot.phase1.Phase1HTMLDocument;
import jsky.app.ot.tpe.TelescopePosEditor;
import jsky.app.ot.tpe.TpeManager;
import jsky.app.ot.tpe.TpeManagerWatcher;
import jsky.app.ot.util.CloseableApp;
import ot.util.DialogUtil;

import orac.helptool.JHLauncher;

import orac.validation.SpValidation;
import orac.validation.ErrorMessage;
import ot.ReportBox;

import orac.ukirt.util.SpTranslator;
import gemini.sp.obsComp.SpInstObsComp;

import orac.util.FileFilterSGML;
import orac.util.FileFilterXML;

/**
 * Button manager base class.  Helper classes derived from this
 * class keep their associated button disabled or enabled appropriately.
 */
abstract class ButtonManagerBase 
    implements SpHierarchyChangeObserver, TreeWidgetWatcher {

    /** The OT tree widget */
    protected OtTreeWidget _tw;

    /** The action object for the button and/or menu item. */
    protected AbstractAction _action;

    /** The root of the science program tree */
    protected SpItem _spRoot;

    /** Initialize with the OT tree widget and the button's action object. */
    ButtonManagerBase(OtTreeWidget tw, AbstractAction action) {
	_tw     = tw;
	_tw.addWatcher(this);
	_action = action;
    }

    /** Reset to use the given science program tree. */
    public void	resetItem(SpItem spRoot) {
	if (_spRoot != null) {
	    _spRoot.getEditFSM().deleteHierarchyChangeObserver(this);
	}
	_spRoot = spRoot;
	_spRoot.getEditFSM().addHierarchyChangeObserver(this);
    }

    /** Receive notification that new items have been added to the program. */
    public void	spItemsAdded(SpItem parent, SpItem[] children, SpItem afterChild) {
	_updateButton();
    }

    /** Receive notification that items have been removed from the program. */
    public void spItemsRemoved(SpItem parent, SpItem[] children) { }

    /** Receive notification that items have been moved in the program. */
    public void	spItemsMoved(SpItem oldParent, SpItem[] children,
			     SpItem newParent, SpItem afterChild)  {
	_updateButton();
    }

    /** Receive notification that a tree node is selected. */
    public void	nodeSelected(TreeWidgetExt tw, TreeNodeWidgetExt tnw) {
	_updateButton();
    }

    /** Receive notification that a node has been acted upon. */
    public void nodeAction(TreeWidgetExt tw, TreeNodeWidgetExt tnw) {}

    /** Enable or disable the button based upon the current context. */
    abstract protected void _updateButton();

} // end ButtonManagerBase


/**
 * Base class for the ObsGroup and ObsFolder button manager classes.
 * This helper class keeps the "create obs group"/"create obs folder"
 * button disabled or enabled appropriately.  
 */
abstract class GroupingButtonManagerBase extends ButtonManagerBase
    implements MultiSelTreeWidgetWatcher {

    GroupingButtonManagerBase(OtTreeWidget tw, AbstractAction action) {
	super(tw, action);
    }

    // This method is called whenever multiple tree nodes are selected.
    public void multiNodeSelect(TreeWidgetExt tw, Vector tnwA) { 
	_updateButton(); 
    }

    protected boolean _makeGroup(SpItem newItem, boolean carryOutActions) {
	SpItem[] spItemA = _tw.getMultiSelectedItems();
	if ((spItemA == null) || (spItemA.length == 0)) {
	    spItemA = new SpItem[1];
	    spItemA[0] = _tw.getSelectedItem();
	    if (spItemA[0] == null) {
		_action.setEnabled(false);
		return false;
	    }
	}

	SpItem lastItem = spItemA[ spItemA.length - 1 ];

	SpInsertData spID = SpTreeMan.evalInsertAfter(newItem, lastItem);
	if (spID == null) {
	    _action.setEnabled(false);
	    return false;
	}

	SpInsertData spID2 = SpTreeMan.evalInsertInside(spItemA, newItem);
	if (spID2 == null) {
	    _action.setEnabled(false);
	    return false;
	}

	_action.setEnabled(true);
	if (carryOutActions) {
	    SpTreeMan.insert(spID);
	    SpTreeMan.move(spID2);
	}
	return true;
    }

} 

/**
 * The ButtonManager for the ObsGroup button.
 */
class ObsGroupButtonManager extends GroupingButtonManagerBase {
    private SpObsGroup _obsGroup = (SpObsGroup) SpFactory.create(SpType.OBSERVATION_GROUP);

    ObsGroupButtonManager(OtTreeWidget tw, AbstractAction action) {
	super(tw, action);
    }

    /**
     * Enable or disable the button based upon the current context.
     */
    protected void _updateButton() {
	// MFO: leave button enabled like in FreeBongo OT.
	//_makeGroup(_obsGroup, false);
    }

    /** Create an observation group. */
    public void addGroup() {
	_tw.addItem( SpFactory.create(SpType.OBSERVATION_GROUP));
	if (_makeGroup(_obsGroup, true)) {
	    // Create a new ObsGroup for next time
	    _obsGroup = (SpObsGroup) SpFactory.create(SpType.OBSERVATION_GROUP);
	}
    }

} // end ObsGroupButtonManager




//
// The ButtonManager for the ObsFolder button.
//
class ObsFolderButtonManager extends GroupingButtonManagerBase
{
    private SpObsFolder _obsFolder = (SpObsFolder) SpFactory.create(SpType.OBSERVATION_FOLDER);

    ObsFolderButtonManager(OtTreeWidget tw, AbstractAction action) {
	super(tw, action);
    }

    /** Enable or disable the button based upon the current context. */
    protected void _updateButton() {
	// MFO: leave button enabled like in FreeBongo OT.
	//_makeGroup(_obsFolder, false);
    }

    /** Create an observation folder. */
    public void addFolder() {
	_tw.addItem(SpFactory.create(SpType.OBSERVATION_FOLDER));
	if (_makeGroup(_obsFolder, true)) {
	    // Create a new folder for next time
	    _obsFolder = (SpObsFolder) SpFactory.create(SpType.OBSERVATION_FOLDER);
	}
    }

} // end ObsFolderButtonManager


/** 
 * The OtWindow class controls the main editing window for a Science
 * Program.  In the GUI it displays, the hierarchy of the program is
 * shown and may be manipulated.  This class initializes the main
 * window and handles basic interaction such as button press events
 * and menu choices.
 *
 * @version $Revision$
 * @author Allan Brighton (ported to Swing/JSky, changed the layout)
 */
public class OtWindow extends SpTreeGUI  
    implements SpEditChangeObserver, TpeManagerWatcher, CloseableApp {

    /** Displays the science program hierarchy. */
    protected OtTreeWidget        _tw;

    /** The editor window for the selected node */
    protected OtItemEditorWindow  _itemEditor;

    /** The current science program root */
    protected SpItem              _curItem;

    protected ProgramInfo         _progInfo;

    /** Manages the Observation Group enabled state */
    protected ObsGroupButtonManager  _obsGroupButtonMan;

    /** Manages the Observation Folder enabled state */
    protected ObsFolderButtonManager _obsFolderButtonMan;

    /** Used to go back to a previous science program */
    protected Stack backStack = new Stack();

    /** Used to go forward to the next science program */
    protected Stack forwStack = new Stack();

    /** Set when the back or forward actions are active to avoid the normal history handling */
    protected boolean noStack = false;

    /** List of OtHistoryItem, for previously viewed science programs. */
    protected LinkedList historyList = new LinkedList();

    /** list of listeners for change events */
    protected EventListenerList listenerList = new EventListenerList();

    // used to exit the application when the last main frame is closed
    static protected int             _frameCount = 0;

    /** XML file filter (*.xml). MFO 04 June 2001 */
    protected FileFilterXML   xmlFilter = new FileFilterXML();

    /** SGML file filter (*.ot, *.sp, *.sgml). MFO 04 June 2001 */
    protected FileFilterSGML sgmlFilter = new FileFilterSGML();

    /**
     * Create an OtWindow of the appropriate type.
     */
    public static void create(SpRootItem spItem, FileInfo fi) {
	SpType spType = spItem.type();
	OtWindow editor = null;
	if (spType.equals(SpType.SCIENCE_PROGRAM) 
	    || spType.equals(SpType.SCIENCE_PLAN)
	    || spType.equals(SpType.LIBRARY)) {
	    editor = new OtProgWindow(spItem, fi);
	} 
	else {
	    DialogUtil.error(editor, "Can't create an OtWindow for type: " + spItem.type());
	    return;
	}
	
	// use internal frames?
	JDesktopPane desktop = OT.getDesktop();
	if (desktop == null) {
	    new OtWindowFrame(editor);
	}
	else {
	    Component c = new OtWindowInternalFrame(editor);
	    desktop.add(c, JLayeredPane.DEFAULT_LAYER);
	    desktop.moveToFront(c);
	}
    }
    

    /**
     * Construct with a brand new program.
     */
    protected OtWindow(SpRootItem spItem) {
	this(spItem, null);
    }

    /**
     * This constructor does all the work of initializing the OtWindow.
     */
    protected OtWindow(SpRootItem spItem, FileInfo fileInfo) {
	_init(spItem, fileInfo);
	_frameCount++;
    }

    /**
     * Do one-time only initialization of the window.
     */
    protected void _init(SpRootItem spItem, FileInfo fileInfo) {
	// Initialize the ProgramInfo structure
	_progInfo         = new ProgramInfo();

	ProgramInfo.register(_progInfo, spItem);

	if (fileInfo == null) {
	    // Init FileInfo from attributes in the spItem if present.  This will
	    // allow programs obtained from the database to have their filename
	    // and directory already set.
	    fileInfo = new FileInfo();
	    SpAvTable avTab = spItem.getTable();

	    String  dir          = OT.getOtUserDir();
	    String  filename     = avTab.get(".gui.filename");
	    boolean hasBeenSaved = avTab.getBool(".gui.hasBeenSaved");
	    if ((dir != null) && (filename != null)) {
		fileInfo.dir          = dir;
		fileInfo.filename     = filename;
		fileInfo.hasBeenSaved = hasBeenSaved;
	    }
	    // Added by MFO (04 June 2001)
	    else {
                fileInfo.dir          = avTab.get(".gui.dir");
	    }
	}
	_progInfo.file = fileInfo;

	// Set up the tree widget
	_tw = this.tree;
	_tw.setInfo(_progInfo);
	_tw.addWatcher( new TreeWidgetWatcher() {
		public void nodeSelected(TreeWidgetExt tw, TreeNodeWidgetExt tnw) {
		    OtWindow.this.nodeSelect(tw, tnw);
		}
		public void nodeAction(TreeWidgetExt tw, TreeNodeWidgetExt tnw) {
		    OtWindow.this.nodeAction(tw, tnw);
		}
	    });

	_obsGroupButtonMan = new ObsGroupButtonManager(_tw, obsGroupAction);
	_obsFolderButtonMan = new ObsFolderButtonManager(_tw, obsFolderAction);

	// Create the item editor window
	if (_itemEditor == null) {
	    _itemEditor = new OtItemEditorWindow();
	    editorPane.add("Center", _itemEditor);
	    _itemEditor.setParentFrame(parent);
	    _itemEditor.setOtWindow(this);
	}
	_itemEditor.setInfo(_progInfo);

	// Initialize the title
	_updateTitle(spItem);

	_updateEditPencil( spItem.getEditState() );

	// Watch filename changes
	_progInfo.file.addObserver(new Observer() {
	 	public void update(Observable obs, Object arg) {
		    _updateTitle();
		}
	    });

	// Watch when a position editor is opened.
	TpeManager.addWatcher(this, spItem);

	// Set the program displayed in the tree.
	resetItem(spItem);
    }

    /**
     * Called when a new program is loaded in the same window to reinitialize everything.
     */
    protected void _reinit(SpRootItem spItem, FileInfo fileInfo) {
	// Initialize the ProgramInfo structure
	_progInfo         = new ProgramInfo();

	ProgramInfo.register(_progInfo, spItem);

	if (fileInfo == null) {
	    // Init FileInfo from attributes in the spItem if present.  This will
	    // allow programs obtained from the database to have their filename
	    // and directory already set.
	    fileInfo = new FileInfo();
	    SpAvTable avTab = spItem.getTable();

	    String  dir          = OT.getOtUserDir();
	    String  filename     = avTab.get(".gui.filename");
	    boolean hasBeenSaved = avTab.getBool(".gui.hasBeenSaved");
	    if ((dir != null) && (filename != null)) {
		fileInfo.dir          = dir;
		fileInfo.filename     = filename;
		fileInfo.hasBeenSaved = hasBeenSaved;
	    }
	    else {
		dir = avTab.get(".gui.dir");
	    }
	}
	_progInfo.file = fileInfo;

	// Set up the tree widget
	_tw.setInfo(_progInfo);
	_itemEditor.setInfo(_progInfo);

	// Initialize the title
	_updateTitle(spItem);

	_updateEditPencil( spItem.getEditState() );

	// Watch when a position editor is opened.
	TpeManager.addWatcher(this, spItem);

	// Set the program displayed in the tree.
	resetItem(spItem);
	
	// notify any listeners that a new program was loaded
	fireChange(new ChangeEvent(this));
    }


    /** Set the top level parent frame (or internal frame) used to close the window */
    public void setParentFrame(Component p) {
	super.setParentFrame(p);
	if (_itemEditor != null)
	    _itemEditor.setParentFrame(p);
    }

    /** Return true if the program has been saved */
    public boolean progHasBeenSaved() {
	return _progInfo.file.hasBeenSaved;
    }

    /**
     * Get the item being edited.
     */
    public SpRootItem getItem() {
	return _tw.getProg();
    }


    /** Return the OT tree widget */
    public OtTreeWidget getTreeWidget() {
	return _tw;
    }


    /**
     * Has the program been edited?
     */
    public boolean isEdited() {
	return (getItem().getEditState() == SpEditState.EDITED);
    }

    /**
     * Reset the item being edited.  The given SpItem is assumed to be a
     * different version of the item already being edited.  So the _progInfo
     * fields should remain the same.
     */
    void resetItem(SpRootItem spItem) {
	SpRootItem oldItem = getItem();
	if (oldItem != null) {
	    TpeManager.remap(oldItem, spItem);
	    oldItem.getEditFSM().deleteEditChangeObserver(this); 
	}

	if(spItem != oldItem) {
	  _tw.resetProg(spItem);
	}

	// Initialize the title
	_updateEditPencil( spItem.getEditState() );
	spItem.getEditFSM().addEditChangeObserver(this);

	_obsGroupButtonMan.resetItem(spItem);
	_obsFolderButtonMan.resetItem(spItem);
    }

    /**
     * Determine whether it is okay to close this program, prompting the
     * user if the program has been edited.
     */
    protected boolean isOkayToClose()  {
	// If not edited, then it is okay to close this program.
	if (!isEdited()) {
	    return true;
	}

	// The program was edited, so now check whether we should prompt
	// the user or not.  If not, it is okay to close this program.
	if (!OtProps.isSaveShouldPrompt()) {
	    return true;
	}

	// The program was edited and we should prompt, so do so.
	String msg = "Save changes to " + getItemType() + " '" +  getFilename() + "'?";
	int response = DialogUtil.confirm(this, msg);
	if (response == JOptionPane.CANCEL_OPTION) {
	    return false;
	}

	// If the user wants to save changes, it is okay to close if the
	// save succeeds.
	if (response == JOptionPane.YES_OPTION) {
	    return doSaveChanges();
	}

	// The user didn't want to save changes, so go ahead and close.
	return true;
    }

    /**
     * Save the changes, returning true if successful.
     */
    public boolean doSaveChanges() {
	return OtFileIO.save(getItem(), _progInfo.file);
    }

    /**
     * Save the observation as an ORAC Sequence, returning true if successful.
     *
     * MFO: copied from FreeBongo OT (orac2) and modified.
     */
    public boolean doSaveSequence() {
      OtTreeNodeWidget tnw;
      tnw = (OtTreeNodeWidget) OtWindow.this._tw.getSelectedNode();
      SpItem spitem = tnw.getItem();

      // Initialise sequence name
      String seqName = "None";
      String seqDir  = "";

      // Check if this is an Observation.
      if ( !( spitem.type().equals( SpType.OBSERVATION ) ) ) {
         spitem = SpTranslator.findSpObs( spitem );
      }
      if ( spitem != null ) {

        SpObs spobs = (SpObs) spitem;

        // Get the instrumnet in scope
        SpInstObsComp inst = ((SpInstObsComp) SpTreeMan.findInstrument(spitem));
        
        // Create a translator class and do the translation
        SpTranslator spt = new SpTranslator (spobs);
        try {
	    spt.setConfigDirectory(OT.getOtUserDir());
	    spt.setSequenceDirectory(OT.getOtUserDir());
	    seqName = spt.translate();
	    seqDir  = spt.getSequenceDirectory();
        }catch (Exception ex) {
	    DialogUtil.error(this, "Exception whilst translating:\n "+ex.getMessage());
	    return false;
        }

      }else{
        // The user didn't select an observation item for the translation.
        DialogUtil.error(this, "Selected node is not an observation or within an observation");
        return false;
      }

      DialogUtil.message(this, "Observation saved to " + seqDir + seqName);
  
      return true;
    }

    /**
     * Close this window if possible.  Return false if not.  The reason
     * why the program might not be closeable is if it has been edited and
     * the user wants to cancel the close instead of saving or ignoring
     * changes.
     *
     * @see CloseableApp
     */
    public boolean closeApp() {
	if (!isOkayToClose()) return false;

	SpItem spItem = _tw.getProg();
	spItem.getEditFSM().deleteEditChangeObserver(this); 

	TpeManager.remove( spItem );
	TpeManager.deleteWatchers( spItem );

	JDesktopPane desktop = OT.getDesktop();
	Component f;

	if (desktop == null) {
	    ((JFrame)parent).setVisible(false);
	    ((JFrame)parent).dispose();
	    OT.removeOtWindowFrame((OtWindowFrame)parent);
	}
	else {
	    parent.setVisible(false);
	    ((JInternalFrame)parent).dispose();
	}

	return true;
    }

    /**
     * Is Phase 1 proposal information available for the current program?
     */
    public boolean isPhase1InfoAvailable()  {
	// First, does this site support a way to create phase 1 html
	// documents for the Phase 1 Browser?
	if (!OtCfg.phase1Available()) {
	    return false;
	}

	SpItem spRoot = _tw.getProg();

	// Is the document a Science Program.  If not, it doesn't have phase 1
	// info.
	if (!(spRoot instanceof SpProg)) {
	    return false;
	}

	// Does the Science Program have phase 1 info.  If created from scratch
	// (not from a Phase 1 tool like the Gemini Phase1Gui) it will not.
	SpPhase1 p1 = ((SpProg) spRoot).getPhase1Item();
	return (p1 != null);
    }

    /**
     * Show the Phase1 information associated with this program.
     */
    public void	showPhase1()  {
	SpItem spRoot = _tw.getProg();

	if (!(spRoot instanceof SpProg)) {
	    DialogUtil.message(this, "Phase 1 information is only available for Science Programs.");
	    return;
	}

	SpPhase1 p1 = ((SpProg) spRoot).getPhase1Item();
	if (p1 == null) {
	    DialogUtil.message(this, "There is no Phase 1 Information for this Science Program.");
	    return;
	}

	Phase1HTMLDocument doc = OtCfg.createHTMLDocument();

	if (doc == null) {
	    String msg = "This installation does not support Phase 1 Info browsing.";
	    DialogUtil.message(this, msg);
	    return;
	}

	try {
	    doc.generate(p1);
	} catch (Exception ex) {
	    System.out.println("Couldn't generate Phase1 information: " + ex);
	    ex.printStackTrace();
	    return;
	}

	/* XXX allan 
	Phase1Browser p1b = Phase1Browser.getBrowser();
	p1b.setPhase1HTMLDocument(doc);
	XXX allan */
    }

    /**
     * Print the program being edit to stdout.  This is a debugging method.
     */
    public void	showProg() {
	_tw.getProg().print();
    }

    /**
     * A position editor has been opened.
     *
     * @see TpeManagerWatcher
     */
    public void	tpeOpened(TelescopePosEditor tpe) {
	//tpe.setTitleColor(getTitleColor());
	tpe.setTitle("Position Editor");
    }

    /**
     * Get the filename associated with the program, plan, or whatever.
     */
    public String getFilename() {
	return _progInfo.file.filename;
    }

    /**
     * Get the human readable name of the type of the thing being edited
     * (for example, "Science Program", "Science Plan", or "Library").
     */
    public String getItemType() {
	return getItem().type().getReadable();
    }

    /**
     * Update the title of the program or plan being edited.
     */
    protected void _updateTitle()  {
	_updateTitle(_tw.getProg());
    }

    /**
     * Update the title of the program or plan being edited.
     */
    protected void _updateTitle(SpItem spItem) {
	String prefix = "";
	if (spItem.type().equals(SpType.SCIENCE_PROGRAM)) {
	    prefix += "Program: ";
	} else if (spItem.type().equals(SpType.SCIENCE_PLAN)) {
	    prefix += "Plan: ";
	} else if (spItem.type().equals(SpType.LIBRARY)) {
	    prefix += "Library: ";
	} else {
	    prefix += "UNKOWN TYPE: ";
	}

	setTitle(prefix + spItem.getTitle());
	if (parent != null)
	    parent.setName(spItem.getTitle());

	TelescopePosEditor tpe = TpeManager.get(spItem);
	if (tpe != null) {
	    tpe.setTitle("Position Editor (" + getTitle() + ")");
	}
    }

    //
    // Update the edit pencil depending upon the edit state of the program.
    // If the program has been edited, the pencil should be showing.  Otherwise
    // it should be hidden.
    //
    private void _updateEditPencil(int state) {
	if (state == SpEditState.EDITED) {
	    saveAction.setEnabled(true);
	} else {
	    saveAction.setEnabled(false);
	}
    }

    /**
     * Receive notification that the program edit state has changed.
     */
    public void	spEditStateChange(SpItem rootNode)  {
	_updateEditPencil( rootNode.getEditState() );
    }

    /**
     * This method is called whenever a tree node is selected.
     */
    public void nodeSelect(TreeWidgetExt tw, TreeNodeWidgetExt tnw) {
	SpItem spItem = ((OtTreeNodeWidget) tnw).getItem();

	_itemEditor.setItem( spItem );
	TelescopePosEditor tpe = TpeManager.get( spItem );
	if (tpe != null) tpe.reset( spItem );

	_curItem = spItem;
    }

    /**
     * This method is called whenever a tree node is double clicked.
     */
    public void	nodeAction(TreeWidgetExt tw, TreeNodeWidgetExt tnw)  {
	_itemEditor.setItem( ((OtTreeNodeWidget) tnw).getItem() );
    }


    // The following three methods were added for the OMP project.
    // (MFO, 09 July 2001)

    /** Create an observation folder. */
    public void addMsbFolder() {
	_tw.addItem(SpFactory.create(SpType.MSB_FOLDER));
    }

    /** Create an observation folder. */
    public void addAndFolder() {
	_tw.addItem(SpFactory.create(SpType.AND_FOLDER));
    }

    /** Create an observation folder. */
    public void addOrFolder() {
	_tw.addItem(SpFactory.create(SpType.OR_FOLDER));
    }

    /** Create an observation folder. */
    public void addFolder() {
        // MFO May 28 2001
	_tw.addItem(SpFactory.create(SpType.OBSERVATION_FOLDER));
	//_obsFolderButtonMan.addFolder();
    }

    /** Create an observation group. */
    public void addGroup() {
	// MFO May 28 2001
	_tw.addItem( SpFactory.create(SpType.OBSERVATION_GROUP));
	//_obsGroupButtonMan.addGroup();
    }

    /**Create an observation. */
    public void addObservation() {
	// MFO, 23 October 2001
	// After spObs has been added call spObs.updateMsbAttributes().
	// This will set attributes in spObs according to whether spObs is the
	// child node of an SpMSB or whether spObs is an MSB in its own right.
	SpObs spObs = (SpObs)SpFactory.create(SpType.OBSERVATION);
	_tw.addItem(spObs);
	spObs.updateMsbAttributes();

	// Update collapsed/expanded display for tree nodes so that the SpIterFolder inside
	// the Observation is displayed as expanded.
	_tw.updateNodeExpansions();
    }

    /** Add a note to the tree. */
    public void addNote() {
	_tw.insertItemAfter(SpFactory.create(SpType.NOTE));
    }

    /** Cut the selected item(s) to the clipboard. */
    public void cut() {
	_tw.cutToClipboard();
    }

    /** Copy the selected item(s) to the clipboard. */
    public void copy() {
	_tw.copyToClipboard();
    }

    /** Paste the selected item(s) from the clipboard. */
    public void paste() {
	_tw.pasteFromClipboard();
    }

    /** 
     * Display a new science program window.
     */
    public void newProgram() {
	JDesktopPane desktop = OT.getDesktop();
	if (desktop == null) {
	    new OtWindowFrame(new OtProgWindow());
	}
	else {
	    Component c = new OtWindowInternalFrame(new OtProgWindow());
	    desktop.add(c, JLayeredPane.DEFAULT_LAYER);
	    desktop.moveToFront(c);
	}
    }


    /** 
     * Pop up a dialog to open a new science program.
     *
     * @param newWindow if true, open a new window, otherwise reuse this one
     */
    public void open(boolean newWindow) {
	if (newWindow) {
	    // open in a new window
	    OtFileIO.open();
	}
	else {
	    if (!isOkayToClose()) 
		return;

	    // open in this window
	    JFileChooser fileChooser = new JFileChooser(OT.getOtUserDir());
            fileChooser.addChoosableFileFilter(xmlFilter);
            fileChooser.addChoosableFileFilter(sgmlFilter);

            if(System.getProperty("OMP") != null) {
              fileChooser.setFileFilter(xmlFilter);
            }
            else {
              fileChooser.setFileFilter(sgmlFilter);
            }

	    int option = fileChooser.showOpenDialog(null);
	    if (option != JFileChooser.APPROVE_OPTION || fileChooser.getSelectedFile() == null) 
		return;

            if(fileChooser.getFileFilter() instanceof FileFilterXML) {
              OtFileIO.setXML(true);
            }
            else {
              OtFileIO.setXML(false);
            }

	    File file = fileChooser.getSelectedFile();
	    if (file != null)
		open(file);
	}
    }

    /** 
     * Load the given science program file in this window.
     *
     * @param file the file containing the program
     */
    public void open(File file) {
	String dir = file.getParent();
	String filename = file.getName();
	if (filename == null) {
	    return;
	}
	OtFileIO.setXML(true);
	SpRootItem spItem = OtFileIO.fetchSp( dir, filename );
	if (spItem != null) {
	    FileInfo fi = new FileInfo(dir, filename, true);
	    open(spItem, fi);
	}
    }

    /** 
     * Load the given science program in this window.
     *
     * @param spItem the program to load
     * @param fi contains information about the file (may be null)
     */
    public void open(SpRootItem spItem, FileInfo fi) {
	addToHistory();
	_reinit(spItem, fi);
    }


    /** 
     * Save the current science program.
     */
    public void save() {
	doSaveChanges();
    }

    /**
     * Go back to the previous item in the history list
     */
    public void back() {
	if (backStack.size() == 0)
	    return;

	SpRootItem rootItem = getItem();
	if (rootItem != null) {
	    String title = rootItem.getTitle();
	    FileInfo fi = _progInfo.file;
	    String filename = fi.dir + File.separatorChar + fi.filename;
	    forwStack.push(new OtHistoryItem(title, filename, rootItem));
	    forwAction.setEnabled(true);
	}
	    
	OtHistoryItem item = (OtHistoryItem)backStack.pop();
	if (backStack.size() == 0)
	    backAction.setEnabled(false);
	noStack = true;
	try {
	    item.actionPerformed((ActionEvent)null);
	}
	catch(Exception e) {
	    DialogUtil.error(this, e);
	}
	noStack = false;
    }

    /**
     * Go forward to the next component in the history list
     */
    public void forward() {
	if (forwStack.size() == 0)
	    return;

	SpRootItem rootItem = getItem();
	if (rootItem != null) {
	    String title = rootItem.getTitle();
	    FileInfo fi = _progInfo.file;
	    String filename = fi.dir + File.separatorChar + fi.filename;
	    backStack.push(new OtHistoryItem(title, filename, rootItem));
	    backAction.setEnabled(true);
	}
	    
	OtHistoryItem item = (OtHistoryItem)forwStack.pop();
	if (forwStack.size() == 0)
	    forwAction.setEnabled(false);
	noStack = true;
	try {
	    item.actionPerformed((ActionEvent)null);
	}
	catch(Exception e) {
	    DialogUtil.error(this, e);
	}
	noStack = false;
    }

    /** 
     * Add the current science program to the history list
     */
    protected void addToHistory() {
	SpRootItem rootItem = getItem();
	if (!noStack && rootItem != null) {
	    String title = rootItem.getTitle();
	    FileInfo fi = _progInfo.file;
	    String filename = fi.dir + File.separatorChar + fi.filename;
	    OtHistoryItem item = new OtHistoryItem(title, filename, rootItem);

	    backStack.push(item);
	    backAction.setEnabled(true);
	    if (forwStack.size() != 0) {
		cleanupHistoryStack(forwStack);
		forwStack.clear();
		forwAction.setEnabled(false);
	    }

	    // add to the history list and remove duplicates
	    ListIterator it = ((LinkedList)historyList.clone()).listIterator(0);
	    for(int i = 0; it.hasNext(); i++) {
		OtHistoryItem hi = (OtHistoryItem)it.next();
		if (hi.title.equals(title))
		    historyList.remove(i);
	    }
	    historyList.addFirst(item);
	    if (historyList.size() > 20)
		historyList.removeLast();
	}
    }

    /** Add history items (for previously displayed science programs) to the given menu */
    public void addHistoryMenuItems(JMenu menu) {
	ListIterator it = historyList.listIterator(0);
	while(it.hasNext()) {
	    menu.add((OtHistoryItem)it.next());
	}
    }

    /** 
     * This method may be redefined in subclasses to do cleanup work before components are 
     * removed from the given history stack (backStack or forwStack).
     */
    protected void cleanupHistoryStack(Stack stack) {
    }


    /** 
     * Display the position editor.
     */
    public void showPositionEditor() {
	// Figure out which node is selected.
	OtTreeNodeWidget destTNW = (OtTreeNodeWidget) _tw.getSelectedNode();
	if (destTNW == null) {
	    destTNW = (OtTreeNodeWidget) _tw.getRoot();
	}
	SpItem spItem = destTNW.getItem();

	TpeManager.open(spItem);
    }

    /**
     * @return true  if selected component has been checked and found valid.
     *         false if the component has been checked and found invalid
     *               OR if the component has not been checked because it is neither an observation nor a science program.
     */
    public boolean doValidation() {  
      SpItem       spItem         = _tw.getSelectedItem();
      String       reportBoxTitle = "Validation Report";
   
      // checking whether an item has been selected that can be checked and
      // returning false otherwise.
      if(!spItem.type().equals(SpType.SCIENCE_PROGRAM) && !spItem.type().equals(SpType.OBSERVATION)) {
        new ReportBox("Please select an observation or science program.");
        return false;
      }

      if(OtCfg.telescopeUtil == null) {
        new ReportBox("Could not find validation tool.\n" + 
	              "Make sure a telescope cfg class is specified in the ot.cfg file.", reportBoxTitle);
      
        return false;
      }
      
      SpValidation spValidation = OtCfg.telescopeUtil.getValidationTool();

      if(spValidation == null) {
        new ReportBox("Could not find validation tool.", reportBoxTitle);

	return false;
      }
      
      Vector          report    = new Vector();
      String          component;

      if(spItem instanceof SpObs) {
        ErrorMessage.reset();
        spValidation.checkObservation((SpObs)spItem, report);
        component = "Observation";
      }
      else { //if(spItem.type().equals(SpType.SCIENCE_PROGRAM))
        ErrorMessage.reset();
        spValidation.checkSciProgram((SpProg)spItem, report);
        component = "Science Program";
      }
   
      // at the moment there is no difference in how errors and warnings are handled.
      if(ErrorMessage.getErrorCount() > 0) {
        new ReportBox(ErrorMessage.messagesToString(report.elements()), reportBoxTitle);
     
        return false;
      }
      else if(ErrorMessage.getWarningCount() > 0) {
        new ReportBox(ErrorMessage.messagesToString(report.elements()), reportBoxTitle);
     
        return false;
      }
      else {
        new ReportBox(component + " settings are valid.", reportBoxTitle);
     
        return true;
      }
    }


    /** 
     * Display a file chooser so that the user can select the name of a file to
     * save the current science program to.
     */
    public void saveAs() {
	SpRootItem spItem = OtFileIO.saveAs(getItem(), _progInfo.file);
	if (spItem != null) {
	    resetItem(spItem);
	}
    }


    /** 
     * Revert to the last saved version of the current science program.
     */
    public void revertToSaved() {
	SpRootItem spItem;
	spItem = OtFileIO.revertToSaved(_progInfo.file);
	if (spItem != null) {
	    resetItem(spItem);
	}
    }

    /**
     * Launch the help tool
     * ORAC (UKIRT) Added item. AB 28-Sep-1999, M.Folger@roe.ac.uk 30/01/2001
     */
    public void launchHelp() {
	if(OT.getHelpLauncher() != null) {
	  OT.getHelpLauncher().launch();
	}
	else {
	    String[] args={"-helpset", System.getProperty("ot.cfgdir", "jsky/app/ot/cfg/") + "help/othelp.hs"};
            OT.setHelpLauncher(new JHLauncher(args));
            //System.out.println ("Help tool launched");
	}
    }

    /** 
     * Exit the application.
     */
    public void exit() {
	if (closeApp())
	    System.exit(0);
    }


    /** 
     * Close this science program and all related windows.
     */
    public void close() {
	closeApp();
    }


    /** 
     * Pop up a dialog to allow the user to edit the science program title.
     */
    public void editItemTitle() {
	OtTreeNodeWidget tnw = (OtTreeNodeWidget) _tw.getSelectedNode();
	//new TitleChangeWindow(tnw.getItem());
	String s = DialogUtil.input(this, "New Title:");
	if (s != null && s.length() != 0) {
	    _curItem.setTitleAttr(s);
	}
    }

    /**
     * register to receive change events from this object whenever 
     * a new science program is loaded.
     */
    public void addChangeListener(ChangeListener l) {
        listenerList.add(ChangeListener.class, l);
    }
    

    /**
     * Stop receiving change events from this object.
     */
    public void removeChangeListener(ChangeListener l) {
        listenerList.remove(ChangeListener.class, l);
    }


    /**
     * Notify any listeners that a new science program was loaded.
     */
    protected void fireChange(ChangeEvent e) {
        Object[] listeners = listenerList.getListenerList();
        for (int i = listeners.length - 2; i >= 0; i -=2 ) {
            if (listeners[i] == ChangeListener.class) {
                ((ChangeListener)listeners[i+1]).stateChanged(e);
            }   
        }
    }
   
    /** 
     * Local class used to store information about previously viewed science programs.
     * During a given session, the program is saved and can be redisplayed
     * if needed. If the application is restarted, the filename can be used instead.
     */
    protected class OtHistoryItem extends AbstractAction {
	/** the origial filename */
	public String filename;
	
	/** The science program. */
	public SpRootItem spItem;

	/** The item's title */
	public String title;

	/** 
	 * Create an OT history item with the given title (for display),
	 * filename, and SpItem. The SpItem is used during this session, 
	 * otherwise the data is read again from the file.
	 */
	public OtHistoryItem(String title, String filename, SpRootItem spItem) {
	    super(title);
	    this.title = title;
	    this.filename = filename;
	    this.spItem = spItem;
	}

	// load the SpItem if it exists, otherwise the file
	public void actionPerformed(ActionEvent evt) {
	    if (spItem != null) {
		FileInfo fileInfo = null;
		if (filename != null) {
		    File file = new File(filename);
		    String dir = file.getParent();
		    String name = file.getName();
		    fileInfo = new FileInfo(dir, name, true);
		}
		open(spItem, fileInfo);
	    }
	    else if (filename != null) {
		File file = new File(filename);
		open(file);
	    }
	}
    }
}


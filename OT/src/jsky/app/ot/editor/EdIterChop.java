// Copyright 1997 Association for Universities for Research in Astronomy, Inc.,
// Observatory Control System, Gemini Telescopes Project.
// See the file COPYRIGHT for complete details.
//
// $Id$
//
package jsky.app.ot.editor;

import java.awt.Event;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.util.Hashtable;
import java.util.List;
import java.util.Vector;
import java.util.Observer;
import java.util.Observable;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.event.TableModelListener;
import javax.swing.event.TableModelEvent;
import javax.swing.DefaultListSelectionModel;

import jsky.app.ot.OtCfg;
import jsky.app.ot.gui.TableWidgetExt;
import jsky.app.ot.gui.TableWidgetWatcher;
import jsky.app.ot.gui.ListBoxWidgetWatcher;
import jsky.app.ot.gui.TextBoxWidgetExt;
import jsky.app.ot.gui.TextBoxWidgetWatcher;
import jsky.app.ot.gui.DropDownListBoxWidgetExt;
import jsky.app.ot.gui.DropDownListBoxWidgetWatcher;
import jsky.app.ot.tpe.TpeManager;

import gemini.sp.SpAvTable;
import gemini.sp.SpItem;
import gemini.sp.SpAvEditState;
import gemini.sp.iter.SpIterChop;




/**
 * Editor for Chop iterator.
 *
 * @author Based on EdIterGenericConfig (Gemini OT), modified by Martin Folger (M.Folger@roe.ac.uk).
 */
public class EdIterChop extends OtItemEditor
    implements DropDownListBoxWidgetWatcher, TextBoxWidgetWatcher,
               TableWidgetWatcher, ActionListener, TableModelListener, Observer {
    
    // The iteration table widget.
    private TableWidgetExt _iterTab;
	
    // Maps attribute names to IterConfigItems.
    private Hashtable  _iterItems;

    private SpIterChop _iterChop;


    // The GUI layout
    IterChopGUI        _w;

    /** If true, ignore table model events. */
    private boolean _ignoreGuiEvents = false;


    /** Default constructor */
    public EdIterChop() {
	_title       = "Chop Iterator";
	_presSource  = _w = new IterChopGUI();
	_description = "Iterate over a chop configurations.";

	// add button action listeners
//	_w.deleteTest.addActionListener(this);
	_w.addStep.addActionListener(this);
	_w.deleteStep.addActionListener(this);
	_w.top.addActionListener(this);
	_w.up.addActionListener(this);
	_w.down.addActionListener(this);
	_w.bottom.addActionListener(this);

	// JBuilder has some problems with image buttons...
	ClassLoader cl = ClassLoader.getSystemClassLoader();
        _w.top.setIcon(new ImageIcon(cl.getResource("jsky/app/ot/images/top.gif")));
        _w.up.setIcon(new ImageIcon(cl.getResource("jsky/app/ot/images/up.gif")));
        _w.bottom.setIcon(new ImageIcon(cl.getResource("jsky/app/ot/images/bottom.gif")));
        _w.down.setIcon(new ImageIcon(cl.getResource("jsky/app/ot/images/down.gif")));

        // MFO: This is probaly JCMT specific. Might need modification
	// when the Chop iterator is used with UKIRT.
        _w.coordFrameListBox.setChoices(OtCfg.telescopeUtil.getCoordSysFor(OtCfg.telescopeUtil.CHOP));
	if(_w.coordFrameListBox.getItemCount() < 2) {
	  _w.coordFrameListBox.setEnabled(false);
	}

	_w.throwTextBox.addWatcher(this);
	_w.angleTextBox.addWatcher(this);
	_w.coordFrameListBox.addWatcher(this);


	// --- XXX the code below was in _init() ---

	_iterTab   = _w.iterStepsTable;
        _iterTab.setColumnHeaders(new String[]{"Throw", "Angle", "Coord Frame"});
        _iterTab.sizeColumnsToFit(TableWidgetExt.AUTO_RESIZE_ALL_COLUMNS);
	_iterTab.addWatcher(this);
	_iterTab.getModel().addTableModelListener(this);

	_iterItems = new Hashtable();
    }

    /**
     * Perform any required one-time initialization.
     */
    protected void _init() {
    }

    /**
     * Override setup() to initialize the array of available items and show
     * the name of the SpItem in the OtItemEditorWindow banner title.
     */
    public void setup(SpItem spItem) {
        _iterChop = (SpIterChop)spItem;

	if (_iterChop != null) {
	    _iterChop.getAvEditFSM().deleteObserver(this);
	}
	super.setup(spItem);
	_iterChop.getAvEditFSM().addObserver(this);
    }


    /**
     * Update the widgets to display the values of the attributes in the
     * current SpItem.
     */
    protected void  _updateWidgets() {
        _ignoreGuiEvents = true;

	if(_iterChop.getStepCount() < 1) {
	  _iterChop.addInitialStep();
	}

	_iterTab.setRows(_iterChop.getAllSteps());

	if(_iterTab.getRowCount() > 0) {
	  _iterTab.selectRowAt(0);
	}

	_updateTableInfo();

	_ignoreGuiEvents = false;
    }

    //
    // Update table info text box that shows the number of items and steps
    // in the table.
    //
    private void _updateTableInfo() {
	JLabel stw = _w.tableInfo;
	int items = _iterTab.getColumnCount();
	int steps = _iterTab.getRowCount();

	String message = "(" + items;
	if (items == 1) {
	    message += " Item, ";
	} else {
	    message += " Items, ";
	}
	message += steps;
	if (steps == 1) {
	    message += " Step)";
	} else {
	    message += " Steps)";
	}

	stw.setText(message);
    }

    /**
     * Observes the associated SpItem attribute table for changes to the
     * chop parameters.
     */
    public void update(Observable o, Object arg) {

	// Was it the spItem attributes?
	if (o instanceof SpAvEditState) {
	    _updateWidgets();
	}
    }

    /**
     * Add an iteration step.
     */
    public void addStep() {
	if (_iterTab.getColumnCount() == 0) {
	    return;
	}

	int rowIndex = _iterTab.getSelectedRow();

//        if(_iterTab.getRowCount() < 1) {
	  Vector rowVector = new Vector();
	  rowVector.add(_iterChop.getDefaultThrow());
	  rowVector.add(_iterChop.getDefaultAngle());
	  rowVector.add(_iterChop.getDefaultCoordFrame());

	  _iterTab.absInsertRowAt(rowVector, ++rowIndex);
//	}
//	else {
//          _iterTab.absInsertRowAt(_iterTab.getRowData(_iterTab.getRowCount() - 1), ++rowIndex);
//	}


	// Select the newly added row
	_iterTab.selectRowAt(rowIndex);
	_iterTab.focusAtRow(rowIndex);

	_updateTableInfo();
    }


    /**
     * Delete an iteration step.
     */
    public void deleteStep() {
	if ((_iterTab.getColumnCount() == 0) || (_iterTab.getRowCount() == 0)) {
	    return;
	}


	int rowIndex = _iterTab.getSelectedRow();
	_iterTab.removeRowAt(rowIndex);


	if (_iterTab.getRowCount() <= rowIndex) {
	    rowIndex = _iterTab.getRowCount() - 1;
	}

	if (rowIndex >= 0) {
	    _iterTab.selectRowAt(rowIndex);
	    _iterTab.focusAtRow(rowIndex);
	    _updateTableInfo();
	} else {
	    //_valueEditor.clear();
	    addStep();  // Leave a blank step
	}
    }

    /**
     * Move the current step to be the first step.
     */
    public void stepToFirst() {

	int rowIndex = _iterTab.getSelectedRow();
	_iterTab.absMoveToFirstRowAt(rowIndex);

	_iterTab.selectRowAt(0);
	_iterTab.focusAtRow(0);
    }



    /**
     * Move the current step up one.
     */
    public void decrementStep() {

	int rowIndex = _iterTab.getSelectedRow();
	_iterTab.absDecrementRowAt(rowIndex);

	if (--rowIndex >= 0) {
	    _iterTab.selectRowAt(rowIndex);
	    _iterTab.focusAtRow(rowIndex);
	}
    }

    /**
     * Move the current step down one.
     */
    public void incrementStep() {

	int rowIndex = _iterTab.getSelectedRow();
	_iterTab.absIncrementRowAt(rowIndex);

	if (++rowIndex <= (_iterTab.getRowCount() - 1)) {
	    _iterTab.selectRowAt(rowIndex);
	    _iterTab.focusAtRow(rowIndex);
	}
    }

    /**
     * Move the current step to the end.
     */
    public void stepToLast() {

	int rowIndex = _iterTab.getSelectedRow();
	_iterTab.absMoveToLastRowAt(rowIndex);

	rowIndex = _iterTab.getRowCount() - 1;
	_iterTab.selectRowAt(rowIndex);
	_iterTab.focusAtRow(rowIndex);
    }

    public void tableRowSelected(TableWidgetExt w, int rowIndex) {
      _ignoreGuiEvents = true;

      _w.throwTextBox.setValue((String)_iterTab.getValueAt(rowIndex, 0));
      _w.angleTextBox.setValue((String)_iterTab.getValueAt(rowIndex, 1));
      _w.coordFrameListBox.setValue(_iterTab.getValueAt(rowIndex, 2));

      _iterChop.setSelectedIndex(rowIndex);

      // MFO
      // I think this is implemented in a different way in Gemini ot-2000B.12.
      try {
        TpeManager.get(_iterChop).reset(_iterChop);
      }
      catch(NullPointerException e) {
        // ignore
      }

      _ignoreGuiEvents = false;
    }

    public void tableAction(TableWidgetExt w, int colIndex, int rowIndex) {
	// Don't care ...
    }


    //
    // Handle action events on the buttons in the editor. 
    //
    public void actionPerformed(ActionEvent e) {
	Object w = e.getSource();

	// Delete the selected column
//	if (w == _w.deleteTest) {
//	    deleteSelectedColumn(); return;
//	}

	// Add a row (iter step) to the table
	if (w == _w.addStep) {
	    addStep(); return;
	}

	// Delete a row (iter step) from the table
	if (w == _w.deleteStep) {
	    deleteStep(); return;
	}

	// Move a row (iter step) to the end
	if (w == _w.top) {
	    stepToFirst(); return;
	}

	// Move a row (iter step) up
	if (w == _w.up) {
	    decrementStep(); return;
	}

	// Move a row (iter step) down
	if (w == _w.down) {
	    incrementStep(); return;
	}

	// Move a row (iter step) to the end
	if (w == _w.bottom) {
	    stepToLast(); return;
	}
    }


  public void textBoxKeyPress(TextBoxWidgetExt tbwe) {
    if(_ignoreGuiEvents) {
      return;
    }

    if(tbwe == _w.throwTextBox) {
      _iterTab.setValueAt(_w.throwTextBox.getValue(), _iterTab.getSelectedRow(), 0);

      return;
    }

    if(tbwe == _w.angleTextBox) {
      _iterTab.setValueAt(_w.angleTextBox.getValue(), _iterTab.getSelectedRow(), 1);

      return;
    }
  }

  public void textBoxAction(TextBoxWidgetExt tbwe) { }


  public void dropDownListBoxAction(DropDownListBoxWidgetExt ddlbwe, int index, String val) {
    if(_ignoreGuiEvents) {
      return;
    }

    if(ddlbwe == _w.coordFrameListBox) {
      _iterTab.setValueAt(val, _iterTab.getSelectedRow(), 2);

      return; 
    }
  }

  public void dropDownListBoxSelect(DropDownListBoxWidgetExt ddlbwe, int index, String val) { }


  public void tableChanged(TableModelEvent e) {
    if(_ignoreGuiEvents) {
      return;
    }

    _iterChop.getAvEditFSM().deleteObserver(this);

    _iterChop.getTable().rmAll();

    for(int i = 0; i < _iterTab.getRowCount(); i++) {
      _iterChop.setThrow((String)_iterTab.getValueAt(i, 0), i);
      _iterChop.setAngle((String)_iterTab.getValueAt(i, 1), i);
      _iterChop.setCoordFrame((String)_iterTab.getValueAt(i, 2), i);
    }

    _iterChop.getAvEditFSM().addObserver(this);

    // MFO
    // I think this is implemented in a different way in Gemini ot-2000B.12.
    try {
      TpeManager.get(_iterChop).reset(_iterChop);
    }
    catch(NullPointerException exception) {
      // ignore
    }
  }
}
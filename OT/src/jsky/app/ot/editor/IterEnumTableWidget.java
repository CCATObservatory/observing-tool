// Copyright 1997 Association for Universities for Research in Astronomy, Inc.,
// Observatory Control System, Gemini Telescopes Project.
// See the file COPYRIGHT for complete details.
//
// $Id$
//
package jsky.app.ot.editor;



import java.util.List;
import java.util.Vector;
import javax.swing.table.DefaultTableModel;
import jsky.app.ot.gui.TableWidgetExt;
import gemini.sp.iter.SpIterStep;
import gemini.sp.iter.SpIterValue;


/**
 * An extension of the TableWidget to support the output of an iterator
 * enumeration.  This table is used with the EdIteratorFolder class.
 *
 * <p>
 * <em>The iterator code, especially the "compilation" or enumeration
 * of iterators, is going to change significantly.</em>
 */
public class IterEnumTableWidget extends TableWidgetExt {

    /**
     * Construct and set table widget options that make sense.
     */
    public IterEnumTableWidget() {
    }


    /**
     */
    public void	addSteps(Vector steps)  {
	int n = steps.size();
	for (int i=0; i<n; ++i) {
	    addStep((SpIterStep) steps.elementAt(i));
	}
    }

    /**
     */
    public void	addSteps(List steps)  {
	int n = steps.size();
	for (int i=0; i<n; ++i) {
	    addStep((SpIterStep) steps.get(i));
	}
    }

    /**
     */
    public void	addStep(SpIterStep sis) {
	String title = sis.title;
	if (sis.stepCount != 0) {
	    title += " (" + (sis.stepCount+1) + ")";
	}
	Vector v = new Vector();
	v.addElement(title);

	if (sis.values.length == 0) {
	    addRow(v);
	} else {
	    SpIterValue siv = sis.values[0];
	    _createRow(siv, v);
	    addRow(v);

	    for (int i=1; i<sis.values.length; ++i) {
		SpIterValue siv2 = sis.values[i];
		Vector        v2 = new Vector(2);
		v2.addElement("");
		_createRow(siv2, v2);
		addRow(v2);
	    }
	}
    }

    /**
     */
    private void _createRow(SpIterValue siv, Vector v) {
	v.addElement(siv.attribute + " = " + siv.values[0]);
    }

    public void addRow(Vector v) {
	((DefaultTableModel)getModel()).addRow(v);
    }
}

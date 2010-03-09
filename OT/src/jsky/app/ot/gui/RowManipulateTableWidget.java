// Copyright 1997 Association for Universities for Research in Astronomy, Inc.,
// Observatory Control System, Gemini Telescopes Project.
// See the file COPYRIGHT for complete details.
//
// $Id$
//
package jsky.app.ot.gui ;

import java.util.Vector ;
import javax.swing.JTable ;
import javax.swing.table.DefaultTableModel ;

/**
 * A TableWidget subclass that supports manipulation of table rows
 * (moving them up and down and to the front and back).
 */
@SuppressWarnings( "serial" )
public class RowManipulateTableWidget extends JTable
{
	/** Default constructor */
	public RowManipulateTableWidget()
	{
		// disable editing by default
		setModel( new DefaultTableModel()
		{
			public boolean isCellEditable( int row , int column )
			{
				return false ;
			}
		} ) ;
		setCellSelectionEnabled( false ) ;
		setRowSelectionAllowed( true ) ;
		setColumnSelectionAllowed( false ) ;

		// need to change some code if this is enabled
		getTableHeader().setReorderingAllowed( false ) ;
	}

	/**
	 * Insert a new row into the table at a given index, not just adding to
	 * the end and changing display indices.
	 */
	public void absInsertRowAt( Vector<String> row , int index )
	{
		if( row == null )
			row = new Vector<String>( getModel().getColumnCount() ) ;

		getModel().insertRow( index , row ) ;
	}

	/** Clear the table */
	public void clear()
	{
		getModel().setRowCount( 0 ) ;
	}

	/* Set the column headers */
	public void setColumnHeaders( String[] names )
	{
		getModel().setColumnIdentifiers( names ) ;
	}

	/* Set the column headers */
	public void setColumnHeaders( Vector<String> names )
	{
		getModel().setColumnIdentifiers( names ) ;
	}

	/* Set the column widths in pixels. */
	public void setColumnWidths( int[] ar )
	{
		for( int i = 0 ; i < ar.length ; ++i )
			getColumnModel().getColumn( i ).setPreferredWidth( ar[ i ] ) ;
	}

	/** Return the value in the cell at the given row and column. */
	@SuppressWarnings( "unchecked" )
    public Object getCell( int col , int row )
	{
		Vector data = getModel().getDataVector() ;
		return ( ( Vector )data.get( row ) ).get( col ) ;
	}

	/** Set the value in the cell at the given row and column. */
	public void setCell( Object value , int col , int row )
	{
		getModel().setValueAt( value , row , col ) ;
	}

	/** Remove all rows */
	public void removeAllRows()
	{
		getModel().setRowCount( 0 ) ;
	}

	/** Remove all columns */
	public void removeAllColumns()
	{
		getModel().setColumnCount( 0 ) ;
	}

	/** Set the focus at the given row. */
	public void focusAtRow( int index )
	{
		// XXX how to do this with JTable?
		getSelectionModel().setSelectionInterval( index , index ) ;
	}

	/** Return the indexes of the selected rows */
	public int[] getSelectedRowIndexes()
	{
		return getSelectedRows() ;
	}

	/** Remove the given row */
	public void removeRowAt( int index )
	{
		getModel().removeRow( index ) ;
	}

	/** Remove the given column */
	@SuppressWarnings( "unchecked" )
    public void removeColumnAt( int index )
	{
		DefaultTableModel model = getModel() ;
		int numCols = model.getColumnCount() ;
		Vector<String> columnNames = new Vector<String>( numCols - 1 ) ;
		for( int i = 0 ; i < numCols ; i++ )
		{
			if( i != index )
				columnNames.add( model.getColumnName( i ) ) ;
		}
		int numRows = model.getRowCount() ;
		Vector data = model.getDataVector() ;
		Vector<Vector> newData = new Vector<Vector>( numRows ) ;
		for( int i = 0 ; i < numRows ; i++ )
		{
			Vector row = new Vector( numCols - 1 ) ;
			for( int j = 0 ; j < numCols ; j++ )
			{
				if( j != index )
					row.add( (( Vector )data.get( i )).get( j ) ) ;
			}
			newData.add( row ) ;
		}

		setModel( new DefaultTableModel( newData , columnNames ) ) ;
	}

	/**
	 * Debugging method that prints a "table row".
	 */
	protected void _printTableRow( int index , Vector<String> v )
	{
		System.out.print( index + ") " ) ;
		for( int i = 0 ; i < v.size() ; ++i )
			System.out.print( "\t\"" + v.elementAt( i ) + "\"" ) ;

		System.out.println() ;
	}

	/**
	 * Move a row to the front (index 0).
	 */
	@SuppressWarnings( "unchecked" )
    public void absMoveToFirstRowAt( int index )
	{
		if( ( index > 0 ) && ( index < getModel().getRowCount() ) )
		{
			Vector[] va = getAllRowsData() ;
			Vector vTemp = va[ index ] ;
	
			// Move the rows above the index row down one.
			for( int i = index ; i > 0 ; --i )
				va[ i ] = va[ i - 1 ] ;
	
			va[ 0 ] = vTemp ;

			setRows( va ) ;
		}
	}

	/**
	 * Decrement a row's absolute position (move it higher in the table).
	 */
	@SuppressWarnings( "unchecked" )
    public void absDecrementRowAt( int index )
	{
		if( ( index > 0 ) && ( index < getModel().getRowCount() ) )
		{
			Vector[] va = getAllRowsData() ;
	
			Vector vTemp = va[ index - 1 ] ;
			va[ index - 1 ] = va[ index ] ;
			va[ index ] = vTemp ;
	
			setRows( va ) ;
		}
	}

	/**
	 * Increment a row's absolute position (move it lower in the table).
	 */
	@SuppressWarnings( "unchecked" )
    public void absIncrementRowAt( int index )
	{
		int lastIndex = getModel().getRowCount() - 1 ;
		if( ( index > 0 ) && ( index < lastIndex ) )
		{
			Vector[] va = getAllRowsData() ;
	
			Vector vTemp = va[ index ] ;
			va[ index ] = va[ index + 1 ] ;
			va[ index + 1 ] = vTemp ;
	
			setRows( va ) ;
		}
	}

	/**
	 * Decrement a row's absolute position (move it higher in the table).
	 */
	@SuppressWarnings( "unchecked" )
    public void absMoveToLastRowAt( int index )
	{
		int lastIndex = getModel().getRowCount() - 1 ;
		if( ( index >= 0 ) && ( index < lastIndex ) )
		{
			Vector[] va = getAllRowsData() ;
	
			Vector vTemp = va[ index ] ;
			for( int i = index ; i < lastIndex ; ++i )
				va[ i ] = va[ i + 1 ] ;

			va[ lastIndex ] = vTemp ;
	
			setRows( va ) ;
		}
	}

	/**
	 * Get the data in all the rows in the table.  Each row is an entry
	 * in the array of Vectors.  Each Vector contains a row's worth of
	 * data.
	 */
	@SuppressWarnings( "unchecked" )
    public Vector[] getAllRowsData()
	{
		Vector[] va = new Vector[ getModel().getRowCount() ] ;
		getAllRowsData( va ) ;
		return va ;
	}

	/**
	 * Get the data in all the rows in the table.  Each row is an entry
	 * in the array of Vectors.  Each Vector contains a row's worth of
	 * data.
	 */
	@SuppressWarnings( "unchecked" )
    public void getAllRowsData( Vector[] va )
	{
		int rowCount = getModel().getRowCount() ;
		Vector data = getModel().getDataVector() ;
		for( int i = 0 ; i < rowCount ; ++i )
			va[ i ] = ( Vector )data.get( i ) ;
	}

	/** Set the contents of the table */
	@SuppressWarnings( "unchecked" )
    public void setRows( Vector[] v )
	{
		int rowCount = v.length ;
		Vector<Vector> data = new Vector<Vector>( rowCount ) ;
		for( int i = 0 ; i < rowCount ; ++i )
			data.add( v[ i ] ) ;

		DefaultTableModel model = getModel() ;
		int colCount = model.getColumnCount() ;
		Vector<String> header = new Vector<String>( colCount ) ;
		for( int i = 0 ; i < colCount ; ++i )
			header.add( model.getColumnName( i ) ) ;

		model.setDataVector( data , header ) ;
	}

	/**
	 * Override marimba's method that prints nasty error message if 
	 * there are no rows. Null vector is ok as result of guidestar search. 
	 */
	@SuppressWarnings( "unchecked" )
    public Vector getRowData( int index )
	{
		DefaultTableModel model = getModel() ;
		// do nothing if one of the parameters is incorrect
		if( index < 0 || index >= model.getRowCount() )
			return null ;

		Vector data = getModel().getDataVector() ;
		return ( Vector )data.get( index ) ;
	}
	
	public DefaultTableModel getModel()
	{
		return ( DefaultTableModel )super.getModel() ;
	}
}

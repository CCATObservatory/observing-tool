// Copyright 1997 Association for Universities for Research in Astronomy, Inc.,
// Observatory Control System, Gemini Telescopes Project.
// See the file COPYRIGHT for complete details.
//
// $Id$
//
package jsky.app.ot;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import java.io.PrintStream;

import gemini.sp.SpItem;
import gemini.sp.SpPhase1;
import gemini.sp.SpRootItem;

import java.awt.FileDialog;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Reader;

import jsky.app.ot.OtTreeWidget;
import jsky.app.ot.OtWindow;

import orac.util.SpItemUtilities;
import orac.util.SpInputXML;
import orac.util.FileFilterXML;


/**
 * This is a utility class used for reading and writing science programs.
 * It handles the GUI end of the problem, classes in gemini.sp do the
 * actual translation from the file format into gemini.sp structures.
 * All methods are static since this is just a collection of helper
 * routines.
 */
public class OtFileIO
{
	private static String _lastDir = OT.getOtUserDir();

	/** XML file filter (*.xml). MFO 2001 */
	protected static FileFilterXML   xmlFilter = new FileFilterXML() ;

	/*
	* Store the Science Program rooted at the given SpItem into the file
	* given by the directory and filename arguments.
	*/
	public static boolean storeSp( SpRootItem spItem , String dir , String filename )
	{
		// Just create a File and pass it to the other storeSp method.
		File f = new File( dir , filename );
		return storeSp( spItem , f );
	}

	/*
	 * Store the Science Program rooted at the give SpItem into the given java.io.File.
	 */
	public static boolean storeSp( SpRootItem spItem , File f )
	{
		// Get a FileOutputStream pointing to the given File.
		if( f.exists() )
		{
			File backup = new File( f.getPath() + ".BAK" );
			f.renameTo( backup );
		}

		FileOutputStream fos = null;
		OutputStream os = null;
		PrintStream printStream = null ;

		try
		{
			fos = new FileOutputStream( f );
			os = new BufferedOutputStream( fos );

			if( System.getProperty( "DEBUG" ) != null )
			{
				System.out.println( "Before removing id/idref\n" );
				System.out.println( spItem.toXML() );
			}

			SpItemUtilities.removeReferenceIDs( spItem );

			if( System.getProperty( "DEBUG" ) != null )
			{
				System.out.println( "After removing id/idref\n" );
				System.out.println( spItem.toXML() );
			}

			SpItemUtilities.setReferenceIDs( spItem );

			// Set the ATTR_ELAPSED_TIME attributes in SpMSB components and
			// SpObs components that are MSBs.
			SpItemUtilities.saveElapsedTimes( spItem );

			// Make sure the msb attributes are set correctly.
			SpItemUtilities.updateMsbAttributes( spItem );

			System.out.println( "Generating XML ... please wait" ) ;
			String xml = spItem.toXML() ;
			System.out.println( "XML generated ... writing file" ) ;
			printStream = new PrintStream( os ) ;
			printStream.print( xml );
			System.out.println( "Flushing ..." ) ;
			printStream.flush() ;
			os.flush() ;
			System.out.println( "Finished writing ... " ) ;
		}
		catch( SecurityException se )
		{
			JOptionPane.showMessageDialog( null , "The Observing Tool does not have access to '" + f.getAbsolutePath() + "'." , "Error" , JOptionPane.ERROR_MESSAGE );
			return false;
		}
		catch( IOException ioe )
		{
			JOptionPane.showMessageDialog( null , ioe.toString() , "Error" , JOptionPane.ERROR_MESSAGE );
			return false;
		}
		catch( Exception e )
		{
			// we don't know what caused the exception, but handle it anyway
			e.printStackTrace();
			JOptionPane.showMessageDialog( null , e.getMessage() , "Problem storing Science Program" , JOptionPane.ERROR_MESSAGE );
			return false;
		}
		finally
		{
			System.out.println( "Attempting to close file ..." ) ;
			try
			{
				if( printStream != null )
					printStream.close() ;
				else
					System.out.println( "PrintStream was null" ) ;
				if( os != null )
					os.close();
				else
					System.out.println( "BufferedOutputStream was null" ) ;
				if( fos != null )
					fos.close();
				else
					System.out.println( "FileOutputStream was null" ) ;
			}
			catch( IOException ioe )
			{
				System.out.println( "IOException trying to close files " + ioe ) ;
				return false ;
			}
			System.out.println( "File closed" ) ;
		}
		System.out.println( "Saved" ) ;
		return true;
	}

	/*
	 * Read a Science Program from the file indicated by the given directory and filename arguments.
	 * 
	 * @return an SpItem root containing the Science Program if successful, null otherwise
	 */
	public static SpRootItem fetchSp( String dir , String filename )
        {
		// Get a File object from the directory and filename ;
		File f = new File( dir , filename ) ;

		FileReader fr ;
            	try 
		{
                	fr = new FileReader( f ) ;
            	} 
		catch( SecurityException se ) 
		{
                	JOptionPane.showMessageDialog
			(
				null , 
				"The Observing Tool does not have access to the '" + dir + "' directory." ,
                        	"Error" , 
				JOptionPane.ERROR_MESSAGE
			) ;
                	return null ;

            	} 
		catch( FileNotFoundException fnfe ) 
		{
                	String path = dir ;
                	if( !dir.endsWith( File.separator ) ) 
			{
                    		path += File.separator ;
                	}
                	path += filename ;
                	JOptionPane.showMessageDialog
			(
				null , 
				"The file '" + path + "' was not found." ,
                        	"Error" , 
				JOptionPane.ERROR_MESSAGE
			) ;
                	return null ;

            	} 
		catch( IOException ioe ) 
		{
                	JOptionPane.showMessageDialog
			(
				null , 
				ioe.toString() , 
				"Error" , 
				JOptionPane.ERROR_MESSAGE
			) ;
                	return null ;
            }
            return fetchSp( fr ) ;
        }

	/*
	* Read a Science Program from the given reader.
	*
	* @return an SpItem root containing the Science Program if successful,
	* null otherwise
	*/
	public static SpRootItem fetchSp( Reader rdr )
	{
                try 
		{
                    return ( SpRootItem )( new SpInputXML() ).xmlToSpItem( rdr ) ;
                }
                catch( Exception e ) 
		{
 			JOptionPane.showMessageDialog
		    	(
				null , 
				"Could not load Science Programme: " + e.getMessage() ,
				"Error" , 
				JOptionPane.ERROR_MESSAGE
			) ;        
			e.printStackTrace() ;
                    	return null ;
                }
	}

	/*
	* Open a program on disk.
	*/
	public static void open()
        {
            JFileChooser fd = new JFileChooser( _lastDir ) ; 
            fd.addChoosableFileFilter( xmlFilter ) ;

            fd.setFileFilter( xmlFilter ) ;

            fd.showOpenDialog( null ) ;
            if( fd.getSelectedFile() == null ) 
	    {
                return ;
            }

            String dir      = fd.getSelectedFile().getParent() ;
            String filename = fd.getSelectedFile().getName() ;
            if( filename == null ) 
	    {
                return ;
            }

            if( dir != null ) _lastDir = dir ;

            SpRootItem spItem = fetchSp( dir , filename ) ;
            if( spItem != null ) 
	    {
                FileInfo fi = new FileInfo( dir , filename , true ) ;
                OtWindow.create( spItem , fi ) ;
            }
        }

	/*
	* Open the given program file.
	*/
	public static void open( String filename )
        {
            	File file = new File( filename ) ;
		if( !( file.exists() || file.canRead() ) )
		{
			System.out.println( "cannot find " + file.getAbsolutePath() ) ; 
			String pwd = OT.getOtUserDir() ;
			if( !pwd.endsWith( File.separator ) )
			{
				pwd += File.separator ;
			}
			
			File nufile = new File( pwd + filename ) ;
			if( nufile.exists() && nufile.canRead() )
			{
				System.out.println( "trying " + nufile.getAbsolutePath() ) ;
				file = null ;
				file = nufile ;
			}
			else
			{
				nufile = null ;
			}
		}
            	String dir = file.getParent() ;
            	String name = file.getName() ;
            	SpRootItem spItem = fetchSp( dir , name ) ;
            	if( spItem != null ) 
		{
                	FileInfo fi = new FileInfo( dir , name , true ) ;
                	OtWindow.create( spItem , fi ) ;
		}
	}

	/*
	* Save a program to disk.
	*/
	public static boolean save( SpRootItem spItem , FileInfo fi )
        {
		if ( fi.hasBeenSaved ) 
	    	{
                	if( !storeSp( spItem , fi.dir , fi.filename ) ) 
			{
				fi.hasBeenSaved = false ;
                    		return save( spItem , fi ) ;
                	}
                	spItem.getEditFSM().reset() ;
                	return true ;
            	}

            	JFileChooser fd = new JFileChooser( fi.dir ) ;
            	fd.addChoosableFileFilter( xmlFilter ) ;

            	fd.setFileFilter( xmlFilter ) ;

            	int rtn = fd.showSaveDialog( null ) ;
            	if( rtn == JFileChooser.CANCEL_OPTION ) 
		{
                	return false ;
            	}

            	File f = fd.getSelectedFile() ;
		String filename = f.getName() ;

            	// Check whether this file has an .xml suffix.
            	// If not, append ".xml" to its name.
            	if( !filename.toLowerCase().endsWith( ".xml" ) ) 
		{
                	f = new File( f.getAbsolutePath() + ".xml" ) ;
            	}

            	if( f == null ) 
			return false ;
            	String      dir = f.getParent() ;

            	if( dir != null ) _lastDir = dir ;

            	// Make sure we aren't going to over-write an existing file.
            	if( f.exists() ) 
		{
                	if( !f.canWrite() ) 
			{
                    		JOptionPane.showMessageDialog
				(
					null , 
					"Can't write to `" + dir + filename + "'" ,
                            		"Error" , 
					JOptionPane.ERROR_MESSAGE
				) ;
                    		return false ;
                	}

                	if( !f.isFile() ) 
			{
                    		JOptionPane.showMessageDialog
				( 
					null , 
					"`" + dir + filename + "' isn't an ordinary file." ,
                            		"Error" , 
					JOptionPane.ERROR_MESSAGE
				) ;
                    		return false ;
                	}

                	String m = "'" + dir + File.separator + filename + "' already exists.  Replace it?" ;
                	if
			(
				JOptionPane.showConfirmDialog
				(
					null , 
					m , 
					"Replace file?" , 
					JOptionPane.YES_NO_OPTION
				) == JOptionPane.NO_OPTION
			) 
			{
                    		return false ;
                	}
            	}

            	String oldFileName = spItem.getTable().get( ".gui.filename" ) ;
            	spItem.getTable().set( ".gui.filename" , filename ) ;

            	SpRootItem spRoot = ( SpRootItem )spItem.getRootItem() ;
            	spRoot.setOTVersion() ;
            	spRoot.setTelescope() ;

            	boolean hasBeenSaved = storeSp( spItem , f ) ;

            	if( hasBeenSaved ) 
		{
                	fi.setDir( dir ) ;
                	fi.setFilename( filename ) ;
                	fi.setHasBeenSaved( true ) ;
                	spItem.getEditFSM().reset() ;
                	OtProps.setSaveShouldPrompt( false ) ;
            	} 
		else 
		{
                	spItem.getTable().set( ".gui.filename" , oldFileName ) ;
            	}

            	return hasBeenSaved ;
	}

	/*
	* Save the program under a new name.
	*/
	public static SpRootItem saveAs( SpRootItem spItem , FileInfo fi )
	{
		// Remember the "previously saved" state, then set it to false to that
		// the save method will prompt for a file name.  If the save fails or
		// is cancelled, reset hasBeenSaved.
		boolean hasBeenSaved = fi.hasBeenSaved ;
		fi.hasBeenSaved = false ;

		SpRootItem spCopy = ( SpRootItem )spItem.deepCopy() ;
		spCopy.setOTVersion() ;
		spCopy.setTelescope() ;

		if( !save( spCopy , fi ) ) 
		{
			fi.hasBeenSaved = hasBeenSaved ;
                        return null ;
		}

                    return spCopy ;
	}

        /*
	* Revert to the saved version of the program.
	*/
	public static SpRootItem revertToSaved( FileInfo fi )
	{
		if( !fi.hasBeenSaved ) 
		{
			JOptionPane.showMessageDialog
			(
				null , 
				"This program has never been saved." ,
                                "Error" , 
				JOptionPane.ERROR_MESSAGE
			) ;
                        return null ;
		}

		String m = "Revert to the saved version of '" + fi.filename + "'?";
		if
		( 
			JOptionPane.showConfirmDialog
			(
				null , 
				m , 
				"Revert?" , 
				JOptionPane.YES_NO_OPTION
			) == JOptionPane.NO_OPTION
		) 
		{
 			return null ;
		}
		return fetchSp( fi.dir , fi.filename ) ;
	}

}


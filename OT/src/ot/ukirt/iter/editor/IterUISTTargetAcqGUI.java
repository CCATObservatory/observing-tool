/**
 * Title:        JSky<p>
 * Description:  <p>
 * Copyright:    Copyright (c) 2000 Association of Universities for Research in Astronomy, Inc. (AURA)<p>
 * Company:      <p>
 * @author Allan Brighton
 * @version 1.0
 * License:
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 * 1) Redistributions of source code must retain the above copyright notice,
 *   this list of conditions and the following disclaimer.
 * 2) Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 * 3) The names of AURA and its representatives may not be used to endorse or
 *   promote products derived from this software without specific prior written
 *   permission.
 *
 * THIS SOFTWARE IS PROVIDED BY AURA "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL AURA BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE
 * GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF
 * THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package ot.ukirt.iter.editor ;

import java.awt.GridBagLayout ;
import java.awt.GridBagConstraints ;
import java.awt.Insets ;
import java.awt.Color ;
import java.awt.Dimension ;
import javax.swing.JLabel ;
import javax.swing.JPanel ;
import javax.swing.BorderFactory ;
import javax.swing.SwingConstants ;
import jsky.app.ot.gui.DropDownListBoxWidgetExt ;
import jsky.app.ot.gui.TextBoxWidgetExt ;
import jsky.app.ot.gui.CommandButtonWidgetExt ;

@SuppressWarnings( "serial" )
public class IterUISTTargetAcqGUI extends JPanel
{
	GridBagLayout gridBagLayout1 = new GridBagLayout() ;
	JLabel jLabel2 = new JLabel() ;
	JLabel jLabel1 = new JLabel() ;
	JLabel jLabel3 = new JLabel() ;
	JLabel jLabel5 = new JLabel() ;
	JLabel jLabel6 = new JLabel() ;
	JLabel jLabel7 = new JLabel() ;
	DropDownListBoxWidgetExt sourceMag = new DropDownListBoxWidgetExt() ;
	TextBoxWidgetExt exposureTime = new TextBoxWidgetExt() ;
	TextBoxWidgetExt coadds = new TextBoxWidgetExt() ;
	TextBoxWidgetExt disperser = new TextBoxWidgetExt() ;
	CommandButtonWidgetExt defaultAcquisition = new CommandButtonWidgetExt() ;

	public IterUISTTargetAcqGUI()
	{
		try
		{
			jbInit() ;
		}
		catch( Exception ex )
		{
			ex.printStackTrace() ;
		}
	}

	void jbInit() throws Exception
	{
		jLabel1.setText( "Coadds" ) ;
		jLabel1.setForeground( Color.black ) ;
		jLabel1.setFont( new java.awt.Font( "Dialog" , 0 , 12 ) ) ;
		jLabel2.setText( "(exp / obs)" ) ;
		jLabel2.setForeground( Color.black ) ;
		jLabel2.setFont( new java.awt.Font( "Dialog" , 0 , 10 ) ) ;
		jLabel3.setText( "Grism" ) ;
		jLabel3.setForeground( Color.black ) ;
		jLabel3.setFont( new java.awt.Font( "Dialog" , 0 , 12 ) ) ;
		this.setMinimumSize( new Dimension( 280 , 278 ) ) ;
		this.setPreferredSize( new Dimension( 280 , 278 ) ) ;
		this.setLayout( gridBagLayout1 ) ;
		jLabel5.setText( "(sec)" ) ;
		jLabel5.setForeground( Color.black ) ;
		jLabel5.setFont( new java.awt.Font( "Dialog" , 0 , 10 ) ) ;
		jLabel6.setText( "Exp. Time" ) ;
		jLabel6.setForeground( Color.black ) ;
		jLabel6.setFont( new java.awt.Font( "Dialog" , 0 , 12 ) ) ;
		jLabel7.setText( "Source mag" ) ;
		jLabel7.setHorizontalAlignment( SwingConstants.CENTER ) ;
		jLabel7.setForeground( Color.black ) ;
		jLabel7.setToolTipText( "" ) ;
		jLabel7.setFont( new java.awt.Font( "Dialog" , 0 , 12 ) ) ;

		sourceMag.setAlignmentX( ( float )0. ) ;
		sourceMag.setBackground( Color.white ) ;
		sourceMag.setFont( new java.awt.Font( "Dialog" , 0 , 12 ) ) ;
		sourceMag.setAutoscrolls( true ) ;
		sourceMag.setPreferredSize( new Dimension( 50 , 26 ) ) ;

		exposureTime.setBorder( BorderFactory.createLoweredBevelBorder() ) ;
		coadds.setBorder( BorderFactory.createLoweredBevelBorder() ) ;

		disperser.setBackground( new Color( 220 , 220 , 220 ) ) ;
		disperser.setBorder( BorderFactory.createLoweredBevelBorder() ) ;
		disperser.setEditable( false ) ;

		defaultAcquisition.setText( "Reset to Default" ) ;
		this.add( jLabel7 , new GridBagConstraints( 0 , 0 , 1 , 1 , 0. , 0. , GridBagConstraints.EAST , GridBagConstraints.NONE , new Insets( 5 , 5 , 5 , 5 ) , 0 , 0 ) ) ;
		this.add( jLabel2 , new GridBagConstraints( 2 , 2 , 1 , 1 , 0. , 0. , GridBagConstraints.WEST , GridBagConstraints.NONE , new Insets( 5 , 5 , 5 , 5 ) , 0 , 0 ) ) ;
		this.add( jLabel1 , new GridBagConstraints( 0 , 2 , 1 , 1 , 0. , 0. , GridBagConstraints.EAST , GridBagConstraints.NONE , new Insets( 5 , 5 , 5 , 5 ) , 0 , 0 ) ) ;
		this.add( jLabel3 , new GridBagConstraints( 0 , 3 , 1 , 1 , 0. , 0. , GridBagConstraints.EAST , GridBagConstraints.NONE , new Insets( 5 , 5 , 5 , 5 ) , 0 , 0 ) ) ;
		this.add( jLabel5 , new GridBagConstraints( 2 , 1 , 1 , 1 , 0. , 0. , GridBagConstraints.WEST , GridBagConstraints.NONE , new Insets( 5 , 5 , 5 , 5 ) , 0 , 0 ) ) ;
		this.add( jLabel6 , new GridBagConstraints( 0 , 1 , 1 , 1 , 0. , 0. , GridBagConstraints.EAST , GridBagConstraints.NONE , new Insets( 5 , 5 , 5 , 5 ) , 0 , 0 ) ) ;

		this.add( sourceMag , new GridBagConstraints( 1 , 0 , 1 , 1 , 0. , 0. , GridBagConstraints.CENTER , GridBagConstraints.HORIZONTAL , new Insets( 0 , 0 , 0 , 0 ) , 30 , 0 ) ) ;
		this.add( exposureTime , new GridBagConstraints( 1 , 1 , 1 , 1 , 0. , 0. , GridBagConstraints.CENTER , GridBagConstraints.HORIZONTAL , new Insets( 0 , 0 , 0 , 0 ) , 0 , 0 ) ) ;
		this.add( coadds , new GridBagConstraints( 1 , 2 , 1 , 1 , 0. , 0. , GridBagConstraints.CENTER , GridBagConstraints.HORIZONTAL , new Insets( 0 , 0 , 0 , 0 ) , 0 , 0 ) ) ;
		this.add( disperser , new GridBagConstraints( 1 , 3 , 1 , 1 , 0. , 0. , GridBagConstraints.CENTER , GridBagConstraints.HORIZONTAL , new Insets( 0 , 0 , 0 , 0 ) , 0 , 0 ) ) ;
		this.add( defaultAcquisition , new GridBagConstraints( 0 , 4 , 3 , 1 , 0. , 0. , GridBagConstraints.CENTER , GridBagConstraints.NONE , new Insets( 10 , 0 , 0 , 0 ) , 0 , 0 ) ) ;
	}
}

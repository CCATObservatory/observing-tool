/*==============================================================*/
/*                                                              */
/*                UK Astronomy Technology Centre                */
/*                 Royal Observatory, Edinburgh                 */
/*                 Joint Astronomy Centre, Hilo                 */
/*                   Copyright (c) PPARC 2001                   */
/*                                                              */
/*==============================================================*/
// $Id$

package edfreq;


import java.awt.*;
import java.awt.event.*;
import java.io.*;
import javax.swing.*;
import javax.swing.table.*;

import java.util.Vector;

import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.XMLReader;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.InputSource;

/**
 * @author Dennis Kelly ( bdk@roe.ac.uk ), modified by Martin Folger (M.Folger@roe.ac.uk)
 */
public class FrequencyTable extends JPanel implements FrequencyEditorConstants
{

   private double lLowLimit;
   private double lHighLimit;
   private double uLowLimit;
   private double uHighLimit;
   Object[] samplers;
   Object[][] data;

   /** Parser for XML input/output. (MFO, 29 November 2001) */
   private XMLReader _xmlReader = null;

   public FrequencyTable ( double feIF, double feBandWidth,
     double [] bandWidths, int [] channels, 
     int samplerCount, int displayWidth,
     SideBandDisplay sideBandDisplay,
     FrontEnd frontEnd ) 
   {

      super ( );


      JPanel[] columns = new JPanel[6];

      int j;
      int i;
      JScrollBar lowBar;
      JScrollBar highBar;
      JComboBox widthChoice;
      SamplerDisplay samplerDisplay;
      ResolutionDisplay resolutionDisplay;
      double gigToPix;


      lLowLimit = -feIF - ( feBandWidth * 0.5 );
      lHighLimit = -feIF + ( feBandWidth * 0.5 );
      uLowLimit = feIF - ( feBandWidth * 0.5 );
      uHighLimit = feIF + ( feBandWidth * 0.5 );

/* Create basic pattern of columns */

      setLayout ( new BoxLayout ( this, BoxLayout.X_AXIS ) );

      JLabel lsbTitle = new JLabel ( "LSB", SwingConstants.CENTER );
      JLabel loTitle = new JLabel ( "IF", SwingConstants.CENTER );
      JLabel bwTitle = new JLabel ( "BW", SwingConstants.CENTER );
      JLabel resTitle = new JLabel ( "Res (KHz)", SwingConstants.CENTER );
      JLabel usbTitle = new JLabel ( "USB", SwingConstants.CENTER );

      for ( i=0; i<6; i++ )
      {
         columns[i] = new JPanel();
         columns[i].setLayout ( new GridLayout ( samplerCount+1, 1 ) );
         add ( columns[i] );
      }

      columns[0].add ( lsbTitle );
      columns[2].add ( loTitle );
      columns[3].add ( bwTitle );
      columns[4].add ( resTitle );
      columns[5].add ( usbTitle );

/* Calculate scale factors such that lLowLimit to uHighLimit maps to 
   displayWidth graphics pixels */

      gigToPix = ((double)displayWidth) / ( uHighLimit - lLowLimit );
      int lWidth = (int) ( gigToPix * ( lHighLimit - lLowLimit ) );
      int uWidth = (int) ( gigToPix * ( uHighLimit - uLowLimit ) );
      int sWidth = 80;
      int tWidth = 80;
      int sp1Width = ( displayWidth - lWidth - uWidth - sWidth - tWidth )
        / 2;
      int sp2Width = displayWidth - lWidth - uWidth - sWidth - tWidth
        - sp1Width;
      int h = 20 * ( 1 + samplerCount );

/* Set column sizes */

      columns[0].setPreferredSize ( new Dimension ( lWidth, h ) );
      columns[0].setMaximumSize ( new Dimension ( lWidth, h ) );
      columns[1].setPreferredSize ( new Dimension ( sp1Width, h ) );
      columns[1].setMaximumSize ( new Dimension ( sp1Width, h ) );
      columns[2].setPreferredSize ( new Dimension ( tWidth, h ) );
      columns[2].setMaximumSize ( new Dimension ( tWidth, h ) );
      columns[3].setPreferredSize ( new Dimension ( sWidth, h ) );
      columns[3].setMaximumSize ( new Dimension ( sWidth, h ) );
      columns[4].setPreferredSize ( new Dimension ( sp2Width, h ) );
      columns[4].setMaximumSize ( new Dimension ( sp2Width, h ) );
      columns[5].setPreferredSize ( new Dimension ( uWidth, h ) );
      columns[5].setMaximumSize ( new Dimension ( uWidth, h ) );

/* Create the samplers and sidebands and their associated displays */

      data = new Object [samplerCount][3];
      samplers = new Object [samplerCount];

      frontEnd.clearSamplerList();

      for ( j=0; j<samplerCount; j++ )
      {
         lowBar = new JScrollBar ( JScrollBar.HORIZONTAL, 
           (int)( gigToPix * (-feIF-0.5*bandWidths[0]) ), 
           (int)( gigToPix * bandWidths[0] ), 
           (int)( gigToPix * lLowLimit ) + 20, 
           (int)( gigToPix * lHighLimit ) - 20 );
         lowBar.setUnitIncrement ( 1 );

         highBar = new JScrollBar ( JScrollBar.HORIZONTAL,
           (int)( gigToPix * (feIF-0.5*bandWidths[0]) ), 
           (int)( gigToPix * bandWidths[0]), 
           (int)( gigToPix * uLowLimit ) + 20, 
           (int)( gigToPix * uHighLimit ) - 20 );
         highBar.setUnitIncrement ( 1 );

         samplerDisplay = new SamplerDisplay ( String.valueOf ( feIF ) );
         resolutionDisplay = new ResolutionDisplay ( channels[0],
           bandWidths[0] );
	 Vector bandWidthItems = new Vector();
	 for(int k = 0; k < bandWidths.length; k++) {
            bandWidthItems.add("" + bandWidths[k] * 1.0E-9);
	 }
         widthChoice = new JComboBox ( bandWidthItems );
         samplers[j] = new Sampler ( feIF, bandWidths, channels, widthChoice );
	 frontEnd.addToSamplerList( (Sampler)samplers[j] );

         data[j][0] = new SideBand ( lLowLimit, lHighLimit, 
           bandWidths[0], -feIF, (Sampler)samplers[j], 
           lowBar, gigToPix );
         data[j][1] = samplers[j];
         data[j][2] = new SideBand ( uLowLimit, uHighLimit, 
           bandWidths[0], feIF, (Sampler)samplers[j],
            highBar, gigToPix );

         ((Sampler)samplers[j]).addSamplerWatcher ( 
           (SamplerWatcher)data[j][0] );
         ((Sampler)samplers[j]).addSamplerWatcher ( 
           (SamplerWatcher)samplerDisplay );
         ((Sampler)samplers[j]).addSamplerWatcher ( 
           (SamplerWatcher)resolutionDisplay );
         ((Sampler)samplers[j]).addSamplerWatcher ( 
           (SamplerWatcher)data[j][2] );

         columns[0].add ( lowBar );
         columns[2].add ( samplerDisplay );
         columns[3].add ( widthChoice );
         columns[4].add ( resolutionDisplay );
         columns[5].add ( highBar );
      }

      // Added by MFO (8 January 2002)
      // Only the top pair of SideBands need to have references of
      // SideBandDisplay and FrontEnd because when one of them is changed
      // the LO1 needs adjusting, depending on whether "usb" or "lsb"
      // has been selected.
      ((SideBand)data[0][0]).connectTopSideBand(sideBandDisplay, frontEnd);
      ((SideBand)data[0][2]).connectTopSideBand(sideBandDisplay, frontEnd);


      // Initialize parser (MFO, 29 November 2001)
      try {
         _xmlReader = SAXParserFactory.newInstance().newSAXParser().getXMLReader();
	 
	 _xmlReader.setContentHandler(new DefaultHandler() {
	 
            public void startElement(String namespaceURI,
                                     String localName,
                                     String qName,
                                     Attributes atts) throws SAXException {
               if(qName.equals(XML_ELEMENT_BANDSYSTEM)) {
                  // ignore count attribute
	          return;
	       }

               if(qName.equals(XML_ELEMENT_SUBSYSTEM)) {
	          int i                  = Integer.parseInt(atts.getValue(XML_ATTRIBUTE_BANDNUM));
	          double centreFrequency = Double.parseDouble(atts.getValue(XML_ATTRIBUTE_CENTRE_FREQUENCY));
	          double bandWidth       = Double.parseDouble(atts.getValue(XML_ATTRIBUTE_BANDWIDTH));
                  SideBand sideband = (SideBand)data[i][0];

                  sideband.setSubBandCentre(centreFrequency);
                  sideband.setSubBandWidth(bandWidth);
	          sideband.updateCentreFrequency();
                  // channels (-1) is not used
	          sideband.updateSamplerValues(centreFrequency, bandWidth, 0);
	          //sideband.updateScrollBarValue();
               
	          ((Sampler)samplers[i]).setBandWidthAndGui(atts.getValue(XML_ATTRIBUTE_BANDWIDTH));
               }	 
	    }
         });
      }
      //catch(SAXException e) { //ParserConfigurationException e) {
      catch(Exception e) { //ParserConfigurationException e) {
          JOptionPane.showMessageDialog(this,
	                                "Could not initialize XMLReader: " + e,
					"XMLReader problem.", JOptionPane.ERROR_MESSAGE);

      }

   }

   /**
    * Not used anymore.
    *
    * @deprecated replaced by {@link #toXML()}.
    */
   public void saveAsASCII ( PrintWriter out )
   {
      int j;

      out.println ( "  <bandSystem" );

      out.println ( "    count=" + samplers.length );
      out.println ( "  >" );

      for ( j=0; j<samplers.length; j++ )
      {
         out.println ( "    <subSystem" );

         out.println ( "      bandNum=" + j );
         out.println ( "      centreFrequency=" +
           ((Sampler)samplers[j]).getCentreFrequency() );
         out.println ( "      bandWidth=" + 
           ((Sampler)samplers[j]).getBandWidth() );
         out.println ( "      channels=" + 
           ((Sampler)samplers[j]).getChannels() );

         out.println ( "    />" );
      }
      out.println ( "  </bandSystem>" );
   }


   public String toXML()
   {
      StringBuffer stringBuffer = new StringBuffer();
   
      int j;

      stringBuffer.append ( "  <" + XML_ELEMENT_BANDSYSTEM);

      stringBuffer.append ( " " + XML_ATTRIBUTE_COUNT + "=\"" + samplers.length + "\">\n");

      for ( j=0; j<samplers.length; j++ )
      {
         stringBuffer.append ( "    <" + XML_ELEMENT_SUBSYSTEM);

         stringBuffer.append ( " " + XML_ATTRIBUTE_BANDNUM + "=\"" + j + "\"");
         stringBuffer.append ( " " + XML_ATTRIBUTE_CENTRE_FREQUENCY + "=\"" +
           ((Sampler)samplers[j]).getCentreFrequency() + "\"");
         stringBuffer.append ( " " + XML_ATTRIBUTE_BANDWIDTH + "=\"" + 
           ((Sampler)samplers[j]).getBandWidth() + "\"");
         stringBuffer.append ( " " + XML_ATTRIBUTE_CHANNELS + "=\"" + 
           ((Sampler)samplers[j]).getChannels() + "\"/>\n" );

      }
      stringBuffer.append ( "  </" + XML_ELEMENT_BANDSYSTEM + ">" );
   
      return stringBuffer.toString();
   }

   public String toConfigXML(String restFrequencyId, int sideband, String dataReductionXML, String indent) {

      StringBuffer xmlBuffer = new StringBuffer();

      for(int i = 0; i < samplers.length; i++) {
         xmlBuffer.append(spectralWindowToXML(i, restFrequencyId, sideband, dataReductionXML, indent) + "\n");
      }

      return xmlBuffer.toString();
   }

   public String spectralWindowToXML(int subsystemIndex,
				     String restFrequencyId,
				     int sideband,
				     String dataReductionXML,
				     String indent) {
      return 
         indent + "<spectral_window id=\"SPW" + (subsystemIndex + 1) + "\">\n" +
         indent + "  <spw_bandwidth_mode mode=\"1GHzx1024\"/>\n" +
         indent + "  <spw_window type=\"truncate\"/>\n" +
         indent + "  <rest_frequency_ref ref=\"" + restFrequencyId +"\"/>\n" +
         indent + "  <front_end_sideband sideband=\"" + sideband + "\"/>\n" +
         indent + "  <spw_if_coordinate>\n" +
         indent + "    <spw_reference_if_frequency units=\"GHz\">" +
           ((Sampler)samplers[subsystemIndex]).getCentreFrequency() +
           "</spw_reference_if_frequency>\n" +
         indent + "    <spw_reference_pixel>" +
	   "4064.0???" +
           "</spw_reference_pixel>\n" +
         indent + "    <spw_if_channel_width units=\"Hz\">" +
           ((Sampler)samplers[subsystemIndex]).getBandWidth() +
           "</spw_if_channel_width>\n" +
         indent + "    <spw_number_if_channel>" +
           ((Sampler)samplers[subsystemIndex]).getChannels() +
           "</spw_number_if_channel>\n" +
         indent + "  </spw_if_coordinate>\n" +
         indent + dataReductionXML + "\n" +
         indent + "</spectral_window>\n";
   }


   /**
    * Set FrequencyTable from XML.
    *
    * Added by Martin Folger (14 November 2001)
    */
   public void update(String xml) throws Exception
   {
      //try {
         _xmlReader.parse(new InputSource(new StringReader(xml)));
      //}
      //catch(ClassCastException e) {
         // There seems to be a class cast exception at the first attempt to parse which
	 // disappears at the second attempt.
	 // This problem will disappear with jdk1.4
	// System.out.println("FrontEnd.update(): SAXParser.parse bug in jdk1.3. Ignore.");
	//_parser.parse(new InputSource(new StringReader(xml)));
      //}
   }

}

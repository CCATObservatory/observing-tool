/*==============================================================*/
/*                                                              */
/*                UK Astronomy Technology Centre                */
/*                 Royal Observatory, Edinburgh                 */
/*                 Joint Astronomy Centre, Hilo                 */
/*                   Copyright (c) PPARC 2001                   */
/*                                                              */
/*==============================================================*/
// $Id$

package orac.util;

import gemini.sp.SpItem;
import gemini.sp.SpAvTable;
import gemini.sp.SpRootItem;
import gemini.sp.SpType;
import gemini.sp.SpTreeMan;
import gemini.sp.SpFactory;
import gemini.sp.SpInsertData;
import gemini.sp.SpTelescopePos;
import gemini.sp.obsComp.SpTelescopeObsComp;
import org.apache.xerces.dom.ElementImpl;
import org.apache.xerces.dom.TextImpl;
import org.apache.xerces.dom.DocumentImpl;
import org.apache.xml.serialize.XMLSerializer;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xerces.parsers.DOMParser;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.w3c.dom.NodeList;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.traversal.TreeWalker;
import java.util.Vector;
import java.util.StringTokenizer;
import java.util.Enumeration;
import java.util.Hashtable;
import java.io.StringWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.IOException;


/**
 * @author Martin Folger
 */
public class SpItemDOM {

  public static final String SP_ITEM_DATA_TAG = "ItemData";
  public static final String SP_ITEM_TYPE     = "type";
  public static final String SP_ITEM_SUBTYPE  = "subtype";
  public static final String SP_ITEM_NAME     = "name";
  public static final String SP_ITEM_PACKAGE  = "package";

  protected static PreTranslator _preTranslator = null;

  /**
   * The root of the DOM sub tree that represents the SpItem of this class.
   */
  protected ElementImpl _element;

  public SpItemDOM(SpItem spItem) throws Exception {
    this(spItem, new DocumentImpl());
  }

  private SpItemDOM(SpItem spItem, DocumentImpl ownerDoc) throws Exception {
  
  
    String classNameTag = spItem.getClass().getName().substring(spItem.getClass().getName().lastIndexOf(".") + 1);
    _element = (ElementImpl)ownerDoc.createElement(classNameTag);

    // Parse av table and write result into _element.
    (new SpAvTableDOM(spItem.getTable(), _element)).parseAvTable();
    
    // Append children
    Enumeration children = spItem.children();
    while (children.hasMoreElements()) {
      _element.appendChild((new SpItemDOM((SpItem) children.nextElement(), ownerDoc)).getElement());
    }

    // Create item data node
    ElementImpl itemData = (ElementImpl)ownerDoc.createElement(SP_ITEM_DATA_TAG);
    itemData.setAttribute(SP_ITEM_TYPE,    spItem.typeStr());
    itemData.setAttribute(SP_ITEM_SUBTYPE, spItem.subtypeStr());
    itemData.setAttribute(SP_ITEM_NAME,    spItem.name());
    itemData.setAttribute(SP_ITEM_PACKAGE, 
      spItem.getClass().getName().substring(0, spItem.getClass().getName().lastIndexOf(".")));
    
    // Insert item data node as first child.
    _element.insertBefore(itemData, _element.getFirstChild());
  }

  public SpItemDOM(Reader xmlReader) throws Exception { //SAXException, IOException {
    DOMParser parser = new DOMParser();

    /*MFO TODO: better error handling.*/
    //try {
    parser.parse(new InputSource(xmlReader));
    //}
    //catch(Exception e) {
    //  e.printStackTrace();
    //}

    _element = (ElementImpl)parser.getDocument().getDocumentElement();

    if(_preTranslator != null) {
      _preTranslator.reverse(_element);
    }  
  }

  ElementImpl getElement() {
    return _element;
  }

  public String toXML(String indent) throws Exception {
    if(_preTranslator != null) {
      _preTranslator.translate(_element);
    }

      OutputFormat    format  = new OutputFormat(_element.getOwnerDocument());   //Serialize DOM
      format.setIndenting(true);
      format.setIndent(2);
      format.setLineSeparator("\n" + indent);
      format.setOmitXMLDeclaration(false);
      format.setEncoding("ISO-8859-1");

      StringWriter  stringOut = new StringWriter();        //Writer will be a String
      XMLSerializer    serial = new XMLSerializer( stringOut, format );
      serial.asDOMSerializer();                            // As a DOM Serializer

      serial.serialize(_element); //avTableToXmlElement(table, "TestTable") );
      //serial.serialize(_element.getOwnerDocument().getDocumentElement()); //avTableToXmlElement(table, "TestTable") );

      return indent + stringOut.toString(); //Spit out DOM as a String
  }

  public String toXML() throws Exception {
    return toXML("");
  }

  /**
   * @return DOM element representing &lt;SP_ITEM_DATA_TAG&gt; if there is one
   *         or null otherwise.
   */
  public ElementImpl getItemDataElement(ElementImpl element) {
    NodeList nodeList = element.getChildNodes();
    
    for(int i = 0; i < nodeList.getLength(); i++) {
      if((nodeList.item(i) instanceof ElementImpl) &&
         nodeList.item(i).getNodeName().equals(SP_ITEM_DATA_TAG)) {
        
	return (ElementImpl)nodeList.item(i);
      }
    }

    return null;
  }

  /**
   * Processes a subtree of the DOM beginning at the level of the direct children of
   * a DOM element representing an SpItem.
   * These subtrees represent the attributes of the SpItem. All the attributes are
   * assembled, and then the attributes and their values are added to the SpAvTable.
   *
   * @see #getAvAttrtibutes(org.apache.xerces.dom.ElementImpl);
   */
/*  protected addAttrtibuteValuePairs(ElementImpl element, SpAvTable avTab) {
    
    String avAttribute = null;
    Hashtable table = buildAvTable(element);

    for(int i = 0; i < avAttributeVector.size(); i++) {
      avAttribute = (String)table.get(i);
      if(avAttribute.startsWith(SP_ITEM_DATA_TAG)) {
        avAttribute = "." + avAttribute.substring(SP_ITEM_DATA_TAG.length());
      }
      avTab.set(avAttribute, );
    }
  }
*/
  /**
   * Is called recursivly.
   *
   * Each attribute or key "grows" from right to left. E.g.
   *       filename = some_file.sp
   *   gui.filename = some_file.sp
   * 
   */
/*  protected Hashtable buildAvTable(ElementImpl element) {
    Hashtable table = new Hashtable();
    Hashtable childTable = null
    NodeList nodeList = element.getChildNodes();

    for(int i = 0; i < nodeList.getLength(); i++) {
      if(nodeList.item(i) instanceof ElementImpl) {
        childTable = buildAvTable((ElementImpl)nodeList.item(i));
	Enumeration keys = childTable.keys();
	while(keys.hasMoreElements()) {
	  String key = (String)keys.nextElement();
          table.put(element.getNodeName() + "." + key, childTable.get(key));
	}
      }
      if(nodeList.item(i) instanceof TextImpl) {
        table.put(element.getNodeName(), ((TextImpl)nodeList.item(i)).getData());
      }
    }

    return table;
  }
*/
  public static void fillAvTable(String nameSoFar, ElementImpl remainingTree, SpAvTable avTab) {
    // Ignore ItemData
    if((remainingTree.getNodeName().equals("ItemData"))) {
      return;
    }

    String nodeName = remainingTree.getNodeName();
    if(remainingTree.getUserData() != null) {
      nodeName += remainingTree.getUserData();
    }

    String prefix;
    if((nameSoFar != null) && (nameSoFar.trim() != "")) {
      if(nameSoFar.equals(SpAvTableDOM.META_DATA_TAG)) {
        prefix = ".";
      }
      else {
        prefix = nameSoFar + ".";
      }	
    }
    else {
      prefix = "";
    }
    
    // Deal with attributes
    if(remainingTree.getAttributes() != null && remainingTree.getAttributes().getLength() > 0) {
      NamedNodeMap nodeMap = remainingTree.getAttributes();
      for(int i = 0; i < nodeMap.getLength(); i++) {
       avTab.noNotifySet(prefix + nodeName
                          + ":" + nodeMap.item(i).getNodeName(),
			          nodeMap.item(i).getNodeValue(), 0);
      }
    }

    // Deal with child nodes
    NodeList nodeList = remainingTree.getChildNodes();

    // Check whether there are siblings with the same node name (tag name) and set user data to
    // string "#" + number
    setNumbers(nodeList);

    Vector valueVector = new Vector();
    for(int i = 0; i < nodeList.getLength(); i++) {
      if(nodeList.item(i) instanceof ElementImpl) {
        if(nodeList.item(i).getNodeName().equals("value")) {
	  if((nodeList.item(i).getFirstChild() != null) && (nodeList.item(i).getFirstChild().getNodeValue() != null)) {
	    valueVector.add(nodeList.item(i).getFirstChild().getNodeValue());
          }
	  else {
            valueVector.add("");
	  }
	}
	else {
	  fillAvTable(prefix + nodeName,
                      (ElementImpl)nodeList.item(i),
		      avTab);
	}	      
      }
      else if(nodeList.item(i) instanceof TextImpl) {
        // Ignore extra items beginning with "\n" created by DOMParser.parse().
        if(!nodeList.item(i).getNodeValue().startsWith("\n")) {
          
	  avTab.noNotifySet(prefix + nodeName,
	                    nodeList.item(i).getNodeValue(), 0);
	}
	else {
	}
      }
      else {
        /*MFO TODO: better error handling.*/ System.out.println("Unknown node type: " + nodeList.item(i).getClass().getName());
      }
    }  
    
    // adding value vector
    if(valueVector.size() > 0) {
      avTab.noNotifySetAll(prefix + nodeName, valueVector);
    }
  }

  public SpRootItem getSpItem() {
    /*MFO TODO: better error handling.*/
    try {
      SpRootItem spRootItem = (SpRootItem)getSpItem(_element);
      
      // return deepCopy of spRootItem instead of spRootItem to prevent bug that cases
      // GUIDE row in target list table not to be displayed.
      return (SpRootItem)spRootItem.deepCopy();
    }
    catch(Exception e) {
      e.printStackTrace();
    }
    return null;
  }

  /**
   * @return SpItem if the DOM element represent an SpItem (i.e. has a &lt;SP_ITEM_DATA_TAG&gt;)
   *         or null otherwise.
   */
  protected SpItem getSpItem(ElementImpl element) { //throws Exception {
    ElementImpl itemDataElement = getItemDataElement(element);

    // If element has no item data child then it does not represent an SpItem.
    if(itemDataElement == null) {
      return null;
    }
    SpType spType = SpType.get(itemDataElement.getAttribute(SP_ITEM_TYPE),
                               itemDataElement.getAttribute(SP_ITEM_SUBTYPE));
    
    SpItem spItem = SpFactory.createShallow(spType);
    spItem.name(itemDataElement.getAttribute(SP_ITEM_NAME));

    // Deal with attributes in SpItem XML Element (the ones who are represented
    // by attribute names starting with ":" in the SpAvTable)
    if((element.getAttributes() != null) && (element.getAttributes().getLength() > 0)) {
      NamedNodeMap nodeMap = element.getAttributes();
      for(int i = 0; i < nodeMap.getLength(); i++) {
        spItem.getTable().set(":" + nodeMap.item(i).getNodeName(),
                                    nodeMap.item(i).getNodeValue());
      }
    }


    NodeList nodeList = element.getChildNodes();
    setNumbers(nodeList);
    ElementImpl childElement = null;
    Vector childV = new Vector();
    
    for(int i = 0; i < nodeList.getLength(); i++) {
      if(nodeList.item(i) instanceof ElementImpl) {
        childElement = (ElementImpl)nodeList.item(i);
        
	// Re-use itemDataElement for children.
        itemDataElement = getItemDataElement(childElement);

        // DOM element represents an SpItem.
        if(itemDataElement != null) {
          childV.addElement(getSpItem(childElement));
        }
	// DOM element represents an SpAvTable attribute.
	else {
	  fillAvTable("", childElement, spItem.getTable());
	  //addAttrtibuteValuePair(childElement, spItem.getTable());
	}
      }
    }  

    if ((childV != null) && (childV.size() > 0)) {
  
      SpItem[] spItemA = new SpItem[ childV.size() ];

      // Convert the vector into an array.
      for (int i=0; i<spItemA.length; ++i) {
        spItemA[i] = (SpItem) childV.elementAt(i);
      }

      SpInsertData spID = SpTreeMan.evalInsertInside(spItemA, spItem);
      if (spID == null) {
        //_problem = "Illegal program or plan.";
        /*MFO TODO: better error handling.*/ //throw new Exception("Illegal program or plan.");
        /*MFO TODO: better error handling.*/ System.out.println("Illegal program or plan.");
      }
      else {
        SpTreeMan.insert(spID);
      }	
    }
    
    return spItem;  
  }

  /**
   * For testing.
   */
  public static void main(String [] args) {
    if(args.length != 1) {
      System.out.println("Usage: AvTableToDom input_file");
      System.out.println("   input_file: contains key value pairs for initialising the test table");
      return;
    }

    java.util.Properties props = new java.util.Properties();
    try {
      props.load(new java.io.FileInputStream(args[0]));
    }
    catch(Exception e) {
      System.out.println("Problems loading properties form file " + args[0] + ": " + e);
    }
    
    Enumeration e = props.propertyNames();
    String key = null;
    String property = null;
    Vector vector = null;
    SpAvTable table = new SpAvTable();
    while(e.hasMoreElements()) {
      key = (String)e.nextElement();
      property = props.getProperty(key);
      
      if(property.startsWith("{") && property.endsWith("}")) {
        vector = new Vector();
        StringTokenizer stringTokenizer = new StringTokenizer(property, ";,{} ");
        
	while(stringTokenizer.hasMoreTokens()) {
          vector.add(stringTokenizer.nextToken());
        }

	table.setAll(key, vector);
      }
      else {
        table.set(key, property);
      }
      

    }
    //System.out.println((new AvTableToDom(table, "TestTable", new DocumentImpl())).toString("    "));
  }


  /**
   * Checks whether there are siblings with the same node name (tag name) and set user data to
   * string "#" + number so they can be save under different names in the av table.
   */
  public static void setNumbers(NodeList siblings) {
    if(siblings == null) {
      return;
    }

    Hashtable elementTable = new Hashtable();

    for(int i = 0; i < siblings.getLength(); i++) {
      if(elementTable.get(siblings.item(i).getNodeName()) == null) {
        elementTable.put(siblings.item(i).getNodeName(), new Vector());
      }

      ((Vector)(elementTable.get(siblings.item(i).getNodeName()))).add(siblings.item(i));
    }

    Enumeration tableEntries = elementTable.elements();
    Vector elementVector;
    while(tableEntries.hasMoreElements()) {
      elementVector = (Vector)tableEntries.nextElement();

      if(elementVector.size() > 1) {
        for(int i = 0; i < elementVector.size(); i++) {
          if(elementVector.get(i) instanceof ElementImpl) {
	    ((ElementImpl)elementVector.get(i)).setUserData("#" + i);
	  }
	}
      }
    }
  }

  public static void setPreTranslator(PreTranslator preTranslator) {
    _preTranslator = preTranslator;
  }
}

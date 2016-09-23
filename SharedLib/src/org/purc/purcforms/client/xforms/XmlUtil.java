package org.purc.purcforms.client.xforms;

import java.util.ArrayList;
import java.util.List;

import org.purc.purcforms.client.xpath.XPathExpression;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.xml.client.CDATASection;
import com.google.gwt.xml.client.Document;
import com.google.gwt.xml.client.Element;
import com.google.gwt.xml.client.NamedNodeMap;
import com.google.gwt.xml.client.Node;
import com.google.gwt.xml.client.NodeList;
import com.google.gwt.xml.client.XMLParser;


/**
 * Utility methods used when manipulating xml documents.
 * 
 * @author daniel
 *
 */
public class XmlUtil {

	/**
	 * All methods in this class are static and hence we expect no external
	 * Instantiation of this class.
	 */
	private XmlUtil(){
	}


	/**
	 * Checks if a node name equals a particular name, regardless of prefix.
	 * NOTE: If checking names containing subsets of others using case or if else statements,
	 * 		 start with one which is not contained by others. 
	 *       e.g "itemset" should be before "item".
	 * 
	 * @param nodeName the node name.
	 * @param name the name to compare with.
	 * @return true if they are the same, else false.
	 */
	public static boolean nodeNameEquals(String nodeName, String name){
		return nodeName.equals(name) || nodeName.contains(":"+name);

	}

	/**
	 * Gets the text value of a node. (includes child textnodes & cdatanodes, but not inner elements)
	 * 
	 * @param node the node whose text value to get.
	 * @return the text value.
	 */
	public static String getTextValueShallow(Node node){
		if (node == null) { return null; }
		if (Node.ATTRIBUTE_NODE == node.getNodeType() || Node.TEXT_NODE == node.getNodeType()) { 
			return node.getNodeValue(); 
		} else if (Node.CDATA_SECTION_NODE == node.getNodeType()) {
			return ((CDATASection) node).getData();
		}
		
		Node inner = node.getFirstChild();
		if (inner == null) { return null; }
		StringBuilder sb = new StringBuilder();
		do {
			if (Node.TEXT_NODE == inner.getNodeType()) {
				sb.append(inner.getNodeValue());
			} else if (Node.CDATA_SECTION_NODE == inner.getNodeType()) {
				return ((CDATASection) inner).getData(); // do not mix cdata nodes with textnodes!
			}
			inner = inner.getNextSibling();
		} while (inner != null);
		
		return sb.toString();
	}
	
	/**
	 * Sets text in a node.
	 * <p> Will replace existing content (of first valid node found).
	 * <p> if Node is a TextNode, Attribute or Cdata, text is set directly.
	 * <p> if Node is an Element: 
	 * <br /> - replaces text if a child is a TextNode or a CData block (first occurence).
	 * <br /> - else creates a new Textnode at the end.
	 * <p> Will not create cdata nodes.
	 * @param node
	 * @param value
	 */
	public static void setTextValueShallow(Node node, String value) {
		if (node == null) {
			GWT.log("Cannot set value of a null node");
			return;
		}
		if (Node.ATTRIBUTE_NODE == node.getNodeType() || Node.TEXT_NODE == node.getNodeType()) { 
			node.setNodeValue(value);
			
		} else if (Node.CDATA_SECTION_NODE == node.getNodeType()) {
			((CDATASection) node).setData(value);
			
		} else {
			Node inner = node.getFirstChild();
			if (inner == null) {
				node.appendChild(node.getOwnerDocument().createTextNode(value));
			} else {
				Node textNode = null;
				do {
					if (Node.TEXT_NODE == inner.getNodeType() && textNode == null) {
						textNode = inner;
					} else if (Node.CDATA_SECTION_NODE == inner.getNodeType()) {
						((CDATASection) inner).setData(value);
						return; // do not mix cdata nodes with textnodes!
					}
					inner = inner.getNextSibling();
				} while (inner != null);
				// if we are still here it means no CData child was found and we need to add to a text node.
				if (textNode != null) {
					textNode.setNodeValue(value);
				} else {
					node.appendChild(node.getOwnerDocument().createTextNode(value));
				}
			}
		}
	}
	
	/**
	 * Gets the text value of a node.
	 * 
	 * @param node the node whose text value to get.
	 * @return the text value.
	 */
	public static String getTextValue(Node node){
		int numOfEntries = node.getChildNodes().getLength();
		for (int i = 0; i < numOfEntries; i++) {
			Node child = node.getChildNodes().item(i);
			Node nextChild = numOfEntries > i + 1 ? node.getChildNodes().item(i+1) : null;
			
			if (child.getNodeType() == Node.CDATA_SECTION_NODE) {
				return ((CDATASection) child).getData();
				
			} else if (nextChild != null && nextChild.getNodeType() == Node.CDATA_SECTION_NODE) {
				return ((CDATASection) nextChild).getData();
				
			} else if (child.getNodeType() == Node.TEXT_NODE) {
				
				//These iterations are for particularly firefox which when comes accross
				//text bigger than 4096, splits it into multiple adjacent text nodes
				//each not exceeding the maximum 4096. This is as of 04/04/2009
				//and for Firefox version 3.0.8
				String s = "";

				for(int index = i; index<numOfEntries; index++){
					Node currentNode = node.getChildNodes().item(index);
					String value = currentNode.getNodeValue();
					if(currentNode.getNodeType() == Node.TEXT_NODE && value != null)
						s += value;
					else
						break;
				}

				return s;
				//return node.getChildNodes().item(i).getNodeValue();
			}

			String val = getTextValue((Element) node.getChildNodes().item(i));
			if (node.getChildNodes().item(i).getNodeType() == Node.ELEMENT_NODE) {
				if (val != null)
					return val;
			}
		}

		return null;
	}


	/**
	 * Sets the text value of a node.
	 * 
	 * @param node the node whose text value to set.
	 * @param value the text value.
	 * @return true if the value was set successfully, else false.
	 */
	public static boolean setTextNodeValue(Element node, String value){
		if(node == null)
			return false;

		int numOfEntries = node.getChildNodes().getLength();
		for (int i = 0; i < numOfEntries; i++) {
			if (node.getChildNodes().item(i).getNodeType() == Node.TEXT_NODE){
				node.getChildNodes().item(i).setNodeValue(value);
				return true;
			}

			if(node.getChildNodes().item(i).getNodeType() == Node.ELEMENT_NODE){
				if(setTextNodeValue((Element)node.getChildNodes().item(i),value))
					return true;
			}
		}
		
		if(numOfEntries == 0){
			node.appendChild(node.getOwnerDocument().createTextNode(value));
			return true;
		}
		
		return false;
	}
	
	
	/*public static boolean setTextValue(Node node, String value){
		if(node == null)
			return false;

		int numOfEntries = node.getChildNodes().getLength();
		for (int i = 0; i < numOfEntries; i++) {
			if (node.getChildNodes().item(i).getNodeType() == Node.TEXT_NODE){
				node.getChildNodes().item(i).setNodeValue(value);
				return true;
			}
		}
		return false;
	}*/



	/**
	 * Gets a node name without the namespace prefix.
	 * 
	 * @param element the node.
	 * @return the name.
	 */
	public static String getNodeName(Element element){
		String name = element.getNodeName();
		String prefix = element.getPrefix();
		if(prefix != null){
			if(name.startsWith(prefix))
				name = name.replace(prefix+":", "");
		}
		return name;
	}


	/**
	 * Gets a child element of a parent node with a given name.
	 * 
	 * @param parent - the parent element
	 * @param name - the name of the child.
	 * @return - the child element.
	 */
	public static Element getNode(Element parent, String name){
		if(parent == null)
			return null;

		for(int i=0; i<parent.getChildNodes().getLength(); i++){
			if(parent.getChildNodes().item(i).getNodeType() != Node.ELEMENT_NODE)
				continue;

			Element child = (Element)parent.getChildNodes().item(i);
			if(XmlUtil.getNodeName(child).equals(name))
				return child;
			else if(name.contains("/")){
				String parentName = name.substring(0,name.indexOf('/'));
				if(XmlUtil.getNodeName(child).equals(parentName)){
					child = getNode(child,name.substring(name.indexOf('/') + 1));
					if(child != null)
						return child;
				}
			}

			child = getNode(child,name);
			if(child != null)
				return child;
		}

		return null;
	}


	/**
	 * Gets the text value of a node with a given name.
	 * 
	 * @param parentNode the parent node of the node whose text value we are to get.
	 *                 this node is normally an xforms instance data node.
	 * @param name the name of the node.
	 * @return the node text value.
	 */
	public static String getNodeTextValue(Element parentNode,String name){
		Element node = XmlUtil.getNode(parentNode,name);
		if(node != null)
			return XmlUtil.getTextValue(node);
		return null;
	}


	/**
	 * Creates an xml document object from its text xml.
	 * 
	 * @param xml the text xml.
	 * @return the document object.
	 */
	public static Document getDocument(String xml){
		return XMLParser.parse(xml);
	}


	/**
	 * Converts an xml document to a string.
	 * 
	 * @param doc the document.
	 * @return the xml string.
	 */
	public static String fromDoc2String(Document doc){
		return doc.toString();
	}


	/**
	 * Gets the next sibling of a node whose type is Node.ELEMENT_NODE
	 * 
	 * @param node the node whose next sibling element to get.
	 * @return the next sibling element.
	 */
	public static Element getNextElementSibling(Element node){
		Node sibling = node.getNextSibling();
		while(sibling != null){
			if(sibling.getNodeType() == Node.ELEMENT_NODE)
				return (Element)sibling;
			sibling = sibling.getNextSibling();
		}

		return node;
	}
	
	
	public static Node getChildElement(Node node){
		NodeList nodes = node.getChildNodes();
		for(int index = 0; index < nodes.getLength(); index++){
			Node child = nodes.item(index);
			if(child.getNodeType() == Node.ELEMENT_NODE)
				return child;
		}
		
		return node;
	}
	
	public static Node getChildCDATA(Node node) {
		NodeList nodes = node.getChildNodes();
		for (int index = 0; index < nodes.getLength(); index++) {
			Node child = nodes.item(index);
			if(child.getNodeType() == Node.CDATA_SECTION_NODE)
				return child;
		}
		
		return node;
	}
	
	/**
	 * Same as node.hasChildNodes() but ignores non-element nodes.
	 * @param node
	 * @return
	 */
	public static boolean hasChildElementNodes(Node node) {
		if (node != null && node.hasChildNodes()) {
			NodeList nodes = node.getChildNodes();
			for (int index = 0; index < nodes.getLength(); index++) {
				Node child = nodes.item(index);
				if (child.getNodeType() == Node.ELEMENT_NODE)
					return true;
			}
		}
		return false;
	}
	
	/**
	 * Merges the data from source into target, notice that only the elements from target are checked (eg. != union) (non-existing target attributes are taken over).
	 * <p>Both elements must have the same structure or nothing will be merged.
	 * 
	 * @param source
	 * @param target
	 */
	public static void merge(Element source, Element target) {
		if (target == null) {
			GWT.log("cannot merge null node");
			return;
		}
		
		String val = source == null ? null : getTextValueShallow(source);
		setTextValueShallow(target, val == null ? "" : val);
		
		List<String> foundAttrs = new ArrayList<String>();
		if (target.hasAttributes()) {
			NamedNodeMap atts = target.getAttributes();
			for (int i = 0; i < atts.getLength(); i++) {
				Node attr = atts.item(i);
				String attVal = source == null ? "" : source.getAttribute(attr.getNodeName());
				attr.setNodeValue(attVal == null ? "" : attVal);
				foundAttrs.add(attr.getNodeName());
			}
		}
		
		if (source != null && source.hasAttributes()) {
			NamedNodeMap atts = source.getAttributes();
			for (int i = 0; i < atts.getLength(); i++) {
				Node attr = atts.item(i);
				if (!foundAttrs.contains(attr.getNodeName())) {
					target.setAttribute(attr.getNodeName(), attr.getNodeValue());
				}
			}
		}
		
		if (target.hasChildNodes()) {
			NodeList children = target.getChildNodes();
			for (int i = 0; i < children.getLength(); i++) {
				Node targetChild = children.item(i);
				if (Node.ELEMENT_NODE == targetChild.getNodeType()) {
					merge(getNode(source, targetChild.getNodeName()), (Element) targetChild);
				}
			}
		}
	}
	
	/**
	 * Returns the first element found with xpathExpression. (ignoring text and other nodes)
	 * @param parent
	 * @param xpathExpr
	 * @return
	 */
	public static Element findElement(Node parent, String xpathExpr) {
		XPathExpression xpls =  new XPathExpression(parent, xpathExpr);
		List<?> result = xpls.getResult();
		if (result != null && result.size() > 0) {
			for (int i = 0; i < result.size(); i++) {
				Node r = (Node) result.get(i);
				if (Node.ELEMENT_NODE == r.getNodeType()) {
					return (Element) r;
				}
			}
		}
		return null;
	}
}

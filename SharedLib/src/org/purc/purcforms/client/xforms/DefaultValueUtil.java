package org.purc.purcforms.client.xforms;

import java.util.Iterator;
import java.util.List;

import org.purc.purcforms.client.model.FormDef;
import org.purc.purcforms.client.model.PageDef;
import org.purc.purcforms.client.model.QuestionDef;
import org.purc.purcforms.client.xpath.XPathExpression;

import com.google.gwt.xml.client.Element;


/**
 * Utility methods used for getting default values in xforms xml documents
 * and updating their corresponding question definition objects in the object model.
 * 
 * @author daniel
 *
 */
public class DefaultValueUtil {

	/**
	 * All methods in this class are static and hence we expect no external
	 * Instantiation of this class.
	 */
	private DefaultValueUtil(){
	}

	public static void setDefaultValue(Element dataNode, QuestionDef questionDef) {
		if (questionDef.isAsAttribute()) {
			setAttributeDefaultValue(questionDef, questionDef.getBareBinding(), dataNode);
			
		} else {
			String defValue = null;
			XPathExpression xpls = new XPathExpression(dataNode, questionDef.getFullBinding());
			List<?> result = xpls.getResult();
			if (result != null && result.size() > 0) {
				Element n = (Element) result.get(0);
				defValue = XmlUtil.getTextValue(n);
				if (defValue != null) { defValue = defValue.trim(); }
				if ("".equals(defValue)) { defValue = null; }
			}
			
			questionDef.setDefaultValue(defValue);
			
			if (QuestionDef.QTN_TYPE_GROUP == questionDef.getDataType() || QuestionDef.QTN_TYPE_REPEAT == questionDef.getDataType()) {
				for (QuestionDef qd : questionDef.getGroupQtnsDef().getQuestions()) {
					setDefaultValue(dataNode, qd);
				}
			}
		}
	}

	/**
	 * Sets all default values of questions in a form definition object
	 * as per the xforms document being parsed.
	 * 
	 * @param dataNode the xforms instance data node.
	 * @param formDef the form definition object.
	 * @param id2VarNameMap a map between questions ids and their binding or variableName.
	 */
	public static void setDefaultValues(Element dataNode, FormDef formDef) {
		if (formDef == null || dataNode == null || formDef.getPageCount() == 0) { return; }
		
		for (PageDef pd : formDef.getPages()) {
			for (QuestionDef qd : pd.getQuestions()) {
				setDefaultValue(dataNode, qd);
			}
		}
	}

	/**
	 * Sets a question's default value which comes from a node attribute value.
	 * 
	 * @param qtn the question definition object.
	 * @param variableName the binding or variable name of the question.
	 * @param dataNode the xforms instance data node.
	 */
	private static void setAttributeDefaultValue(QuestionDef qtn, String variableName, Element dataNode) {
		String xpath = qtn.getAttributeBinding();

		XPathExpression xpls = new XPathExpression(dataNode, xpath);
		List result = xpls.getResult();

		for (Iterator e = result.iterator(); e.hasNext();) {
			Object obj = e.next();
			if (obj instanceof Element){
				Element el = (Element) obj;
				qtn.setDefaultValue(el.getAttribute(variableName));
				return;
			}
		}
		// -- element is not available (may be optional)
		// throw new IllegalStateException("Node: " + xpath + " niet gevonden! Controleer het veld attribuutBinding van vraag: " + variableName);
	}

}

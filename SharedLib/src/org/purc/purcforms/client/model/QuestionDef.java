package org.purc.purcforms.client.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.purc.purcforms.client.controller.QuestionChangeListener;
import org.purc.purcforms.client.locale.LocaleText;
import org.purc.purcforms.client.util.FormUtil;
import org.purc.purcforms.client.xforms.UiElementBuilder;
import org.purc.purcforms.client.xforms.XformBuilderUtil;
import org.purc.purcforms.client.xforms.XformConstants;
import org.purc.purcforms.client.xforms.XformUtil;
import org.purc.purcforms.client.xforms.XmlUtil;
import org.purc.purcforms.client.xpath.XPathExpression;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.xml.client.Document;
import com.google.gwt.xml.client.Element;
import com.google.gwt.xml.client.Node;
import com.google.gwt.xml.client.NodeList;


/** 
 * This is the question definition.
 * 
 * @author Daniel Kayiwa
 *
 */
public class QuestionDef implements Serializable{

	/**
	 * Generated serialization ID
	 */
	private static final long serialVersionUID = -7618662640178320577L;

	/** The value to save for boolean questions when one selects the yes option. */
	public static final String TRUE_VALUE = "true";

	/** The value to save for the boolean questions when one selects the no option. */
	public static final String FALSE_VALUE = "false";

	/** The text to display for boolean questions for the yes option. */
	public static final String TRUE_DISPLAY_VALUE = LocaleText.get("yes");

	/** The text to display for boolean questions for the no option. */
	public static final String FALSE_DISPLAY_VALUE = LocaleText.get("no");

	/** The prompt text. The text the user sees. */
	private String text = ModelConstants.EMPTY_STRING;

	/** The help text. */
	private String helpText = ModelConstants.EMPTY_STRING;

	/** The type of question. eg Numeric,Date,Text etc. */
	private int dataType = QTN_TYPE_TEXT;

	/** The value supplied as answer if the user has not supplied one. */
	private String defaultValue;

	/** The question answer of value. */
	private String answer;

	//TODO For a smaller payload, may need to combine (mandatory,visible,enabled,locked) 
	//into bit fields forming one byte. This would be a saving of 3 bytes per question.
	/** A flag to tell whether the question is to be answered or is optional. */
	private boolean required = false;

	/** A flag to tell whether the question should be shown or not. */
	private boolean visible = true;

	/** A flag to tell whether the question should be enabled or disabled. */
	private boolean enabled = true;

	/** A flag to tell whether a question is to be locked or not. A locked question 
	 * is one which is visible, enabled, but cannot be edited.
	 */
	private boolean locked = false;
	
	/**
	 * A flag to tell whether a question should be presisted as attribute
	 */
	private boolean asAttribute = false;
	
	/**
	 * The Element to receive the attribute if this question is to be persisted as Attribute.
	 */
	private String attributeBinding = ModelConstants.EMPTY_STRING;

	//TODO We have a bug here when more than one question, on a form, have the 
	//same variable names.
	//TODO May not need to serialize this property for smaller pay load. Then we would just rely on the id.
	/** The text indentifier of the question. This is used by the users of the questionaire 
	 * but in code we use the dynamically generated numeric id for speed. 
	 */
	private String binding = ModelConstants.EMPTY_STRING;

	/** The allowed set of values (OptionDef) for an answer of the question. 
	 * This also holds group sets of questions (GroupQtnsDef / RepeatQtnsDef) for the QTN_TYPE_REPEAT / QTN_TYPE_GROUP.
	 * This is an optimization aspect to prevent storing these guys differently as 
	 * they can't both happen at the same time. The internal storage implementation of these
	 * repeats is hidden from the user by means of getRepeatQtnsDef() and setRepeatQtnsDef().
	 */
	private Object options;

	/** The numeric identifier of a question. When a form definition is being built, each question is 
	 * given a unique (on a form) id starting from 1 .
	 */
	private int id = ModelConstants.NULL_ID;

	/** Text question type. */
	public static final int QTN_TYPE_TEXT = 1;

	/** Numeric question type. These are numbers without decimal points*/
	public static final int QTN_TYPE_NUMERIC = 2;

	/** Decimal question type. These are numbers with decimals */
	public static final int QTN_TYPE_DECIMAL = 3;

	/** Date question type. This has only date component without time. */
	public static final int QTN_TYPE_DATE = 4;

	/** Time question type. This has only time element without date*/
	public static final int QTN_TYPE_TIME = 5;

	/** This is a question with alist of options where not more than one option can be selected at a time. */
	public static final int QTN_TYPE_LIST_EXCLUSIVE = 6;

	/** This is a question with alist of options where more than one option can be selected at a time. */
	public static final int QTN_TYPE_LIST_MULTIPLE = 7;

	/** Date and Time question type. This has both the date and time components*/
	public static final int QTN_TYPE_DATE_TIME = 8;

	/** Question with true and false answers. */
	public static final int QTN_TYPE_BOOLEAN = 9;

	/** Question with repeat sets of questions. */
	public static final int QTN_TYPE_REPEAT = 10;

	/** Question with image. */
	public static final int QTN_TYPE_IMAGE = 11;

	/** Question with recorded video. */
	public static final byte QTN_TYPE_VIDEO = 12;

	/** Question with recoded audio. */
	public static final byte QTN_TYPE_AUDIO = 13;

	/** Question whose list of options varies basing on the value selected from another question.
	 * An example of such a question would be countries where the list depends on the continent
	 * selected in the continent question.
	 */
	public static final int QTN_TYPE_LIST_EXCLUSIVE_DYNAMIC = 14;

	/** Question with GPS cordinates. */
	public static final int QTN_TYPE_GPS = 15;

	/** Question with barcode cordinates. */
	public static final int QTN_TYPE_BARCODE = 16;
	
	/** Question with group of questions. */
	public static final int QTN_TYPE_GROUP = 17;


	/** The xforms model data node into which this question will feed its answer. */
	private Element dataNode;
	private Node dataNodeParentNode;
	private Node dataNodeNextSibling;

	/** The xforms label node for this question. */
	private Element labelNode;

	/** The xforms hint node for this question. */
	private Element hintNode;

	/** The xforms bind node for this question. */
	private Element bindNode;

	/** The xforms input,select, or select1 node for the question. */
	private Element controlNode;

	/** For select and select1 questions, this is the reference to the node representing
	 * the first option.
	 */
	private Element firstOptionNode;


	/** A list of interested listeners to the question change events. */
	private List<QuestionChangeListener> changeListeners = new ArrayList<QuestionChangeListener>();

	/** The parent object for this question. It could be a page or
	 * just another question as for repeat/group question kids. 
	 */
	private Object parent;
	
	/** The xpath expression pointing to the corresponding node in the xforms document. */
	private String xpathExpressionLabel;
	private String xpathExpressionHint;
	
	private boolean repeatChild = false;
	private String repeatBinding;

	private boolean asCdata = false;

	/** This constructor is used mainly during deserialization. */
	public QuestionDef(Object parent){
		this.parent = parent;
	}

	/** The copy constructor. */
	public QuestionDef(QuestionDef questionDef, Object parent){
		this(parent);
		setId(questionDef.getId());
		setText(questionDef.getText());
		setHelpText(questionDef.getHelpText());
		setDataType(questionDef.getDataType());
		setDefaultValue(questionDef.getDefaultValue());
		setVisible(questionDef.isVisible());
		setEnabled(questionDef.isEnabled());
		setLocked(questionDef.isLocked());
		setRequired(questionDef.isRequired());
		setBinding(questionDef.getBareBinding());
		setAsAttribute(questionDef.isAsAttribute());
		setAttributeBinding(questionDef.getAttributeBinding());
		this.xpathExpressionLabel = questionDef.xpathExpressionLabel;
		this.xpathExpressionHint = questionDef.xpathExpressionHint;

		if(getDataType() == QuestionDef.QTN_TYPE_LIST_EXCLUSIVE || getDataType() == QuestionDef.QTN_TYPE_LIST_MULTIPLE)
			copyQuestionOptions(questionDef.getOptions());
		else if(getDataType() == QuestionDef.QTN_TYPE_REPEAT)
			this.options = new RepeatQtnsDef((RepeatQtnsDef) questionDef.getGroupQtnsDef());
		else if(getDataType() == QuestionDef.QTN_TYPE_GROUP)
			this.options = new GroupQtnsDef(questionDef.getGroupQtnsDef());
	}

	public QuestionDef(int id,String text, int type, String variableName, Object parent) {
		this(parent);
		setId(id);
		setText(text);
		setDataType(type);
		setBinding(variableName);
	}

	/**
	 * Constructs a new question definition object from the supplied parameters.
	 * For String type parameters, they should NOT be NULL. They should instead be empty,
	 * for the cases of missing values.
	 * 
	 * @param id
	 * @param text
	 * @param helpText - The hint or help text. Should NOT be NULL.
	 * @param mandatory
	 * @param type
	 * @param defaultValue
	 * @param visible
	 * @param enabled
	 * @param locked
	 * @param variableName
	 * @param options
	 */
	public QuestionDef(int id,String text, String helpText, boolean mandatory, int type, String defaultValue, boolean visible, boolean enabled, boolean locked, String variableName, Object options,Object parent) {
		this(parent);
		setId(id);
		setText(text);
		setHelpText(helpText);
		setDataType(type);
		setDefaultValue(defaultValue);
		setVisible(visible);
		setEnabled(enabled);
		setLocked(locked);
		setRequired(mandatory);		
		setBinding(variableName);
		setOptions(options);
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getDefaultValue() {
		return defaultValue;
	}

	public static boolean isDateFunction(String value){
		if(value == null)
			return false;

		return (value.contains("now()") || value.contains("date()")
				||value.contains("getdate()") || value.contains("today()"));
	}

	public static Date getDateFunctionValue(String function){
		return new Date();
	}

	public boolean isDate(){
		return (dataType == QuestionDef.QTN_TYPE_DATE_TIME || 
				dataType == QuestionDef.QTN_TYPE_DATE ||
				dataType == QuestionDef.QTN_TYPE_TIME);
	}

	public String getDefaultValueDisplay() {
		if(isDate() && isDateFunction(defaultValue)){
			if(dataType == QuestionDef.QTN_TYPE_TIME)
				return FormUtil.getTimeDisplayFormat().format(getDateFunctionValue(defaultValue));
			else if(dataType == QuestionDef.QTN_TYPE_DATE_TIME)
				return FormUtil.getDateTimeDisplayFormat().format(getDateFunctionValue(defaultValue));
			else
				return FormUtil.getDateDisplayFormat().format(getDateFunctionValue(defaultValue));
		}

		return defaultValue;
	}

	/**
	 * returns defaultvalue, or a newly generated date if there is a dateFormula.
	 * @return
	 */
	public String getDefaultValueSubmit() {
		if(isDate() && isDateFunction(defaultValue)){
			if(dataType == QuestionDef.QTN_TYPE_TIME)
				return FormUtil.getTimeSubmitFormat().format(new Date());
			else if(dataType == QuestionDef.QTN_TYPE_DATE_TIME)
				return FormUtil.getDateTimeSubmitFormat().format(new Date());
			else
				return FormUtil.getDateSubmitFormat().format(new Date());
		}

		return defaultValue;
	}

	/**
	 * This will set both the defaultvalue and overwrite the answer. Do not call after data has been loaded!
	 * 
	 * @param defaultValue
	 */
	public void setDefaultValue(String defaultValue) {
		//if(defaultValue != null && defaultValue.trim().length() > 0)
		this.defaultValue = defaultValue;
		this.answer =  defaultValue;
	}

	public String getAnswer() {
		return answer;
	}
	
	/**
	 * The answer is available (in result xml) if the question is enabled or locked or required
	 * @return
	 */
	public String getAnswerIfAvailable() {
		return enabled || locked || required ? answer : (QTN_TYPE_DECIMAL == dataType || QTN_TYPE_NUMERIC == dataType) ? ""+0 : "";
	}

	/**
	 * Expects formatted answer (locale) (if numeric/decimal)
	 * 
	 * @param answer
	 */
	public void setAnswer(String answer) {
		if (getDataType() == QuestionDef.QTN_TYPE_DECIMAL) {
			this.answer = FormUtil.parseDecimalFormat(answer);
		} else if(getDataType() == QuestionDef.QTN_TYPE_NUMERIC) {
			this.answer = FormUtil.parseNumberFormat(answer);
		} else {
			this.answer = answer;
		}
	}

	/**
	 * Set data without i18n conversion.
	 * @param answer
	 */
	public void setAnswerRaw(String answer) {
		this.answer = answer;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		boolean changed = this.enabled != enabled;

		this.enabled = enabled;

		if(changed){
			for(int index = 0; index < changeListeners.size(); index++)
				changeListeners.get(index).onEnabledChanged(this,enabled);
		}
	}

	public String getHelpText() {
		return helpText;
	}

	public void setHelpText(String helpText) {
		this.helpText = helpText;
	}

	public boolean isLocked() {
		return locked;
	}

	public void setLocked(boolean locked) {
		boolean changed = this.locked != locked;

		this.locked = locked;

		if(changed){
			for(int index = 0; index < changeListeners.size(); index++)
				changeListeners.get(index).onLockedChanged(this,locked);
		}
	}

	public boolean isRequired() {
		return required;
	}

	public void setRequired(boolean required) {
		boolean changed = this.required != required;

		this.required = required;

		if(changed){
			for(int index = 0; index < changeListeners.size(); index++)
				changeListeners.get(index).onRequiredChanged(this,required);
		}
	}

	@SuppressWarnings("rawtypes")
	public List getOptions() {
		if (options == null) {
			return null;
		} else {
			return (List) options;
		}
	}

	public void setOptions(Object options) {
		// TODO test for datatype
		this.options = options;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {	
		this.text = text;
	}

	public int getDataType() {
		return dataType;
	}

	public void setDataType(int dataType) {
		boolean changed = this.dataType != dataType;

		if (changed) {
			this.dataType = dataType;
			
			//if(controlNode != null && (dataType == QuestionDef.QTN_TYPE_LIST_EXCLUSIVE || dataType == QuestionDef.QTN_TYPE_LIST_MULTIPLE))
			//	options = new ArrayList();

			for(int index = 0; index < changeListeners.size(); index++)
				changeListeners.get(index).onDataTypeChanged(this,dataType);
		}
	}

	/**
	 * @return attributePath + name if Attribute, normal binding if not.
	 */
	public String getBinding() {
		if (isAsAttribute()) {
			return getAttributeBinding() + "@" + binding;
		} else {
			return binding;
		}
	}

	/**
	 * Only last node
	 * @return
	 */
	public String getBareBinding() {
		return binding;
	}

	/**
	 * This includes the parentname, but not the page (eg. if the parent is a group or repeat)
	 * @return
	 */
	public String getFullBinding() {
		if (!isAsAttribute() && getParent() != null && getParent() instanceof QuestionDef) {
			QuestionDef parent = (QuestionDef) getParent();
			if (parent.getDataType() == QuestionDef.QTN_TYPE_REPEAT || parent.getDataType() == QuestionDef.QTN_TYPE_GROUP) {
				return getParentBinding() + "/" + getBareBinding();
			}
		}
		return getBinding();
	}
	
	/**
	 * This includes the parentname, but not the page (eg. if the parent is a group or repeat)
	 * @return
	 */
	public String getFullText() {
		if (getParent() != null && getParent() instanceof QuestionDef) {
			QuestionDef parent = (QuestionDef) getParent();
			if (parent.getDataType() == QuestionDef.QTN_TYPE_REPEAT || parent.getDataType() == QuestionDef.QTN_TYPE_GROUP) {
				return getParentText() + " / " + getText();
			}
		}

		return getText();
	}

	public String getParentBinding() {
		if (getParent() != null && getParent() instanceof QuestionDef) {
			return ((QuestionDef) getParent()).getBinding();
		} else {
			return null;
		}
	}
	
	public String getParentText() {
		if (getParent() != null && getParent() instanceof QuestionDef) {
			return ((QuestionDef) getParent()).getText();
		} else {
			return null;
		}
	}
	
	public void setBinding(String variableName) {
		boolean changed = getBinding() != variableName;
		this.binding = variableName;

		// only take last part of binding
		if (binding != null && !"".equals(binding)) {
			int idx = binding.lastIndexOf("@");
			if (idx == -1) { idx = binding.lastIndexOf("/"); }
			if (idx > -1) { binding = binding.substring(idx + 1); }
		}

		if(changed){
			for(int index = 0; index < changeListeners.size(); index++)
				changeListeners.get(index).onBindingChanged(this, getBinding());
		}
	}

	public Object getParent() {
		return parent;
	}

	public void setParent(Object parent) {
		this.parent = parent;
	}

	/**
	 * @return the bindNode
	 */
	public Element getBindNode() {
		return bindNode;
	}

	/**
	 * @param bindNode the bindNode to set
	 */
	public void setBindNode(Element bindNode) {
		this.bindNode = bindNode;
	}

	/**
	 * @return the dataNode
	 */
	public Element getDataNode() {
		return dataNode;
	}

	/**
	 * @param dataNode the dataNode to set
	 */
	public void setDataNode(Element dataNode) {
		this.dataNode = dataNode;
	}

	/**
	 * @return the hintNode
	 */
	public Element getHintNode() {
		return hintNode;
	}

	/**
	 * @param hintNode the hintNode to set
	 */
	public void setHintNode(Element hintNode) {
		this.hintNode = hintNode;
	}

	/**
	 * @return the labelNode
	 */
	public Element getLabelNode() {
		return labelNode;
	}

	/**
	 * @param labelNode the labelNode to set
	 */
	public void setLabelNode(Element labelNode) {
		this.labelNode = labelNode;
	}

	/**
	 * @return the controlNode
	 */
	public Element getControlNode() {
		return controlNode;
	}

	/**
	 * @param controlNode the controlNode to set
	 */
	public void setControlNode(Element controlNode) {
		this.controlNode = controlNode;
	}

	/**
	 * @return the firstOptionNode
	 */
	public Element getFirstOptionNode() {
		return firstOptionNode;
	}

	/**
	 * @param firstOptionNode the firstOptionNode to set
	 */
	public void setFirstOptionNode(Element firstOptionNode) {
		this.firstOptionNode = firstOptionNode;
	}

	public boolean isVisible() {
		return visible;
	}

	public void setVisible(boolean visible) {
		boolean changed = this.visible != visible;

		this.visible = visible;

		if(changed){
			for(int index = 0; index < changeListeners.size(); index++)
				changeListeners.get(index).onVisibleChanged(this,visible);
		}
	}

	public void removeChangeListener(QuestionChangeListener changeListener) {
		changeListeners.remove(changeListener);
	}

	public void addChangeListener(QuestionChangeListener changeListener) {
		if(!changeListeners.contains(changeListener))
			changeListeners.add(changeListener);
	}

	public void clearChangeListeners(){
		if(changeListeners != null)
			changeListeners.clear();
	}

	public void addOption(OptionDef optionDef){
		addOption(optionDef, null, true);
	}
	
	public void addOption(OptionDef optionDef, OptionDef refOptionDef){
		addOption(optionDef, refOptionDef, true);
	}

	public void addOption(OptionDef optionDef, OptionDef refOptionDef, boolean setAsParent){
		if(options == null || !(options instanceof ArrayList))
			options = new ArrayList();
		
		if(refOptionDef == null)
			((List)options).add(optionDef);
		else
			((List)options).add(((List)options).indexOf(refOptionDef) + 1, optionDef);
		
		if(setAsParent)
			optionDef.setParent(this);
	}

	public GroupQtnsDef getGroupQtnsDef(){
		if(options == null || !(options instanceof GroupQtnsDef)) { options = (getDataType() == QuestionDef.QTN_TYPE_GROUP) ? new GroupQtnsDef() : new RepeatQtnsDef(); }
		return (GroupQtnsDef) options;
	}

	public void addGroupQtnsDef(QuestionDef qtn){
		if(options == null || !(options instanceof GroupQtnsDef)) { options = new GroupQtnsDef(qtn); }
		((GroupQtnsDef) options).addQuestion(qtn);
		qtn.setParent(this);
	}
	
	public void addRepeatQtnsDef(QuestionDef qtn){
		if(options == null || !(options instanceof GroupQtnsDef)) { options = new RepeatQtnsDef(qtn); }
		((GroupQtnsDef) options).addQuestion(qtn);
		qtn.setParent(this);
	}

	public void setGroupQtnsDef(GroupQtnsDef groupQtnsDef){
		options = groupQtnsDef;
	}

	public String toString() {
		return getText();
	}

	/**
	 * This is iffy, does not work for group/repeat questions.
	 */
	private void copyQuestionOptions(List options){
		if(options == null)
			return;

		this.options = new ArrayList();
		for(int i=0; i<options.size(); i++)
			((List)this.options).add(new OptionDef((OptionDef)options.get(i),this));
	}

	public void removeOption(OptionDef optionDef){
		if(options instanceof List){ //Could be a RepeatQtnsDef
			((List)options).remove(optionDef);

			if(((List)options).size() == 0)
				firstOptionNode = null;
		}

		if(controlNode != null && optionDef.getControlNode() != null)
			controlNode.removeChild(optionDef.getControlNode());
	}

	public void moveOptionUp(OptionDef optionDef){
		if(!(getDataType()==QuestionDef.QTN_TYPE_LIST_EXCLUSIVE ||
				getDataType()==QuestionDef.QTN_TYPE_LIST_MULTIPLE))
			return;

		List optns = (List)options;
		int index = optns.indexOf(optionDef);

		optns.remove(optionDef);

		//Store the question to replace
		OptionDef currentOptionDef = (OptionDef)optns.get(index-1);
		if(controlNode != null && optionDef.getControlNode() != null && currentOptionDef.getControlNode() != null)
			controlNode.removeChild(optionDef.getControlNode());

		List list = new ArrayList();
		//Remove all from index before selected all the way downwards
		while(optns.size() >= index){
			currentOptionDef = (OptionDef)optns.get(index-1);
			list.add(currentOptionDef);
			optns.remove(currentOptionDef);
		}

		optns.add(optionDef);
		for(int i=0; i<list.size(); i++){
			if(i == 0){
				OptionDef optnDef = (OptionDef)list.get(i);
				if(controlNode != null && optnDef.getControlNode() != null && optionDef.getControlNode() != null)
					controlNode.insertBefore(optionDef.getControlNode(), optnDef.getControlNode());
			}
			optns.add(list.get(i));
		}
	}

	public void moveOptionDown(OptionDef optionDef){
		if(!(getDataType()==QuestionDef.QTN_TYPE_LIST_EXCLUSIVE ||
				getDataType()==QuestionDef.QTN_TYPE_LIST_MULTIPLE))
			return;

		List optns = (List)options;
		int index = optns.indexOf(optionDef);	

		optns.remove(optionDef);

		if(controlNode != null && optionDef.getControlNode() != null)
			controlNode.removeChild(optionDef.getControlNode());

		OptionDef currentItem; // = parent.getChild(index - 1);
		List list = new ArrayList();

		//Remove all otions below selected index
		while(optns.size() > 0 && optns.size() > index){
			currentItem = (OptionDef)optns.get(index);
			list.add(currentItem);
			optns.remove(currentItem);
		}

		for(int i=0; i<list.size(); i++){
			if(i == 1){
				optns.add(optionDef); //Add after the first item but before the current (second).

				if(controlNode != null){
					OptionDef optnDef = getNextSavedOption(list,i); //(OptionDef)list.get(i);
					if(optnDef.getControlNode() != null && optionDef.getControlNode() != null)
						controlNode.insertBefore(optionDef.getControlNode(), optnDef.getControlNode());
					else if(optionDef.getControlNode() != null)
						controlNode.appendChild(optionDef.getControlNode());
				}
			}
			optns.add(list.get(i));
		}

		//If was second last and hence becoming last
		if(list.size() == 1){
			optns.add(optionDef);

			if(controlNode != null && optionDef.getControlNode() != null)
				controlNode.appendChild(optionDef.getControlNode());
		}
	}

	private OptionDef getNextSavedOption(List options, int index){
		for(int i=index; i<options.size(); i++){
			OptionDef optionDef = (OptionDef)options.get(i);
			if(optionDef.getControlNode() != null)
				return optionDef;
		}
		return (OptionDef)options.get(index);
	}

	@Deprecated // there's something wrong with updating -- always create new (KH)
	public boolean updateDoc(Document doc, Element xformsNode, FormDef formDef, Element formNode, Element modelNode,Element groupNode,boolean appendParentBinding, boolean withData, String orgFormVarName, String parentBinding){
		boolean isNew = controlNode == null;
		if(controlNode == null) //Must be new question.
			UiElementBuilder.fromQuestionDef2Xform(this, doc, xformsNode, formDef, formNode, modelNode, groupNode, true);
		else
			updateControlNodeName(); // == type

		if(labelNode != null) //How can this happen
			XmlUtil.setTextNodeValue(labelNode,text);

		Element node = bindNode;
		if(node == null){
			/*//We are using a ref instead of bind
			node = controlNode;
			appendParentBinding = false;*/
			
			String bindingId = (parentBinding != null ? parentBinding + "/" : "") + binding;
			node = doc.createElement(XformConstants.NODE_NAME_BIND);
			node.setAttribute(XformConstants.ATTRIBUTE_NAME_ID, bindingId);
			node.setAttribute(XformConstants.ATTRIBUTE_NAME_NODESET, binding); //Will ensure that nodeset gets set below.
			
			if(FormUtil.isJavaRosaSaveFormat())
				insertBeforeLastChild(formDef.getModelNode(), node); //Insert before itext
			else
				formDef.getModelNode().appendChild(node);
			
			bindNode = node;
		}

		if(node != null){
			String binding = this.binding;
			if(!binding.startsWith("/"+ formDef.getBinding()+"/") && appendParentBinding){
				//if(!binding.contains("/"+ formDef.getVariableName()+"/"))
				if(!binding.startsWith(formDef.getBinding()+"/")){
					if(parentBinding != null && !binding.contains("/"))
						binding = "/"+ formDef.getBinding()+"/" + parentBinding + "/" + binding;
					else{
						binding = "/"+ formDef.getBinding() + (binding.startsWith("/") ? "" : "/") + binding;
					}
				}
				else{
					this.binding = "/" + this.binding; //correct user binding syntax error
					binding = this.binding;
				}
			}

			if(dataType != QuestionDef.QTN_TYPE_GROUP && dataType != QuestionDef.QTN_TYPE_REPEAT)
				node.setAttribute(XformConstants.ATTRIBUTE_NAME_TYPE, XformBuilderUtil.getXmlType(dataType,node));
			if(node.getAttribute(XformConstants.ATTRIBUTE_NAME_NODESET) != null)
				node.setAttribute(XformConstants.ATTRIBUTE_NAME_NODESET,binding);
			if(node.getAttribute(XformConstants.ATTRIBUTE_NAME_REF) != null)
				node.setAttribute(XformConstants.ATTRIBUTE_NAME_REF,binding);

			if(required)
				node.setAttribute(XformConstants.ATTRIBUTE_NAME_REQUIRED,XformConstants.XPATH_VALUE_TRUE);
			else
				node.removeAttribute(XformConstants.ATTRIBUTE_NAME_REQUIRED);

			if(!enabled)
				node.setAttribute(XformConstants.ATTRIBUTE_NAME_READONLY,XformConstants.XPATH_VALUE_TRUE);
			else
				node.removeAttribute(XformConstants.ATTRIBUTE_NAME_READONLY);

			if(locked)
				node.setAttribute(XformConstants.ATTRIBUTE_NAME_LOCKED,XformConstants.XPATH_VALUE_TRUE);
			else
				node.removeAttribute(XformConstants.ATTRIBUTE_NAME_LOCKED);

			if(!visible)
				node.setAttribute(XformConstants.ATTRIBUTE_NAME_VISIBLE,XformConstants.XPATH_VALUE_FALSE);
			else
				node.removeAttribute(XformConstants.ATTRIBUTE_NAME_VISIBLE);


			if(!(dataType == QuestionDef.QTN_TYPE_IMAGE || dataType == QuestionDef.QTN_TYPE_AUDIO ||
					dataType == QuestionDef.QTN_TYPE_VIDEO || dataType == QuestionDef.QTN_TYPE_GPS))
				node.removeAttribute(XformConstants.ATTRIBUTE_NAME_FORMAT);

			if(!(dataType == QuestionDef.QTN_TYPE_IMAGE || dataType == QuestionDef.QTN_TYPE_AUDIO ||
					dataType == QuestionDef.QTN_TYPE_VIDEO))
				controlNode.removeAttribute(XformConstants.ATTRIBUTE_NAME_MEDIATYPE);
			else
				UiElementBuilder.setMediaType(controlNode, dataType);
			

			if(dataNode != null)
				updateDataNode(doc,formDef,orgFormVarName, parentBinding);
		}

		if((getDataType() == QuestionDef.QTN_TYPE_LIST_EXCLUSIVE ||
				getDataType() == QuestionDef.QTN_TYPE_LIST_MULTIPLE) && options != null){

			boolean allOptionsNew = areAllOptionsNew();
			List newOptns = new ArrayList();
			List optns = (List)options;
			for(int i=0; i<optns.size(); i++){
				OptionDef optionDef = (OptionDef)optns.get(i);

				if(!allOptionsNew && optionDef.getControlNode() == null)
					newOptns.add(optionDef);

				optionDef.updateDoc(doc,controlNode);
				if(i == 0)
					firstOptionNode = optionDef.getControlNode();
			}

			for(int k = 0; k < newOptns.size(); k++){
				OptionDef optionDef = (OptionDef)newOptns.get(k);
				int proposedIndex = optns.size() - (newOptns.size() - k);
				int currentIndex = optns.indexOf(optionDef);
				if(currentIndex == proposedIndex)
					continue;

				moveOptionNodesUp(optionDef,getRefOption(optns,newOptns,currentIndex /*currentIndex+1*/));
			}
		}
		else if(getDataType() == QuestionDef.QTN_TYPE_GROUP || getDataType() == QuestionDef.QTN_TYPE_REPEAT) {
			getGroupQtnsDef().updateDoc(doc,xformsNode,formDef,formNode,modelNode,groupNode,withData,orgFormVarName);

			if(controlNode != null)
				((Element)controlNode.getParentNode()).setAttribute(XformConstants.ATTRIBUTE_NAME_ID, binding);

			if(!withData && dataNode != null){
				//Remove all repeating data kids
				Element parent = (Element)dataNode.getParentNode();
				NodeList nodes = parent.getElementsByTagName(dataNode.getNodeName());
				for(int index = 1; index < nodes.getLength(); index++){
					Node child = nodes.item(index);
					child.getParentNode().removeChild(child);
				}
			}
		}

		//Put after options because it depends on the firstOptionNode
		if(hintNode != null){
			if(helpText != null && helpText.trim().length() > 0)
				XmlUtil.setTextNodeValue(hintNode,helpText);
			else{
				controlNode.removeChild(hintNode);
				hintNode = null;
			}
		}
		else if(hintNode == null && helpText != null && helpText.trim().length() > 0)
			UiElementBuilder.addHelpTextNode(this, doc, controlNode, firstOptionNode);

		if(withData)
			updateNodeValue(doc,formNode,(answer != null) ? answer : defaultValue, withData, -1);
		else
			updateNodeValue(doc,formNode,defaultValue,withData, -1);

		return isNew;
	}

	private boolean areAllOptionsNew(){
		if(options == null)
			return false;

		List optns = (List)options;
		for(int i=0; i<optns.size(); i++){
			OptionDef optionDef = (OptionDef)optns.get(i);
			if(optionDef.getControlNode() != null)
				return false;
		}
		return true;
	}

	private OptionDef getRefOption(List options, List newOptions, int index){
		OptionDef optionDef;
		int i = index + 1;
		while(i < options.size()){
			optionDef = (OptionDef)options.get(i);
			if(!newOptions.contains(optionDef))
				return optionDef;
			i++;
		}

		return null;
	}

	public void updateNodeValue(FormDef formDef) {
		updateNodeValue(formDef.getDoc(), formDef.getDataNode(), answer, true, -1);
	}

	public void updateNodeValue(FormDef formDef, int row) {
		updateNodeValue(formDef.getDoc(), formDef.getDataNode(), answer, true, row);
	}

	public boolean hasDefaultValue() {
		return defaultValue != null && !"".equals(defaultValue.trim());
	}
	
	public void updateNodeValue(Document doc, Element formNode, String value, boolean withData, int row) {
		
		boolean keepNode = isEnabled() && (isVisible() || isRequired() || hasDefaultValue() || isLocked());
		
		if (!isAsAttribute() && !keepNode) {
			if (dataNode != null && dataNode.getParentNode() != null) {
				dataNodeParentNode = dataNode.getParentNode();
				dataNodeNextSibling = dataNode.getNextSibling();
				dataNode.getParentNode().removeChild(dataNode);
			}
			return;
		} else {
			if (!isAsAttribute() && dataNode != null && dataNode.getParentNode() == null) {
				if (dataNodeNextSibling != null) {
					dataNodeParentNode.insertBefore(dataNode, dataNodeNextSibling);
				} else {
					dataNodeParentNode.appendChild(dataNode);
				}
			}
		}
		
		if (dataType == QuestionDef.QTN_TYPE_GROUP || dataType == QuestionDef.QTN_TYPE_REPEAT) {
			return; // no more config for groups
		}
		
		if ((dataType == QuestionDef.QTN_TYPE_DATE || dataType == QuestionDef.QTN_TYPE_DATE_TIME) && value != null && value.trim().length() > 0){
			if(withData){
				DateTimeFormat formatter = (dataType == QuestionDef.QTN_TYPE_DATE_TIME) ? FormUtil.getDateTimeSubmitFormat() : FormUtil.getDateSubmitFormat(); //DateTimeFormat.getFormat(); //new DateTimeFormat("yyyy-MM-dd");

				if(value.contains("now()") || value.contains("date()") || value.contains("getdate()") || value.contains("today()"))
					value = formatter.format(new Date());
				else{
					//if(formatter != null)
					//	value = formatter.format(FormUtil.getDateTimeDisplayFormat().parse(value));
				}
			}
		}

		if (value != null && value.trim().length() > 0) {
			if(isAsAttribute())
				updateAttributeValue(formNode, value, row);
			else if(dataNode != null){
				if(isBinaryType()){
					NodeList childNodes = dataNode.getChildNodes();
					while(childNodes.getLength() > 0)
						dataNode.removeChild(childNodes.item(0));
					//Window.alert(variableName+"="+value.length());
					dataNode.appendChild(doc.createTextNode(value));
					
				} else {
					if(dataNode.getChildNodes().getLength() > 0) {
						if (isAsCdata()) {
							Node n = dataNode.getChildNodes().item(0);
							if (n.getNodeType() != Node.CDATA_SECTION_NODE) {
								dataNode.replaceChild(doc.createCDATASection(value), n);
							} else {
								n.setNodeValue(value);
							}
						} else {
							dataNode.getChildNodes().item(0).setNodeValue(value);
						}
					} else {
						if (isAsCdata()) {
							dataNode.appendChild(doc.createCDATASection(value));
						} else {
							dataNode.appendChild(doc.createTextNode(value));
						}
					}
				}
			}
		} else {
			if (isAsAttribute()) {
				updateAttributeValue(formNode, "", row);
			} else {
				// -- clear content
				if (dataNode != null && dataType != QuestionDef.QTN_TYPE_GROUP && dataType != QuestionDef.QTN_TYPE_REPEAT) {
					NodeList childNodes = dataNode.getChildNodes();
					while (childNodes.getLength() > 0) {
						dataNode.removeChild(childNodes.item(0));
					}
				}
				// -- remove from parent
				if (dataNode != null && dataNode.getParentNode() != null) {
					dataNodeParentNode = dataNode.getParentNode();
					dataNodeNextSibling = dataNode.getNextSibling();
					dataNode.getParentNode().removeChild(dataNode);
				}
			}
		}
	}

	/**
	 * Checks if this question is a multimedia (Picture,Audio & Video) type.
	 * 
	 * @return true if yes, else false.
	 */
	private boolean isBinaryType(){
		return (dataType == QuestionDef.QTN_TYPE_IMAGE || dataType == QuestionDef.QTN_TYPE_VIDEO ||
				dataType == QuestionDef.QTN_TYPE_AUDIO);
	}

	public void updateAttributeValue(Element formNode, String value) {
		updateAttributeValue(formNode, value, -1);
	}

	/**
	 * Update attribute
	 * @param formNode
	 * @param value
	 * @param row the row the attribute is in (only for repeat questions)
	 */
	public void updateAttributeValue(Element formNode, String value, int row) {
		if(dataType != QuestionDef.QTN_TYPE_GROUP && dataType != QuestionDef.QTN_TYPE_REPEAT) {
			if (row < 0) {
				XPathExpression xpls = new XPathExpression(formNode, attributeBinding);
				List<?> result = xpls.getResult();
				for (Object obj : result) {
					if (obj instanceof Element){
						Element el = (Element) obj;
						if ("".equals(value)) {
							el.removeAttribute(getBareBinding());
						} else {
							el.setAttribute(getBareBinding(), value);
						}
					}
				}
				return;

			} else { // repeat -- is a bit special because we cannot just take row (might have been deleted)
				String recordName = attributeBinding.substring(0, attributeBinding.indexOf("/"));
				String fieldName = attributeBinding.substring(attributeBinding.indexOf("/") + 1);
				XPathExpression xpls = new XPathExpression(formNode, recordName);
				List<?> result = xpls.getResult();
				if (result == null || result.size() < row+1) {
					GWT.log("Unexpected rowCount");
					return; 
				}
				
				Object obj = result.get(row);
				if (obj instanceof Element){
					Element el = XmlUtil.getNode((Element) obj, fieldName);
					if (el != null) {
						if ("".equals(value)) {
							el.removeAttribute(getBareBinding());
						} else {
							el.setAttribute(getBareBinding(), value);
						}
					} // else field was empty and removed, so also no attributes
					return;
				} else {
					throw new IllegalStateException("Node: " + attributeBinding + " - " + row + " is geen element! Controleer het veld attribuutBinding van vraag: " + getBinding());
				}
			}
		}
		
		throw new IllegalStateException("Kan een Groep of Repeat niet als Attribuut bewaren!");
	}

	@Deprecated // thou shall not update!
	private void updateDataNode(Document doc, FormDef formDef, String orgFormVarName, String parentBinding){
		if(binding.contains("@"))
			return;

		String name = dataNode.getNodeName();
		if(name.equals(binding)){ //equalsIgnoreCase was bug because our xpath lib is case sensitive
			if(dataType != QuestionDef.QTN_TYPE_GROUP && dataType != QuestionDef.QTN_TYPE_REPEAT)
				return;
			if((dataType == QuestionDef.QTN_TYPE_GROUP || dataType == QuestionDef.QTN_TYPE_REPEAT) && formDef.getBinding().equals(dataNode.getParentNode().getNodeName()))
				return;
		}

		if(binding.contains("/") && name.equals(binding.substring(binding.lastIndexOf("/")+1)) && dataNode.getParentNode().getNodeName().equals(binding.substring(0,binding.indexOf("/"))))
			return;


		String xml = dataNode.toString();
		if(!binding.contains("/")){
			xml = xml.replace(name, binding);
			Element node = XformUtil.getNode(xml);
			node = (Element)controlNode.getOwnerDocument().importNode(node, true);
			Element parent = (Element)dataNode.getParentNode();
			if(formDef.getBinding().equals(parent.getNodeName()))
				parent.replaceChild(node, dataNode);
			else
				parent.replaceChild(node, dataNode);
			//formDef.getDataNode().replaceChild(node, parent);

			dataNode = node;
		}
		else{
			String newName = binding.substring(binding.lastIndexOf("/")+1);
			if(!name.equals(newName)){
				xml = xml.replace(name, newName);
				Element node = XformUtil.getNode(xml);
				node = (Element)controlNode.getOwnerDocument().importNode(node, true);
				Element parent = (Element)dataNode.getParentNode();
				parent.replaceChild(node, dataNode);
				dataNode = node;
			}

			String parentName = binding.substring(0,binding.indexOf("/"));
			if(parentName.trim().length() == 0)
				return;
			
			String parentNodeName = dataNode.getParentNode().getNodeName();
			if(!parentName.equals(parentNodeName)){ //equalsIgnoreCase was bug because our xpath lib is case sensitive
				if(binding.equals(parentName+"/"+parentNodeName+"/"+name))
					return;
				
				if(binding.endsWith("/"+parentNodeName+"/"+name))
					return; //Some bindings have nested paths which expose some bug here.

				Element parentNode = doc.createElement(parentName);
				//parentNode = EpihandyXform.getNode(parentNode.toString());
				Element parent = (Element)dataNode.getParentNode();
				Element node = (Element)dataNode.cloneNode(true);
				parentNode.appendChild(node);
				if(formDef.getBinding().equals(parent.getNodeName()))
					parent.replaceChild(parentNode, dataNode);
				else{
					//if(dataNode.getParentNode().getParentNode() != null)
					try{
						formDef.getDataNode().replaceChild(parentNode, dataNode.getParentNode());
					}
					catch(Exception ex){
						ex.printStackTrace();
						return; //TODO Am not sure what causes this.
					}
				}

				dataNode = node;
			}
		}

		String id = binding;
		if(id.contains("/")) { id = id.substring(id.lastIndexOf('/')+1); }
		id = (parentBinding != null ? parentBinding + "/" : "") + binding;


		//update binding node
		if(bindNode != null && bindNode.getAttribute(XformConstants.ATTRIBUTE_NAME_ID) != null)
			bindNode.setAttribute(XformConstants.ATTRIBUTE_NAME_ID, id);

		//update control node referencing the binding
		if(controlNode != null&& controlNode.getAttribute(XformConstants.ATTRIBUTE_NAME_BIND) != null)
			controlNode.setAttribute(XformConstants.ATTRIBUTE_NAME_BIND, id);
		else if(controlNode != null && controlNode.getAttribute(XformConstants.ATTRIBUTE_NAME_REF) != null){
			/*String ref = controlNode.getAttribute(EpihandyXform.ATTRIBUTE_NAME_REF);
			if(!ref.contains("/"))
				controlNode.setAttribute(EpihandyXform.ATTRIBUTE_NAME_REF,variableName);
			else
				ref = ref.substring(0,ref.indexOf('/')) + variableName;*/
			controlNode.setAttribute(XformConstants.ATTRIBUTE_NAME_REF, id);
		}

		if(dataType == QuestionDef.QTN_TYPE_GROUP || dataType == QuestionDef.QTN_TYPE_REPEAT)
			getGroupQtnsDef().updateDataNodes(dataNode);

		formDef.updateRuleConditionValue(orgFormVarName+"/"+name, formDef.getBinding()+"/"+binding);
	}


	/**
	 * Checks if the xforms ui node name of this question requires to
	 * be changed and does so, if it needs to be changed.
	 */
	private void updateControlNodeName(){
		//TODO How about cases where the prefix is not xf?
		String name = controlNode.getNodeName();
		Element parent = (Element)controlNode.getParentNode();
		String xml = controlNode.toString();
		boolean modified = false;
		
		if((name.contains(XformConstants.NODE_NAME_INPUT_MINUS_PREFIX) || 
				name.contains(XformConstants.NODE_NAME_UPLOAD_MINUS_PREFIX)) &&
				dataType == QuestionDef.QTN_TYPE_LIST_MULTIPLE){
			xml = xml.replace(name, XformConstants.NODE_NAME_SELECT);
			modified = true;
		}
		else if((name.contains(XformConstants.NODE_NAME_INPUT_MINUS_PREFIX) || 
				name.contains(XformConstants.NODE_NAME_UPLOAD_MINUS_PREFIX)) &&
				(dataType == QuestionDef.QTN_TYPE_LIST_EXCLUSIVE || dataType == QuestionDef.QTN_TYPE_LIST_EXCLUSIVE_DYNAMIC)){
			xml = xml.replace(name, XformConstants.NODE_NAME_SELECT1);
			modified = true;
		}
		else if(name.contains(XformConstants.NODE_NAME_SELECT1_MINUS_PREFIX) &&
				dataType == QuestionDef.QTN_TYPE_LIST_MULTIPLE){
			xml = xml.replace(name, XformConstants.NODE_NAME_SELECT);
			modified = true;
		}
		else if((name.contains(XformConstants.NODE_NAME_SELECT_MINUS_PREFIX) &&
				!name.contains(XformConstants.NODE_NAME_SELECT1_MINUS_PREFIX)) && 
				(dataType == QuestionDef.QTN_TYPE_LIST_EXCLUSIVE || dataType == QuestionDef.QTN_TYPE_LIST_EXCLUSIVE_DYNAMIC)){
			xml = xml.replace(name, XformConstants.NODE_NAME_SELECT1);
			modified = true;
		}
		else if((name.contains(XformConstants.NODE_NAME_SELECT1_MINUS_PREFIX) || 
				name.contains(XformConstants.NODE_NAME_SELECT_MINUS_PREFIX)) &&
				!(dataType == QuestionDef.QTN_TYPE_LIST_EXCLUSIVE ||
						dataType == QuestionDef.QTN_TYPE_LIST_MULTIPLE ||
						dataType == QuestionDef.QTN_TYPE_LIST_EXCLUSIVE_DYNAMIC ||
						isMultiMedia(dataType))){
			
			if(firstOptionNode != null){
				firstOptionNode.getParentNode().removeChild(firstOptionNode);
				firstOptionNode = null;
				xml = controlNode.toString();
			}
			
			xml = xml.replace(name, XformConstants.NODE_NAME_INPUT);
			modified = true;
		}
		else if(!(name.contains(XformConstants.NODE_NAME_UPLOAD_MINUS_PREFIX)) &&
				(dataType == QuestionDef.QTN_TYPE_IMAGE || dataType == QuestionDef.QTN_TYPE_AUDIO ||
						dataType == QuestionDef.QTN_TYPE_VIDEO)){
			xml = xml.replace(name, XformConstants.NODE_NAME_UPLOAD);
			modified = true;
		}
		else if(name.contains(XformConstants.NODE_NAME_UPLOAD_MINUS_PREFIX) &&
				!isMultiMedia(dataType)){
			xml = xml.replace(name, XformConstants.NODE_NAME_INPUT);
			modified = true;
		}

		if(modified){
			Element child = XformUtil.getNode(xml);
			child = (Element)controlNode.getOwnerDocument().importNode(child, true);
			parent.replaceChild(child, controlNode);
			controlNode =  child;
			updateControlNodeChildren();
		}
	}

	private boolean isMultiMedia(int dataType){
		return dataType == QuestionDef.QTN_TYPE_IMAGE || dataType == QuestionDef.QTN_TYPE_AUDIO ||
		dataType == QuestionDef.QTN_TYPE_VIDEO;
	}

	/**
	 * Updates xforms ui nodes of the child nodes when the name of xforms ui 
	 * node of this question has changed. Eg when changes from select to select1.
	 */
	private void updateControlNodeChildren(){
		NodeList list = controlNode.getElementsByTagName(XformConstants.NODE_NAME_LABEL_MINUS_PREFIX);
		if(list.getLength() > 0)
			labelNode = (Element)list.item(0);

		list = controlNode.getElementsByTagName(XformConstants.NODE_NAME_HINT_MINUS_PREFIX);
		if(list.getLength() > 0)
			hintNode = (Element)list.item(0);

		if(dataType == QuestionDef.QTN_TYPE_LIST_EXCLUSIVE ||
				dataType == QuestionDef.QTN_TYPE_LIST_MULTIPLE){
			if(options != null){
				updateOptionNodeChildren();
			}
		}
		else if(dataType == QuestionDef.QTN_TYPE_LIST_EXCLUSIVE_DYNAMIC){
			list = controlNode.getElementsByTagName(XformConstants.NODE_NAME_ITEMSET_MINUS_PREFIX);
			if(list.getLength() > 0)
				firstOptionNode = (Element)list.item(0);
		}
	}
	
	private void updateOptionNodeChildren(){
		List optns = (List)options;
		for(int i=0; i<optns.size(); i++){
			OptionDef optionDef = (OptionDef)optns.get(i);
			updateOptionNodeChildren(optionDef);
			if(i == 0)
				firstOptionNode = optionDef.getControlNode();
		}
	}

	/**
	 * Updates xforms ui nodes of an option definition object when the name of
	 * xforms ui node of this question has changed.
	 */
	private void updateOptionNodeChildren(OptionDef optionDef){
		int count = controlNode.getChildNodes().getLength();
		for(int i=0; i<count; i++){
			Node node = controlNode.getChildNodes().item(i);
			if(node.getNodeType() != Node.ELEMENT_NODE)
				continue;
			if(node.getNodeName().equals(XformConstants.NODE_NAME_ITEM)){
				NodeList list = ((Element)node).getElementsByTagName(XformConstants.NODE_NAME_LABEL_MINUS_PREFIX);
				if(list.getLength() == 0)
					continue;

				if(optionDef.getText().equals(XmlUtil.getTextValue((Element)list.item(0)))){
					optionDef.setLabelNode((Element)list.item(0));
					optionDef.setControlNode((Element)node);

					list = ((Element)node).getElementsByTagName(XformConstants.NODE_NAME_VALUE_MINUS_PREFIX);
					if(list.getLength() > 0)
						optionDef.setValueNode((Element)list.item(0));
					return;
				}
			}
		}
	}

	/**
	 * Gets the option with a given display text.
	 * 
	 * @param text the option text.
	 * @return the option definition object.
	 */
	public OptionDef getOptionWithText(String text){
		if(options == null || text == null)
			return null;

		List list = (List)options;
		for(int i=0; i<list.size(); i++){
			OptionDef optionDef = (OptionDef)list.get(i);
			if(optionDef.getText().equals(text))
				return optionDef;
		}
		return null;
	}

	/**
	 * Gets the option with a given id.
	 * 
	 * @param id the option id
	 * @return the option definition object.
	 */
	public OptionDef getOption(int id){
		if(options == null)
			return null;

		List list = (List)options;
		for(int i=0; i<list.size(); i++){
			OptionDef optionDef = (OptionDef)list.get(i);
			if(optionDef.getId() == id)
				return optionDef;
		}
		return null;
	}

	/**
	 * Gets the option with a given variable name or binding.
	 * 
	 * @param value the variable name or binding.
	 * @return the option definition object.
	 */
	public OptionDef getOptionWithValue(String value){
		if(options == null || value == null)
			return null;

		List list = (List)options;
		for(int i=0; i<list.size(); i++){
			OptionDef optionDef = (OptionDef)list.get(i);
			if(optionDef.getBinding().equals(value))
				return optionDef;
		}
		return null;
	}

	/**
	 * Updates this questionDef (as the main) with the parameter one (which is the old)
	 * 
	 * @param questionDef the old question before the refresh
	 */
	public void refresh(QuestionDef questionDef){
		setText(questionDef.getText());
		setHelpText(questionDef.getHelpText());
		setDefaultValue(questionDef.getDefaultValue());

		int prevDataType = dataType;

		//The old data type can only overwrite the new one if its not text (The new one is this question)
		if(questionDef.getDataType() != QuestionDef.QTN_TYPE_TEXT)
			setDataType(questionDef.getDataType());

		setEnabled(questionDef.isEnabled());
		setRequired(questionDef.isRequired());
		setLocked(questionDef.isLocked());
		setVisible(questionDef.isVisible());

		if((dataType == QuestionDef.QTN_TYPE_LIST_EXCLUSIVE || dataType == QuestionDef.QTN_TYPE_LIST_MULTIPLE) &&
				(questionDef.getDataType() == QuestionDef.QTN_TYPE_LIST_EXCLUSIVE || questionDef.getDataType() == QuestionDef.QTN_TYPE_LIST_MULTIPLE) ){
			refreshOptions(questionDef);

			if(!(prevDataType == QuestionDef.QTN_TYPE_LIST_EXCLUSIVE || prevDataType == QuestionDef.QTN_TYPE_LIST_MULTIPLE)){
				//A single or multiple select may have had options added on the client and so we do wanna
				//lose them for instance when the server has text data type.
				//TODO We may need to assign new option ids
				for(int index = 0; index < questionDef.getOptionCount(); index++)
					addOption(new OptionDef(questionDef.getOptionAt(index),this));
			}

		}
		else if((dataType == QuestionDef.QTN_TYPE_GROUP && questionDef.getDataType() == QuestionDef.QTN_TYPE_GROUP) ||
				(dataType == QuestionDef.QTN_TYPE_REPEAT && questionDef.getDataType() == QuestionDef.QTN_TYPE_REPEAT)) {
			getGroupQtnsDef().refresh(questionDef.getGroupQtnsDef()); //TODO Finish this
		}
	}

	public int getOptionIndex(String varName){
		if(options == null)
			return -1;

		for(int i=0; i<getOptions().size(); i++){
			OptionDef def = (OptionDef)getOptions().get(i);
			if(def.getBinding().equals(varName))
				return i;
		}

		return -1;
	}

	/**
	 * Updates this questionDef's options (as the main) with the parameter one (which is the old)
	 * 
	 * @param questionDef the old question before the refresh
	 */
	private void refreshOptions(QuestionDef questionDef){
		List options2 = questionDef.getOptions();
		if(options == null || options2 == null)
			return;

		if(getText().equals("MODE OF TRANSPORT OF ARRIVAL AT HOSPITAL") ||
				getText().equals("During the most recent hospital visit for this illness/clinician, what means of transport did [NAME] use to arrive at the hospital?")){
			options.toString();
		}
		
		Vector<OptionDef> orderedOptns = new Vector<OptionDef>();
		Vector<OptionDef> missingOptns = new Vector<OptionDef>();
		
		for(int index = 0; index < options2.size(); index++){
			OptionDef optn = (OptionDef)options2.get(index);
			OptionDef optionDef = this.getOptionWithValue(optn.getBinding());
			if(optionDef == null){
				missingOptns.add(optn);
				continue;
			}
			
			optionDef.setText(optn.getText());

			orderedOptns.add(optionDef); //add the option in the order it was before the refresh.
			
			
			//Preserve the previous option ordering even in the xforms document nodes.
			int newIndex = ((List)options).indexOf(optionDef);
			
			int tempIndex = index - missingOptns.size();
			if(newIndex < ((List)options).size()){
				if(tempIndex != newIndex){
					if(newIndex < tempIndex){
						while(newIndex < tempIndex){
							moveOptionDown(optionDef);
							newIndex++;
						}
					}
					else{
						while(newIndex > tempIndex){
							moveOptionUp(optionDef);
							newIndex--;
						}
					}
				}
			}

			/*int index1 = this.getOptionIndex(optn.getVariableName());
			if(index != index1 && index1 != -1 && index < this.getOptionCount() - 1){
				((List)this.getOptions()).remove(optionDef);
				((List)this.getOptions()).set(index, optionDef);
			}*/
		}

		int oldCount = questionDef.getOptionCount();
		
		//now add the new options which have just been added by refresh.
		int count = getOptionCount();
		for(int index = 0; index < count; index++){
			OptionDef optionDef = getOptionAt(index);
			if(questionDef.getOptionWithValue(optionDef.getBinding()) == null){
				
				//TODO Make sure this is not buggy.
				//If before refresh number of options is the same as the new number,
				//then we preserve the old option text and binding by replacing new
				//ones with the old values.
				if(oldCount == count){
					//Commented out because its really buggy. When provider id changes, it adds duplicate and with same old id.
					/*OptionDef optnDef = questionDef.getOptionAt(index);
					optionDef.setBinding(optnDef.getBinding());
					optionDef.setText(optnDef.getText());*/
				}
				
				orderedOptns.add(optionDef);
			}
		}
		
		//Now add the missing options. Possibly they were added by user and not existing in the
		//original server side form.
		for(int index = 0; index < missingOptns.size(); index++){
			OptionDef optnDef = missingOptns.get(index);
			orderedOptns.add(new OptionDef((orderedOptns.size() + index + 1), optnDef.getText(), optnDef.getBinding(), this));
		}

		options = orderedOptns;
	}

	/**
	 * Gets the number of options for this questions.
	 * 
	 * @return the number of options.
	 */
	public int getOptionCount(){
		if(options == null || !(options instanceof List))
			return 0;
		return ((List)options).size();
	}

	/**
	 * Gets the option at a given position (zero based).
	 * 
	 * @param index the position.
	 * @return the option definition object.
	 */
	public OptionDef getOptionAt(int index){
		return (OptionDef)((List)options).get(index);
	}

	/**
	 * Clears the list of option for a question.
	 */
	public void clearOptions(){
		if(options != null && options instanceof List)
			((List)options).clear();
	}

	public void moveOptionNodesUp(OptionDef optionDef, OptionDef refOptionDef) {
		Element controlNode = optionDef.getControlNode();
		Element parentNode = controlNode != null ? (Element)controlNode.getParentNode() : null;

		if(controlNode != null)
			parentNode.removeChild(controlNode);

		if(refOptionDef.getControlNode() != null)
			parentNode.insertBefore(controlNode, refOptionDef.getControlNode());
	}

	/**
	 * Sets the list of options for a question.
	 * 
	 * @param optionList the option list.
	 */
	public void setOptionList(List<OptionDef> optionList){
		options = optionList;

		for(int index = 0; index < changeListeners.size(); index++)
			changeListeners.get(index).onOptionsChanged(this,optionList);
	}


	/**
	 * Updates the xforms instance data nodes referenced by this question and its children.
	 * 
	 * @param parentDataNode the parent data node for this question.
	 */
	public void updateDataNodes(Element parentDataNode){
		if(dataNode == null)
			return;

		String xpath = /*"/"+formDef.getVariableName()+"/"+*/dataNode.getNodeName();
		XPathExpression xpls = new XPathExpression(parentDataNode, xpath);
		Vector result = xpls.getResult();
		if(result == null || result.size() == 0)
			return;

		dataNode = (Element)result.elementAt(0);

		if(dataType == QuestionDef.QTN_TYPE_GROUP || dataType == QuestionDef.QTN_TYPE_REPEAT)
			getGroupQtnsDef().updateDataNodes(dataNode);
	}

	/**
	 * Builds the locale xpath xpressions and their text  values for this question.
	 * 
	 * @param parentXpath the parent xpath expression we are building onto.
	 * @param doc the locale document that we are building.
	 * @param parentXformNode the parent xforms node for this question.
	 * @param parentLangNode the parent language node we are building onto.
	 */
	public void buildLanguageNodes(String parentXpath, com.google.gwt.xml.client.Document doc, Element parentXformNode, Element parentLangNode, Map<String, String> changedXpaths){
		if(controlNode == null)
			return;

		String xpath = parentXpath + FormUtil.getNodePath(controlNode,parentXformNode);

		if(dataType == QuestionDef.QTN_TYPE_GROUP || dataType == QuestionDef.QTN_TYPE_REPEAT) {
			Element parent = (Element) controlNode.getParentNode();
			xpath = parentXpath + FormUtil.getNodePath(parent,parentXformNode);

			String id = parent.getAttribute(XformConstants.ATTRIBUTE_NAME_ID);
			if(id != null && id.trim().length() > 0)
				xpath += "[@" + XformConstants.ATTRIBUTE_NAME_ID + "='" + id + "']";
		}
		else{
			String id = controlNode.getAttribute(XformConstants.ATTRIBUTE_NAME_BIND);
			if(id != null && id.trim().length() > 0)
				xpath += "[@" + XformConstants.ATTRIBUTE_NAME_BIND + "='" + id + "']";
			else{
				id = controlNode.getAttribute(XformConstants.ATTRIBUTE_NAME_REF);
				if(id != null && id.trim().length() > 0)
					xpath += "[@" + XformConstants.ATTRIBUTE_NAME_REF + "='" + id + "']";
			}
		}

		if(labelNode != null){
			Element node = doc.createElement(XformConstants.NODE_NAME_TEXT);
			String newXpath = xpath + "/" + FormUtil.getNodeName(labelNode);
			node.setAttribute(XformConstants.ATTRIBUTE_NAME_XPATH, newXpath);
			
			//Store the old xpath expression for localization processing which identifies us by the previous value.
			if(this.xpathExpressionLabel != null && !newXpath.equalsIgnoreCase(this.xpathExpressionLabel)){
				node.setAttribute(XformConstants.ATTRIBUTE_NAME_PREV_XPATH, this.xpathExpressionLabel);
				changedXpaths.put(this.xpathExpressionLabel, newXpath);
			}
			this.xpathExpressionLabel = newXpath;
			
			node.setAttribute(XformConstants.ATTRIBUTE_NAME_VALUE, text);
			parentLangNode.appendChild(node);
		}

		if(hintNode != null && helpText != null){
			Element node = doc.createElement(XformConstants.NODE_NAME_TEXT);
			String newXpath = xpath + "/" + FormUtil.getNodeName(hintNode);
			node.setAttribute(XformConstants.ATTRIBUTE_NAME_XPATH, newXpath);
			
			//Store the old xpath expression for localization processing which identifies us by the previous value.
			if(this.xpathExpressionHint != null && !newXpath.equalsIgnoreCase(this.xpathExpressionHint)){
				node.setAttribute(XformConstants.ATTRIBUTE_NAME_PREV_XPATH, this.xpathExpressionHint);
				changedXpaths.put(this.xpathExpressionHint, newXpath);
			}
			this.xpathExpressionHint = newXpath;
			
			node.setAttribute(XformConstants.ATTRIBUTE_NAME_VALUE, helpText);
			parentLangNode.appendChild(node);
		}

		if(dataType == QuestionDef.QTN_TYPE_GROUP || dataType == QuestionDef.QTN_TYPE_REPEAT)
			getGroupQtnsDef().buildLanguageNodes(parentXpath,doc,parentXformNode,parentLangNode, changedXpaths);

		if(dataType == QuestionDef.QTN_TYPE_LIST_EXCLUSIVE || dataType == QuestionDef.QTN_TYPE_LIST_MULTIPLE) {
			if(options != null){
				List optionsList = (List)options;
				for(int index = 0; index < optionsList.size(); index++)
					((OptionDef)optionsList.get(index)).buildLanguageNodes(xpath, doc, parentLangNode, changedXpaths);
			}
		}
	}

	/**
	 * Gets the form to which this question belongs.
	 * 
	 * @return the form.
	 */
	public FormDef getParentFormDef(){
		return getParentFormDef(this);
	}

	private FormDef getParentFormDef(QuestionDef questionDef){
		Object parent = questionDef.getParent();
		if(parent instanceof PageDef)
			return ((PageDef)parent).getParent();
		else if(parent instanceof QuestionDef)
			return getParentFormDef((QuestionDef)parent);
		return null;
	}
	
//	public boolean isRepeatGroupChild() {
//		Object parent = getParent();
//		if (parent instanceof QuestionDef) {
//			return ((QuestionDef) parent).getDataType() == QuestionDef.QTN_TYPE_REPEAT;	
//		}
//		return false;
//	}

	public String getDisplayText(){
		String text = getText();
		if(text == null)
			return null;
		
		String displayText = text;
		do{
			displayText = getDisplayText(text);
			if(displayText.equals(text))
				break;
			
			text = displayText;
		}while(true);
		
		return displayText;
	}
	
	public String getDisplayText(String displayText){
		int pos1 = displayText.indexOf("${");
		int pos2 = displayText.indexOf("}$");
		if(pos1 > -1 && pos2 > -1 && (pos2 > pos1))
			displayText = displayText.replace(displayText.substring(pos1,pos2+2),"");
		return displayText;
	}
	
	private void insertBeforeLastChild(Element parent, Element node){
		NodeList nodes = parent.getChildNodes();
		for(int index = nodes.getLength() - 1; index >= 0; index--){
			Node child = nodes.item(index);
			if(child.getNodeType() == Node.ELEMENT_NODE){
				parent.insertBefore(node, child);
				return;
			}
		}
	}
	
	public boolean isAsAttribute() {
		return asAttribute  && QuestionDef.QTN_TYPE_GROUP != dataType && QuestionDef.QTN_TYPE_REPEAT != dataType;
	}

	public void setAsAttribute(boolean asAttribute) {
		if (this.asAttribute != asAttribute) {
			this.asAttribute = asAttribute;
		
			for(int index = 0; index < changeListeners.size(); index++) {
				changeListeners.get(index).onAttributeStateChanged(this);
			}
		}
	}

	public String getAttributeBinding() {
		return attributeBinding;
	}
	
	public void setAttributeBinding(String attributeBinding) {
		if (this.attributeBinding != attributeBinding) {
			this.attributeBinding = attributeBinding;
		
			for(int index = 0; index < changeListeners.size(); index++) {
				changeListeners.get(index).onAttributeStateChanged(this);
			}
		}
	}
	
	public boolean isAsCdata() {
		return asCdata;
	}
	
	public void setAsCdata(boolean asCdata) {
		this.asCdata = asCdata;
	}
	
	public boolean isRepeatChild() {
		return repeatChild;
	}

	public void setRepeatChild(boolean repeatChild) {
		this.repeatChild = repeatChild;
	}

	public void setRepeatBinding(String repeatBinding) {
		this.repeatBinding = repeatBinding;
	}
	
	public String getRepeatBinding() {
		return repeatBinding;
	}
	
}


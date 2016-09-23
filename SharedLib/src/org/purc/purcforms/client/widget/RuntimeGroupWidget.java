package org.purc.purcforms.client.widget;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import org.purc.purcforms.client.controller.OpenFileDialogEventListener;
import org.purc.purcforms.client.controller.QuestionChangeListener;
import org.purc.purcforms.client.locale.LocaleText;
import org.purc.purcforms.client.model.Condition;
import org.purc.purcforms.client.model.FormDef;
import org.purc.purcforms.client.model.GroupQtnsDef;
import org.purc.purcforms.client.model.ModelConstants;
import org.purc.purcforms.client.model.OptionDef;
import org.purc.purcforms.client.model.QuestionDef;
import org.purc.purcforms.client.model.SkipRule;
import org.purc.purcforms.client.model.ValidationRule;
import org.purc.purcforms.client.util.FormUtil;
import org.purc.purcforms.client.util.StringUtil;
import org.purc.purcforms.client.util.TableUtil;
import org.purc.purcforms.client.view.FormRunnerView;
import org.purc.purcforms.client.view.FormRunnerView.Images;
import org.purc.purcforms.client.view.OpenFileDialog;
import org.purc.purcforms.client.xforms.XformParser;
import org.purc.purcforms.client.xforms.XmlUtil;
import org.purc.purcforms.client.xpath.XPathExpression;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.http.client.URL;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.Focusable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasEnabled;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.xml.client.Element;
import com.google.gwt.xml.client.Node;
import com.google.gwt.xml.client.NodeList;

/**
 * 
 * @author daniel
 *
 */
public class RuntimeGroupWidget extends Composite implements OpenFileDialogEventListener, QuestionChangeListener, Focusable, HasEnabled {

	private final Images images;
	private GroupQtnsDef groupQtnsDef;
	private HashMap<String,RuntimeWidgetWrapper> parentBindingWidgetMap = new HashMap<String,RuntimeWidgetWrapper>();
	private EditListener editListener;
	private WidgetListener widgetListener;
	private EnabledChangeListener enabledListener;
	private FlexTable table;
	private List<RuntimeWidgetWrapper> buttons = new ArrayList<RuntimeWidgetWrapper>();
	private List<RuntimeWidgetWrapper> widgets = new ArrayList<RuntimeWidgetWrapper>();
	private VerticalPanel verticalPanel = new VerticalPanel();
	private List<Element> dataNodes = new ArrayList<Element>();
	private AbsolutePanel selectedPanel = new AbsolutePanel();
	private boolean isRepeated = false;
	private Image image;
	private HTML html;
	private FormDef formDef;
	private Button btnAdd;
	private RuntimeWidgetWrapper firstInvalidWidget;
	private boolean enabled = false;

	protected HashMap<QuestionDef,List<Label>> labelMap = new HashMap<QuestionDef,List<Label>>();
	protected HashMap<Label,String> labelText = new HashMap<Label,String>();
	protected HashMap<Label,String> labelReplaceText = new HashMap<Label,String>();

	protected HashMap<QuestionDef,List<CheckBox>> checkBoxGroupMap = new HashMap<QuestionDef,List<CheckBox>>();

	/**
	 * A map of filtered single select dynamic questions and their corresponding 
	 * non label widgets. Only questions of single select dynamic which have the
	 * widget filter property set are put in this list
	 */
	protected HashMap<QuestionDef,RuntimeWidgetWrapper> filtDynOptWidgetMap = new HashMap<QuestionDef,RuntimeWidgetWrapper>();

	protected HashMap<PushButton, List<FormDef>> repeatRowFormMap = new HashMap<PushButton, List<FormDef>>();


	public RuntimeGroupWidget(Images images, FormDef formDef, GroupQtnsDef groupQtnsDef, EditListener editListener, WidgetListener widgetListener, boolean isRepeated, EnabledChangeListener enabledListener){
		this.images = images;
		this.formDef = formDef;
		this.groupQtnsDef = groupQtnsDef;
		this.editListener = editListener;
		this.widgetListener = widgetListener;
		this.isRepeated = isRepeated;
		this.enabledListener = enabledListener;

		if (isRepeated) {
			table = new PurcFlexTable();
			table.addStyleName("purcforms-repeat-records-table");
			verticalPanel.add(table);
			initWidget(verticalPanel);
		} else {
			initWidget(selectedPanel);
		}

		//setupEventListeners();

		//table.setStyleName("cw-FlexTable");
		this.addStyleName("purcforms-repeat-table");
	}

	// TODO The code below needs great refactoring together with FormRunnerView
	private RuntimeWidgetWrapper getOrCreateParentBindingWrapper(Widget widget, Element node, String parentBinding) {
		RuntimeWidgetWrapper parentWrapper = parentBindingWidgetMap.get(parentBinding);
		if(parentWrapper == null){
			QuestionDef qtn = null;
			if(groupQtnsDef != null)
				qtn = groupQtnsDef.getQuestion(parentBinding);
			else
				qtn = formDef.getQuestion(parentBinding);

			if(qtn != null){
				parentWrapper = new RuntimeWidgetWrapper(widget, images.error(),editListener, widgetListener, enabledListener);
				parentWrapper.setQuestionDef(qtn,true);
				parentBindingWidgetMap.put(parentBinding, parentWrapper);
				qtn.addChangeListener(this);
				List<CheckBox> list = new ArrayList<CheckBox>();
				list.add((CheckBox)widget);
				checkBoxGroupMap.put(qtn, list);
			}
		}
		else
			checkBoxGroupMap.get(parentWrapper.getQuestionDef()).add((CheckBox)widget);

		return parentWrapper;
	}

	public void loadWidgets(FormDef formDef,NodeList nodes, List<RuntimeWidgetWrapper> externalSourceWidgets,
			HashMap<QuestionDef,List<QuestionDef>> calcQtnMappings, final HashMap<QuestionDef,List<RuntimeWidgetWrapper>> calcWidgetMap,
			HashMap<QuestionDef,RuntimeWidgetWrapper> filtDynOptWidgetMap){

		List<RuntimeWidgetWrapper> widgetMap = new ArrayList<RuntimeWidgetWrapper>();
		List<RuntimeWidgetWrapper> labelMap = new ArrayList<RuntimeWidgetWrapper>();
		
		for(int i=0; i<nodes.getLength(); i++){
			if(nodes.item(i).getNodeType() != Node.ELEMENT_NODE)
				continue;
			try{
				Element node = (Element)nodes.item(i);
				loadWidget(formDef,node,widgetMap,externalSourceWidgets,calcQtnMappings, calcWidgetMap, filtDynOptWidgetMap, labelMap);
			}
			catch(Exception ex){
				ex.printStackTrace();
			}
		}
		
		//We are adding widgets to the panel according to the position.
		FormUtil.sortWidgetsByPosition(widgetMap);
		for (RuntimeWidgetWrapper rww : widgetMap) {
			addWidget(rww);
		}
		
		if(isRepeated) {
			TableUtil.setColumnWidths(table, widgets);
			TableUtil.setRowCellWidths(table, 0);

			Scheduler.get().scheduleDeferred(new Command() {
				@Override
				public void execute() {
					for (RuntimeWidgetWrapper widget : widgets) {
						if(widget.getQuestionDef() != null) {
							widget.getQuestionDef().setRepeatChild(true);
							widget.getQuestionDef().setRepeatBinding(widget.getQuestionDef().getParentBinding());
							if (widget.getQuestionDef().isLocked()) {
								widget.hideErrorLabel();
							}
						}
					}
					
					for (RuntimeWidgetWrapper widget : widgets) {
						if(!(widget.getQuestionDef() == null || widget.getQuestionDef().getDataNode() == null)){
							Element repeatDataNode = getParentNode(widget.getQuestionDef().getDataNode(),(widget.getWrappedWidget() instanceof CheckBox) ? widget.getParentBinding() : widget.getBinding(), ((QuestionDef)widget.getQuestionDef().getParent()).getBinding());
							Element dataNodeParent = (Element)repeatDataNode.getParentNode();
	
							RuntimeWidgetWrapper wrapper = (RuntimeWidgetWrapper)getParent().getParent();
							int y = getHeightInt();
	
							if (groupQtnsDef.getDataToLoad() != null) {
								NodeList newNodeList = groupQtnsDef.getDataToLoad().getElementsByTagName(repeatDataNode.getNodeName());
								NodeList origNodeList = dataNodeParent.getElementsByTagName(repeatDataNode.getNodeName());
								Node originalNode = origNodeList.item(0);
								Node nextSibling = originalNode.getNextSibling();
								
								for(int index = 1; index < newNodeList.getLength(); index++) {	// -- First row already exists!!
									Element row = (Element)newNodeList.item(index);
									Node newNode = originalNode.cloneNode(true);
									XmlUtil.merge(row, (Element) newNode);
									if (nextSibling != null) {
										dataNodeParent.insertBefore(newNode, nextSibling);
									} else {
										dataNodeParent.appendChild(newNode);
									}
								}
								
								// must merge first row as well
								XmlUtil.merge((Element)newNodeList.item(0), (Element) originalNode);
								
								// now add rows to the gui for the not yet existing rows
								origNodeList = dataNodeParent.getElementsByTagName(repeatDataNode.getNodeName()); // refresh list
								for(int index = 1; index < origNodeList.getLength(); index++) { // -- First row already exists!!
									Element row = (Element)origNodeList.item(index);
									addNewRow(row, calcWidgetMap);
								}
							}
							
							editListener.onRowAdded(wrapper,getHeightInt()-y);
							return;
						}
					}
					Window.alert("Groep of Lijst bevat geen volwaardige vraag (enkel vragen als attribuut, er dient minstens één vraag te zijn die geen attribuut is).");
				}
			});	
		}

		//Now add the button and label widgets, if any.
		if(isRepeated) {
			HorizontalPanel panel = new HorizontalPanel();
			panel.setSpacing(10);
			for(int index = 0; index < buttons.size(); index++) {
				panel.add(buttons.get(index));
			}
			verticalPanel.add(panel);

			if (!FormUtil.isReadOnlyMode()) {
				addDeleteButton(table.getRowCount() - 1);
			}

			FormUtil.maximizeWidget(panel);
		}
		else{
			for(int index = 0; index < buttons.size(); index++){
				RuntimeWidgetWrapper widget = buttons.get(index);
				selectedPanel.add(widget);
				FormUtil.setWidgetPosition(widget,widget.getLeft(),widget.getTop());
			}
		}
	}

	private PushButton addDeleteButton(int row){
		PushButton btn = new PushButton(LocaleText.get("deleteItem"));
		btn.addClickHandler(new ClickHandler(){
			public void onClick(ClickEvent event){
				removeRow((Widget)event.getSource());
			}
		});
		table.setWidget(row, widgets.size(), btn);

		return btn;
	}

	private void removeRow(Widget sender) {
		if(table.getRowCount() == 1){//There should be at least one row{
			clearValue();
			RuntimeWidgetWrapper wrapper = (RuntimeWidgetWrapper)getParent().getParent();
			editListener.onValueChanged(wrapper);
			return;
		}

		int rowStartIndex = 1;
		if(((RuntimeWidgetWrapper)table.getWidget(0, 0)).getWrappedWidget() instanceof Label)
			rowStartIndex = 2;
		
		for(int row = rowStartIndex; row < table.getRowCount(); row++){
			if(sender == table.getWidget(row, widgets.size())){

				RuntimeWidgetWrapper wrapper = (RuntimeWidgetWrapper)getParent().getParent();
				int y = getHeightInt();

				table.removeRow(row);
				Element node = dataNodes.get(row-rowStartIndex);
				node.getParentNode().removeChild(node);
				dataNodes.remove(node);
				if(btnAdd != null)
					btnAdd.setEnabled(true);

				editListener.onRowRemoved(wrapper, y-getHeightInt());

				RuntimeWidgetWrapper parent = (RuntimeWidgetWrapper)getParent().getParent();
				ValidationRule validationRule = parent.getValidationRule();
				if(validationRule != null){
					parent.getQuestionDef().setAnswer(table.getRowCount()+"");
					
					//Add error message
					if(getParent().getParent() instanceof RuntimeWidgetWrapper)
						((RuntimeWidgetWrapper)getParent().getParent()).isValid(true);
				}

				List<FormDef> forms = repeatRowFormMap.get(sender);
				if(forms != null){
					for(FormDef formDef : forms)
						((FormRunnerView)editListener).removeRepeatQtnFormDef(formDef);
				}
			}
		}
		
		FormUtil.onResize();
	}

	private int loadWidget(FormDef formDef, Element node, List<RuntimeWidgetWrapper> widgets, List<RuntimeWidgetWrapper> externalSourceWidgets,
			HashMap<QuestionDef,List<QuestionDef>> calcQtnMappings, final HashMap<QuestionDef,List<RuntimeWidgetWrapper>> calcWidgetMap,
			HashMap<QuestionDef,RuntimeWidgetWrapper> filtDynOptWidgetMap, List<RuntimeWidgetWrapper> labelWidgetMap){

		RuntimeWidgetWrapper parentWrapper = null;

		String s = node.getAttribute(WidgetEx.WIDGET_PROPERTY_WIDGETTYPE);
		int tabIndex = (node.getAttribute(WidgetEx.WIDGET_PROPERTY_TABINDEX) != null ? Integer.parseInt(node.getAttribute(WidgetEx.WIDGET_PROPERTY_TABINDEX)) : 0);

		QuestionDef questionDef = null;
		String binding = node.getAttribute(WidgetEx.WIDGET_PROPERTY_BINDING);
		String parentBinding = node.getAttribute(WidgetEx.WIDGET_PROPERTY_PARENTBINDING);

		if(isRepeated || groupQtnsDef.getQtnDef().getDataType() == QuestionDef.QTN_TYPE_GROUP){
			if(binding != null && binding.trim().length() > 0 && groupQtnsDef != null){
				questionDef = groupQtnsDef.getQuestion(binding);
			}
		}
		else{
			if(binding != null && binding.trim().length() > 0){
				questionDef = formDef.getQuestion(binding);
			}
		}

		RuntimeWidgetWrapper wrapper = null;
		boolean wrapperCreated = false;
		Widget widget = null;
		if(s.equalsIgnoreCase(WidgetEx.WIDGET_TYPE_RADIOBUTTON)){
			widget = new RadioButtonWidget(parentBinding,node.getAttribute(WidgetEx.WIDGET_PROPERTY_TEXT));

			if(parentBindingWidgetMap.get(parentBinding) == null)
				wrapperCreated = true;

			parentWrapper = getOrCreateParentBindingWrapper(widget,node,parentBinding);
			((RadioButton)widget).setTabIndex(tabIndex);

			if(wrapperCreated){
				wrapper = parentWrapper;
				questionDef = formDef.getQuestion(parentBinding);
			}
		}
		else if(s.equalsIgnoreCase(WidgetEx.WIDGET_TYPE_CHECKBOX)){
			widget = new CheckBoxWidget(node.getAttribute(WidgetEx.WIDGET_PROPERTY_TEXT));
			if(parentBindingWidgetMap.get(parentBinding) == null)
				wrapperCreated = true;

			parentWrapper = getOrCreateParentBindingWrapper(widget,node,parentBinding);
			if(wrapperCreated) {
				wrapper = parentWrapper;
				questionDef = formDef.getQuestion(parentBinding);
			}
		}
		else if(s.equalsIgnoreCase(WidgetEx.WIDGET_TYPE_BUTTON)){
			widget = new Button(node.getAttribute(WidgetEx.WIDGET_PROPERTY_TEXT));
			widget.setVisible(!FormUtil.isReadOnlyMode());
		}
		else if(s.equalsIgnoreCase(WidgetEx.WIDGET_TYPE_LISTBOX)){
			widget = new ListBoxWidget(false);
		}
		else if(s.equalsIgnoreCase(WidgetEx.WIDGET_TYPE_TEXTAREA)){
			widget = new RichTextAreaWidget();
		}
		else if(s.equalsIgnoreCase(WidgetEx.WIDGET_TYPE_DATEPICKER)){
			widget = new DatePickerWidget();
		}
		else if(s.equalsIgnoreCase(WidgetEx.WIDGET_TYPE_DATETIME)){
			widget = new DateTimeWidget();
		}
		else if(s.equalsIgnoreCase(WidgetEx.WIDGET_TYPE_TIME)){
			widget = new TimeWidget();
		}
		else if(s.equalsIgnoreCase(WidgetEx.WIDGET_TYPE_TEXTBOX)){
			widget = new TextBoxWidget();
			if(questionDef != null && (questionDef.getDataType() == QuestionDef.QTN_TYPE_NUMERIC || questionDef.getDataType() == QuestionDef.QTN_TYPE_DECIMAL))
				FormUtil.allowNumericOnly((TextBox)widget,questionDef.getDataType() == QuestionDef.QTN_TYPE_DECIMAL);
		}
		else if(s.equalsIgnoreCase(WidgetEx.WIDGET_TYPE_LABEL)){
			String text = node.getAttribute(WidgetEx.WIDGET_PROPERTY_TEXT);
			if(text == null) text = "";
			if (text.contains("[")) {
				widget = new HTML(StringUtil.parseBBCode(text));
			} else {
				widget = new Label(text);
			}

			int pos1 = text.indexOf("${");
			int pos2 = text.indexOf("}$");
			if(pos1 > -1 && pos2 > -1 && (pos2 > pos1)){
				String varname = text.substring(pos1+2,pos2);
				labelText.put((Label)widget, text);
				labelReplaceText.put((Label)widget, "${"+varname+"}$");

				((Label)widget).setText(text.replace("${"+varname+"}$", ""));
				if(varname.startsWith("/"+ formDef.getBinding()+"/"))
					varname = varname.substring(("/"+ formDef.getBinding()+"/").length(),varname.length());

				QuestionDef qtnDef = formDef.getQuestion(varname);
				List<Label> labels = labelMap.get(qtnDef);
				if(labels == null){
					labels = new ArrayList<Label>();
					labelMap.put(qtnDef, labels);
				}
				labels.add((Label)widget);
			}
		}
		else if(s.equalsIgnoreCase(WidgetEx.WIDGET_TYPE_IMAGE)){
			widget = new Image();
			String xpath = binding;
			if(!xpath.startsWith(formDef.getBinding()))
				xpath = "/" + formDef.getBinding() + "/" + binding;
			((Image)widget).setUrl(URL.encode(FormUtil.getMultimediaUrl()+"?formId="+formDef.getId()+"&xpath="+xpath+"&time="+ new java.util.Date().getTime()));
		}
		else if(s.equalsIgnoreCase(WidgetEx.WIDGET_TYPE_LOGO)){
			widget = new Image();
			((Image)widget).setUrl(URL.encode(FormUtil.getHostPageBaseURL() + node.getAttribute(WidgetEx.WIDGET_PROPERTY_EXTERNALSOURCE)));
		}
		else if(s.equalsIgnoreCase(WidgetEx.WIDGET_TYPE_VIDEO_AUDIO) && questionDef != null){
			widget = new HTML();
			String xpath = binding;
			if(!xpath.startsWith(formDef.getBinding()))
				xpath = "/" + formDef.getBinding() + "/" + binding;

			String extension = "";//.3gp ".mpeg";
			String contentType = "&contentType=video/3gpp";
			if(questionDef.getDataType() == QuestionDef.QTN_TYPE_AUDIO)
				contentType = "&contentType=audio/3gpp"; //"&contentType=audio/x-wav";
			//extension = ".wav";

			contentType += "&name="+questionDef.getBinding()+".3gp";

			((HTML)widget).setHTML("<a href=" + URL.encode(FormUtil.getMultimediaUrl()+extension + "?formId="+formDef.getId()+"&xpath="+xpath+contentType+"&time="+ new java.util.Date().getTime()) + ">"+node.getAttribute(WidgetEx.WIDGET_PROPERTY_TEXT)+"</a>");

			String answer = questionDef.getAnswer();
			if(answer == null || answer.trim().length() == 0 )
				((HTML)widget).setVisible(false);
		}
		else if(s.equalsIgnoreCase(WidgetEx.WIDGET_TYPE_GROUPBOX)||s.equalsIgnoreCase(WidgetEx.WIDGET_TYPE_GROUPSECTION)||s.equalsIgnoreCase(WidgetEx.WIDGET_TYPE_REPEATSECTION)){
			GroupQtnsDef groupQtnsDef = null;
			if(questionDef != null)
				groupQtnsDef = questionDef.getGroupQtnsDef();

			boolean repeated = false;
			String value = node.getAttribute(WidgetEx.WIDGET_PROPERTY_REPEATED);
			if(value != null && value.trim().length() > 0)
				repeated = (value.equals(WidgetEx.REPEATED_TRUE_VALUE));

			widget = new RuntimeGroupWidget(images, formDef, groupQtnsDef, editListener, widgetListener, repeated, enabledListener);
			((RuntimeGroupWidget)widget).loadWidgets(formDef,node.getChildNodes(),externalSourceWidgets,calcQtnMappings,calcWidgetMap,filtDynOptWidgetMap);
			/*getLabelMap(((RuntimeGroupWidget)widget).getLabelMap());
			getLabelText(((RuntimeGroupWidget)widget).getLabelText());
			getLabelReplaceText(((RuntimeGroupWidget)widget).getLabelReplaceText());
			getCheckBoxGroupMap(((RuntimeGroupWidget)widget).getCheckBoxGroupMap());*/
		}

		/*else if(s.equalsIgnoreCase(WidgetEx.WIDGET_TYPE_REPEATSECTION)){
			//Not dealing with nested repeats
			//widget = new RunTimeGroupWidget();
		}*/
		else
			return tabIndex;

		if(!wrapperCreated){
			wrapper = new RuntimeWidgetWrapper(widget, images.error(), editListener, widgetListener, enabledListener);

			if(parentWrapper != null){ //Check box or radio button
				if(!parentWrapper.getQuestionDef().isVisible())
					wrapper.setVisible(false);
				if(!parentWrapper.getQuestionDef().isEnabled() && !FormUtil.isReadOnlyMode())
					wrapper.setEnabled(false);
				if(parentWrapper.getQuestionDef().isLocked() && !FormUtil.isReadOnlyMode())
					wrapper.setLocked(true);
			}
		}

		boolean loadWidget = true;

//		String value = node.getAttribute(WidgetEx.WIDGET_PROPERTY_CSSCLASS);
//		if(value != null && value.trim().length() > 0)
//			wrapper.addCssClass(value);

		String value = node.getAttribute(WidgetEx.WIDGET_PROPERTY_WIDTH);
		if(value != null && value.trim().length() > 0)
			wrapper.setWidth(value);

		value = node.getAttribute(WidgetEx.WIDGET_PROPERTY_HEIGHT);
		if(value != null && value.trim().length() > 0)
			wrapper.setHeight(value);

		value = node.getAttribute(WidgetEx.WIDGET_PROPERTY_EXTERNALSOURCE);
		if(value != null && value.trim().length() > 0)
			wrapper.setExternalSource(value);

		value = node.getAttribute(WidgetEx.WIDGET_PROPERTY_DISPLAYFIELD);
		if(value != null && value.trim().length() > 0)
			wrapper.setDisplayField(value);

		value = node.getAttribute(WidgetEx.WIDGET_PROPERTY_FILTERFIELD);
		if(value != null && value.trim().length() > 0)
			wrapper.setFilterField(value);

		value = node.getAttribute(WidgetEx.WIDGET_PROPERTY_ID);
		if(value != null && value.trim().length() > 0)
			wrapper.setId(value);

		value = node.getAttribute(WidgetEx.WIDGET_PROPERTY_HELPTEXT);
		if(value != null && value.trim().length() > 0)
			wrapper.setTitle(value);

		if(s.equalsIgnoreCase(WidgetEx.WIDGET_TYPE_VIDEO_AUDIO) || s.equalsIgnoreCase(WidgetEx.WIDGET_TYPE_IMAGE)){
			if(binding != null && binding.trim().length() > 0){
				questionDef = formDef.getQuestion(binding);
			}
		}

		if(questionDef != null){
			if(questionDef.getDataType() == QuestionDef.QTN_TYPE_LIST_EXCLUSIVE_DYNAMIC){
				questionDef.setOptions(null); //may have been set by the preview
				//if(wrapper.getWrappedWidget() instanceof ListBox || wrapper.getWrappedWidget() instanceof TextBox)
				if(wrapper.getFilterField() != null && wrapper.getFilterField().trim().length() > 0)
					filtDynOptWidgetMap.put(questionDef, wrapper);
			}

			wrapper.setQuestionDef(questionDef,false);
			ValidationRule validationRule = formDef.getValidationRule(questionDef);
			wrapper.setValidationRule(validationRule);

		} else if (parentWrapper != null) { // children have same questiondef as parent
			wrapper.setQuestionDef(parentWrapper.getQuestionDef(), false);
		}

		if(parentBinding != null)
			wrapper.setParentBinding(parentBinding);

		if(binding != null)
			wrapper.setBinding(binding);

		if(parentWrapper != null)
			parentWrapper.addChildWidget(wrapper);


		value = node.getAttribute(WidgetEx.WIDGET_PROPERTY_VALUEFIELD);
		if(value != null && value.trim().length() > 0){
			wrapper.setValueField(value);

//			if(externalSourceWidgets != null && wrapper.getExternalSource() != null && wrapper.getDisplayField() != null
//					&& (wrapper.getWrappedWidget() instanceof TextBox || wrapper.getWrappedWidget() instanceof ListBox)
//					&& questionDef != null){
//
//				if(!(questionDef.getDataType() == QuestionDef.QTN_TYPE_LIST_EXCLUSIVE
//						||questionDef.getDataType() == QuestionDef.QTN_TYPE_LIST_MULTIPLE)){
//					questionDef.setDataType(QuestionDef.QTN_TYPE_LIST_EXCLUSIVE);
//				}
//
//				externalSourceWidgets.add(wrapper);
//				loadWidget = false;
//
//				wrapper.addSuggestBoxChangeEvent();
//			}
		}

		wrapper.setTabIndex(0);

		if (loadWidget)
			wrapper.loadQuestion();

//		wrapper.setExternalSourceDisplayValue();

		value = node.getAttribute(WidgetEx.WIDGET_PROPERTY_HEIGHT);
		if(value != null && value.trim().length() > 0)
			wrapper.setHeight(value);

		String left = node.getAttribute(WidgetEx.WIDGET_PROPERTY_LEFT);
		if(left != null && left.trim().length() > 0)
			wrapper.setLeft(left);

		String top = node.getAttribute(WidgetEx.WIDGET_PROPERTY_TOP);
		if(top != null && top.trim().length() > 0)
			wrapper.setTop(top);

		//if(wrapper.getWrappedWidget() instanceof Label)
		WidgetEx.loadLabelProperties(node,wrapper);

		//wrapper.setParentBinding(parentBinding);

		if(!(wrapper.getWrappedWidget() instanceof Label) && !(wrapper.getWrappedWidget() instanceof Button))
			widgets.add(wrapper);
		else if (wrapper.getWrappedWidget() instanceof Label && this.isRepeated)
			labelWidgetMap.add(wrapper);
		else
			addWidget(wrapper);

		if(wrapperCreated)
			;//FormUtil.setWidgetPosition(wrapper,left,top);

		if(widget instanceof Button && binding != null){
			//wrapper.setParentBinding(parentBinding);

			if(binding.equals("addnew")||binding.equals("remove") || binding.equals("submit") ||
					binding.equals("browse")||binding.equals("clear")||binding.equals("cancel") ||
					binding.equals("search") || binding.equals("nextPage")||binding.equals("prevPage")){
				((Button)widget).addClickHandler(new ClickHandler(){
					public void onClick(ClickEvent event){
						execute((Widget)event.getSource(), calcWidgetMap);
					}
				});
				
				if(binding.equals("addnew"))
					btnAdd = (Button)widget;
			}
		}

		if(wrapper.isEditable() && questionDef != null)
			FormRunnerView.updateCalcWidgetMapping(wrapper, calcQtnMappings, calcWidgetMap);

		return tabIndex;
	}

	/**
	 * Just adds the first row. Other runtime rows are added using addNewRow
	 * @param wrapper
	 */
	private void addWidget(RuntimeWidgetWrapper wrapper) {
		String binding = wrapper.getBinding();
		if(wrapper.getWrappedWidget() instanceof Button && 
				!("browse".equals(binding) || "clear".equals(binding))){

			//Ensure that the Add New and Remove buttons are displayed according to tab index
			if(buttons.size() == 0 || (buttons.get(0).getTabIndex() <= wrapper.getTabIndex()))
				buttons.add(wrapper);
			else{
				RuntimeWidgetWrapper w = buttons.remove(0);
				buttons.add(wrapper);
				buttons.add(w);
			}
			return;
		}

		if(isRepeated){
			//widgets.add(new RuntimeWidgetWrapper(wrapper));
			widgets.add(wrapper);
			
			int row = 0 , col = 0;
			if(table.getRowCount() > 0){
				if(((RuntimeWidgetWrapper)table.getWidget(0, 0)).getWrappedWidget() instanceof Label) {
					row = 1;
					if(table.getRowCount() == 1)
						col = 0;
					else
						col = table.getCellCount(row);
				}
				else{
					col = table.getCellCount(row);
				}
			}
			table.setWidget(row, col, wrapper);
		}
		else{
			selectedPanel.add(wrapper);
			FormUtil.setWidgetPosition(wrapper,wrapper.getLeft(),wrapper.getTop());
			//FormUtil.setWidgetPosition(selectedPanel,wrapper,wrapper.getLeft(),wrapper.getTop());
		}
	}

	private void execute(Widget sender, HashMap<QuestionDef,List<RuntimeWidgetWrapper>> calcWidgetMap){
		String binding = ((RuntimeWidgetWrapper)sender.getParent().getParent()).getBinding();

		if(binding.equalsIgnoreCase("search")){
//			RuntimeWidgetWrapper wrapper = getCurrentMultimediWrapper(sender);
//			if(wrapper != null && wrapper.getExternalSource() != null)
//				;//FormUtil.searchExternal(wrapper.getExternalSource(),sender.getElement(),wrapper.getWrappedWidget().getElement(),null);
		}
		else if(binding.equalsIgnoreCase("submit"))
			((FormRunnerView)getParent().getParent().getParent().getParent().getParent().getParent().getParent()).onSubmit();
		else if(binding.equalsIgnoreCase("cancel"))
			((FormRunnerView)getParent().getParent().getParent().getParent().getParent().getParent().getParent()).onCancel();
		else if(binding.equalsIgnoreCase("nextPage"))
			((FormRunnerView)getParent().getParent().getParent().getParent().getParent().getParent().getParent()).nextPage();
		else if(binding.equalsIgnoreCase("prevPage"))
			((FormRunnerView)getParent().getParent().getParent().getParent().getParent().getParent().getParent()).prevPage();
		else if(groupQtnsDef != null){
			if(binding.equalsIgnoreCase("addnew")){
				RuntimeWidgetWrapper wrapper = (RuntimeWidgetWrapper)getParent().getParent();
				int y = getHeightInt();

				addNewRow(sender, calcWidgetMap);

				editListener.onRowAdded(wrapper,getHeightInt()-y);
			}
			else if(binding.equalsIgnoreCase("remove")){
				if(table.getRowCount() > 1) { // There should be at least one row
					RuntimeWidgetWrapper wrapper = (RuntimeWidgetWrapper)getParent().getParent();
					int y = getHeightInt();

					table.removeRow(table.getRowCount()-1);
					Element node = dataNodes.get(dataNodes.size() - 1);
					node.getParentNode().removeChild(node);
					dataNodes.remove(node);
					if(btnAdd != null)
						btnAdd.setEnabled(true);

					editListener.onRowRemoved(wrapper,y-getHeightInt());
				}

				RuntimeWidgetWrapper parent = (RuntimeWidgetWrapper)getParent().getParent();
				ValidationRule validationRule = parent.getValidationRule();
				if(validationRule != null)
					parent.getQuestionDef().setAnswer(table.getRowCount()+"");
			}
		}
		else{
			if(binding.equalsIgnoreCase("clear")){
				RuntimeWidgetWrapper wrapper = getCurrentMultimediWrapper(sender);
				if(wrapper == null)
					return;

				if(wrapper.getWrappedWidget() instanceof Image && (((Image)wrapper.getWrappedWidget()).getUrl() == null ||
						((Image)wrapper.getWrappedWidget()).getUrl().trim().length() == 0))
					return;
				if(wrapper.getWrappedWidget() instanceof HTML && !wrapper.getWrappedWidget().isVisible())
					return;

				if(!Window.confirm(LocaleText.get("deleteItemPrompt")))
					return;

				QuestionDef questionDef = wrapper.getQuestionDef();
				if(questionDef != null)
					questionDef.setAnswer(null);

				if(wrapper.getWrappedWidget() instanceof Image){
					image = (Image)wrapper.getWrappedWidget();
					image.setUrl((String) null);
					html = null;
				}
				else if(wrapper.getWrappedWidget() instanceof Label)
					((Label)wrapper.getWrappedWidget()).setText(LocaleText.get("noSelection"));
				else{
					html = (HTML)wrapper.getWrappedWidget();
					html.setHTML(LocaleText.get("clickToPlay"));
					html.setVisible(false);
					image = null;
				}
				return;
			}
			else if(binding.equalsIgnoreCase("browse")){
				RuntimeWidgetWrapper wrapper = getCurrentMultimediWrapper(sender);
				if(wrapper == null)
					return;

				if(wrapper.getWrappedWidget() instanceof Image)
					image = (Image)wrapper.getWrappedWidget();
				else
					html = (HTML)wrapper.getWrappedWidget();

				String xpath = wrapper.getBinding();
				if(!xpath.startsWith(formDef.getBinding()))
					xpath = "/" + formDef.getBinding() + "/" + wrapper.getBinding();

				String contentType = "&contentType=video/3gpp";
				contentType += "&name="+wrapper.getQuestionDef().getBinding()+".3gp";

				//TODO What if the multimedia url suffix already has a ?
				String url = FormUtil.getMultimediaUrl()+"?formId="+formDef.getId()+"&xpath="+xpath+contentType+"&time="+ new java.util.Date().getTime();
				OpenFileDialog dlg = new OpenFileDialog(this,url);
				dlg.center();
			}
		}
	}

	/*private Image getCurrentImage(Widget sender){			
		RuntimeWidgetWrapper wrapper = getCurrentMultimediWrapper(sender);
		if(wrapper != null)
			return (Image)wrapper.getWrappedWidget();

		return null;
	}*/

	private RuntimeWidgetWrapper getCurrentMultimediWrapper(Widget sender){
		RuntimeWidgetWrapper button = (RuntimeWidgetWrapper)sender.getParent().getParent();
		for(int index = 0; index < selectedPanel.getWidgetCount(); index++){
			RuntimeWidgetWrapper widget = (RuntimeWidgetWrapper)selectedPanel.getWidget(index);
			Widget wrappedWidget = widget.getWrappedWidget();
			if(wrappedWidget instanceof Image || wrappedWidget instanceof HTML /*|| wrappedWidget instanceof Label*/){
				String binding  = widget.getBinding();
				if(binding != null && binding.equalsIgnoreCase(button.getParentBinding()))
					return widget;
			}
		}
		return null;
	}

	private void addNewRow(Widget sender, HashMap<QuestionDef,List<RuntimeWidgetWrapper>> calcWidgetMap) {
		HashMap<String,RuntimeWidgetWrapper> widgetMap = new HashMap<String,RuntimeWidgetWrapper>();

		RuntimeWidgetWrapper firstWidget = null;
		Element newRepeatDataNode = null;
		String parentRptBinding = null;
		int row = table.getRowCount();

		List<Integer> qtnIds = new ArrayList<Integer>();
		List<QuestionDef> qtns = new ArrayList<QuestionDef>();
		
		FormDef copyFormDef = new FormDef(formDef);
		GroupQtnsDef groupDef = copyFormDef.getQuestion(groupQtnsDef.getQtnDef().getBinding()).getGroupQtnsDef();
		groupDef.getQuestions().clear();
		
		List<RuntimeWidgetWrapper> copyWidgets = new ArrayList<RuntimeWidgetWrapper>();
		
		for(int index = 0; index < widgets.size(); index++){
			RuntimeWidgetWrapper mainWidget = widgets.get(index);
			RuntimeWidgetWrapper copyWidget = getPreparedWidget(mainWidget,false);

			if(mainWidget.getQuestionDef() == null && (mainWidget.getWrappedWidget() instanceof CheckBox)){
				parentRptBinding = ((QuestionDef)widgets.get(0).getQuestionDef().getParent()).getBinding();
				copyWidget.setQuestionDef(new QuestionDef(widgets.get(0).questionDef, widgets.get(0).questionDef.getParent()), false);
			}
			else
				parentRptBinding = ((QuestionDef)mainWidget.getQuestionDef().getParent()).getBinding();

			//table.setWidget(row, index, copyWidget);

			if (index == 0) {
				Element dataNode = null;
				RuntimeWidgetWrapper widget = null;
				if(mainWidget.getQuestionDef().isAsAttribute()) {
					for (RuntimeWidgetWrapper rww : widgets) {
						if (rww.getQuestionDef().getDataNode() != null) {
							dataNode = rww.getQuestionDef().getDataNode();
							widget = rww;
							break;
						}
					}
					if (dataNode == null) {
						Window.alert("Groep of Lijst bevat geen volwaardige vraag (enkel vragen als attribuut, er dienst minstens een vraag te zijn die geen attribuut is).");
						return;
					}
				} else {
					dataNode = mainWidget.getQuestionDef().getDataNode();
					widget = mainWidget;
					if (dataNode == null) {
						Window.alert(LocaleText.get("repeatChildDataNodeNotFound"));
						return; //possibly form not yet saved
					}
				}
				
				Element repeatDataNode = getParentNode(dataNode,(widget.getWrappedWidget() instanceof CheckBox) ? widget.getParentBinding() : widget.getBinding(), parentRptBinding);
				newRepeatDataNode = (Element)repeatDataNode.cloneNode(true);
				Node prev = repeatDataNode;
				while (prev != null) {
					 Node sib = prev.getNextSibling();
					 if (sib != null && sib.getNodeName().equals(prev.getNodeName())) {
						 prev = sib;
					 } else {
						 break;
					 }
				}
				if (prev.getNextSibling() != null) {
					repeatDataNode.getParentNode().insertBefore(newRepeatDataNode, prev.getNextSibling());
				} else {
					repeatDataNode.getParentNode().appendChild(newRepeatDataNode);
				}
				//workonDefaults(newRepeatDataNode);
				dataNodes.add(newRepeatDataNode);

				if (firstWidget == null && copyWidget.isEditable() && copyWidget.isEnabled() && !copyWidget.isLocked()) {
					firstWidget = copyWidget;
				}
			}

			table.setWidget(row, index, copyWidget);

			setDataNode(copyWidget,newRepeatDataNode,copyWidget.getBinding(),false, parentRptBinding);

			//Loading widget from here instead of in getPreparedWidget because setDataNode may clear default values			
			copyWidget.loadQuestion();

			if(copyWidget.getWrappedWidget() instanceof RadioButton)
				((RadioButton)copyWidget.getWrappedWidget()).setName(((RadioButton)copyWidget.getWrappedWidget()).getName()+row);

			if(copyWidget.getWrappedWidget() instanceof CheckBox){
				RuntimeWidgetWrapper widget = widgetMap.get(copyWidget.getParentBinding());
				if(widget == null){
					widget = copyWidget;
					widgetMap.put(copyWidget.getParentBinding(), widget);
				}
				widget.addChildWidget(copyWidget);
			}

			copyWidget.getQuestionDef().addChangeListener(copyWidget);
			qtnIds.add(copyWidget.getQuestionDef().getId());
			qtns.add(copyWidget.getQuestionDef());
		
			copyFormDef.addQuestion(copyWidget.getQuestionDef()); // this one is used for persisting
			groupDef.addQuestion(copyWidget.getQuestionDef()); // this one is used for calculations
			copyWidgets.add(copyWidget);
			copyWidget.updateStyle();
		}

		PushButton deleteButton = addDeleteButton(row);
		copySkipRules(qtnIds, qtns, deleteButton);
		copyCalculations(copyWidgets, copyFormDef, calcWidgetMap);

		TableUtil.setRowCellWidths(table, row);
		
		btnAdd = (Button)sender;
		RuntimeWidgetWrapper parent = (RuntimeWidgetWrapper)getParent().getParent();
		ValidationRule validationRule = parent.getValidationRule();
		if(validationRule != null){
			row++;
			parent.getQuestionDef().setAnswer(row+"");
			if(validationRule.getMaxValue(formDef) == row){
				((Button)sender).setEnabled(false);
				
				//Remove error message.
				if(getParent().getParent() instanceof RuntimeWidgetWrapper)
					((RuntimeWidgetWrapper)getParent().getParent()).isValid(true);
			}
		}

		if(firstWidget != null)
			firstWidget.setFocus();

		//byte maxRows = repeatQtnsDef.getMaxRows();
		//if(maxRows > 0 && row == maxRows)
		//	((Button)sender).setEnabled(false);
		
		FormUtil.onResize();
	}

	private void addNewRow(Element dataNode, HashMap<QuestionDef,List<RuntimeWidgetWrapper>> calcWidgetMap){
		dataNodes.add(dataNode);

		List<Integer> qtnIds = new ArrayList<Integer>();
		List<QuestionDef> qtns = new ArrayList<QuestionDef>();
		
		FormDef copyFormDef = new FormDef(formDef);
		GroupQtnsDef groupDef = copyFormDef.getQuestion(groupQtnsDef.getQtnDef().getBinding()).getGroupQtnsDef();
		groupDef.getQuestions().clear();
		
		List<RuntimeWidgetWrapper> copyWidgets = new ArrayList<RuntimeWidgetWrapper>();
		
		int row = table.getRowCount();
		for(int index = 0; index < widgets.size(); index++){
			RuntimeWidgetWrapper mainWidget = widgets.get(index);
			RuntimeWidgetWrapper copyWidget = getPreparedWidget(mainWidget,false);

			table.setWidget(row, index, copyWidget);

			setDataNode(copyWidget,dataNode,copyWidget.getBinding(),true, ((QuestionDef)mainWidget.getQuestionDef().getParent()).getBinding());
			
			//Loading widget from here instead of in getPreparedWidget because setDataNode may clear default values			
			copyWidget.loadQuestion();

			if(copyWidget.getWrappedWidget() instanceof RadioButton)
				((RadioButton)copyWidget.getWrappedWidget()).setName(((RadioButton)copyWidget.getWrappedWidget()).getName()+row);

			if(copyWidget.getWrappedWidget() instanceof CheckBox){
				RuntimeWidgetWrapper widget = parentBindingWidgetMap.get(copyWidget.getParentBinding());
				if(widget == null){
					widget = copyWidget;
					parentBindingWidgetMap.put(copyWidget.getParentBinding(), widget);
				}
				widget.addChildWidget(copyWidget);
			}
			
			copyWidget.getQuestionDef().addChangeListener(copyWidget);
			qtnIds.add(copyWidget.getQuestionDef().getId());
			qtns.add(copyWidget.getQuestionDef());
			
//			copyFormDef.removeQuestion(copyFormDef.getQuestion(copyWidget.getQuestionDef().getId()));
			copyFormDef.addQuestion(copyWidget.getQuestionDef()); // this one is used for persisting
			groupDef.addQuestion(copyWidget.getQuestionDef()); // this one is used for calculations
			copyWidgets.add(copyWidget);
			copyWidget.updateStyle();
		}

		PushButton deleteButton = addDeleteButton(row);
		copySkipRules(qtnIds, qtns, deleteButton);
		copyCalculations(copyWidgets, copyFormDef, calcWidgetMap);
		
		TableUtil.setRowCellWidths(table, row);
		
		FormUtil.onResize();
	}

	private Element getParentNode(Node node, String binding, String parentBinding){	
		String name = binding;
		if(parentBinding != null && binding.startsWith(parentBinding) && binding.indexOf('/', parentBinding.length() + 1) > 0)
			name = binding.substring(parentBinding.length() + 1, binding.indexOf('/', parentBinding.length() + 1));
		else{
			int pos = binding.indexOf('/');
			if(pos > 0){
				name = binding.substring(0, pos);
				int pos2 = binding.lastIndexOf('/');
				if(pos != pos2)
					return (Element)node.getParentNode(); //name = binding.substring(pos+1, pos2);
			}
		}

		return getParentNodeWithName(node,name);
	}

	private Element getParentNodeWithName(Node node, String name){
		Element parentNode = (Element)node.getParentNode();
		String nodeName = node.getNodeName();
		if(nodeName.startsWith("xf:")){//caters for xforms from other conversions
			nodeName = nodeName.substring(3);
		}
		if(nodeName.equalsIgnoreCase(name))
			return parentNode;
		else if(name.contains("/")) //TODO This needs to be well tested such that we do not introduce bugs.
			return parentNode;
		return getParentNodeWithName(parentNode,name);
	}

	private void setDataNode(RuntimeWidgetWrapper widget, Element parentDataNode, String binding, boolean loadQtn, String parentBinding) {
		if(widget.getQuestionDef() == null) {
			return; // labels do not necessarily have a questiondef
		}

		// set datanode
		XformParser.setRepeatQuestionDataNode(widget.getQuestionDef(), parentDataNode);
		
		// set answer
		if(loadQtn){
			QuestionDef qtn = widget.getQuestionDef();
			String xpath;
			if (qtn.isAsAttribute()) {
				xpath = qtn.getAttributeBinding();
				String parent = qtn.getParentBinding();
				if (xpath.startsWith(parent)) {
					xpath = xpath.substring(parent.length() + 1);
				}
			} else {
				xpath = qtn.getBinding();
			}
			XPathExpression xpls = new XPathExpression(parentDataNode, xpath);
			List<?> result = xpls.getResult();
			String value = "";
			if (result != null && result.size() > 0) { // can be null if question is optional & not filled in.
				Element el = (Element) result.get(0);
				if (qtn.isAsAttribute()) {
					value = el.getAttribute(qtn.getBareBinding());
				} else {
					value = XmlUtil.getTextValue(el);
				}
			}
			qtn.setAnswerRaw(value == null ? "" : value);
		}
	}

	private RuntimeWidgetWrapper getPreparedWidget(RuntimeWidgetWrapper w, boolean loadQtn){
		RuntimeWidgetWrapper widget = new RuntimeWidgetWrapper(w);

		if(loadQtn)
			widget.loadQuestion();

		QuestionDef questionDef = widget.getQuestionDef();
		questionDef.setRepeatChild(true);
		questionDef.setRepeatBinding(w.getQuestionDef().getParentBinding());
		if (isRepeated && questionDef.isLocked()) {
			widget.hideErrorLabel();
		}

		if(questionDef != null && (questionDef.getDataType() == QuestionDef.QTN_TYPE_NUMERIC || questionDef.getDataType() == QuestionDef.QTN_TYPE_DECIMAL))
			FormUtil.allowNumericOnly((TextBox)widget.getWrappedWidget(),questionDef.getDataType() == QuestionDef.QTN_TYPE_DECIMAL);

		widget.refreshSize();
		return widget;
	}

	@Override
	public boolean isEnabled() {
		return enabled;
	}
	
	@Override
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
		if(isRepeated){
			HorizontalPanel panel = (HorizontalPanel)verticalPanel.getWidget(1);
			for(int index = 0; index < panel.getWidgetCount(); index++)
				((RuntimeWidgetWrapper)panel.getWidget(index)).setEnabled(enabled);

			for(int row = 0; row < table.getRowCount(); row++){
				for(int col = 0; col < table.getCellCount(row); col++) {
					Widget w = table.getWidget(row, col);
					if (w instanceof HasEnabled) {
						((HasEnabled) w).setEnabled(enabled);
					}
				}
			}
		}
		else{
			for(int index = 0; index < selectedPanel.getWidgetCount(); index++){
				((RuntimeWidgetWrapper)selectedPanel.getWidget(index)).setEnabled(enabled);
			}
		}
	}

	public void setLocked(boolean locked){
		if (isRepeated) {
			HorizontalPanel panel = (HorizontalPanel)verticalPanel.getWidget(1);
			for(int index = 0; index < panel.getWidgetCount(); index++)
				((RuntimeWidgetWrapper)panel.getWidget(index)).setLocked(locked);

			for(int row = 0; row < table.getRowCount(); row++){
				for(int col = 0; col < table.getCellCount(row)-1; col++)
					((RuntimeWidgetWrapper)table.getWidget(row, col)).setLocked(locked);
			}
			
		} else {
			for(int index = 0; index < selectedPanel.getWidgetCount(); index++){
				((RuntimeWidgetWrapper)selectedPanel.getWidget(index)).setLocked(locked);
			}
		}
	}

	/** 
	 * remove all the nodes of a repeat from the result (when invisible / !enabled)
	 * @param formDef
	 */
	public void saveRemove(FormDef formDef) {
		if(isRepeated){
			List<Node> toRemove = new ArrayList<Node>();
			for(int row = 0; row < table.getRowCount(); row++){
				if (table.getCellCount(row)-1 > 0) {
					Node dataNode = ((RuntimeWidgetWrapper)table.getWidget(row, 0)).getQuestionDef().getDataNode();
					Node groupNode = dataNode != null && dataNode.getParentNode() != null ? dataNode.getParentNode() : null;
	
					if(groupNode != null && groupNode.getParentNode() != null) {
						toRemove.add(groupNode);
					}
				}
			}

			for (Node group : toRemove) {
				group.getParentNode().removeChild(group);
			}
		}
	}

	public void saveValue(FormDef formDef){
		if(isRepeated){
			List<Node> toRemove = new ArrayList<Node>();
			for(int row = 0; row < table.getRowCount(); row++){
				if (table.getCellCount(row)-1 > 0) {
					Node dataNode = ((RuntimeWidgetWrapper)table.getWidget(row, 0)).getQuestionDef().getDataNode();
					Node groupNode = dataNode != null && dataNode.getParentNode() != null ? dataNode.getParentNode() : null;
	
					// save data
					for(int col = 0; col < table.getCellCount(row)-1; col++) {
						((RuntimeWidgetWrapper)table.getWidget(row, col)).saveValue(formDef, row);
					}
	
					// remove group if empty
					if(groupNode != null && groupNode.getParentNode() != null && !XmlUtil.hasChildElementNodes(groupNode)) {
						toRemove.add(groupNode);
					}
				}
			}

			// remove empty rows
			for (Node group : toRemove) {
				group.getParentNode().removeChild(group);
			}
		}
		else{
			if (selectedPanel.getWidgetCount() > 0) {
				QuestionDef qd = null;
				Node groupNode = null;
				
				// save data
				for (int index = 0; index < selectedPanel.getWidgetCount(); index++) {
					RuntimeWidgetWrapper rww = ((RuntimeWidgetWrapper) selectedPanel.getWidget(index));

					// -- get group
					if (qd == null && rww.getQuestionDef() != null && rww.getQuestionDef().getDataNode() != null) { 
						qd = rww.getQuestionDef(); 
						Node dataNode = qd != null ? qd.getDataNode() : null;
						groupNode = dataNode != null && dataNode.getParentNode() != null ? dataNode.getParentNode() : null;
					}
					
					rww.saveValue(formDef);
				}
				
				// remove group if empty
				if(groupNode != null && groupNode.getParentNode() != null && !XmlUtil.hasChildElementNodes(groupNode)) {
					groupNode.getParentNode().removeChild(groupNode);
				}
			}
		}

		if(isRepeated && groupQtnsDef != null)
			groupQtnsDef.getQtnDef().setAnswer(getRowCount()+"");
	}

	public int getRowCount(){
		int rows = 0;

		for(int row = 0; row < table.getRowCount(); row++){
			boolean answerFound = false;
			for(int col = 0; col < table.getCellCount(row)-1; col++){
				if(((RuntimeWidgetWrapper)table.getWidget(row, col)).isAnswered()){
					answerFound = true;
					break;
				}
			}

			if(answerFound)
				rows++;
		}

		return rows;
	}

	public void onSetFileContents(String contents) {
		if(contents != null && contents.trim().length() > 0){
			contents = contents.replace("<pre>", "");
			contents = contents.replace("</pre>", "");
			RuntimeWidgetWrapper widgetWrapper = null;

			if(image != null)
				widgetWrapper = (RuntimeWidgetWrapper)image.getParent().getParent();
			else
				widgetWrapper = (RuntimeWidgetWrapper)html.getParent().getParent();

			String xpath = widgetWrapper.getBinding();
			if(!xpath.startsWith(formDef.getBinding()))
				xpath = "/" + formDef.getBinding() + "/" + widgetWrapper.getBinding();

			if(image != null)
				image.setUrl(FormUtil.getMultimediaUrl()+"?action=recentbinary&time="+ new java.util.Date().getTime()+"&formId="+formDef.getId()+"&xpath="+xpath);
			else{
				String extension = "";//.3gp ".mpeg";
				String contentType = "&contentType=video/3gpp";
				if(widgetWrapper.getQuestionDef().getDataType() == QuestionDef.QTN_TYPE_AUDIO)
					contentType = "&contentType=audio/3gpp"; //"&contentType=audio/x-wav";
				//extension = ".wav";

				contentType += "&name="+widgetWrapper.getQuestionDef().getBinding()+".3gp";

				html.setVisible(true);
				html.setHTML("<a href=" + URL.encode(FormUtil.getMultimediaUrl()+extension + "?formId="+formDef.getId()+"&xpath="+xpath+contentType+"&time="+ new java.util.Date().getTime()) + ">"+html.getText()+"</a>");				
			}

			widgetWrapper.getQuestionDef().setAnswer(contents);
		}
	}

	public void clearValue(){
		if(isRepeated){
			while(table.getRowCount() > 1)
				table.removeRow(1);

			for(int col = 0; col < table.getCellCount(0)-1; col++)
				((RuntimeWidgetWrapper)table.getWidget(0, col)).clearValue();

			//TODO Causes an infinite loop for repeat questions having skip logic that refers
			//     to non repeat children.
			//((FormRunnerView)editListener).fireSkipRules();
		}
		else{
			for(int index = 0; index < selectedPanel.getWidgetCount(); index++)
				((RuntimeWidgetWrapper)selectedPanel.getWidget(index)).clearValue();
		}
	}

	public boolean isValid(boolean fireValueChanged){
		firstInvalidWidget = null;

		if(isRepeated){
			for(int row = 0; row < table.getRowCount(); row++){
				for(int col = 0; col < table.getCellCount(row)-1; col++){
					boolean valid = ((RuntimeWidgetWrapper)table.getWidget(row, col)).isValid(fireValueChanged);
					if(!valid){
						firstInvalidWidget = (RuntimeWidgetWrapper)table.getWidget(row, col);
						return false;
					}
				}
			}
			return true;
		}
		else{
			boolean valid = true;
			for(int index=0; index<selectedPanel.getWidgetCount(); index++){
				RuntimeWidgetWrapper widget = (RuntimeWidgetWrapper)selectedPanel.getWidget(index);
				if(!widget.isValid(fireValueChanged)){
					valid = false;
					if(firstInvalidWidget == null && widget.isFocusable())
						firstInvalidWidget = widget.getInvalidWidget();
				}

				if(fireValueChanged && widget.getQuestionDef() != null)
					editListener.onValueChanged(widget);
			}
			return valid;
		}
	}

	/**
	 * @return true if at least one question has been answered, note that this does not necessarily mean that the group is valid.
	 */
	public boolean isAnswered() {
		if (isRepeated) {
			for (int row = 0; row < table.getRowCount(); row++) {
				for (int col = 0; col < table.getCellCount(row) - 1; col++) {
					if (((RuntimeWidgetWrapper) table.getWidget(row, col)).isAnswered()) {
						return true;
					}
				}
			}
		} else {
			for (int index = 0; index < selectedPanel.getWidgetCount(); index++) {
				if (((RuntimeWidgetWrapper) selectedPanel.getWidget(index)).isAnswered()) {
					return true;
				}
			}
		}
		return false;
	}

	public RuntimeWidgetWrapper getInvalidWidget(){
		if(firstInvalidWidget == null)
			return (RuntimeWidgetWrapper)getParent().getParent();
		return firstInvalidWidget;
	}

	public boolean setFocus(){
		if(isRepeated){
			for(int row = 0; row < table.getRowCount(); row++){
				for(int col = 0; col < table.getCellCount(row)-1; col++){
					RuntimeWidgetWrapper widget = (RuntimeWidgetWrapper)table.getWidget(row, col);
					if(widget.isFocusable()){
						if(widget.setFocus())
							return true;
					}
				}
			}
		}
		else{
			for(int index = 0; index < selectedPanel.getWidgetCount(); index++){
				RuntimeWidgetWrapper widget = (RuntimeWidgetWrapper)selectedPanel.getWidget(index);
				if(widget.isFocusable()){
					if(widget.setFocus())
						return true;
				}
			}
		}

		return false;
	}

	public boolean onMoveToNextWidget(Widget widget) {
		int index = selectedPanel.getWidgetIndex(widget);

		if(index == -1){
			//Handle tabbing for repeats within the flex table
			if(isRepeated){
				boolean found = false;
				for(int row = 0; row < table.getRowCount(); row++){
					for(int col = 0; col < table.getCellCount(row); col++){
						if(found){
							Widget curWidget = table.getWidget(row, col);
							if(curWidget instanceof RuntimeWidgetWrapper && ((RuntimeWidgetWrapper)curWidget).setFocus())
								return true;
						}

						if(table.getWidget(row, col) == widget)
							found = true;
					}
				}
			}

			return false;
		}

		return moveToNextWidget(index);
	}

	public boolean onMoveToPrevWidget(Widget widget){
		int index = selectedPanel.getWidgetIndex(widget);
		
		if(index == -1){
			//Handle tabbing for repeats within the flex table
			if(isRepeated){
				boolean found = false;
				for(int row = table.getRowCount() - 1; row >= 0; row--){
					for(int col = table.getCellCount(row) - 1; col >= 0 ; col--){
						if(found){
							Widget curWidget = table.getWidget(row, col);
							if(curWidget instanceof RuntimeWidgetWrapper && ((RuntimeWidgetWrapper)curWidget).setFocus())
								return true;
						}

						if(table.getWidget(row, col) == widget)
							found = true;
					}
				}
			}

			return false;
		}
		
		
		while(--index > 0){
			if(((RuntimeWidgetWrapper)selectedPanel.getWidget(index)).setFocus())
				return true;
		}

		return false;
	}

	protected boolean moveToNextWidget(int index){
		while(++index < selectedPanel.getWidgetCount())
			if(((RuntimeWidgetWrapper)selectedPanel.getWidget(index)).setFocus()){
				return true;
			}

		return false;
	}

	public HashMap<QuestionDef,List<Label>> getLabelMap(){
		return labelMap;
	}

//	public HashMap<QuestionDef,List<RuntimeWidgetWrapper>> getCalcWidgetMap(){
//		return calcWidgetMap;
//	}

	public HashMap<QuestionDef,RuntimeWidgetWrapper> getFiltDynOptWidgetMap(){
		return filtDynOptWidgetMap;
	}

	public HashMap<Label,String> getLabelText(){
		return labelText;
	}

	public HashMap<Label,String> getLabelReplaceText(){
		return labelReplaceText;
	}

	public HashMap<QuestionDef,List<CheckBox>> getCheckBoxGroupMap(){
		return checkBoxGroupMap;
	}

	public void onEnabledChanged(QuestionDef sender,boolean enabled){
		List<CheckBox> list = checkBoxGroupMap.get(sender);
		if(list == null)
			return;

		for(CheckBox checkBox : list){
			checkBox.setEnabled(enabled);
			if(!enabled)
				checkBox.setValue(false);
		}
	}

	public void onVisibleChanged(QuestionDef sender,boolean visible){
		List<CheckBox> list = checkBoxGroupMap.get(sender);
		if(list == null)
			return;

		for(CheckBox checkBox : list){
			checkBox.setVisible(visible);
			if(!visible)
				checkBox.setValue(false);
		}
	}

	@Override
	public void onRequiredChanged(QuestionDef sender,boolean required){
	}

	@Override
	public void onLockedChanged(QuestionDef sender,boolean locked){
	}

	@Override
	public void onBindingChanged(QuestionDef sender,String newValue){
	}

	@Override
	public void onAttributeStateChanged(QuestionDef sender) { // only changes designtime
	}

	@Override
	public void onDataTypeChanged(QuestionDef sender,int dataType){
	}

	@Override
	public void onOptionsChanged(QuestionDef sender,List<OptionDef> optionList){
	}

	public int getHeightInt(){
		return getElement().getOffsetHeight();
	}

	/*private void workonDefaults(Node repeatDataNode){
		NodeList nodes = repeatDataNode.getChildNodes();
		for(int index = 0; index < nodes.getLength(); index++){
			Node child = nodes.item(index);
			if(child.getNodeType() != Node.ELEMENT_NODE)
				continue;

			if(XformConstants.XPATH_VALUE_FALSE.equals(((Element)child).getAttribute("default")))
				XmlUtil.setTextValue((Element)child, "");

			workonDefaults(child);
		}
	}*/

	public void copySkipRules(List<Integer> qtnIds, List<QuestionDef> qtns, PushButton deleteButton){
		List<FormDef> forms = new ArrayList<FormDef>();

		Vector rules = formDef.getSkipRules();
		if(rules != null){
			for(int i=0; i<rules.size(); i++){
				SkipRule rule = (SkipRule)rules.elementAt(i);

				for(int k = 0; k < rule.getConditionCount(); k++){
					Condition condition = rule.getConditionAt(k);
					if(qtnIds.contains(condition.getQuestionId())){
						SkipRule skipRule = new SkipRule(rule);	
						FormDef formDef = new FormDef();
						formDef.addSkipRule(skipRule);
						forms.add(formDef);

						for(QuestionDef qtn : qtns)
							formDef.addQuestion(qtn);

						skipRule.fire(formDef);

						break;
					}
				}
			}
		}

		for(FormDef form : forms)
			((FormRunnerView)editListener).addRepeatQtnFormDef(form);

		if(forms.size() > 0)
			repeatRowFormMap.put(deleteButton, forms);
	}
	
	private void copyCalculations(List<RuntimeWidgetWrapper> widgets, FormDef formDef, HashMap<QuestionDef, List<RuntimeWidgetWrapper>> calcWidgetMap){
		HashMap<QuestionDef,List<QuestionDef>> calcQtnMappings = FormRunnerView.getCalcQtnMappings(formDef);

		for(RuntimeWidgetWrapper widget : widgets){
			if(widget.isEditable()){
				FormRunnerView.updateCalcWidgetMapping(widget, calcQtnMappings, calcWidgetMap);
			}
		}
	}

	// TODO FIXME check hier voor DBA-144
	public boolean isAnyWidgetVisible(){
		if(isRepeated){
			for(int row = 0; row < table.getRowCount(); row++){
				for(int col = 0; col < table.getCellCount(row)-1; col++){
					RuntimeWidgetWrapper widget = (RuntimeWidgetWrapper)table.getWidget(row, col);
					if(widget.isVisible() && widget.isFocusable()){
						return true;
					}
				}
			}
		}
		else{
			for(int index = 0; index < selectedPanel.getWidgetCount(); index++){
				RuntimeWidgetWrapper widget = (RuntimeWidgetWrapper)selectedPanel.getWidget(index);
				if(widget.isVisible() && widget.isFocusable()){
					return true;
				}
			}
		}

		return false;
	}

	public int getHeaderHeight(){
		if(isRepeated || groupQtnsDef.getQtnDef().getDataType() == QuestionDef.QTN_TYPE_GROUP)
			return 0;

		RuntimeWidgetWrapper headerLabel = (RuntimeWidgetWrapper)selectedPanel.getWidget(0);
		return headerLabel.getHeightInt();
	}

	public void onWidgetHidden(RuntimeWidgetWrapper widget, int decrement){
		Widget parent = getParent().getParent();
		if(parent instanceof RuntimeGroupWidget){
			RuntimeGroupWidget groupWidget = (RuntimeGroupWidget)parent;
			RuntimeWidgetWrapper wrapper = (RuntimeWidgetWrapper)groupWidget.getParent().getParent();
			groupWidget.onWidgetHidden(wrapper, decrement);
		}

		int bottomYpos = widget.getTopInt();

		if(!isRepeated){
			for(int index = 0; index < selectedPanel.getWidgetCount(); index++){
				RuntimeWidgetWrapper currentWidget = (RuntimeWidgetWrapper)selectedPanel.getWidget(index);
				if(currentWidget == widget)
					continue;

				int top = currentWidget.getTopInt();
				if(top >= bottomYpos)
					currentWidget.setTopInt(top - decrement);
			}
		}
		
		FormUtil.onResize();
	}

	public void onWidgetShown(RuntimeWidgetWrapper widget, int increment){
		Widget parent = getParent().getParent();
		if(parent instanceof RuntimeGroupWidget){
			RuntimeGroupWidget groupWidget = (RuntimeGroupWidget)parent;
			RuntimeWidgetWrapper wrapper = (RuntimeWidgetWrapper)groupWidget.getParent().getParent();
			groupWidget.onWidgetShown(wrapper, increment);
		}

		int bottomYpos = widget.getTopInt();

		if(!isRepeated){
			for(int index = 0; index < selectedPanel.getWidgetCount(); index++){
				RuntimeWidgetWrapper currentWidget = (RuntimeWidgetWrapper)selectedPanel.getWidget(index);
				if(currentWidget == widget)
					continue;

				int top = currentWidget.getTopInt();
				if(top >= bottomYpos)
					currentWidget.setTopInt(top + increment);
			}
		}
		
		FormUtil.onResize();
	}
	
	public void onValidationFailed(ValidationRule validationRule){
		if(btnAdd != null && hasEqualOperator(validationRule))
			btnAdd.setEnabled(validationRule.getMaxValue(formDef) > table.getRowCount());
	}
	
	public void onValidationPassed(ValidationRule validationRule){
		if(btnAdd != null && hasEqualOperator(validationRule))
			btnAdd.setEnabled(false);
	}
	
	private boolean hasEqualOperator(ValidationRule validationRule){
		return validationRule.getConditionAt(0).getOperator() == ModelConstants.OPERATOR_EQUAL;
	}

	@Override
	public int getTabIndex() {
		return 0;
	}

	@Override
	public void setAccessKey(char key) {
	}

	@Override
	public void setFocus(boolean focused) {
	}

	@Override
	public void setTabIndex(int index) {
	}

	public List<String> getAnswersIfAvailable(String aggregateQuestionName) {
		return getAnswers(aggregateQuestionName, true);
	}
	
	public List<String> getAnswers(String aggregateQuestionName) {
		return getAnswers(aggregateQuestionName, false);
	}
	
	/**
	 * Only supported on repeats, for groups you can access the questions directly (/groupname/question)
	 * 
	 * @param aggregateQuestionName not fully qualified (eg. only last part of binding)
	 * @return
	 */
	public List<String> getAnswers(String aggregateQuestionName, boolean onlyIfAvailable) {
		List<String> res = new ArrayList<String>();
		if(isRepeated){
			for(int row = 0; row < table.getRowCount(); row++){
				if (table.getCellCount(row)-1 > 0) {
					for(int col = 0; col < table.getCellCount(row)-1; col++) {
						RuntimeWidgetWrapper wrap = ((RuntimeWidgetWrapper)table.getWidget(row, col));
						if (wrap.getQuestionDef() != null && wrap.getQuestionDef().getBareBinding().equals(aggregateQuestionName)) {
							if(wrap.getWrappedWidget() instanceof TextBox) {
								res.add(onlyIfAvailable ? wrap.getQuestionDef().getAnswerIfAvailable() : wrap.getQuestionDef().getAnswer());
								break;
							}
						}
					}
				}
			}
		}
		return res;
	}

}

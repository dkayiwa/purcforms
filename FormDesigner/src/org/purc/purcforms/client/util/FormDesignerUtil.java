package org.purc.purcforms.client.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import org.purc.purcforms.client.model.FormDef;
import org.purc.purcforms.client.model.OptionDef;
import org.purc.purcforms.client.model.PageDef;
import org.purc.purcforms.client.model.QuestionDef;
import org.purc.purcforms.client.widget.DesignGroupWidget;
import org.purc.purcforms.client.widget.DesignWidgetWrapper;

import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.MultiWordSuggestOracle;
import com.google.gwt.user.client.ui.Widget;


/**
 * Utilities used by the form designer.
 * 
 * @author daniel
 *
 */
public class FormDesignerUtil {

	/** The form designer title. */
	private static String title = "PurcForms FormDesigner";


	/**
	 * Creates an HTML fragment that places an image & caption together, for use
	 * in a group header.
	 * 
	 * @param imageProto an image prototype for an image
	 * @param caption the group caption
	 * @return the header HTML fragment
	 */
	public static String createHeaderHTML(ImageResource imageProto, String caption) {

		//Add the image and text to a horizontal panel
		HorizontalPanel hPanel = new HorizontalPanel();
		hPanel.setSpacing(0);

		hPanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
		hPanel.add(FormUtil.createImage(imageProto));
		//HTML headerText = new HTML(caption);
		Widget headerText = new Label(caption);
		hPanel.add(headerText);

		return hPanel.getElement().getString();
	}


	/**
	 * Disables the browsers default context menu for the specified element.
	 *
	 * @param elem the element whose context menu will be disabled
	 */
	public static native void disableContextMenu(Element elem) /*-{
	    elem.oncontextmenu=function() {  return false};
	}-*/; 


	/**
	 * Enabled the browsers default context menu for the specified element.
	 *
	 * @param elem the element whose context menu will be enabled
	 */
	public static native void enableContextMenu(Element elem) /*-{
	    elem.oncontextmenu=function() {  return true};
	}-*/; 



	/**
	 * Puts a widget at a given position.
	 * 
	 * @param w the widget.
	 * @param left the left position in pixels.
	 * @param top the top position in pixels.
	 */
	public static void setWidgetPosition(Widget w, String left, String top) {
		FormUtil.setWidgetPosition(w, left, top);
	}

	/**
	 * Loads a list of questions into a MultiWordSuggestOracle for a given reference question.
	 * 
	 * @param questions the list of questions.
	 * @param refQuestion the reference question.
	 * @param oracle the MultiWordSuggestOracle.
	 * @param dynamicOptions set to true if we are loading for dynamic options.
	 * @param sameTypesOnly set to true if you want to load only questions of the same type
	 * 						as the referenced question.
	 */
	public static void loadQuestions(boolean includeBinding, Vector<QuestionDef> questions, QuestionDef refQuestion, MultiWordSuggestOracle oracle, boolean dynamicOptions, boolean sameTypesOnly, QuestionDef parentQuestionDef){
		if(questions == null)
			return;

		for(int i=0; i<questions.size(); i++){
			QuestionDef questionDef = questions.elementAt(i);

			if(!dynamicOptions && refQuestion != null && refQuestion.getDataType() != questionDef.getDataType() && sameTypesOnly)
				continue;

			if(dynamicOptions && !(questionDef.getDataType() == QuestionDef.QTN_TYPE_LIST_EXCLUSIVE ||
					questionDef.getDataType() == QuestionDef.QTN_TYPE_LIST_EXCLUSIVE_DYNAMIC))
				continue;

			if(dynamicOptions && refQuestion == questionDef)
				continue;

			if(!dynamicOptions && refQuestion == questionDef)
				continue;

			if(questionDef == parentQuestionDef)
				continue;

			//oracle.add(includeBinding ? questionDef.getDisplayText() + " - "+ questionDef.getBinding() : questionDef.getDisplayText());	
			oracle.add(questionDef.getDisplayText()); // TODO questionDef.getFullBinding() ???

			//TODO Allowed for now since repeat questions will have ids which cant be equal to
			//those of parents. But test this to ensure it does not bring in bugs.
			if(questionDef.getDataType() == QuestionDef.QTN_TYPE_GROUP || questionDef.getDataType() == QuestionDef.QTN_TYPE_REPEAT)
				loadQuestions(includeBinding, questionDef.getGroupQtnsDef().getQuestions(),refQuestion,oracle,dynamicOptions,sameTypesOnly, parentQuestionDef); //TODO These have different id sets and hence we are leaving them out for now
		}
	}

	/**
	 * Loads a list of questions into a MultiWordSuggestOracle for a given reference question.
	 * 
	 * @param questions the list of questions.
	 * @param refQuestion the reference question.
	 * @param oracle the MultiWordSuggestOracle.
	 * @param dynamicOptions set to true if we are loading for dynamic options.
	 */
	public static void loadQuestions(boolean includeBinding, Vector<QuestionDef> questions, QuestionDef refQuestion, MultiWordSuggestOracle oracle, boolean dynamicOptions){
		loadQuestions(includeBinding, questions, refQuestion, oracle, dynamicOptions,/*true*/ false, null);
	}

	/**
	 * Loads valid options for attribute mapping.
	 * @param includeAttributes
	 * @param questions
	 * @param refQuestion
	 * @param listbox
	 */
	public static void loadQuestions(boolean includeAttributes, List<QuestionDef> questions, QuestionDef refQuestion, ListBox listbox) {
		loadQuestions(includeAttributes, questions, refQuestion, listbox, true);
	}
 
	/**
	 * Loads valid options for attribute mapping.
	 * @param includeAttributes
	 * @param questions
	 * @param refQuestion
	 * @param listbox
	 */
	public static void loadQuestions(boolean includeAttributes, List<QuestionDef> questions, QuestionDef refQuestion, ListBox listbox, boolean clear) {
		if (listbox != null) {
			if (clear) {
				listbox.clear();
				listbox.addItem("");
			}
			if (questions != null) {
				boolean parentIsRepeat = (refQuestion != null && refQuestion.getParent() instanceof QuestionDef && QuestionDef.QTN_TYPE_REPEAT == ((QuestionDef) refQuestion.getParent()).getDataType());
				for (QuestionDef qtn : questions) {
					if (includeAttributes || !qtn.isAsAttribute()) {
						if (refQuestion == null || !refQuestion.getFullBinding().equals(qtn.getFullBinding())) {
							if (!parentIsRepeat || refQuestion.getParent().equals(qtn.getParent())) { // exactly matches, children are not matched (eg. are included in list)
								listbox.addItem(qtn.getFullText(), qtn.getFullBinding());
							}
						}
						if ((QuestionDef.QTN_TYPE_GROUP == qtn.getDataType() && !parentIsRepeat) || (parentIsRepeat && QuestionDef.QTN_TYPE_REPEAT == qtn.getDataType())) {
							if (!parentIsRepeat || qtn.equals(refQuestion.getParent())) {
								loadQuestions(includeAttributes, qtn.getGroupQtnsDef().getQuestions(), refQuestion, listbox, false);
							}
						}
					}
				}
			}
		}
	}
	
	/**
	 * Draws a widget selection rubber band on the mouse down event.
	 * 
	 * @param event the current mouse down event.
	 * @param elem the rubber band widget.
	 */
	public static native void startRubber(Event event, Element elem) /*-{
		elem.style.width = 0;
		elem.style.height = 0;
		elem.style.left = event.x;
		elem.style.top = event.y;
		elem.style.visibility = 'visible';
	}-*/;

	/**
	 * Removes the widget selection rubber band on the mouse up event.
	 * 
	 * @param event the current mouse up event.
	 * @param elem the rubber band element.
	 */
	public static native void stopRubber(Event event,Element elem) /*-{
		elem.style.visibility = 'hidden';
    }-*/;


	/**
	 * Moves the rubber band on the mouse move event.
	 * 
	 * @param event the current mouse move event.
	 * @param elem the rubber band element.
	 */
	public static native void moveRubber(Event event, Element elem) /*-{
		elem.style.width = event.x - elem.style.left;
		elem.style.height = event.y - elem.style.top;
	}-*/;


	/**
	 * Gets the title of the form designer.
	 * 
	 * @return the form designer title.
	 */
	public static String getTitle(){
		return title;
	}

	/**
	 * Sets the title of the form designer.
	 */
	public static void setDesignerTitle(){
		String s = FormUtil.getDivValue("title", true);
		if(s != null && s.trim().length() > 0)
			title = s;
		Window.setTitle(title);
	}

	/**
	 * Checks if the CTRL key is currently pressed.
	 * 
	 * @return true if pressed, else false.
	 */
	public static boolean getCtrlKey(){
		Event event = DOM.eventGetCurrentEvent();
		if(event == null)
			return false;
		return event.getCtrlKey();
	}

	/**
	 * Converts a string into a valid XML token (tag name)
	 * 
	 * @param s string to convert into XML token
	 * @return valid XML token based on s
	 */
	public static String getXmlTagName(String s) {
		return FormUtil.getXmlTagName(s);
	}

	/**
	 * Creates a default binding of a question with a given id and at a given position.
	 * 
	 * @param id the question id.
	 * @param pos the question position which is 1 based.
	 * @return the question binding.
	 */
	public static String getQtnBinding(int id, int pos){
		String binding = getCustomQuestionBinding(id, pos);
		if(binding == null)
			binding = "question" + pos;
		
		return binding;
	}

	/**
	 * Creates a default binding of a question option with a given id and at a given position.
	 * 
	 * @param id the question option id.
	 * @param pos the question option position which is 1 based.
	 * @return the question option binding.
	 */
	public static String getOptnBinding(int id, int pos){
		String binding = getCustomOptionBinding(id, pos);
		if(binding == null)
			binding = "option" + pos;
		
		return binding;
	}

	private static native String getCustomQuestionBinding(int id, int pos) /*-{
		return $wnd.getQuestionBinding(id, pos);
	}-*/;

	private static native String getCustomOptionBinding(int id, int pos) /*-{
		return $wnd.getOptionBinding(id, pos);
	}-*/;
	
	public static boolean inReadOnlyMode(){
		/*boolean readOnly = false;
		String s = FormUtil.getDivValue("readOnly", false);
		if(s != null && s.trim().length() > 0){
			if("1".equals(s) || "true".equals(s))
				readOnly = true;
		}
		
		return readOnly;*/
		
		return FormUtil.isReadOnlyMode();
	}
	
	public static int findHighestYValue(AbsolutePanel panel) {
		if (panel == null || panel.getWidgetCount() == 0) {
			return 0;
		} else {
			int highest = 0; Widget w = null; int pos = 0;
			for (int i = 0; i < panel.getWidgetCount(); i++) {
				w = panel.getWidget(i);
				pos = panel.getWidgetTop(w);
				if (pos > highest) highest = pos;
			}
			return highest - 10;
		}
	}

	public static int findHighestXValue(AbsolutePanel panel) {
		if (panel == null || panel.getWidgetCount() == 0) {
			return 0;
		} else {
			int highest = 0; Widget w = null; int pos = 0;
			for (int i = 0; i < panel.getWidgetCount(); i++) {
				w = panel.getWidget(i);
				pos = panel.getWidgetLeft(w);
				if (pos > highest) highest = pos;
			}
			return highest;
		}
	}
	

	public static int findHighestTabIndex(AbsolutePanel panel) {
		if (panel == null || panel.getWidgetCount() == 0) {
			return 0;
		} else {
			int highest = 0; int t = 0;
			for (int i = 0; i < panel.getWidgetCount(); i++) {
				t = ((DesignWidgetWrapper) panel.getWidget(i)).getTabIndex();
				if (t > highest) highest = t;
			}
			return highest;
		}
	}
	
	/**
	 * Returns true if there are questions that are not children of this question that reference any of the children of this group. (eg. you cannot delete this group).
	 * @param qd
	 * @return
	 */
	public static boolean hasExternalChildAttributeDependencies(QuestionDef qd) {
		if (qd != null && (QuestionDef.QTN_TYPE_REPEAT == qd.getDataType() || QuestionDef.QTN_TYPE_GROUP == qd.getDataType()) && qd.getGroupQtnsDef().size() > 0) {
			for (PageDef pd : qd.getParentFormDef().getPages()) {
				if (hasExternalChildAttributeDependencies(qd, pd.getQuestions())) {
					return true;
				}
			}
		}
		return false;
	}
	
	private static boolean hasExternalChildAttributeDependencies(QuestionDef qd, Vector<QuestionDef> qds) {
		for (QuestionDef child : qds) {
			if (!child.equals(qd)) {
				if (child.isAsAttribute() && child.getAttributeBinding() != null) { // groups are never attributes but still...
					// the group itself is allowed, only inner is not allowed
					if (child.getAttributeBinding().startsWith(qd.getFullBinding()) && !child.getAttributeBinding().equals(qd.getFullBinding())) {
						return true;
					}
				} else if (QuestionDef.QTN_TYPE_REPEAT == child.getDataType() || QuestionDef.QTN_TYPE_GROUP == child.getDataType()) {
					return hasExternalChildAttributeDependencies(qd, child.getGroupQtnsDef().getQuestions());
				}
			}
		}
		return false;
	}
	
	/**
	 * Returns true if there are any attribute questions referring to the given question, can also be inner child questions
	 * @param obj
	 * @return
	 */
	public static boolean hasAttributeDependencies(Object obj) {
		if (obj != null && obj instanceof QuestionDef) {
			QuestionDef qd = (QuestionDef) obj;
			if (!qd.isAsAttribute()) { // can't add attributes to attributes...
				for (PageDef pd : qd.getParentFormDef().getPages()) {
					if (hasAttributeDependencies(qd, pd.getQuestions())) {
						return true;
					}
				}
			}
		}
		return false;
	}

	private static boolean hasAttributeDependencies(QuestionDef qd, Vector<QuestionDef> qds) {
		if (qds != null) {
			for (QuestionDef child : qds) {
				if (!child.equals(qd)) {
					if (child.isAsAttribute() && child.getAttributeBinding() != null) {
						if (child.getAttributeBinding().startsWith(qd.getFullBinding())) {
							return true;
						}
					}
				}
				if (QuestionDef.QTN_TYPE_REPEAT == child.getDataType() || QuestionDef.QTN_TYPE_GROUP == child.getDataType()) {
					return hasAttributeDependencies(qd, child.getGroupQtnsDef().getQuestions());
				}
			}
		}
		return false;
	}

	/**
	 * @param fullBindingOriginal
	 * @param qd The questiondef that has been changed
	 */
	public static void updateAttributeDependencyNames(String fullBindingOriginal, QuestionDef qd) {
		if (fullBindingOriginal == null || qd == null || fullBindingOriginal.equals(qd.getFullBinding())) { return; }
		
		for (PageDef pd : qd.getParentFormDef().getPages()) {
			updateAttributeDependencyNames(fullBindingOriginal, qd.getFullBinding(), pd.getQuestions());
		}
	}

	private static void updateAttributeDependencyNames(String fullBindingOriginal, String fullBindingNew, Vector<QuestionDef> qds) {
		if (qds != null) {
			for (QuestionDef child : qds) {
				if (child.isAsAttribute() && child.getAttributeBinding() != null) {
					if (child.getAttributeBinding().equals(fullBindingOriginal)) {
						child.setAttributeBinding(fullBindingNew);
					} else if (child.getAttributeBinding().startsWith(fullBindingOriginal + "/")) { // must include slash or we might get false positives.
						child.setAttributeBinding(child.getAttributeBinding().replaceFirst(fullBindingOriginal + "/", fullBindingNew + "/"));
					}
				} else if (QuestionDef.QTN_TYPE_REPEAT == child.getDataType() || QuestionDef.QTN_TYPE_GROUP == child.getDataType()) {
					updateAttributeDependencyNames(fullBindingOriginal, fullBindingNew, child.getGroupQtnsDef().getQuestions());
				}
			}
		}
	}
	
	/**
	 * Does some extra sanity checks to see if formdef is valid.
	 * @param formDef
	 * @return
	 */
	public static boolean isFormDefValid(FormDef formDef) {
		if (formDef != null) {
			// -- Test QuestionDefs
			List<String> errors = new ArrayList<String>();
			for (PageDef pd : formDef.getPages()) {
				for (QuestionDef qd : pd.getQuestions()) {
					errors.addAll(validateQuestionDef(qd));
				}
			}
			
			// -- Show Response
			if (errors.size() > 0) {
				showErrors(errors);
				return false;
			}
		}
		return true;
	}
	
	private static void showErrors(List<String> errors) {
		StringBuilder sb = new StringBuilder();
		sb.append("Er zijn fouten gevonden bij het valideren van het formulier:\n\n");
		for (String err : errors) {
			sb.append("- " + err + "\n");
		}
		Window.alert(sb.toString());
	}

	public static boolean isWidgetsValid(FormDef formDef, List<DesignWidgetWrapper> widgets) {
		if (formDef != null) {
			Set<String> bindings = getBindings(formDef);
			
			List<String> errors = checkWidgetsValid(bindings, widgets);
			if (!errors.isEmpty()) { 
				showErrors(errors);
				return false;
			} else {
				return true;
			}
		}
		return true;
	}
	
	private static List<String> checkWidgetsValid(Set<String> bindings, List<DesignWidgetWrapper> widgets) {
		List<String> errors = new ArrayList<String>();

		// -- check bindings of widgets
		for (DesignWidgetWrapper widget : widgets) {
			if (widget.getBinding() != null && !(widget.getWrappedWidgetEx() instanceof Button)) {
				// this is a bit fishy but all widgets always have a binding even if there is no question mapped, (something like: LEFT27pxTOP477px)
				if (!widget.getBinding().startsWith("LEFT")) {
					String binding = (widget.getParentBinding() == null ? "" : widget.getParentBinding() + ".") + widget.getBinding();
					if (!bindings.contains(binding)) {
						errors.add(stringFormat(VALIDATION_ERROR_INVALID_QUESTION_BINDING, widget.getWidgetName(), widget.getText(), binding));
					}
				}
			}
			
			if (widget.getWrappedWidgetEx() instanceof DesignGroupWidget) {
				DesignGroupWidget group = (DesignGroupWidget) widget.getWrappedWidgetEx();
				errors.addAll(checkWidgetsValid(bindings, group.getDesignWidgetWrappers()));
			}
		}
		
		return errors;
	}
	
	/**
	 * Contains Questions and Options. not fully qualified.
	 *  
	 * @param formDef
	 * @return
	 */
	public static Set<String> getBindings(FormDef formDef) {
		Set<String> names = new HashSet<String>();
		
		for (PageDef page : formDef.getPages()) {
			names.addAll(getBindings(page.getQuestions()));
		}
		
		return names;
	}
	
	private static Set<String> getBindings(Collection<QuestionDef> questions) {
		Set<String> names = new HashSet<String>();
		
		for (QuestionDef question : questions) {
			names.add(question.getBinding());
			if (question.getDataType() == QuestionDef.QTN_TYPE_REPEAT || question.getDataType() == QuestionDef.QTN_TYPE_GROUP) {
				names.addAll(getBindings(question.getGroupQtnsDef().getQuestions()));
				
			} else if (question.getDataType() == QuestionDef.QTN_TYPE_LIST_EXCLUSIVE || question.getDataType() == QuestionDef.QTN_TYPE_LIST_MULTIPLE) {
				for (Object option : question.getOptions()) {
					if (option instanceof OptionDef) {
						names.add(question.getBinding() + "." + ((OptionDef) option).getBinding());
					} else {
						Window.alert("Lijst Vraag bevat geen opties? '" + question.getFullBinding() + "'");
					}
				}
			}
		}
		
		return names;
	}
	
	/**
	 * Returns a list of errors.
	 * @param qd
	 * @return
	 */
	public static List<String> validateQuestionDef(QuestionDef qd) {
		List<String> errors = new ArrayList<String>();
		if (qd != null) {
			if (qd.getBareBinding() == null || "".equals(qd.getBareBinding().trim())) {
				errors.add(stringFormat(VALIDATION_ERROR_PROPERTY_NOT_SET, "Binding", qd.getFullText()));
			}
			if (qd.getText() == null || "".equals(qd.getText().trim())) {
				errors.add(stringFormat(VALIDATION_ERROR_PROPERTY_NOT_SET, "Text", qd.getFullBinding()));
			}
			if (qd.isAsAttribute() && (qd.getAttributeBinding() == null || "".equals(qd.getAttributeBinding().trim()))) {
				errors.add(stringFormat(VALIDATION_ERROR_DEPENDENT_PROPERTY_NOT_SET, "Als attribuut", "Attribuut parent binding", qd.getFullText()));
			}
			if (qd.isLocked() && qd.isRequired() && (qd.getDefaultValue() == null || "".equals(qd.getDefaultValue().trim()))) {
				errors.add(stringFormat(VALIDATION_ERROR_DEPENDENT_PROPERTY_NOT_SET, "Locked' en 'Required", "Default Value", qd.getFullText()));
			}
			
			if (QuestionDef.QTN_TYPE_GROUP == qd.getDataType() || QuestionDef.QTN_TYPE_REPEAT == qd.getDataType()) {
				
				if (qd.getGroupQtnsDef() != null && qd.getGroupQtnsDef().getQuestions() != null && qd.getGroupQtnsDef().getQuestions().size() > 0) {
					
					for (QuestionDef child : qd.getGroupQtnsDef().getQuestions()) {
						// -- check children
						errors.addAll(validateQuestionDef(child));
						// -- children must not be groups						
						if (QuestionDef.QTN_TYPE_GROUP == child.getDataType() || QuestionDef.QTN_TYPE_REPEAT == child.getDataType()) {
							errors.add(stringFormat(VALIDATION_ERROR_GROUPREPEAT_CANNOT_NEST, child.getFullText()));
						}
					}

				} else {
					errors.add(stringFormat(VALIDATION_ERROR_GROUP_NOCHILDREN, qd.getFullText()));
				}
				
			} else if (QuestionDef.QTN_TYPE_LIST_EXCLUSIVE == qd.getDataType() || QuestionDef.QTN_TYPE_LIST_MULTIPLE == qd.getDataType()) {
				if (qd.getOptions() == null || qd.getOptions().isEmpty()) {
					errors.add(stringFormat(VALIDATION_ERROR_LIST_NOCHILDREN, qd.getFullText()));
				}
			}
			
			if (!isValidDefaultValue(qd)) {
				errors.add(stringFormat(VALIDATION_ERROR_INVALID_DEFAULT_VALUE, qd.getFullText()));
			}
		}
		return errors;
	}
	
	private static final String VALIDATION_ERROR_GROUP_NOCHILDREN = "De groep of repeat vraag '{}' bevat geen inner vragen. (voeg minstens één vraag toe of verwijder de groep)";
	private static final String VALIDATION_ERROR_LIST_NOCHILDREN = "De lijst vraag '{}' bevat geen opties. (voeg minstens één optie toe of verwijder de lijst)";
	private static final String VALIDATION_ERROR_PROPERTY_NOT_SET = "De eigenschap '{}' van de vraag '{}' is niet gezet. (deze eigenschap is verplicht)";
	private static final String VALIDATION_ERROR_DEPENDENT_PROPERTY_NOT_SET = "Indien de eigenschap '{}' gezet is dan moet de eigenschap '{}' ook gezet zijn (vraag: '{}')";
	private static final String VALIDATION_ERROR_INVALID_DEFAULT_VALUE = "De standaard waarde van de vraag '{}' is niet geldig voor het gekozen datatype.";
	private static final String VALIDATION_ERROR_GROUPREPEAT_CANNOT_NEST = "Groepen / Repeats mogen niet genest worden (eg. geen groepen in groepen) (vraag: '{}')";
	
	private static final String VALIDATION_ERROR_INVALID_QUESTION_BINDING = "Widget verwijst naar een niet bestaande vraag (Type: '{}', Widget: '{}', vraag: '{}')";
	
	
	/**
	 * Alternative for String.format(...).
	 * Not very fast or good... just for your convenience.
	 * @param format
	 * @param objs
	 * @return
	 */
	public static String stringFormat(String format, Object... objs) {
		if (format == null || objs == null || objs.length == 0) { return format; }
		
		String res = format;
		for (Object o : objs) {
			if (o != null) {
				res = res.replaceFirst("\\{\\}", o.toString());
			}
		}
		
		return res;
	}
	
	private static final String DATE_DEF_VAL_FUNC_DATE = "date()";
	private static final String DATE_DEF_VAL_FUNC_NOW = "now()";

	private static boolean isValidDefaultValue(QuestionDef qd) {
		String val = qd.getDefaultValue();
		if (val == null || "".equals(val.trim())) { return true; }
		
		if (QuestionDef.QTN_TYPE_DECIMAL == qd.getDataType()) {
			try {
				Double.valueOf(val);
			} catch (Exception e) {
				return false;
			}
		} else if (QuestionDef.QTN_TYPE_NUMERIC == qd.getDataType()) {
			try {
				Long.valueOf(val);
			} catch (Exception e) {
				return false;
			}
		} else if (QuestionDef.QTN_TYPE_TIME == qd.getDataType()) {
			try {
				if (DATE_DEF_VAL_FUNC_DATE.equalsIgnoreCase(val) || DATE_DEF_VAL_FUNC_NOW.equals(val)) { return true; }
				FormUtil.getTimeSubmitFormat().parse(val);
			} catch (Exception e) {
				return false;
			}
		} else if (QuestionDef.QTN_TYPE_DATE == qd.getDataType()) {
			try {
				if (DATE_DEF_VAL_FUNC_DATE.equalsIgnoreCase(val) || DATE_DEF_VAL_FUNC_NOW.equals(val)) { return true; }
				FormUtil.getDateSubmitFormat().parse(val);
			} catch (Exception e) {
				return false;
			}
		} else if (QuestionDef.QTN_TYPE_DATE_TIME == qd.getDataType()) {
			try {
				if (DATE_DEF_VAL_FUNC_DATE.equalsIgnoreCase(val) || DATE_DEF_VAL_FUNC_NOW.equals(val)) { return true; }
				FormUtil.getDateTimeSubmitFormat().parse(val);
			} catch (Exception e) {
				return false;
			}
		}  else if (QuestionDef.QTN_TYPE_LIST_EXCLUSIVE == qd.getDataType() || QuestionDef.QTN_TYPE_LIST_MULTIPLE == qd.getDataType()) {
			if (qd.getOptions() == null) { return false; }
			String[] opts = val.split(" ");
			for (String defOp : opts) {
				if (qd.getOptionWithValue(defOp) == null) {	return false; }
			}
		}
		return true;
	}
}

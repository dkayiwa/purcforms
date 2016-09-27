package org.purc.purcforms.client.locale;

import com.google.gwt.i18n.client.Dictionary;

import java.util.HashMap;
import java.util.Map;


/**
 * Used for getting localized text messages.
 * This method of setting localized messages as a javascript object is the html host file
 * has been chosen because it does not require the form designer to be compiled into
 * various languages, which would be required if we had used the other method of
 * localization in GWT. The html host file holding the widget will always have text for
 * one locale. When the user switched to another locale, the page has to be reloaded such
 * that the server replaces this text with that of the new locale.
 *
 * @author daniel
 */
public class LocaleText {

	/**
	 * The dictionary having all localized text.
	 */
//    private static Dictionary purcformsText = Dictionary.getDictionary("PurcformsText");

	private static Map<String, String> purcformsText = null;


	/**
	 * Gets the localized text for a given key.
	 *
	 * @param key the key
	 * @return the localized text.
	 */
	public static String get(String key) {
		if (purcformsText == null) {
			initDic();
		}
		return purcformsText.get(key);
	}

	//
	private static void initDic() {

		purcformsText = new HashMap<String, String>();

		purcformsText.put("file", "File");
		purcformsText.put("view", "View");
		purcformsText.put("item", "Item");
		purcformsText.put("tools", "Tools");
		purcformsText.put("help", "Help");
		purcformsText.put("newItem", "New");
		purcformsText.put("open", "Open");
		purcformsText.put("save", "Save");
		purcformsText.put("saveAs", "Save As");

		purcformsText.put("openLayout", "Open Layout");
		purcformsText.put("saveLayout", "Save Layout");
		purcformsText.put("openLanguageText", "Open Language Text");
		purcformsText.put("saveLanguageText", "Save Language Text");
		purcformsText.put("close", "Close");

		purcformsText.put("refresh", "Refresh");
		purcformsText.put("addNew", "Add New");
		purcformsText.put("addNewChild", "Add New Child");
		purcformsText.put("deleteSelected", "Delete Selected");
		purcformsText.put("moveUp", "Move Up");
		purcformsText.put("moveDown", "Move Down");
		purcformsText.put("cut", "Cut");
		purcformsText.put("copy", "Copy");
		purcformsText.put("paste", "Paste");

		purcformsText.put("format", "Format");
		purcformsText.put("languages", "Languages");
		purcformsText.put("options", "Options");

		purcformsText.put("helpContents", "Help Contents");
		purcformsText.put("about", "About");

		purcformsText.put("forms", "Forms");
		purcformsText.put("widgetProperties", "Widget Properties");
		purcformsText.put("properties", "Properties");
		purcformsText.put("xformsSource", "Xforms Source");
		purcformsText.put("designSurface", "Design Surface");
		purcformsText.put("layoutXml", "Layout Xml");
		purcformsText.put("languageXml", "Language Xml");
		purcformsText.put("preview", "Preview");
		purcformsText.put("modelXml", "Model Xml");

		purcformsText.put("text", "Text");
		purcformsText.put("helpText", "Help Text");
		purcformsText.put("type", "Type");
		purcformsText.put("binding", "Binding");
		purcformsText.put("visible", "Visible");
		purcformsText.put("enabled", "Enabled");
		purcformsText.put("locked", "Locked");
		purcformsText.put("required", "Required");
		purcformsText.put("defaultValue", "Default Value");
		purcformsText.put("descriptionTemplate", "Description Template");

		purcformsText.put("language", "Language");
		purcformsText.put("skipLogic", "Skip Logic");
		purcformsText.put("validationLogic", "Validation Logic");
		purcformsText.put("dynamicLists", "Dynamic Lists");

		purcformsText.put("valuesFor", "Values for");
		purcformsText.put("whenAnswerFor", "when the answer for ");
		purcformsText.put("isEqualTo", "is equal t ");
		purcformsText.put("forQuestion", "For question ");
		purcformsText.put("enable", "Enable");
		purcformsText.put("disable", "Disable");
		purcformsText.put("show", "Show");
		purcformsText.put("hide", "Hide");
		purcformsText.put("makeRequired", "Make Required");

		purcformsText.put("when", "When ");
		purcformsText.put("ofTheFollowingApply", "of the following apply");
		purcformsText.put("all", "all");
		purcformsText.put("any", "any");
		purcformsText.put("none", "none");
		purcformsText.put("notAll", "not all");

		purcformsText.put("addNewCondition", "Click here to add new condition");

		purcformsText.put("isEqualTo", "is equal to");
		purcformsText.put("isNotEqual", "is not equal to");
		purcformsText.put("isLessThan", "is less than");
		purcformsText.put("isLessThanOrEqual", "is less than or equal to");
		purcformsText.put("isGreaterThan", "is greater than");
		purcformsText.put("isGreaterThanOrEqual", "is greater than or equal to");
		purcformsText.put("isNull", "is null");
		purcformsText.put("isInList", "is in list");
		purcformsText.put("isNotInList", "is not in list");
		purcformsText.put("startsWith", "starts with");
		purcformsText.put("doesNotStartWith", "does not start with");
		purcformsText.put("contains", "contains");
		purcformsText.put("doesNotContain", "does not contain");
		purcformsText.put("isBetween", "is between");
		purcformsText.put("isNotBetween", "is not between");

		purcformsText.put("isValidWhen", "is valid when");
		purcformsText.put("errorMessage", "Error Message");
		purcformsText.put("question", "Question");

		purcformsText.put("addField", "Add Field");
		purcformsText.put("submit", "Submit");
		purcformsText.put("addWidget", "Add Widget");
		purcformsText.put("newTab", "New Tab");
		purcformsText.put("deleteTab", "Delete Tab");
		purcformsText.put("selectAll", "Select All");
		purcformsText.put("load", "Load");

		purcformsText.put("label", "Label");
		purcformsText.put("textBox", "TextBox");
		purcformsText.put("checkBox", "CheckBox");
		purcformsText.put("radioButton", "RadioButton");
		purcformsText.put("dropdownList", "DropdownList");
		purcformsText.put("textArea", "TextArea");
		purcformsText.put("button", "Button");
		purcformsText.put("datePicker", "Date Picker");
		purcformsText.put("groupBox", "Group Box");
		purcformsText.put("listBox", "ListBox");
		purcformsText.put("repeatSection", "Repeat Section");
		purcformsText.put("picture", "Picture");
		purcformsText.put("videoAudio", "Video/Audio");

		purcformsText.put("deleteWidgetPrompt", "Do you really want to delete the selected Widget (s)?");
		purcformsText.put("deleteTreeItemPrompt", "Do you really want to delete the selected item and all its children (if any) ?");
		purcformsText.put("selectDeleteItem", "Please first select the item to delete");

		purcformsText.put("selectedPage", "The selected page [");
		purcformsText.put("shouldNotSharePageBinding", "] should not share the same page number/binding with page [");
		purcformsText.put("selectedQuestion", "The selected question [");
		purcformsText.put("shouldNotShareQuestionBinding", "] should not share the same binding with question [");
		purcformsText.put("selectedOption", "The selected option [");
		purcformsText.put("shouldNotShareOptionBinding", "] should not share the same binding with option [");
		purcformsText.put("newForm", "New Form");
		purcformsText.put("page", "Page");
		purcformsText.put("option", "Option");
		purcformsText.put("noDataFound", "No data found.");

		purcformsText.put("formSaveSuccess", "Form saved successfully");
		purcformsText.put("selectSaveItem", "Please select the item to save.");
		purcformsText.put("deleteAllWidgetsFirst", "Please first delete all the widgets.");
		purcformsText.put("deleteAllTabWidgetsFirst", "This tab has one or more widgets, please first delete them");
		purcformsText.put("cantDeleteAllTabs", "This tab cannot be deleted because there should be atleast one tab.");
		purcformsText.put("noFormId", "No formId or ");
		purcformsText.put("divFound", " div found");
		purcformsText.put("noFormLayout", "No form layout found. Please first design and save the form.");
		purcformsText.put("formSubmitSuccess", "Form Data Submitted Successfully");
		purcformsText.put("missingDataNode", "Missing data node for ");

		purcformsText.put("openingForm", "Opening Form");
		purcformsText.put("openingFormLayout", "Opening Form Layout");
		purcformsText.put("savingForm", "Saving Form");
		purcformsText.put("savingFormLayout", "Saving Form Layout");
		purcformsText.put("refreshingForm", "Refresing Form");
		purcformsText.put("translatingFormLanguage", "Translating Form Language");
		purcformsText.put("savingLanguageText", "Saving Language Text");
		purcformsText.put("refreshingDesignSurface", "Refreshing Design Surface");
		purcformsText.put("loadingDesignSurface", "Loading Design Surface");
		purcformsText.put("refreshingPreview", "Refreshing Preview");

		purcformsText.put("count", "Count");
		purcformsText.put("clickToPlay", "Click to play");
		purcformsText.put("loadingPreview", "Loading Preview");
		purcformsText.put("unexpectedFailure", "Unexpected Failure");
		purcformsText.put("uncaughtException", "Uncaught exception ");
		purcformsText.put("causedBy", "Caused by ");
		purcformsText.put("openFile", "Open File");
		purcformsText.put("saveFileAs", "Save File As");

		purcformsText.put("alignLeft", "Align Left");
		purcformsText.put("alignRight", "Align Right");
		purcformsText.put("alignTop", "Align Top");
		purcformsText.put("alignBottom", "Align Bottom");
		purcformsText.put("makeSameWidth", "Make Same Width");
		purcformsText.put("makeSameHeight", "Make Same Height");
		purcformsText.put("makeSameSize", "Make Same Size");
		purcformsText.put("layout", "Layout");
		purcformsText.put("deleteTabPrompt", "Do you really want to delete this tab?");

		purcformsText.put("text", "Text");
		purcformsText.put("toolTip", "Tooltip");
		purcformsText.put("childBinding", "Child Binding");
		purcformsText.put("width", "Width");
		purcformsText.put("height", "Height");
		purcformsText.put("left", "Left");
		purcformsText.put("top", "Top");
		purcformsText.put("tabIndex", "Tab Index");
		purcformsText.put("repeat", "Repeat");
		purcformsText.put("externalSource", "External Source");
		purcformsText.put("displayField", "Display Field");
		purcformsText.put("valueField", "Value Field");
		purcformsText.put("fontFamily", "Font Family");
		purcformsText.put("foreColor", "Fore Color");
		purcformsText.put("fontWeight", "Font Weight");
		purcformsText.put("fontStyle", "Font Style");
		purcformsText.put("fontSize", "Font Size");
		purcformsText.put("textDecoration", "Text Decoration");
		purcformsText.put("textAlign", "Text Align");
		purcformsText.put("backgroundColor", "Background Color");
		purcformsText.put("borderStyle", "Border Style");
		purcformsText.put("borderWidth", "Border Width");
		purcformsText.put("borderColor", "Border Color");
		purcformsText.put("aboutMessage", "This is a form designer widget based on GWT");
		purcformsText.put("more", "More");
		purcformsText.put("requiredErrorMsg", "Please answer this required question.");
		purcformsText.put("questionTextDesc", "The question text.");
		purcformsText.put("questionDescDesc", "The question description.");
		purcformsText.put("questionIdDesc", "The question internal identifier. For Questions, it should be a valid xml node name.");
		purcformsText.put("defaultValDesc", "The default value or answer");
		purcformsText.put("questionTypeDesc", "The type of question or type of expected answers.");

		purcformsText.put("qtnTypeText", "Text");
		purcformsText.put("qtnTypeNumber", "Number");
		purcformsText.put("qtnTypeDecimal", "Decimal");
		purcformsText.put("qtnTypeDate", "Date");
		purcformsText.put("qtnTypeTime", "Time");
		purcformsText.put("qtnTypeDateTime", "Date and Time");
		purcformsText.put("qtnTypeBoolean", "Boolean");
		purcformsText.put("qtnTypeSingleSelect", "Single Select");
		purcformsText.put("qtnTypeMultSelect", "Multiple Select");
		purcformsText.put("qtnTypeRepeat", "Repeat");
		purcformsText.put("qtnTypePicture", "Picture");
		purcformsText.put("qtnTypeVideo", "Video");
		purcformsText.put("qtnTypeAudio", "Audio");
		purcformsText.put("qtnTypeSingleSelectDynamic", "Single Select Dynamic");
		purcformsText.put("deleteCondition", "Delete Condition");
		purcformsText.put("addCondition", "Add Condition");
		purcformsText.put("value", "Value");
		purcformsText.put("questionValue", "Question value");
		purcformsText.put("and", "and");
		purcformsText.put("deleteItemPrompt", "Do you really want to delete this item?");
		purcformsText.put("changeWidgetTypePrompt", "Do you really want to change to this type and lose all the options created, if any?");
		purcformsText.put("removeRowPrompt", "Do you really want to remove this row?");
		purcformsText.put("remove", "Remove");
		purcformsText.put("browse", "Browse");
		purcformsText.put("clear", "Clear");
		purcformsText.put("deleteItem", "Delete");
		purcformsText.put("cancel", "Cancel");
		purcformsText.put("clickToAddNewCondition", "< Click here to add new condition >");
		purcformsText.put("qtnTypeGPS", "GPS");
		purcformsText.put("palette", "Palette");
		purcformsText.put("saveAsXhtml", "Save As XHTML");
		purcformsText.put("groupWidgets", "Group Widgets");
		purcformsText.put("action", "Action");
		purcformsText.put("submitting", "Submitting");
		purcformsText.put("authenticationPrompt", "Please enter your user name and password");
		purcformsText.put("invalidUser", "Invalid UserName or Password");
		purcformsText.put("login", "Login");
		purcformsText.put("userName", "User Name");
		purcformsText.put("password", "Password");
		purcformsText.put("noSelection", "No Selection");
		purcformsText.put("cancelFormPrompt", "Do you really want to Cancel and Discard changes on this form?");
		purcformsText.put("print", "Print");
		purcformsText.put("yes", "Yes");
		purcformsText.put("no", "No");
		purcformsText.put("searchServer", "Search Server");
		purcformsText.put("recording", "Recording");
		purcformsText.put("search", "Search");
		purcformsText.put("processingMsg", "Please wait while processing...");
		purcformsText.put("length", "Length");
		purcformsText.put("clickForOtherQuestions", "< Click here for other questions >");
		purcformsText.put("ok", "Ok");
		purcformsText.put("loading", "loading");
		purcformsText.put("allQuestions", "All Questions");
		purcformsText.put("selectedQuestions", "Selected Questions");
		purcformsText.put("otherQuestions", "Other Questions");

	}
}

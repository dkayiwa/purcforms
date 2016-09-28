// Copyright (C) 2011-2012 RWO, http://rwo.vlaanderen.be/
// All rights reserved

package org.purc.purcforms.client.widget;

import org.purc.purcforms.client.util.FormUtil;
import org.purc.purcforms.client.widget.richtexttoolbar.RichTextToolbar;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.logical.shared.InitializeEvent;
import com.google.gwt.event.logical.shared.InitializeHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.Focusable;
import com.google.gwt.user.client.ui.HasEnabled;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.RichTextArea;
import com.google.gwt.user.client.ui.Widget;

/**
 * Textarea with toolbar + some extra interfaces (hasValue, HasText, Focusable, HasEnabled)
 * 
 * @author Kristof Heirwegh
 */
public class RichTextAreaWidget extends Composite implements HasValue<String>, HasText, Focusable, HasEnabled, HasReadonly {

	private static final MyUiBinder UIBINDER = GWT.create(MyUiBinder.class);
	interface MyUiBinder extends UiBinder<Widget, RichTextAreaWidget> {}
	
	private static final String TEXTAREA_STYLE = "body {font-size: 13px; font-family: 'Trebuchet MS', Arial, Sans-Serif;}";
	private static final String TOOLBAR_HEIGHT= "31px";
	
	private static String readOnlyStyle = "readonly";
	private static String disabledStyle = "disabled";
	
	@UiField(provided = true)
	protected RichTextArea textArea;

	@UiField(provided = true)
	protected RichTextToolbar toolBar;
	
	@UiField
	protected DockLayoutPanel container;
	
	private boolean enabled = true;

	public RichTextAreaWidget() {
		super();
		textArea = new RichTextArea();
		textArea.addInitializeHandler(new InitializeHandler() {
			@Override
			public void onInitialize(InitializeEvent event) {
				try {
					// set some styling
					Element head = getHeadElement(textArea.getElement());
				    Element style = DOM.createElement("style");
				    style.setInnerText(TEXTAREA_STYLE);
				    head.appendChild(style);
				} catch (Exception e) {
					GWT.log("RichTextAreaWidget: fout bij zetten stijl inner iframe");
				}
			}
		});
		toolBar = new RichTextToolbar(textArea, false, false, false);
		initWidget(UIBINDER.createAndBindUi(this));
		textArea.addKeyDownHandler(new KeyDownHandler() {
			@Override
			public void onKeyDown(KeyDownEvent event) {
				if (!enabled || isReadOnly()) {
					event.preventDefault();
				}
			}
		});

		if (FormUtil.isReadOnlyMode()) {
			setEnabled(false);
			setReadOnly(true);
		}
	}

	private static native Element getHeadElement(Element iframe) /*-{
		return iframe.contentWindow.document.head;
	}-*/;

	// ---------------------------------------------------

	@Override
	public HandlerRegistration addValueChangeHandler(ValueChangeHandler<String> handler) {
		return textArea.addHandler(handler, ValueChangeEvent.getType());
	}

	@Override
	public String getValue() {
		String val = textArea.getHTML();
		if (val == null || "".equals(val.trim()) || "<br>".equals(val.trim())) {
			return null;
		} else {
			return val;
		}
	}

	@Override
	public void setValue(String value) {
		textArea.setHTML(value);
	}

	@Override
	public void setValue(String value, boolean fireEvents) {
		String oldVal = textArea.getHTML();
		if (!(oldVal == null && value == null) || (oldVal != null && !oldVal.equals(value))) {
			textArea.setHTML(value);
			ValueChangeEvent.fire(this, value);
		}
	}
	
	@Override
	public boolean isReadOnly() {
		return DOM.getElementPropertyBoolean(getElement(), "readOnly");
	}
	
	@Override
	public void setReadOnly(boolean readOnly) {
		enabled = !readOnly;
		textArea.setEnabled(!readOnly);
		
		DOM.setElementPropertyBoolean(getElement(), "readOnly", readOnly);
		
		if (readOnly) {
			addStyleDependentName(readOnlyStyle);
			container.setWidgetHidden(toolBar, true);
		} else {
			removeStyleDependentName(readOnlyStyle);
			container.setWidgetHidden(toolBar, false);
		}
	}

	// ---------------------------------------------------
	
	@Override
	public int getTabIndex() {
		return textArea.getTabIndex();
	}

	@Override
	public void setAccessKey(char key) {
		textArea.setAccessKey(key);
	}

	@Override
	public void setFocus(boolean focused) {
		textArea.setFocus(enabled && focused);
	}

	@Override
	public void setTabIndex(int index) {
		textArea.setTabIndex(index);
	}

	@Override
	public boolean isEnabled() {
		return textArea.isEnabled();
	}

	@Override
	public void setEnabled(boolean enabled) {
		textArea.setEnabled(enabled);
		toolBar.setEnabled(enabled);

		this.enabled = enabled;
		if (!enabled) {
			textArea.setFocus(false);
			textArea.addStyleDependentName(disabledStyle);
		} else {
			textArea.removeStyleDependentName(disabledStyle);
		}
	}

	@Override
	public String getText() {
		return getValue();
	}

	@Override
	public void setText(String text) {
		setValue(text);
	}
}
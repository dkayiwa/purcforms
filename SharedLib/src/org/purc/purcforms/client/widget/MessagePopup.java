package org.purc.purcforms.client.widget;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.HeadingElement;
import com.google.gwt.dom.client.TableCellElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

public class MessagePopup extends PopupPanel {

	/**
	 * UI binder interface for the MessageBox content.
	 * 
	 * @author Pieter De Graef
	 */
	interface MyUiBinder extends UiBinder<Widget, MessagePopup> {}

	private static final MyUiBinder UIBINDER = GWT.create(MyUiBinder.class);

	public enum MessageType {
		INFO, HELP, WARN, ERROR
	}

	private MessageType messageType = MessageType.INFO;

	@UiField
	protected HeadingElement titleElement;

	@UiField
	protected TableCellElement messageElement;

	@UiField
	protected SimplePanel closeBtn;

	// ------------------------------------------------------------------------
	// Constructor:
	// ------------------------------------------------------------------------

	/** Create a new instance without setting the text. The default message type is "INFO". */
	public MessagePopup() {
		this(false);
	}

	public MessagePopup(boolean autoHide) {
		super(autoHide);
		setAnimationEnabled(true);
		setWidget(UIBINDER.createAndBindUi(this));
		setMessageType(MessageType.INFO);

		closeBtn.getElement().setInnerText("x");
		closeBtn.addDomHandler(new ClickHandler() {

			public void onClick(ClickEvent event) {
				MessagePopup.this.hide();
			}
		}, ClickEvent.getType());
	}

	// ------------------------------------------------------------------------
	// Getters and setters:
	// ------------------------------------------------------------------------

	public void setTitle(String title) {
		titleElement.setInnerText(title);
	}

	public String getTitle() {
		return titleElement.getInnerText();
	}

	/**
	 * Set the text to be displayed within this message widget.
	 * 
	 * @param message
	 *            The text to display.
	 */
	public void setText(String message) {
		messageElement.setInnerHTML(message);
	}

	/**
	 * Get the text displayed within this message widget.
	 * 
	 * @return The text displayed.
	 */
	public String getText() {
		return messageElement.getInnerHTML();
	}

	/**
	 * Apply the message type. Setting the type will alter the style.
	 * 
	 * @param messageType
	 *            The new type of message.
	 */
	public void setMessageType(MessageType messageType) {
		this.messageType = messageType;
		switch (messageType) {
			case HELP:
				addStyleName("message-help");
				removeStyleName("message-warn");
				removeStyleName("message-error");
				removeStyleName("message-info");
				break;
			case WARN:
				addStyleName("message-warn");
				removeStyleName("message-help");
				removeStyleName("message-error");
				removeStyleName("message-info");
				break;
			case ERROR:
				addStyleName("message-error");
				removeStyleName("message-warn");
				removeStyleName("message-help");
				removeStyleName("message-info");
				break;
			default:
				addStyleName("message-info");
				removeStyleName("message-warn");
				removeStyleName("message-error");
				removeStyleName("message-help");
		}
	}

	/**
	 * Get the type of message displayed in this widget.
	 * 
	 * @return The type of message displayed in this widget.
	 */
	public MessageType getMessageType() {
		return messageType;
	}
}
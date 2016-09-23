/*
 * Copyright 2008 Google Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.purc.purcforms.client.widget.richtexttoolbar;

import org.purc.purcforms.client.i18n.RichTextToolbarMessages;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasEnabled;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.RichTextArea;
import com.google.gwt.user.client.ui.ToggleButton;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * A sample toolbar for use with {@link RichTextArea}. It provides a simple UI for all rich text formatting, dynamically
 * displayed only for the available functionality.
 */
@SuppressWarnings("deprecation")
public class RichTextToolbar extends Composite implements HasEnabled {
	
	private boolean enabled = true;

	/**
	 * We use an inner EventHandler class to avoid exposing event methods on the RichTextToolbar itself.
	 */
	private class EventHandler implements ClickHandler, ChangeHandler, KeyUpHandler {
		@Override
		public void onChange(ChangeEvent event) {
			Widget sender = (Widget) event.getSource();

			if (sender == backColors) {
				basic.setBackColor(backColors.getValue(backColors.getSelectedIndex()));
				backColors.setSelectedIndex(0);
			} else if (sender == foreColors) {
				basic.setForeColor(foreColors.getValue(foreColors.getSelectedIndex()));
				foreColors.setSelectedIndex(0);
			} else if (sender == fonts) {
				basic.setFontName(fonts.getValue(fonts.getSelectedIndex()));
				fonts.setSelectedIndex(0);
			} else if (sender == fontSizes) {
				basic.setFontSize(fontSizesConstants[fontSizes.getSelectedIndex() - 1]);
				fontSizes.setSelectedIndex(0);
			}
		}

		@Override
		public void onClick(ClickEvent event) {
			if (enabled) {
				Widget sender = (Widget) event.getSource();
	
				if (sender == bold) {
					basic.toggleBold();
				} else if (sender == italic) {
					basic.toggleItalic();
				} else if (sender == underline) {
					basic.toggleUnderline();
				} else if (sender == subscript) {
					basic.toggleSubscript();
				} else if (sender == superscript) {
					basic.toggleSuperscript();
				} else if (sender == strikethrough) {
					extended.toggleStrikethrough();
				} else if (sender == indent) {
					extended.rightIndent();
				} else if (sender == outdent) {
					extended.leftIndent();
				} else if (sender == justifyLeft) {
					basic.setJustification(RichTextArea.Justification.LEFT);
				} else if (sender == justifyCenter) {
					basic.setJustification(RichTextArea.Justification.CENTER);
				} else if (sender == justifyRight) {
					basic.setJustification(RichTextArea.Justification.RIGHT);
				} else if (sender == insertImage) {
					String result = Window.prompt(MSG.enterImageUrl(), "http://");
					if (result != null) {
						extended.insertImage(result);
					}
				} else if (sender == createLink) {
					String result = Window.prompt(MSG.enterLinkUrl(), "http://");
					if (result != null) {
						extended.createLink(result);
					}
				} else if (sender == removeLink) {
					extended.removeLink();
				} else if (sender == hr) {
					extended.insertHorizontalRule();
				} else if (sender == ol) {
					extended.insertOrderedList();
				} else if (sender == ul) {
					extended.insertUnorderedList();
				} else if (sender == removeFormat) {
					extended.removeFormat();
				} else if (sender == richText) {
					// We use the RichTextArea's onKeyUp event to update the toolbar status.
					// This will catch any cases where the user moves the cursur using the
					// keyboard, or uses one of the browser's built-in keyboard shortcuts.
					updateStatus();
				}
			}
		}

		public void onKeyUp(KeyUpEvent event) {
			Widget sender = (Widget) event.getSource();
			if (sender == richText) {
				// We use the RichTextArea's onKeyUp event to update the toolbar status.
				// This will catch any cases where the user moves the cursur using the
				// keyboard, or uses one of the browser's built-in keyboard shortcuts.
				updateStatus();
			}
		}
	}

	private static final RichTextArea.FontSize[] fontSizesConstants = new RichTextArea.FontSize[] {
			RichTextArea.FontSize.XX_SMALL, RichTextArea.FontSize.X_SMALL, RichTextArea.FontSize.SMALL,
			RichTextArea.FontSize.MEDIUM, RichTextArea.FontSize.LARGE, RichTextArea.FontSize.X_LARGE,
			RichTextArea.FontSize.XX_LARGE };

	private ImageResources images = (ImageResources) GWT.create(ImageResources.class);

	private RichTextToolbarMessages MSG = (RichTextToolbarMessages) GWT.create(RichTextToolbarMessages.class);

	private EventHandler handler = new EventHandler();

	private RichTextArea richText;

	private RichTextArea.BasicFormatter basic;

	private RichTextArea.ExtendedFormatter extended;

	private VerticalPanel outer = new VerticalPanel();

	private HorizontalPanel topPanel = new HorizontalPanel();

	private HorizontalPanel bottomPanel = new HorizontalPanel();

	private ToggleButton bold;

	private ToggleButton italic;

	private ToggleButton underline;

	private ToggleButton subscript;

	private ToggleButton superscript;

	private ToggleButton strikethrough;

	private PushButton indent;

	private PushButton outdent;

	private PushButton justifyLeft;

	private PushButton justifyCenter;

	private PushButton justifyRight;

	private PushButton hr;

	private PushButton ol;

	private PushButton ul;

	private PushButton insertImage;

	private PushButton createLink;

	private PushButton removeLink;

	private PushButton removeFormat;

	private ListBox backColors;

	private ListBox foreColors;

	private ListBox fonts;

	private ListBox fontSizes;

	/**
	 * must have a parameterless constructor for uibinder.
	 * Don 't use it though... (eg. use '@uiFactory' or '@uiField(provided = true')
	 */
	public RichTextToolbar() {
	}
	
	/**
	 * Creates a new toolbar that drives the given rich text area.
	 * 
	 * @param richText
	 *            the rich text area to be controlled
	 */
	public RichTextToolbar(RichTextArea richText) {
		this(richText, true, true, true);
	}

	/**
	 * Creates a new toolbar that drives the given rich text area.
	 * 
	 * @param richText
	 *            the rich text area to be controlled
	 */
	public RichTextToolbar(RichTextArea richText, boolean showBottomRow, boolean showInsertLinks, boolean showInsertImage) {
		this.richText = richText;
		this.basic = richText.getBasicFormatter();
		this.extended = richText.getExtendedFormatter();

		outer.setWidth("100%");
		outer.add(topPanel);
		outer.add(bottomPanel);

		// topPanel.setWidth("100%");
		// bottomPanel.setWidth("100%");

		bottomPanel.setVisible(showBottomRow);

		initWidget(outer);
		setStyleName("gwt-RichTextToolbar");
		richText.addStyleName("hasRichTextToolbar");

		if (basic != null) {
			topPanel.add(bold = createToggleButton(images.bold(), MSG.bold()));
			topPanel.add(italic = createToggleButton(images.italic(), MSG.italic()));
			topPanel.add(underline = createToggleButton(images.underline(), MSG.underline()));
			topPanel.add(subscript = createToggleButton(images.subscript(), MSG.subscript()));
			topPanel.add(superscript = createToggleButton(images.superscript(), MSG.superscript()));
			topPanel.add(justifyLeft = createPushButton(images.justifyLeft(), MSG.justifyLeft()));
			topPanel.add(justifyCenter = createPushButton(images.justifyCenter(), MSG.justifyCenter()));
			topPanel.add(justifyRight = createPushButton(images.justifyRight(), MSG.justifyRight()));
		}

		if (extended != null) {
			topPanel.add(strikethrough = createToggleButton(images.strikeThrough(), MSG.strikeThrough()));
			topPanel.add(indent = createPushButton(images.indent(), MSG.indent()));
			topPanel.add(outdent = createPushButton(images.outdent(), MSG.outdent()));
			topPanel.add(hr = createPushButton(images.hr(), MSG.hr()));
			topPanel.add(ol = createPushButton(images.ol(), MSG.ol()));
			topPanel.add(ul = createPushButton(images.ul(), MSG.ul()));
			if (showInsertImage) {
				topPanel.add(insertImage = createPushButton(images.insertImage(), MSG.insertImage()));
			}
			if (showInsertLinks) {
				topPanel.add(createLink = createPushButton(images.createLink(), MSG.createLink()));
				topPanel.add(removeLink = createPushButton(images.removeLink(), MSG.removeLink()));
			}
			topPanel.add(removeFormat = createPushButton(images.removeFormat(), MSG.removeFormat()));
		}

		if (basic != null) {
			bottomPanel.add(backColors = createColorList("Background"));
			bottomPanel.add(foreColors = createColorList("Foreground"));
			bottomPanel.add(fonts = createFontList());
			bottomPanel.add(fontSizes = createFontSizes());

			// We only use these handlers for updating status, so don't hook them up
			// unless at least basic editing is supported.
			richText.addKeyUpHandler(handler);
			richText.addClickHandler(handler);
		}
	}

	private ListBox createColorList(String caption) {
		ListBox lb = new ListBox();
		lb.addChangeHandler(handler);
		lb.setVisibleItemCount(1);

		lb.addItem(caption);
		lb.addItem(MSG.white(), "white");
		lb.addItem(MSG.black(), "black");
		lb.addItem(MSG.red(), "red");
		lb.addItem(MSG.green(), "green");
		lb.addItem(MSG.yellow(), "yellow");
		lb.addItem(MSG.blue(), "blue");
		return lb;
	}

	private ListBox createFontList() {
		ListBox lb = new ListBox();
		lb.addChangeHandler(handler);
		lb.setVisibleItemCount(1);

		lb.addItem(MSG.font(), "");
		lb.addItem(MSG.normal(), "");
		lb.addItem("Times New Roman", "Times New Roman");
		lb.addItem("Arial", "Arial");
		lb.addItem("Courier New", "Courier New");
		lb.addItem("Georgia", "Georgia");
		lb.addItem("Trebuchet", "Trebuchet");
		lb.addItem("Verdana", "Verdana");
		return lb;
	}

	private ListBox createFontSizes() {
		ListBox lb = new ListBox();
		lb.addChangeHandler(handler);
		lb.setVisibleItemCount(1);

		lb.addItem(MSG.size());
		lb.addItem(MSG.xxsmall());
		lb.addItem(MSG.xsmall());
		lb.addItem(MSG.small());
		lb.addItem(MSG.medium());
		lb.addItem(MSG.large());
		lb.addItem(MSG.xlarge());
		lb.addItem(MSG.xxlarge());
		return lb;
	}

	private PushButton createPushButton(ImageResource img, String tip) {
		PushButton pb = new PushButton(new Image(img));
		pb.addClickHandler(handler);
		pb.setTitle(tip);
		return pb;
	}

	private ToggleButton createToggleButton(ImageResource img, String tip) {
		ToggleButton tb = new ToggleButton(new Image(img));
		tb.addClickHandler(handler);
		tb.setTitle(tip);
		return tb;
	}

	/**
	 * Updates the status of all the stateful buttons.
	 */
	private void updateStatus() {
		if (basic != null) {
			bold.setDown(basic.isBold());
			italic.setDown(basic.isItalic());
			underline.setDown(basic.isUnderlined());
			subscript.setDown(basic.isSubscript());
			superscript.setDown(basic.isSuperscript());
		}

		if (extended != null) {
			strikethrough.setDown(extended.isStrikethrough());
		}
	}

	public PushButton getInsertImage() {
		return insertImage;
	}

	@Override
	public boolean isEnabled() {
		return false;
	}

	@Override
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
		
		if (topPanel != null) {
			for (int i = 0; i < topPanel.getWidgetCount(); i++) {
				Widget w = topPanel.getWidget(i);
				if (w instanceof HasEnabled) {
					((HasEnabled) w).setEnabled(enabled);
				}
			}
		}
		if (bottomPanel != null) {
			for (int i = 0; i < bottomPanel.getWidgetCount(); i++) {
				Widget w = bottomPanel.getWidget(i);
				if (w instanceof HasEnabled) {
					((HasEnabled) w).setEnabled(enabled);
				}
			}
		}
	}
}

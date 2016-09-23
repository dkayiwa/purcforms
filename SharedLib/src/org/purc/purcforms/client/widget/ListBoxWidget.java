package org.purc.purcforms.client.widget;

import org.purc.purcforms.client.util.FormUtil;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.ListBox;


/**
 * This class is only to enable us have list boxes that can be locked
 * 
 * @author daniel
 *
 */
public class ListBoxWidget extends ListBox implements hasReadonly {
	
	/** 
	 * This allows us keep track of the selected index such that we can restore it
	 * whenever the user tries to change the selected value of a locked list box.
	 * We had to do this because for now we have not been successful at disabling 
	 * mouse clicks on locked list boxes.
	 */
	private int selectedIndex = -1;
	
	private boolean readonly = false;
	
	/**
	 * Creates a new instance of the list box widget.
	 * 
	 * @param isMultipleSelect set to true if you want to allow multiple selection.
	 */
	public ListBoxWidget(boolean isMultipleSelect){
		super(isMultipleSelect);
		if (!FormUtil.isReadOnlyMode()) {
			sinkEvents(Event.getTypeInt(ChangeEvent.getType().getName()));
		} else {
			setEnabled(false);
			setReadOnly(true);
		}
	}

	
	@Override
	public void onBrowserEvent(Event event){
		if(DOM.eventGetType(event) == Event.ONCHANGE){
			if(getParent().getParent() instanceof RuntimeWidgetWrapper &&
					((RuntimeWidgetWrapper)getParent().getParent()).isLocked()){
				super.setSelectedIndex(selectedIndex);
				return;
			}
		}

		super.onBrowserEvent(event);
	}
	
	@Override
	public void setSelectedIndex(int index) {
		 selectedIndex = index;
		 super.setSelectedIndex(index);
	}

	@Override
	public boolean isReadOnly() {
		return readonly;
	}

	@Override
	public void setReadOnly(boolean readOnly) {
		readonly = readOnly;
		String readOnlyStyle = "readonly";
		if (readOnly) {
			addStyleDependentName(readOnlyStyle);
		} else {
			removeStyleDependentName(readOnlyStyle);
		}
	}
	
	@Override
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		if (enabled) {
			removeStyleDependentName("disabled");
		} else {
			addStyleDependentName("disabled");
		}
	}

}

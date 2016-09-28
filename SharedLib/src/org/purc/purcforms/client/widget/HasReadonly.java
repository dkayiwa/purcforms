package org.purc.purcforms.client.widget;

/**
 * A widget that implements this interface can be put in a "readonly" state.
 * 
 * @author Kristof Heirwegh
 */
public interface HasReadonly {

	/**
	 * Returns true if the widget is enabled, false if not.
	 */
	boolean isReadOnly();

	/**
	 * Sets whether this widget is readonly.
	 * 
	 * @param readonly
	 *            <code>true</code> to enable the widget, <code>false</code> to disable it
	 */
	void setReadOnly(boolean readonly);
}

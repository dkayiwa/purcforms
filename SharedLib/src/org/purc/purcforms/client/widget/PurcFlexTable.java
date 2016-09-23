package org.purc.purcforms.client.widget;

import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.ui.FlexTable;

/**
 * @author Kristof Heirwegh
 */
public class PurcFlexTable extends FlexTable {

	public PurcFlexTable() {
		super();
		setColumnFormatter(new PurcColumnFormatter());
	}
	
	// ---------------------------------------------------

	public class PurcColumnFormatter extends ColumnFormatter {
		public String getWidth(int col) {
			if (columnGroup.getChildCount() > col) {
				Element e = columnGroup.getChild(col).cast();
				return e.getPropertyString("width");
			}
			return null;
		}
	}
}

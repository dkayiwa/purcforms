package org.purc.purcforms.client.util;

import java.util.List;

import org.purc.purcforms.client.widget.PurcFlexTable.PurcColumnFormatter;
import org.purc.purcforms.client.widget.RuntimeWidgetWrapper;

import com.google.gwt.user.client.ui.FlexTable;


/**
 * @author Kristof Heirwegh
 */
public class TableUtil {

	/**
	 * widths are calculated from left of first to left of next widget, so starting from second widget + must add width of last + delete button
	 * @param table
	 * @param widgetMap
	 */
	public static void setColumnWidths(FlexTable table, List<RuntimeWidgetWrapper> widgetMap) {
		if (table != null && widgetMap != null && widgetMap.size() > 1) {
			int totalSize = 0;
			for (int i = 1; i < widgetMap.size(); i++) {
				RuntimeWidgetWrapper rww = widgetMap.get(i);
				int width = rww.getLeftInt() - totalSize;
				table.getColumnFormatter().setWidth(i-1, width+"px");
				totalSize += width+10;
			}
			// -- now add last column
			table.getColumnFormatter().setWidth(widgetMap.size()-1, (widgetMap.get(widgetMap.size()-1).getWidthInt() + 25) + "px");
			
			// -- there is an extra column with delete button
			table.getColumnFormatter().setWidth(widgetMap.size(), "70px");
		}
	}

	/**
	 * You must set Columnwidths before calling setRowCellWidths so columns are correctly initialized.
	 * 
	 * @param table
	 * @param rowIndex
	 */
	public static void setRowCellWidths(FlexTable table, int rowIndex) {
		if (table != null && table.getColumnFormatter() instanceof PurcColumnFormatter) {
			PurcColumnFormatter formatter = (PurcColumnFormatter) table.getColumnFormatter();
			int count = table.getCellCount(rowIndex);
			for (int col = 0; col < count; col++) {
				table.getFlexCellFormatter().setWidth(rowIndex, col, formatter.getWidth(col));
			}
		}
	}

}

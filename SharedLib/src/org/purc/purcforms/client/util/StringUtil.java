package org.purc.purcforms.client.util;

import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;

/**
 * Singleton String utils
 * @author Kristof Heirwegh
 */
public class StringUtil {

	private StringUtil() {}
	
	/**
	 * Not very efficient, but normally is just a short label, so should not be a problem
	 * @param raw
	 * @return
	 */
	public static SafeHtml parseBBCode(String raw) {
		if (raw == null || "".equals(raw)) { return SafeHtmlUtils.fromTrustedString(""); }
		
		String res = raw;
		res = res.replaceAll("\\[sup\\]", "<sup>");
		res = res.replaceAll("\\[/sup\\]", "</sup>");
		res = res.replaceAll("\\[sub\\]", "<sub>");
		res = res.replaceAll("\\[/sub\\]", "</sub>");
		
		return SafeHtmlUtils.fromTrustedString(res);
	}
}

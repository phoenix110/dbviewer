/*
 * 著作権: Copyright (c) 2007−2008 ZIGEN
 * ライセンス：Eclipse Public License - v 1.0 
 * 原文：http://www.eclipse.org/legal/epl-v10.html
 */
package zigen.plugin.db.csv;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class Messages {

	private static final String BUNDLE_NAME = "zigen.plugin.db.csv.messages"; //$NON-NLS-1$

	private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle(BUNDLE_NAME);

	private Messages() {}

	public static String getString(String key) {
		try {
			return RESOURCE_BUNDLE.getString(key);
		} catch (MissingResourceException e) {
			return '!' + key + '!';
		}
	}
}

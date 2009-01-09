/*
 * 著作権: Copyright (c) 2007−2008 ZIGEN
 * ライセンス：Eclipse Public License - v 1.0 
 * 原文：http://www.eclipse.org/legal/epl-v10.html
 */
package zigen.plugin.db.ext.oracle.internal;

import java.io.Serializable;

public class OracleSourceDetailInfo extends OracleSourceInfo implements Serializable {

	private static final long serialVersionUID = 1L;

	private String text;

	/**
	 * コンストラクタ
	 */
	public OracleSourceDetailInfo() {}

	public String getText() {
		return this.text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append("[OracleSourceDetailInfo:"); //$NON-NLS-1$
		buffer.append(" text: "); //$NON-NLS-1$
		buffer.append(text);
		buffer.append("]"); //$NON-NLS-1$
		return buffer.toString();
	}

}

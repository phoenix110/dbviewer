/*
 * 著作権: Copyright (c) 2007−2008 ZIGEN
 * ライセンス：Eclipse Public License - v 1.0
 * 原文：http://www.eclipse.org/legal/epl-v10.html
 */

package zigen.plugin.db.ext.oracle.tablespace;

/**
 * OracleIndexColumnクラス.
 * 
 * @author ZIGEN
 * @version 1.0
 * @since JDK1.4 history Symbol Date Person Note [1] 2005/10/01 ZIGEN create.
 * 
 */
public class OracleIndexColumn extends OracleTableColumn {

	private static final long serialVersionUID = 1L;

	protected String index_name;

	public OracleIndexColumn() {}

	public String getIndex_name() {
		return this.index_name;
	}

	public void setIndex_name(String index_name) {
		this.index_name = index_name;
	}

	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append("[IndexColumnInfo:"); //$NON-NLS-1$
		buffer.append(" index_name: "); //$NON-NLS-1$
		buffer.append(index_name);
		buffer.append("]"); //$NON-NLS-1$
		return buffer.toString();
	}
}

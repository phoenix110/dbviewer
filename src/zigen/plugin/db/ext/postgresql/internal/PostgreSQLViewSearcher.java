/*
 * 著作権: Copyright (c) 2007−2008 ZIGEN
 * ライセンス：Eclipse Public License - v 1.0 
 * 原文：http://www.eclipse.org/legal/epl-v10.html
 */

package zigen.plugin.db.ext.postgresql.internal;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import zigen.plugin.db.DbPlugin;
import zigen.plugin.db.core.IDBConfig;
import zigen.plugin.db.core.ResultSetUtil;
import zigen.plugin.db.core.SQLUtil;
import zigen.plugin.db.core.StatementUtil;
import zigen.plugin.db.core.Transaction;

/**
 * PostgreSQLViewSearcherクラス.
 * 
 * @author ZIGEN
 * @version 1.0
 * @since JDK1.4 history Symbol Date Person Note [1] 2005/03/24 ZIGEN create.
 * 
 */
public class PostgreSQLViewSearcher {

	public static String execute(IDBConfig config, String owner, String view) throws Exception {
		try {
			Connection con = Transaction.getInstance(config).getConnection();
			return execute(con, owner, view);

		} catch (Exception e) {
			throw e;
		}
	}

	public static String execute(Connection con, String owner, String view) throws Exception {
		ResultSet rs = null;
		Statement st = null;
		try {
			st = con.createStatement();
			rs = st.executeQuery(getSQL(owner, view));

			if (rs.next()) {
				return rs.getString(1); //$NON-NLS-1$
			}
			return null;

		} catch (Exception e) {
			DbPlugin.log(e);
			throw e;
		} finally {
			ResultSetUtil.close(rs);
			StatementUtil.close(st);
		}

	}

	// Oracle用SQL
	private static String getSQL(String owner, String view) {
		StringBuffer sb = new StringBuffer();
		sb.append("SELECT");
		sb.append("        definition");
		sb.append("    FROM");
		sb.append("        pg_views");
		sb.append("    WHERE");
		sb.append("        schemaname = '" + SQLUtil.encodeQuotation(owner) + "'");
		sb.append("        AND viewname = '" + SQLUtil.encodeQuotation(view) + "'");
		return sb.toString();
	}

}

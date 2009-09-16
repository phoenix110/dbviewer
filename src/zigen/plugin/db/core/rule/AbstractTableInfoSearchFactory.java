package zigen.plugin.db.core.rule;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import zigen.plugin.db.DbPlugin;
import zigen.plugin.db.core.DBType;
import zigen.plugin.db.core.ResultSetUtil;
import zigen.plugin.db.core.SQLUtil;
import zigen.plugin.db.core.StatementUtil;
import zigen.plugin.db.core.TableInfo;
import zigen.plugin.db.core.rule.mysql.MySQLColumnSearcharFactory;
import zigen.plugin.db.core.rule.oracle.OracleColumnSearcharFactory;
import zigen.plugin.db.core.rule.symfoware.SymfowareColumnSearcharFactory;


abstract public class AbstractTableInfoSearchFactory implements ITableInfoSearchFactory {

	/**
	 * Factoryのキャッシュ化
	 */
	private static Map map = new HashMap();

	public static ITableInfoSearchFactory getFactory(DatabaseMetaData meta) throws SQLException{
		ITableInfoSearchFactory factory = null;
		String key = meta.getDriverName();
		if (map.containsKey(key)) {
			factory = (ITableInfoSearchFactory) map.get(key);
		} else {
			switch (DBType.getType(key)) {
				case DBType.DB_TYPE_ORACLE:
					factory = new OracleCommentSearchFactory(meta);
					break;
				case DBType.DB_TYPE_SYMFOWARE:
					factory = new SymfowareCommentSearchFactory(meta);
					break;

				default:
					factory = new DefaultCommentSearchFactory(meta);
					break;
			}

			map.put(key, factory);
		}

		return factory;

	}

	public List getTableInfoAll(Connection con, String owner, String[] types) throws Exception {
		List result = null;
		ResultSet rs = null;
		Statement st = null;
		try {
			st = con.createStatement();
			String query = getTableInfoAllSql(owner, types);
			System.out.println(query);
			if (query != null) {
				result = new ArrayList();
				rs = st.executeQuery(query);
				while (rs.next()) {
					TableInfo info = new TableInfo();
					info.setName(rs.getString("TABLE_NAME"));
					info.setTableType(rs.getString("TABLE_TYPE"));
					info.setComment(rs.getString("REMARKS"));
					result.add(info);
				}
			}
		} catch (Exception e) {
			DbPlugin.log(e);
			throw e;
		} finally {
			ResultSetUtil.close(rs);
			StatementUtil.close(st);
		}
		return result;
	}

	public TableInfo getTableInfo(Connection con, String owner, String tableName, String type) throws Exception {
		TableInfo info = null;
		ResultSet rs = null;
		Statement st = null;
		try {
			st = con.createStatement();
			String query = getTableInfoSql(owner, tableName, type);
//			System.out.println(query);
			if (query != null) {
				rs = st.executeQuery(query);
				if (rs.next()) {
					info = new TableInfo();
					info.setName(rs.getString("TABLE_NAME"));
					info.setTableType(rs.getString("TABLE_TYPE"));
					info.setComment(rs.getString("REMARKS"));
				}
			}
		} catch (Exception e) {
			DbPlugin.log(e);
			throw e;
		} finally {
			ResultSetUtil.close(rs);
			StatementUtil.close(st);
		}
		return info;
	}
	abstract public String getDbName();

	abstract public String getTableInfoAllSql(String owner, String[] types);

	abstract public String getTableInfoSql(String owner, String tableName, String type);

//	public Map getRemarkMap(Connection con, String owner) throws Exception {
//	Map result = null;
//	ResultSet rs = null;
//	Statement st = null;
//	try {
//		st = con.createStatement();
//		String query = getCommnentsSql(owner);
//		if (query != null) {
//			result = new HashMap();
//			rs = st.executeQuery(query);
//			while (rs.next()) {
//				TableComment info = new TableComment();
//				info.setTableName(rs.getString(1));
//				info.setRemarks(rs.getString(2));
//				result.put(info.getTableName(), info);
//			}
//		}
//	} catch (Exception e) {
//		DbPlugin.log(e);
//		throw e;
//	} finally {
//		ResultSetUtil.close(rs);
//		StatementUtil.close(st);
//	}
//	return result;
//}

}

/*
 * 著作権: Copyright (c) 2007−2008 ZIGEN
 * ライセンス：Eclipse Public License - v 1.0 
 * 原文：http://www.eclipse.org/legal/epl-v10.html
 */

package zigen.plugin.db.ui.views;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.StructuredViewer;

import zigen.plugin.db.DbPlugin;
import zigen.plugin.db.DbPluginConstant;
import zigen.plugin.db.core.DBType;
import zigen.plugin.db.core.IDBConfig;
import zigen.plugin.db.core.SchemaSearcher;
import zigen.plugin.db.core.TableColumn;
import zigen.plugin.db.core.TableConstraintColumn;
import zigen.plugin.db.core.TableFKColumn;
import zigen.plugin.db.core.TableIDXColumn;
import zigen.plugin.db.core.TablePKColumn;
import zigen.plugin.db.core.Transaction;
import zigen.plugin.db.core.rule.DefaultColumnSearcherFactory;
import zigen.plugin.db.core.rule.DefaultConstraintSearcherFactory;
import zigen.plugin.db.core.rule.IColumnSearcherFactory;
import zigen.plugin.db.core.rule.IConstraintSearcherFactory;
import zigen.plugin.db.ext.oracle.internal.OracleSynonymInfoSearcher;
import zigen.plugin.db.ext.oracle.internal.SynonymInfo;
import zigen.plugin.db.ui.editors.exceptions.NotFoundSynonymInfoException;
import zigen.plugin.db.ui.internal.Bookmark;
import zigen.plugin.db.ui.internal.Column;
import zigen.plugin.db.ui.internal.ITable;
import zigen.plugin.db.ui.internal.OracleColumn;
import zigen.plugin.db.ui.internal.Synonym;

/**
 * 
 * ColumnSearchThreadクラス.
 * 
 * @author ZIGEN
 * @version 1.0
 * @since JDK1.4 history Symbol Date Person Note [1] 2005/03/21 ZIGEN create.
 * 
 */
public class ColumnSearchAction implements Runnable {


	StructuredViewer viewer;

	ITable table;

	TablePKColumn[] pks = null;

	TableFKColumn[] fks = null;

	TableConstraintColumn[] cons = null;

	TableIDXColumn[] uidxs = null;

	TableIDXColumn[] nonuidxs = null;

	boolean isAssist = false;

	public ColumnSearchAction(StructuredViewer viewer, ITable table) {
		this.viewer = viewer;
		this.table = table;
	}

	/**
	 * コードアシスト用(軽量版)
	 * 
	 * @param treeViewer
	 * @param table
	 */
	public ColumnSearchAction(ITable table) {
		this.viewer = null;
		this.isAssist = true;
		this.table = table;
	}

	public void run() {

		TableColumn[] columns = null;

		try {
			// "読み込み中"を削除
			// table.removeChild((Column)
			// table.getChild(DbPluginConstant.TREE_LEAF_LOADING));
			table.removeChild(table.getChild(DbPluginConstant.TREE_LEAF_LOADING));

			if (viewer != null) {
				// コード保管で使う場合は、ViewerがNULLの場合がある
				viewer.refresh(table);// 再描画
			}

			IDBConfig config = table.getDbConfig();
			Connection con = Transaction.getInstance(config).getConnection();

			String schemaName = table.getSchemaName();
			String tableName = table.getName();

			switch (DBType.getType(con.getMetaData())) {
			case DBType.DB_TYPE_ORACLE:
				if (table instanceof Synonym) {
					// OracleでかつSynonymの場合
					Synonym synonym = (Synonym) table;
					schemaName = synonym.getTable_owner();
					tableName = synonym.getTable_name();
				} else if (table instanceof Bookmark) {
					Bookmark bm = (Bookmark) table;
					if (bm.isSynonym()) {
						SynonymInfo info = OracleSynonymInfoSearcher.execute(con, bm.getSchemaName(), bm.getName());
						schemaName = info.getTable_owner();
						tableName = info.getTable_name();

					}
				}
				break;
			}

			IConstraintSearcherFactory constraintFactory = DefaultConstraintSearcherFactory.getFactory(config);
			if (SchemaSearcher.isSupport(con)) {
				// columns = ColumnSearcher.execute(con, schemaName, tableName, config.isConvertUnicode());
				IColumnSearcherFactory factory = DefaultColumnSearcherFactory.getFactory(config);
				columns = factory.execute(con, schemaName, tableName);

				// pks = ConstraintSearcher.getPKColumns(con, schemaName, tableName);
				pks = constraintFactory.getPKColumns(con, schemaName, tableName);

				if (!isAssist) {
					// fks = ConstraintSearcher.getFKColumns(con, schemaName, tableName);
					fks = constraintFactory.getFKColumns(con, schemaName, tableName);
					cons = constraintFactory.getConstraintColumns(con, schemaName, tableName);
					uidxs = constraintFactory.getUniqueIDXColumns(con, schemaName, tableName, true);
					nonuidxs = constraintFactory.getUniqueIDXColumns(con, schemaName, tableName, false);

				}
			} else {
				// columns = ColumnSearcher.execute(con, null, tableName, config.isConvertUnicode());
				IColumnSearcherFactory factory = DefaultColumnSearcherFactory.getFactory(config);
				columns = factory.execute(con, null, tableName);

				// pks = ConstraintSearcher.getPKColumns(con, null, tableName);
				pks = constraintFactory.getPKColumns(con, schemaName, tableName);

				if (!isAssist) {
					// fks = ConstraintSearcher.getFKColumns(con, null, tableName);
					// uidxs = ConstraintSearcher.getUniqueIDXColumns(con, null, tableName, true);
					// nonuidxs = ConstraintSearcher.getUniqueIDXColumns(con, null, tableName, false);

					fks = constraintFactory.getFKColumns(con, schemaName, tableName);
					uidxs = constraintFactory.getUniqueIDXColumns(con, schemaName, tableName, true);
					nonuidxs = constraintFactory.getUniqueIDXColumns(con, schemaName, tableName, false);

				}
			}

			// Table要素にPK,FKを登録
			table.setTablePKColumns(pks);
			if (!isAssist) {
				table.setTableFKColumns(fks);
				table.setTableConstraintColumns(cons);
				table.setTableUIDXColumns(uidxs);
				table.setTableUIDXColumns(nonuidxs);
			}

			// Table要素(Table)にカラム要素(Column)を追加(共通）

			// JDBCMapping mapping = new JDBCMapping(table.getDBConfig());
			for (int i = 0; i < columns.length; i++) {
				TableColumn w_column = columns[i];
				TablePKColumn w_pk = getPKColumn(pks, w_column);
				TableFKColumn[] w_fks = getFKColumns(fks, w_column);

				// <- [003] 2005/11/22 add zigen
				addChild(con, w_column, w_pk, w_fks);
				// [003] 2005/11/22 add zigen -->

			}

			// 再描画
			if (viewer != null) {
				// コード保管で使う場合は、ViewerがNULLの場合がある
				viewer.refresh(table);// 再描画
			}

		} catch (NotFoundSynonymInfoException e) {
			table.setEnabled(false);
			table.removeChildAll(); // 子ノードを全て削除
			if (viewer != null) {
				viewer.refresh(table);// 再描画
			}
			DbPlugin.getDefault().showErrorDialog(e);

		} catch (Exception e) {
			DbPlugin.getDefault().showErrorDialog(e);
		}

	}

	public void addChild(Connection con, TableColumn w_column, TablePKColumn w_pk, TableFKColumn[] w_fks) throws Exception {

		switch (DBType.getType(con.getMetaData())) {
		case DBType.DB_TYPE_ORACLE:
			table.addChild(new OracleColumn(w_column, w_pk, w_fks));
			break;

		default:
			// 通常
			table.addChild(new Column(w_column, w_pk, w_fks));
			break;
		}
	}

	private TablePKColumn getPKColumn(TablePKColumn[] pks, TableColumn column) throws Exception {
		TablePKColumn pk = null;
		for (int i = 0; i < pks.length; i++) {
			if (pks[i].getColumnName().equals(column.getColumnName())) {
				pk = pks[i];
				break;
			}
		}
		return pk;

	}

	private TableFKColumn[] getFKColumns(TableFKColumn[] fks, TableColumn column) throws Exception {
		if (isAssist)
			return null;

		List list = new ArrayList();
		for (int i = 0; i < fks.length; i++) {
			if (fks[i].getColumnName().equals(column.getColumnName())) {
				list.add(fks[i]);
			}
		}
		return (TableFKColumn[]) list.toArray(new TableFKColumn[0]);

	}
}

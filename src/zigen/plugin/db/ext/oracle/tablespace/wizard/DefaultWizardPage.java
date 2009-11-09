/*
 * Copyright (c) 2007−2009 ZIGEN
 * Eclipse Public License - v 1.0 
 * http://www.eclipse.org/legal/epl-v10.html
 */

package zigen.plugin.db.ext.oracle.tablespace.wizard;

import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

/**
 * DefaultWizardPageクラス.
 * 
 * @author ZIGEN
 * @version 1.0
 * @since JDK1.4 history Symbol Date Person Note [1] 2005/10/02 ZIGEN create.
 * 
 */
abstract class DefaultWizardPage extends WizardPage {

	protected int LEVEL_FIELD_WIDTH = 20;

	protected int TEXT_FIELD_WIDTH = 50;

	protected int HEIGHT_HINT = 150;

	protected int WIDTH_HINT = 450;


	protected TableItem[] tableItems = null;

	protected TableViewer tableViewer = null;;

	public DefaultWizardPage(String pageName) {
		super(pageName);
	}

	abstract public void createControl(Composite parent);

	/**
	 * 各カラムの幅の自動計算
	 */
	protected void columnsPack(Table table) {
		TableColumn[] cols = table.getColumns();
		for (int i = 0; i < cols.length; i++) {
			cols[i].pack();
		}
	}

	/**
	 * Statusバーを更新
	 * 
	 * @param message
	 */
	void updateStatus(String message) {
		setErrorMessage(message);
		setPageComplete(message == null);
	}

	protected void createLine(Composite parent, int ncol) {
		Label line = new Label(parent, SWT.SEPARATOR | SWT.HORIZONTAL | SWT.BOLD);
		GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.horizontalSpan = ncol;
		line.setLayoutData(gridData);
	}

	/**
	 * 共通のComposite作成メソッド
	 * 
	 * @param parent
	 * @return
	 */
	protected Composite createDefaultComposite(Composite parent) {
		Composite composite = new Composite(parent, SWT.NULL);
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		gridLayout.marginHeight = 0;
		gridLayout.marginWidth = 0;
		gridLayout.horizontalSpacing = 0;
		gridLayout.verticalSpacing = 0;

		composite.setLayout(gridLayout);

		GridData data = new GridData();
		data.verticalAlignment = GridData.FILL;
		data.horizontalAlignment = GridData.FILL;

		composite.setLayoutData(data);

		return composite;
	}

	/**
	 * 共通のGridData取得メソッド GridDataの使いまわしNGの為、必ずNewしたものを返す
	 * 
	 * @param width
	 * @return
	 */
	protected GridData getGridData(int width) {
		GridData gd = new GridData();
		gd.widthHint = convertWidthInCharsToPixels(width);
		return gd;
	}

}

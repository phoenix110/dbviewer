/*
 * Copyright (c) 2007�|2009 ZIGEN
 * Eclipse Public License - v 1.0 
 * http://www.eclipse.org/legal/epl-v10.html
 */

package zigen.plugin.db.ui.editors.internal.action;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;

import zigen.plugin.db.DbPlugin;
import zigen.plugin.db.core.IDBConfig;
import zigen.plugin.db.core.rule.AbstractSQLCreatorFactory;
import zigen.plugin.db.core.rule.ISQLCreatorFactory;
import zigen.plugin.db.ui.actions.TableViewEditorAction;
import zigen.plugin.db.ui.editors.internal.wizard.UKWizard;
import zigen.plugin.db.ui.internal.ITable;

public class AddUniqueKeyAction extends TableViewEditorAction {

	public AddUniqueKeyAction() {
		this.setText(Messages.getString("AddUniqueKeyAction.0")); //$NON-NLS-1$
		this.setToolTipText(Messages.getString("AddUniqueKeyAction.1")); //$NON-NLS-1$
	}

	public void run() {

		Shell shell = DbPlugin.getDefault().getShell();
		IDBConfig config = editor.getDBConfig();
		ITable tableNode = editor.getTableNode();
		ISQLCreatorFactory factory = AbstractSQLCreatorFactory.getFactory(config, tableNode);

		UKWizard wizard = new UKWizard(factory, tableNode);
		WizardDialog dialog = new WizardDialog(shell, wizard);
		int ret = dialog.open();
		if (ret == IDialogConstants.OK_ID) {
		}
	}

}

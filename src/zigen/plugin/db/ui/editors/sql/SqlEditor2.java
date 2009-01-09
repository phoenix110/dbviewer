/*
 * 著作権: Copyright (c) 2007−2008 ZIGEN
 * ライセンス：Eclipse Public License - v 1.0 
 * 原文：http://www.eclipse.org/legal/epl-v10.html
 */
package zigen.plugin.db.ui.editors.sql;

import org.eclipse.core.commands.Command;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.commands.ActionHandler;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.jface.text.ITextOperationTarget;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.IVerticalRuler;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorActionBarContributor;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;

import zigen.plugin.db.DbPlugin;
import zigen.plugin.db.IStatusChangeListener;
import zigen.plugin.db.ImageCacher;
import zigen.plugin.db.core.IDBConfig;
import zigen.plugin.db.core.TableElement;
import zigen.plugin.db.csv.CreateCSVForQueryAction;
import zigen.plugin.db.ui.actions.CopyRecordDataAction;
import zigen.plugin.db.ui.actions.GlobalAction;
import zigen.plugin.db.ui.actions.SelectAllRecordAction;
import zigen.plugin.db.ui.editors.IQueryViewEditor;
import zigen.plugin.db.ui.editors.ITableViewEditor;
import zigen.plugin.db.ui.editors.TableViewContentProvider;
import zigen.plugin.db.ui.editors.TableViewLabelProvider;
import zigen.plugin.db.ui.editors.TextCellEditor;
import zigen.plugin.db.ui.editors.event.TableKeyAdapter;
import zigen.plugin.db.ui.editors.event.TableKeyEventHandler;
import zigen.plugin.db.ui.editors.event.TableSortListener;
import zigen.plugin.db.ui.editors.internal.CellEditorType;
import zigen.plugin.db.ui.internal.Column;
import zigen.plugin.db.ui.internal.ITable;
import zigen.plugin.db.ui.jobs.ChangeColorJob;
import zigen.plugin.db.ui.views.ISQLOperationTarget;
import zigen.plugin.db.ui.views.StatusLineContributionItem;
import zigen.plugin.db.ui.views.internal.SQLOutinePage;
import zigen.plugin.db.ui.views.internal.SQLToolBarForSqlEditor;

public class SqlEditor2 extends SqlEditor implements ITableViewEditor, IQueryViewEditor, IStatusChangeListener, IDocumentListener {

	public void documentAboutToBeChanged(DocumentEvent event) {
	// TODO 自動生成されたメソッド・スタブ
	}

	public void documentChanged(DocumentEvent event) {
		setDirty(true);
	}

	private ImageCacher ic = ImageCacher.getInstance();

	private Table table;

	private TableViewer viewer;

	private TableElement[] elements;

	private String query;

	private TableSortListener sortListener;

	protected StatusLineContributionItem responseTimeItem;

	protected String responseTime;

	protected SelectAllRecordAction selectAllRecordAction;

	protected CopyRecordDataAction copyAction;

	protected CreateCSVForQueryAction createCSVForQueryAction;

	protected ChangeColorJob changeColorJob;

	protected Label infoLabel;

	TableKeyEventHandler handler;

	CellEditor[] cellEditors;

	SashForm sash;

	int[] defaultWeight = {700, 300};

	boolean isFocusResultView = false;

	public SqlEditor2() {
		super();
	}

	public void createPartControl(Composite parent) {
		sash = new SashForm(parent, SWT.VERTICAL | SWT.NONE);

		super.createPartControl(sash);
		createResultPartControl();
		makeActions();

		getSqlViewer().getDocument().addDocumentListener(this);
		// setKeyBinding();
	}

	private void makeActions() {
		selectAllRecordAction = new SelectAllRecordAction();
		// selectAllRecordAction.setActionDefinitionId("org.eclipse.ui.edit.selectAll");
		copyAction = new CopyRecordDataAction();
		createCSVForQueryAction = new CreateCSVForQueryAction();

		selectAllRecordAction.setActiveEditor(this);
		copyAction.setActiveEditor(this);
		createCSVForQueryAction.setActiveEditor(this);

	}

	public int getOffset() {
		return getSourceViewer().getTextWidget().getCaretOffset();
	}

	protected ISourceViewer createSourceViewer(Composite parent, IVerticalRuler ruler, int styles) {

		// Composite header = new Composite(parent, SWT.NONE);
		FormLayout layout = new FormLayout();
		parent.setLayout(layout);
		// createToolbarPart(parent);
		toolBar = new SQLToolBarForSqlEditor(this);
		toolBar.createPartControl(parent);

		Composite sqlComposite = new Composite(parent, SWT.NONE);
		sqlComposite.setLayout(new FillLayout());
		FormData data = new FormData();
		data.top = new FormAttachment(toolBar.getCoolBar(), 0);
		data.left = new FormAttachment(0, 0);
		data.right = new FormAttachment(100, 0);
		data.bottom = new FormAttachment(100, 0);
		sqlComposite.setLayoutData(data);

		fAnnotationAccess = getAnnotationAccess();
		fOverviewRuler = createOverviewRuler(getSharedColors());

		SQLSourceViewer2 viewer = new SQLSourceViewer2(sqlComposite, ruler, getOverviewRuler(), isOverviewRulerVisible(), styles);
		viewer.setSqlEditor(this);
		getSourceViewerDecorationSupport(viewer);

		viewer.getTextWidget().addFocusListener(new FocusAdapter() {

			public void focusGained(FocusEvent e) {
				IActionBars bars = getEditorSite().getActionBars();
				setGlobalActionForEditor(bars);
				bars.updateActionBars();
				isFocusResultView = false;
			}
		});

		return viewer;
	}

	public void createResultPartControl() {
		table = new Table(sash, SWT.MULTI | SWT.FULL_SELECTION | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		GridData gridData2 = new GridData(GridData.FILL_BOTH);
		table.setLayoutData(gridData2);
		table.setHeaderVisible(true);// ヘッダを可視にする
		table.setLinesVisible(true); // ラインを表示
		table.setFont(DbPlugin.getDefaultFont());
		viewer = new TableViewer(table);
		setHeaderColumn(table);
		viewer.setContentProvider(new TableViewContentProvider());
		viewer.setLabelProvider(new TableViewLabelProvider());

		table.addKeyListener(new KeyAdapter() {

			public void keyPressed(KeyEvent e) {
				if (e.keyCode == SWT.F2) {
					// レコード選択を解除し、先頭のカラムを編集状態にする
					int row = handler.getSelectedRow();
					handler.editTableElement(row, 1);

				}
			}
		});

		table.addFocusListener(new FocusAdapter() {

			public void focusGained(FocusEvent e) {
				if (table.getSelectionIndex() == -1) {
					table.select(0); // 未選択の場合は、強制的に1レコード目を選択
					table.notifyListeners(SWT.Selection, null); // 選択状態を通知
				}
				IActionBars bars = getEditorSite().getActionBars();
				setGlobalActionForResultView(bars);
				bars.updateActionBars();
				isFocusResultView = true;
			}

			public void focusLost(FocusEvent e) {
				table.deselectAll();
			}
		});

		viewer.addSelectionChangedListener(new ISelectionChangedListener() {

			public void selectionChanged(SelectionChangedEvent e) {
				selectionChangeHandler(e);
			}
		});

		viewer.setInput(elements);

		changeColorJob = new ChangeColorJob(table);
		changeColorJob.setPriority(ChangeColorJob.LONG);
		changeColorJob.setUser(false);
		changeColorJob.schedule();

		// / <--セルを選択できるようにカスタマイズ
		handler = new TableKeyEventHandler(this);
		setCellModify(viewer, handler);
		// -->

		columnsPack(table);
		// SelectionProviderに登録(変更を通知させるため）
		getSite().setSelectionProvider(viewer);

		table.addControlListener(new ControlListener() {

			public void controlMoved(ControlEvent e) {
				int[] weight = sash.getWeights();
				if (weight[0] != 1000) {
					defaultWeight = sash.getWeights();
				}
			}

			public void controlResized(ControlEvent e) {}
		});

		if (elements == null) {
			setResultVisible(false);
		} else {
			setResultVisible(true);
		}
		hookContextMenu();

	}

	void setGlobalActionForEditor(IActionBars bars) {

		bars.clearGlobalActionHandlers();
		copyAction.refresh();

		bars.setGlobalActionHandler(ActionFactory.COPY.getId(), new GlobalAction(sqlViewer, ITextOperationTarget.COPY));
		bars.setGlobalActionHandler(ActionFactory.SELECT_ALL.getId(), new GlobalAction(sqlViewer, ITextOperationTarget.SELECT_ALL));
		bars.setGlobalActionHandler(ActionFactory.SELECT_ALL.getId(), new GlobalAction(sqlViewer, ITextOperationTarget.SELECT_ALL));
		bars.setGlobalActionHandler("zigen.plugin.db.actions.SQLExecuteAction", new GlobalAction(sqlViewer, ISQLOperationTarget.ALL_EXECUTE)); //$NON-NLS-1$
		bars.setGlobalActionHandler("zigen.plugin.db.actions.SQLCurrentExecuteAction", new GlobalAction(sqlViewer, ISQLOperationTarget.CURRENT_EXECUTE)); //$NON-NLS-1$
		bars.setGlobalActionHandler("zigen.plugin.db.actions.SQLSelectedExecuteAction", new GlobalAction(sqlViewer, ISQLOperationTarget.SELECTED_EXECUTE)); //$NON-NLS-1$

		bars.updateActionBars();

		ICommandService commandService = (ICommandService) getSite().getService(ICommandService.class);

		// CTRL+Cに対するコマンドの変更
		Command copy = commandService.getCommand("org.eclipse.ui.edit.copy");
		copy.setHandler(new ActionHandler(new GlobalAction(sqlViewer, ITextOperationTarget.COPY)));

		// CTRL+Aに対するコマンドの変更
		Command select = commandService.getCommand("org.eclipse.ui.edit.selectAll");
		select.setHandler(new ActionHandler(new GlobalAction(sqlViewer, ITextOperationTarget.SELECT_ALL)));

	}

	void setGlobalActionForResultView(IActionBars bars) {

		bars.clearGlobalActionHandlers();
		copyAction.refresh();

		bars.setGlobalActionHandler(ActionFactory.COPY.getId(), copyAction);
		bars.setGlobalActionHandler(ActionFactory.SELECT_ALL.getId(), selectAllRecordAction);
		bars.updateActionBars();

		// CTRL+C
		ICommandService commandService = (ICommandService) getSite().getService(ICommandService.class);
		Command copy = commandService.getCommand("org.eclipse.ui.edit.copy");
		copy.setHandler(new ActionHandler(copyAction));

		// CTRL+Aに対するコマンドの変更
		Command select = commandService.getCommand("org.eclipse.ui.edit.selectAll");
		select.setHandler(new ActionHandler(selectAllRecordAction));

	}

	private void setCellModify(TableViewer viewer, TableKeyEventHandler handler) {
		if (elements == null)
			return;
		final IActionBars bars = getEditorSite().getActionBars();
		TableElement element = elements[0];// ヘッダー用TableElementの取得
		int size = element.getColumns().length + 1; // ROW用に追加
		String[] properties = new String[size];
		zigen.plugin.db.core.TableColumn[] cols = element.getColumns();
		cellEditors = new CellEditor[size];
		TableKeyAdapter keyAdapter = new TableKeyAdapter(handler);
		for (int i = 0; i < cellEditors.length; i++) {
			properties[i] = String.valueOf(i); // property としてIndex番号を渡す
			if (i > 0) { // 1カラム目以降を更新可能とする
				CellEditor cellEditor = new TextCellEditor(table, i);

				if (cellEditor.getControl() instanceof Text) {
					Text txt = (Text) cellEditor.getControl();
					txt.setEditable(false); // 編集不可
				}
				cellEditor.getControl().addKeyListener(keyAdapter);
				cellEditor.getControl().addTraverseListener(keyAdapter);
				cellEditor.getControl().addFocusListener(new FocusAdapter() {

					public void focusGained(FocusEvent e) {
						bars.clearGlobalActionHandlers();
						bars.updateActionBars();
					}

					public void focusLost(FocusEvent e) {
					// setInfomationText(EDIT_MODE_OFF); non message
					}
				});
				cellEditors[i] = cellEditor;

			}
		}
		viewer.setColumnProperties(properties);
		viewer.setCellModifier(new ICellModifier() {

			public boolean canModify(Object element, String property) {
				return true;
			}

			public Object getValue(Object element, String property) {
				int index = Integer.parseInt(property);// 数値に変換
				if (element instanceof TableElement) {
					TableElement elem = (TableElement) element;
					Object obj = elem.getItems()[index - 1]; // rowNo分
					if (obj != null) {
						if (obj instanceof String) {
							return (String) obj;
						} else {
							return CellEditorType.getDataTypeName(elem.getColumns()[index]);
						}
					} else {
						return ""; //$NON-NLS-1$

					}
				}
				return null;
			}

			public void modify(Object element, String property, Object value) {}

		});
		viewer.setCellEditors(cellEditors);
	}

	public void refleshAction() {
		// selectAllRecordAction.refresh();
		copyAction.refresh();
	}

	void selectionChangeHandler(SelectionChangedEvent event) {
		refleshAction();
	}

	private void setHeaderColumn(Table table) {
		if (elements != null) {
			// TableColumn row = new TableColumn(table, SWT.LEFT);
			TableColumn row = new TableColumn(table, SWT.RIGHT);

			sortListener = new TableSortListener(this, 0);
			row.addSelectionListener(sortListener);
			row.pack();
			TableElement element = elements[0]; // ヘッダー用カラム
			zigen.plugin.db.core.TableColumn[] columns = element.getColumns();
			for (int i = 0; i < columns.length; i++) {
				zigen.plugin.db.core.TableColumn tColumn = columns[i];
				TableColumn col = new TableColumn(table, SWT.LEFT);
				col.setText(tColumn.getColumnName());
				col.addSelectionListener(new TableSortListener(this, i + 1));
				col.pack();
			}
		}
	}

	private void columnsPack(Table table) {
		table.setVisible(false);
		TableColumn[] cols = table.getColumns();
		for (int i = 0; i < cols.length; i++) {
			cols[i].pack();
		}
		table.setVisible(true);
	}

	public void changeColumnColor() {
		// TODO 自動生成されたメソッド・スタブ
		throw new UnsupportedOperationException("未実装です"); //$NON-NLS-1$

	}

	public void changeColumnColor(Column column) {
		// TODO 自動生成されたメソッド・スタブ
		throw new UnsupportedOperationException("未実装です"); //$NON-NLS-1$		
	}

	public void editTableElement(Object element, int column) {
		// TODO 自動生成されたメソッド・スタブ
		throw new UnsupportedOperationException("未実装です"); //$NON-NLS-1$
	}

	public String getCondition() {
		// TODO 自動生成されたメソッド・スタブ
		throw new UnsupportedOperationException("未実装です"); //$NON-NLS-1$
	}

	public IDBConfig getDBConfig() {
		return super.getConfig();
	}

	public TableElement getHeaderTableElement() {
		if (this.elements.length > 0) {
			return elements[0];
		}
		return null;
	}

	public ITable getTableNode() {
		return null;
	}

	public TableViewer getViewer() {
		return viewer;
	}

	public void setEnabled(boolean enabled) {
		// TODO 自動生成されたメソッド・スタブ
		throw new UnsupportedOperationException("未実装です"); //$NON-NLS-1$

	}

	public void setTotalCount(int dispCount, long totalCount) {
	// TODO 自動生成されたメソッド・スタブ

	}

	public void setResultVisible(boolean visibled) {
		if (visibled) {
			sash.setWeights(defaultWeight);
		} else {
			sash.setWeights(new int[] {100, 0});
		}
	}

	public void update(String query, TableElement[] elements, String responseTime, boolean isReload) {
		try {
			// Queryは置き換える
			this.query = query;
			this.elements = elements;
			table.dispose();

			createResultPartControl();

			sash.layout(true);
			sash.getParent().layout(true);

			// 応答時間の表示
			// setResponseTime(responseTime);

			// レコード件数の表示
			int dispCnt = elements.length - 1;
			setTotalCount(dispCnt, -1); //$NON-NLS-1$


		} catch (Exception e) {
			DbPlugin.getDefault().showErrorDialog(e);
		}
	}

	public String getQuery() {
		return query;
	}

	private void hookContextMenu() {
		MenuManager menuMgr = new MenuManager("#PopupMenu"); //$NON-NLS-1$
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {

			public void menuAboutToShow(IMenuManager manager) {
				if (isFocusResultView) {
					getContributor().fillContextMenuForResultView(manager);
				} else {
					getContributor().fillContextMenu(manager);
				}
			}
		});
		Menu menu = menuMgr.createContextMenu(viewer.getControl());
		viewer.getControl().setMenu(menu);
		getSite().registerContextMenu(menuMgr, viewer);
	}

	// 現在未使用(エディターの場合はこのメソッドをオーバライドしてコンテキストメニューを出すことが標準)
	protected void editorContextMenuAboutToShow(IMenuManager menu) {
		super.editorContextMenuAboutToShow(menu);
		menu.add(new Separator());
	}

	private SqlEditorContributor getContributor() {
		IEditorActionBarContributor contributor = getEditorSite().getActionBarContributor();
		if (contributor instanceof SqlEditorContributor) {
			return (SqlEditorContributor) contributor;
		} else {
			return null;
		}
	}

	public Object getAdapter(Class adapter) {
		if (IContentOutlinePage.class.equals(adapter)) {
			if (outlinePage == null) {
				outlinePage = new SQLOutinePage(this);
			}
			return outlinePage;
		}
		return super.getAdapter(adapter);
	}

	public int getRecordLimit() {
		return 0;
	}

	public int getRecordOffset() {
		return 0;
	}

}
// try {
// ICommandService commandService = (ICommandService)
// getSite().getService(ICommandService.class);
// IHandlerService handlerService = (IHandlerService)
// getSite().getService(IHandlerService.class);
// IBindingService bindingService = (IBindingService)
// getSite().getService(IBindingService.class);
// IContextService contextService = (IContextService)
// getSite().getService(IContextService.class);
//
// Category editCat =
// commandService.getCategory("org.eclipse.ui.category.edit");
// String commandId = "z.ex.view.SCommand";
// Command scmd = commandService.getCommand(commandId);
// if (!scmd.isDefined()) {
// scmd.define("SCommand", "Run the SCommand", editCat);
// }
//
// IHandler handler = new AbstractHandler() {
// public Object execute(ExecutionEvent event) throws ExecutionException {
// System.out.println("The Handler has landed!");
// return null;
// }
// };
//
// handlerService.activateHandler(commandId, handler);
//
// // now set up the keybindings
// String sampleContextId = "sampleViewContext";
// String parentContextId = "org.eclipse.ui.contexts.window";
//
// Context sampleContext = contextService.getContext(sampleContextId);
// if (!sampleContext.isDefined()) {
// sampleContext.define("Sample Context", "My Sample Context", parentContextId);
// }
// contextService.activateContext(sampleContextId);
//
// String defaultSchemeId = "org.eclipse.ui.defaultAcceleratorConfiguration";
// Scheme defaultScheme = bindingService.getScheme(defaultSchemeId);
//
// ParameterizedCommand pscmd = new ParameterizedCommand(scmd, null);
//
// //KeySequence keySequence = KeySequence.getInstance("CTRL+ALT+.");
// KeySequence keySequence = KeySequence.getInstance("CTRL+A");
// Binding newKey = new KeyBinding(keySequence, pscmd, defaultSchemeId,
// sampleContextId, null, null, null, Binding.USER);
// //
// Binding[] bindings = bindingService.getBindings();
// Binding[] newBindings = new Binding[bindings.length + 1];
// newBindings[0] = newKey;
// System.arraycopy(bindings, 0, newBindings, 1, bindings.length);
// bindingService.savePreferences(defaultScheme, newBindings);
//
//
// } catch (ParseException e) {
// // TODO Auto-generated catch block
// e.printStackTrace();
// } catch (IOException e) {
// // TODO Auto-generated catch block
// e.printStackTrace();
// }

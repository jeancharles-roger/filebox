/**
 * Filebox developed by Kawane.
 * LGPL License.
 */
package org.kawane.filebox.ui;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MenuAdapter;
import org.eclipse.swt.events.MenuEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.kawane.filebox.core.Preferences;
import org.kawane.filebox.ui.internal.Application;
import org.kawane.filebox.ui.internal.Resources;
import org.kawane.filebox.ui.toolkit.ToolKit;

/**
 * 
 * Handles the global application menu.
 * @author Jean-Charles Roger
 *
 */
public class MenuManager {

	/** Shared resources instances. */
	protected Resources resources = Resources.getInstance();
	
	/** Parent application */
	protected final Application application;
	
	/** Toolkit used for dialogs */
	protected final ToolKit tk = new ToolKit();

	/** The handled menu bar  */
	protected Menu menuBar = null;

	/** FileBox menu action list */
	protected List<IAction> fileBoxActions = null;
	
	public MenuManager(Application application) {
		this.application = application;
	}
	
	protected Preferences getPreferences() {
		return application.getFilebox().getPreferences();
	}
	
	/** Generic selection listener for MenuItems that have Actions as data.  */
	protected SelectionAdapter selectionListener = new SelectionAdapter() {
		@Override
		public void widgetSelected(SelectionEvent e) {
			// TODO handles toolitems
			if (e.getSource() instanceof MenuItem && ((MenuItem) e.getSource()).getData() instanceof IAction) {
				IAction action = (IAction) ((MenuItem) e.getSource()).getData();
				// TODO handles end status
				action.run();
			}
		}
	};
	
	/** Creates the shell menu bar */
	public void createMenuBar(Shell shell) {

		menuBar = new Menu(shell, SWT.BAR);
		shell.setMenuBar(menuBar);
		
		createMenu(shell, menuBar, "FileBox", getFileBoxActions());

	}

	/** Creates a cascaded {@link MenuItem} from a list of actions. */
	protected Menu createMenu(final Shell shell, final Menu bar, final String name, final List<IAction> actions) {
		MenuItem menuItem = new MenuItem(bar, SWT.CASCADE);
		menuItem.setText(name);

		final Menu menu = new Menu(shell, SWT.DROP_DOWN);
		menuItem.setMenu(menu);

		menu.addMenuListener(new MenuAdapter() {
			@Override
			public void menuShown(MenuEvent e) {
				depopulateMenu(menu);
				populateMenu(menu, actions);
			}
		});

		return menu;
	}

	/** Populates menu with the given actions.  */
	protected void populateMenu(Menu menu, List<IAction> actions) {
		for (IAction oneAction : actions) {

			int visibility = oneAction.getVisibility();
			if (visibility != IAction.VISIBILITY_HIDDEN ) {
				if (oneAction.hasStyle(IAction.STYLE_SEPARATOR)) {
					new MenuItem(menu, SWT.SEPARATOR);
				} else {
					MenuItem menuItem = new MenuItem(menu, SWT.PUSH);
					menuItem.setText(oneAction.getLabel());
					menuItem.setImage(oneAction.getImage());
					menuItem.setData(oneAction);
					menuItem.setEnabled(visibility == IAction.VISIBILITY_ENABLE);
					menuItem.addSelectionListener(selectionListener);
				}
			}
		}
	}

	protected void depopulateMenu(Menu menu) {
		MenuItem[] items = menu.getItems();
		for (int i = 0; i < items.length; i++) {
			items[i].dispose();
		}
	}

	
	public List<IAction> getFileBoxActions() {
		if ( fileBoxActions == null ) {
			fileBoxActions = new ArrayList<IAction>();
			
			
			fileBoxActions.add(new IAction.Stub("About") {
				@Override
				public int run() {
					Shell dialog = tk.dialogShell(application.getActiveShell(), "About");
					tk.message(dialog, "FileBox version 1.0.");
					tk.message(dialog, "Copyrights Kawane 2009.");
					Button[] buttons = tk.buttons(dialog, "Ok");
					tk.computeSizes(dialog, 200);
					dialog.open();
					tk.waitSelectedButton(buttons);
					dialog.dispose();
					return STATUS_OK;
				}
			});
			
			fileBoxActions.add(new IAction.Stub("Preferences") {
				@Override
				public int run() {
					Shell dialog = tk.dialogShell(application.getActiveShell(), "Preferences");
					Text nameText = tk.textField(dialog, "Name:", getPreferences().getName() );
					Text portText = tk.textField(dialog, "Port:", Integer.toString(getPreferences().getPort()) );
					Button[] buttons = tk.buttons(dialog, "Ok", "Cancel");
					dialog.setDefaultButton(buttons[0]);
					tk.computeSizes(dialog, 300);
					dialog.open();
					int result = tk.waitSelectedButton(buttons);
					if (result == 0) {
						getPreferences().setName(nameText.getText());
						try {
							getPreferences().setPort(Integer.valueOf(portText.getText()));
						} catch (NumberFormatException e) { /* do nothing */ }
						getPreferences().saveProperties();
					}
					dialog.dispose();
					return result == 0 ? STATUS_OK : STATUS_CANCEL;
				}
			});
			
			fileBoxActions.add(new IAction.Stub(IAction.STYLE_SEPARATOR));
			
			fileBoxActions.add(new IAction.Stub("Quit") {
				@Override
				public int run() {
					application.stop();
					return STATUS_OK;
				}
			});
			
		}
		return fileBoxActions;
	}
}

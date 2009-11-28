package org.kawane.filebox.ui;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.kawane.filebox.core.DistantFilebox;
import org.kawane.filebox.core.Filebox;
import org.kawane.filebox.core.FileboxRegistry;
import org.kawane.filebox.core.Globals;

public class ContactShellController {
	
	private final Filebox filebox;
	private final FileboxRegistry registry;

	private final Display display;
	
	private Shell shell;
	private Listener shellListener = new Listener() {
		public void handleEvent(Event event) {
			switch( event.type ) {
			case SWT.Close:
				boolean visible = !shell.isVisible();
				shell.setVisible(visible);
				
				// do not quit the application when closing the shell
				event.doit = false;
				break;
			case SWT.Dispose:
				filebox.removePropertyChangeListener(propertiesListener);
				registry.removePropertyChangeListener(propertiesListener);
				break;
			}
		}
	};
	
	/** Shared resources instances. */
	private Resources resources = Resources.getInstance();

	private Composite meComposite;
	private Label meLabel;
	private Combo statusCombo;
	private Listener statusComboListener = new Listener(){
		public void handleEvent(Event event) {
			updateModel(event);
		}
	};

	private Table contactsTable;
	private TableColumn statusColumn;
	private TableColumn nameColumn;
	private TableColumn hostColumn;
	private Listener contactsTableListener = new Listener() {
		public void handleEvent(Event e) {
			if ( e.type == SWT.SetData ) {
					TableItem item = (TableItem)e.item;
					int index = contactsTable.indexOf(item);
					DistantFilebox distantFilebox = Globals.getFileboxRegistry().getFilebox(index);
					item.setData(distantFilebox);
					//item.setImage(0, resources.getImage(distantFilebox.isConnected() ? "connected.png" : "disconnected.png"));
					item.setImage(0, resources.getImage("connected.png"));
					item.setText(1, distantFilebox.getName());
					item.setText(2, distantFilebox.getHost());
				return;
			}

			if ( e.type == SWT.Resize ) {
				resizeContactTable();
				return;
			}
		}
	};

	private PropertyChangeListener propertiesListener = new PropertyChangeListener() {
		public void propertyChange(final PropertyChangeEvent evt) {
			shell.getDisplay().asyncExec(new Runnable(){
				public void run() {
					refreshUI();
				}
			});
		}
	};
	
	public ContactShellController(Display display, Filebox filebox, FileboxRegistry registry) {
		this.display = display;
		this.filebox = filebox;
		this.registry = registry;
	}

	public Shell getShell() {
		return shell;
	}
	
	private void resizeContactTable() {
		final float hostRatio = 0.4f;
		final int statusSize = 20;
		final int margin = 10;
		int width = contactsTable.getSize().x - margin - statusSize;
		int hostSize = (int) (hostRatio * width);
		statusColumn.setWidth( statusSize );
		nameColumn.setWidth( width - hostSize - statusSize);
		hostColumn.setWidth(hostSize);
	}

	
	public Shell createShell() {

		shell = new Shell(display);
		shell.setLayout(new GridLayout(1,false));
		shell.setImage(resources.getImage("filebox-icon-256x256.png"));
		shell.setSize(300, 300);
		shell.setText("FileBox");
		shell.addListener(SWT.Close, shellListener);
		shell.addListener(SWT.Dispose, shellListener);
		
		
		meComposite = new Composite(shell, SWT.NONE);
		meComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		meComposite.setLayout(new GridLayout(2,false));

		// my name label
		meLabel = new Label(meComposite, SWT.NONE);
		meLabel.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
		meLabel.setText("Me");

		// status combo
		statusCombo = new Combo(meComposite, SWT.READ_ONLY);
		statusCombo.setLayoutData(new GridData(SWT.END, SWT.CENTER, true, false));
		statusCombo.setItems( new String[] { "On line", "Off line" } );
		statusCombo.select(1);
		statusCombo.addListener(SWT.Selection, statusComboListener);

		// a separator
		Label separator = new Label(shell, SWT.SEPARATOR | SWT.HORIZONTAL);
		separator.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

		// contacts label
		Label contactsLabel = new Label(shell, SWT.NONE);
		contactsLabel.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, false));
		contactsLabel.setText("Contacts" + ":");

		// contacts table
		contactsTable = new Table(shell,  SWT.MULTI | SWT.VIRTUAL | SWT.BORDER | SWT.FULL_SELECTION);
		contactsTable.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		contactsTable.setLinesVisible(true);
		statusColumn = new TableColumn(contactsTable, SWT.CENTER);
		nameColumn = new TableColumn(contactsTable, SWT.NONE);
		hostColumn = new TableColumn(contactsTable, SWT.RIGHT);
		contactsTable.addListener(SWT.SetData, contactsTableListener);
		contactsTable.addListener(SWT.Resize, contactsTableListener);
		contactsTable.setItemCount(0);
		resizeContactTable();
		
		filebox.addPropertyChangeListener(propertiesListener);
		registry.addPropertyChangeListener(propertiesListener);
		return shell;
	}
	
	public void refreshUI() {
		meLabel.setText(filebox.getName());
		meLabel.getParent().layout();
		statusCombo.setEnabled(filebox.getState() != Filebox.PENDING);
		if ( filebox.getState() != Filebox.PENDING )  {
			statusCombo.select(filebox.getState() - 1);
		}
		meComposite.layout();
		
		contactsTable.clearAll();
		contactsTable.setItemCount(registry.getFileboxesCount());
	}
	
	public boolean updateModel(Event event) {
		if ( statusCombo == event.widget ) {
			if ( statusCombo.getSelectionIndex()+1 == filebox.getState() ) return false;
			
			if ( statusCombo.getSelectionIndex() == 0 ) {
				filebox.connect();
			} else {
				filebox.disconnect();
			}
			return true;
		}
		return false;
	}
	
	
	public DistantFilebox getSelectedFilebox() {
		int index = contactsTable.getSelectionIndex();
		if ( index < 0 || index > (registry.getFileboxesCount() - 1) ) return null;
		return registry.getFilebox(index);
	}

}

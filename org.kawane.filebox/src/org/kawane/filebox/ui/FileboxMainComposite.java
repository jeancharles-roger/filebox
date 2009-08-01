/**
 * Filebox developed by Kawane.
 * LGPL License.
 */
package org.kawane.filebox.ui;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.kawane.filebox.core.DistantFilebox;
import org.kawane.filebox.core.Filebox;
import org.kawane.filebox.core.FileboxRegistry;
import org.kawane.filebox.core.Globals;

/**
 * @author Jean-Charles Roger
 *
 */
public class FileboxMainComposite extends Composite {

	/** Shared resources instances. */
	protected Resources resources = Resources.getInstance();

	protected GridLayout layout;

	protected Label meLabel;
	protected Combo statusCombo;
	protected SelectionAdapter statusComboListener = new SelectionAdapter(){
		@Override
		public void widgetSelected(SelectionEvent e) {
			if ( statusCombo.getSelectionIndex() == 0 ) {
				Globals.getLocalFilebox().connect();
			} else {
				Globals.getLocalFilebox().disconnect();
			}
		}
	};

	protected Table contactsTable;
	protected TableColumn statusColumn;
	protected TableColumn nameColumn;
	protected TableColumn hostColumn;
	protected Listener contactsTableListener = new Listener() {
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



	protected Filebox filebox;
	protected PropertyChangeListener propertiesListener = new PropertyChangeListener() {
		public void propertyChange(final PropertyChangeEvent evt) {
			getDisplay().asyncExec(new Runnable(){
				public void run() {
					
					// local filebox changed
					if ( evt.getSource() == Globals.getLocalFilebox() ) {
						meLabel.setText(Globals.getLocalFilebox().getName());
						meLabel.getParent().layout();
						statusCombo.select(Globals.getLocalFilebox().isConnected() ? 0 : 1);
					}
					
					// fileboxes changed
					if ( evt.getSource() == Globals.getFileboxRegistry() ) {
						if ( FileboxRegistry.FILEBOXES.equals(evt.getPropertyName()) ) {
							contactsTable.setItemCount(Globals.getFileboxRegistry().getFileboxesCount());
							contactsTable.clearAll();
						}
						return;
					}
				}
			});
		}
	};
	public FileboxMainComposite(Composite parent, int style) {
		super(parent, style);
		Globals.getLocalFilebox().addPropertyChangeListener(propertiesListener);
		Globals.getFileboxRegistry().addPropertyChangeListener(propertiesListener);

		layout = new GridLayout(1,false);
		setLayout(layout);

		// the me line
		Composite meComposite = new Composite(this, SWT.NONE);
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
		statusCombo.addSelectionListener(statusComboListener);
//		statusCombo.select(0);

		// a separator
		Label separator = new Label(this, SWT.SEPARATOR | SWT.HORIZONTAL);
		separator.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

		// contacts label
		Label contactsLabel = new Label(this, SWT.NONE);
		contactsLabel.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, false));
		contactsLabel.setText("Contacts" + ":");

		// contacts table
		contactsTable = new Table(this,  SWT.MULTI | SWT.VIRTUAL | SWT.BORDER);
		contactsTable.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		contactsTable.setLinesVisible(true);
		statusColumn = new TableColumn(contactsTable, SWT.CENTER);
		nameColumn = new TableColumn(contactsTable, SWT.NONE);
		hostColumn = new TableColumn(contactsTable, SWT.RIGHT);
		contactsTable.addListener(SWT.SetData, contactsTableListener);
		contactsTable.addListener(SWT.Resize, contactsTableListener);
		contactsTable.setItemCount(0);
		resizeContactTable();
	}

	protected void resizeContactTable() {
		final float hostRatio = 0.4f;
		final int statusSize = 20;
		final int margin = 10;
		int width = contactsTable.getSize().x - margin - statusSize;
		int hostSize = (int) (hostRatio * width);
		statusColumn.setWidth( statusSize );
		nameColumn.setWidth( width - hostSize - statusSize);
		hostColumn.setWidth(hostSize);
	}

	@Override
	public void dispose() {
		super.dispose();
		
		Globals.getLocalFilebox().removePropertyChangeListener(propertiesListener);
		Globals.getFileboxRegistry().removePropertyChangeListener(propertiesListener);
	
	}
	
}

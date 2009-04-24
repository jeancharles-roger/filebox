/**
 * Filebox developed by Kawane.
 * LGPL License.
 */
package org.kawane.filebox.ui;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.rmi.RemoteException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.kawane.filebox.core.Filebox;
import org.kawane.filebox.core.IFilebox;
import org.kawane.filebox.core.Preferences;
import org.kawane.filebox.ui.internal.Resources;
import org.kawane.services.IServiceRegistry;
import org.kawane.services.advanced.Inject;

/**
 * @author Jean-Charles Roger
 *
 */
public class FileboxMainComposite extends Composite {

	private static Logger logger = Logger.getLogger(FileboxMainComposite.class.getName());

	/** Shared resources instances. */
	protected Resources resources = Resources.getInstance();

	protected GridLayout layout;

	protected Label meLabel;
	protected Combo statusCombo;
	protected SelectionAdapter statusComboListener = new SelectionAdapter(){
		@Override
		public void widgetSelected(SelectionEvent e) {
			if ( statusCombo.getSelectionIndex() == 0 ) {
				getLocalFilebox().connect();
			} else {
				getLocalFilebox().disconnect();
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
				try {
					TableItem item = (TableItem)e.item;
					int index = contactsTable.indexOf(item);
					IFilebox distantFilebox = filebox.getFilebox(index);
					item.setData(distantFilebox);
					item.setImage(0, resources.getImage(distantFilebox.isConnected() ? "connected.png" : "disconnected.png"));
					item.setText(1, distantFilebox.getName());
					item.setText(2, distantFilebox.getHost());
				} catch (RemoteException e1) {
					logger.log(Level.SEVERE, "An Error Occured", e1);
				}
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
					// application changed
					if ( evt.getSource() == getLocalFilebox() ) {
						if ( Filebox.FILEBOXES.equals(evt.getPropertyName()) ) {
							contactsTable.setItemCount(getLocalFilebox().getFileboxesCount());
						}
						return;
					}

					// preferences changed
					if (evt.getSource() == getLocalFilebox().getPreferences() ) {
						if ( Preferences.NAME.equals(evt.getPropertyName()) ) {
							meLabel.setText(getLocalFilebox().getPreferences().getName());
							// refresh parent's layout for label length
							meLabel.getParent().layout();
						}
						return;
					}
				}
			});
		}
	};
	public FileboxMainComposite(Composite parent, int style) {
		super(parent, style);
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

	@Inject
	public int setFilebox(final Filebox filebox) {
		if ( this.filebox != null ) {
			this.filebox.removePropertyChangeListener(propertiesListener);
			this.filebox.getPreferences().removePropertyChangeListener(propertiesListener);
		}
		if ( filebox != null ) {
			filebox.addPropertyChangeListener(propertiesListener);
			filebox.getPreferences().addPropertyChangeListener(propertiesListener);
		}
		this.filebox = filebox;
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				if (filebox != null) {
					try {
						meLabel.setText(filebox.getName());
						meLabel.getParent().layout();
						statusCombo.select(filebox.isConnected() ? 0 : 1);
					} catch (RemoteException e) {
						logger.log(Level.SEVERE, "An Error Occured", e);
					}
				} else {
					meLabel.setText("Me");
					meLabel.getParent().layout();
					statusCombo.select(1);
				}
			}
		});
		return IServiceRegistry.DEPENDENCY_RESOLVED;
	}

	public Filebox getLocalFilebox() {
		return filebox;
	}

}

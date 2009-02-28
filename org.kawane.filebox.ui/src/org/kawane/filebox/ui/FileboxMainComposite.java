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
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.kawane.filebox.core.IFilebox;
import org.kawane.filebox.core.LocalFilebox;
import org.kawane.filebox.core.Preferences;

/**
 * @author Jean-Charles Roger
 *
 */
public class FileboxMainComposite extends Composite {

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
	protected Listener contactsDataListener = new Listener() {
		public void handleEvent(Event e) {
			TableItem item = (TableItem)e.item;
			int index = contactsTable.indexOf(item);
			IFilebox distantFilebox = filebox.getFilebox(index);
			item.setData(distantFilebox);
			item.setText(distantFilebox.getName());
		}
	};
	
	
	protected Button testButton;
	
	protected LocalFilebox filebox;
	protected PropertyChangeListener propertiesListener = new PropertyChangeListener() {
		public void propertyChange(PropertyChangeEvent evt) {

			// application changed
			if ( evt.getSource() == getLocalFilebox() ) {
				if ( LocalFilebox.FILEBOXES.equals(evt.getPropertyName()) ) {
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
		statusCombo.setItems( new String[] { "On line", "Off line", "Don't disturb"} );
		statusCombo.select(1);
		statusCombo.addSelectionListener(statusComboListener);
		
		// a separator
		Label separator = new Label(this, SWT.SEPARATOR | SWT.HORIZONTAL);
		separator.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		
		// contacts label
		Label contactsLabel = new Label(this, SWT.NONE);
		contactsLabel.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, false));
		contactsLabel.setText("Contacts" + ":");
		
		// contacts table
		contactsTable = new Table(this,  SWT.VIRTUAL | SWT.BORDER);
		contactsTable.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		contactsTable.addListener(SWT.SetData, contactsDataListener);
		contactsTable.setItemCount(0);
		
		// test button
		testButton = new Button(this, SWT.PUSH);
		testButton.setText("Add contact");
		testButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				getLocalFilebox().addFilebox(0, new IFilebox.Stub("Lolo"));
			}
		});
		
		
	}
	
	public void setFilebox(LocalFilebox filebox) {
		if ( this.filebox != null ) {
			this.filebox.removePropertyChangeListener(propertiesListener);
			this.filebox.getPreferences().removePropertyChangeListener(propertiesListener);
		}
		if ( filebox != null ) {
			filebox.addPropertyChangeListener(propertiesListener);
			filebox.getPreferences().addPropertyChangeListener(propertiesListener);
		}
		this.filebox = filebox;
		if ( filebox != null ) {
			meLabel.setText(filebox.getName());
			statusCombo.select(filebox.isConnected() ? 0 : 1);
		} else {
			meLabel.setText("Me");
			statusCombo.select(1);
		}
		
	}

	public LocalFilebox getLocalFilebox() {
		return filebox;
	}
	
}

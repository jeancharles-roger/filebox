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
import org.kawane.filebox.core.Contact;
import org.kawane.filebox.core.Filebox;
import org.kawane.filebox.core.Preferences;

/**
 * @author Jean-Charles Roger
 *
 */
public class FileboxMainComposite extends Composite {

	protected GridLayout layout;

	protected Label meLabel;
	protected Combo statusCombo;
	
	protected Table contactsTable;
	protected Listener contactsDataListener = new Listener() {
		public void handleEvent(Event e) {
			TableItem item = (TableItem)e.item;
			int index = contactsTable.indexOf(item);
			item.setData(filebox.getContacts().get(index));
			item.setText("Item "+ filebox.getContacts().get(index).getName());
		}
	};
	
	
	protected Button testButton;
	
	protected Filebox filebox;
	protected PropertyChangeListener propertiesListener = new PropertyChangeListener() {
		public void propertyChange(PropertyChangeEvent evt) {

			// application changed
			if ( evt.getSource() == getApplication() ) {
				if ( Filebox.MY_CONTACTS.equals(evt.getPropertyName()) ) {
					contactsTable.setItemCount(getApplication().getContactsCount());
				}
				return;
			}
			
			// preferences changed
			if (evt.getSource() == getApplication().getPreferences() ) {
				if ( Preferences.NAME.equals(evt.getPropertyName()) ) {
					meLabel.setText(getApplication().getPreferences().getName());
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
		meLabel.setText("Me" + ":");
		
		// status combo
		statusCombo = new Combo(meComposite, SWT.READ_ONLY);
		statusCombo.setLayoutData(new GridData(SWT.END, SWT.CENTER, true, false));
		statusCombo.setItems( new String[] { "On line", "Off line", "Don't disturb"} );
		statusCombo.select(1);
		
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
				getApplication().addContact(0, new Contact("Lolo"));
			}
		});
		
		
	}
	
	public void setFilebox(Filebox filebox) {
		if ( this.filebox != null ) {
			this.filebox.removePropertyChangeListener(propertiesListener);
			this.filebox.getPreferences().removePropertyChangeListener(propertiesListener);
		}
		if ( filebox != null ) {
			filebox.addPropertyChangeListener(propertiesListener);
			filebox.getPreferences().addPropertyChangeListener(propertiesListener);
		}
		this.filebox = filebox;
		
		meLabel.setText(filebox.getMe().getName() + ":");
	}

	public Filebox getApplication() {
		return filebox;
	}
	
}

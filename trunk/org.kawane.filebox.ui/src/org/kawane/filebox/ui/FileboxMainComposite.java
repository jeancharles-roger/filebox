/**
 * Filebox developed by Kawane.
 * LGPL License.
 */
package org.kawane.filebox.ui;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.eclipse.swt.SWT;
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
import org.kawane.filebox.core.FileboxApplication;

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
			item.setData(application.getContacts().get(index));
			item.setText("Item "+ application.getContacts().get(index).getName());
		}
	};
	
	
	protected Button testButton;
	
	protected FileboxApplication application;
	protected PropertyChangeListener propertiesListener = new PropertyChangeListener() {
		public void propertyChange(PropertyChangeEvent evt) {
			// do something only if application isn't null.
			if ( getApplication() == null ) return;
			
			System.out.println("Application property " + evt.getPropertyName() + " changed.");
			if ( evt.getSource() == application && FileboxApplication.MY_CONTACTS.equals(evt.getPropertyName()) ) {
				contactsTable.setItemCount(getApplication().getContactsCount());
			}
			if ( evt.getSource() == getApplication().getMe() && Contact.STATUS.equals(evt.getPropertyName()) ) {
				statusCombo.select(getApplication().getMe().getStatus().ordinal());
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
//		testButton = new Button(this, SWT.PUSH);
//		testButton.setText("Add contact");
//		testButton.addSelectionListener(new SelectionAdapter() {
//			@Override
//			public void widgetSelected(SelectionEvent e) {
//				getApplication().addContact(0, new Contact("Lolo"));
//			}
//		});
		
		
	}
	
	public void setApplication(FileboxApplication application) {
		if ( this.application != null ) this.application.removePropertyChangeListener(propertiesListener);
		if ( application != null ) application.addPropertyChangeListener(propertiesListener);
		this.application = application;
		
		meLabel.setText(application.getMe().getName() + ":");
	}

	public FileboxApplication getApplication() {
		return application;
	}
	
}

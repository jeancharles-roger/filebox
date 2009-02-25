package org.kawane.filebox.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FileboxApplication extends Observable {

	public static final String MY_CONTACTS = "myContacts";
	
	final protected Contact me;
	final private List<Contact> myContacts = new ArrayList<Contact>();

	public FileboxApplication(String name) {
		this.me = new Contact(name);
	}
	
	public int getContactsCount() {
		return myContacts.size();
	}
	
	public List<Contact> getContacts() {
		return Collections.unmodifiableList(myContacts);
	}
	
	public void addContact(int index, Contact newContact) {
		myContacts.add(index, newContact);
		getObservable().fireIndexedPropertyChange(MY_CONTACTS, index, null, newContact);
	}
	
	public Contact removeContact(int index) {
		Contact oldContact = myContacts.remove(index);
		getObservable().fireIndexedPropertyChange(MY_CONTACTS, index, oldContact, null);
		return oldContact;
	}
	
	public Contact getMe() {
		return me;
	}
	
}

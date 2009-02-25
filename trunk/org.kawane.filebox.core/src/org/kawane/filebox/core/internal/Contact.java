/**
 * Filebox developed by Kawane.
 * LGPL License.
 */

package org.kawane.filebox.core.internal;

/**
 * @author Jean-Charles Roger
 *
 */
public class Contact extends Observable {
	
	public static final String STATUS = "status";
	
	public enum Status {
		ONLINE,
		OFFLINE,
		DONOTDISTURB
	}
	
	protected String name;
	protected Status status;

	/**
	 * Creates a {@link Contact}
	 * @param name contact name
	 */
	public Contact(String name) {
		super();
		this.name = name;
		this.status = Status.OFFLINE;
	}

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		Status oldStatus = this.status;
		this.status = status;
		getObservable().firePropertyChange(STATUS, oldStatus, status);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
}

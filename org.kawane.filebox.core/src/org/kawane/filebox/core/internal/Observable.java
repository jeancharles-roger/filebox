package org.kawane.filebox.core.internal;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

public class Observable {

	private PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

	public void addPropertyChangeListener(PropertyChangeListener listener) {
		propertyChangeSupport.addPropertyChangeListener(listener);
	}
	
	public void removePropertyChangeListener(PropertyChangeListener listener) {
		propertyChangeSupport.removePropertyChangeListener(listener);
	}
	
	protected PropertyChangeSupport getObservable() {
		return propertyChangeSupport;
	}
	
}

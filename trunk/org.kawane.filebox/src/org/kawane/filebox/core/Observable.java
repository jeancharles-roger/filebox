package org.kawane.filebox.core;

import java.beans.IndexedPropertyChangeEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

public interface Observable {

	public void addPropertyChangeListener(PropertyChangeListener listener);

	public void removePropertyChangeListener(PropertyChangeListener listener);

	public class Stub implements Observable {
		private PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

		public void addPropertyChangeListener(PropertyChangeListener listener) {
			propertyChangeSupport.addPropertyChangeListener(listener);
		}

		public void removePropertyChangeListener(PropertyChangeListener listener) {
			propertyChangeSupport.removePropertyChangeListener(listener);
		}

		public void fireIndexedPropertyChange(Object source, String propertyName, int index, Object oldValue, Object newValue) {
			propertyChangeSupport.firePropertyChange(new IndexedPropertyChangeEvent(source, propertyName, oldValue, newValue, index));
		}

		public void firePropertyChange(Object source, String propertyName, Object oldValue, Object newValue) {
			propertyChangeSupport.firePropertyChange(new PropertyChangeEvent(source, propertyName, oldValue, newValue));
		}
	}
}

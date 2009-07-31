package org.kawane.filebox.core;

import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class FileboxRegistry implements Observable {

	public static final String FILEBOXES = "fileboxes";

	final private ArrayList<DistantFilebox> fileboxes = new ArrayList<DistantFilebox>();

	final protected Observable.Stub obs = new Observable.Stub();

	public FileboxRegistry () {
	}

	public int getFileboxesCount() {
		return fileboxes.size();
	}

	public List<DistantFilebox> getFileboxes() {
		List<DistantFilebox> result = new ArrayList<DistantFilebox>(fileboxes.size());
		for ( DistantFilebox desc : fileboxes ) {
			result.add(desc);
		}
		return Collections.unmodifiableList(result);
	}

	public DistantFilebox getFilebox(int index) {
		return fileboxes.get(index);
	}
	
	public DistantFilebox getFilebox(String id) {
		for ( DistantFilebox filebox : fileboxes ) {
			if ( filebox.getId().equals(id) ) {
				return filebox;
			}
		}
		return null;
	}

	public void addFilebox(DistantFilebox newFilebox) {
		addFilebox(0, newFilebox);
	}

	public void addFilebox(int index, DistantFilebox newFilebox) {
		fileboxes.add(index, newFilebox);
		obs.fireIndexedPropertyChange(this, FILEBOXES, index, null, newFilebox);
	}

	public DistantFilebox removeFilebox(DistantFilebox filebox) {
		int index = fileboxes.indexOf(filebox);
		if ( index < 0) return null;
		return removeFilebox(index);
	}

	public DistantFilebox removeFilebox(int index) {
		DistantFilebox oldFilebox = fileboxes.remove(index);
		obs.fireIndexedPropertyChange(this, FILEBOXES, index, oldFilebox, null);
		return oldFilebox;
	}
	
	public void addPropertyChangeListener(PropertyChangeListener listener) {
		obs.addPropertyChangeListener(listener);
	}

	public void removePropertyChangeListener(PropertyChangeListener listener) {
		obs.removePropertyChangeListener(listener);
	}

	public void registerFilebox(String name, String host, int port) {

		DistantFilebox desc = new DistantFilebox(name, host, port);
		if ( !fileboxes.contains(desc)) {
			addFilebox(desc);
		}
	}

	public void unregisterFilebox(String name, String host, int port) {
		DistantFilebox desc = new DistantFilebox(name, host, port);
		removeFilebox(desc);
	}
}

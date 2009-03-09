package org.kawane.filebox.core;

import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.kawane.services.IServiceListener;
import org.kawane.services.ServiceRegistry;



public class FileboxNetwork implements IObservable {

	public static final String FILEBOXES = "fileboxes";
	
	final private List<IFilebox> fileboxes = new ArrayList<IFilebox>();
	final private IServiceListener<IFilebox> serviceListener = new IServiceListener<IFilebox>(){
		
		public void serviceAdded(Class<IFilebox> clazz, IFilebox service) {
			addFilebox(service);
		}
	
		public void serviceRemoved(Class<IFilebox> clazz, IFilebox service) {
			removeFilebox(service);
		}
	};
	
	final protected IObservable.Stub obs = new IObservable.Stub();
	
	public FileboxNetwork () {
		ServiceRegistry.instance.addListener(IFilebox.class, serviceListener, true);
	}
	
	public int getFileboxesCount() {
		return fileboxes.size();
	}
	
	public List<IFilebox> getFileboxes() {
		return Collections.unmodifiableList(fileboxes);
	}
	
	public IFilebox getFilebox(int index) {
		return fileboxes.get(index);
	}
	
	public void addFilebox(IFilebox newFilebox) {
		addFilebox(0, newFilebox);
	}

	public void addFilebox(int index, IFilebox newFilebox) {
		fileboxes.add(index, newFilebox);
		obs.fireIndexedPropertyChange(this, FILEBOXES, index, null, newFilebox);
	}
	
	public IFilebox removeFilebox(IFilebox filebox) {
		int index = fileboxes.indexOf(filebox);
		if ( index < 0) return null;
		return removeFilebox(index);
	}
	
	public IFilebox removeFilebox(int index) {
		IFilebox oldFilebox = fileboxes.remove(index);
		obs.fireIndexedPropertyChange(this, FILEBOXES, index, oldFilebox, null);
		return oldFilebox;
	}
	
	public void clearFileboxes() {
		while ( !fileboxes.isEmpty() ) removeFilebox(0);
	}

	public void addPropertyChangeListener(PropertyChangeListener listener) {
		obs.addPropertyChangeListener(listener);
	}

	public void removePropertyChangeListener(PropertyChangeListener listener) {
		obs.removePropertyChangeListener(listener);
	}

	
	
}

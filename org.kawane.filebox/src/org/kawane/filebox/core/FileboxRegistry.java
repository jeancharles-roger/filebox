package org.kawane.filebox.core;

import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class FileboxRegistry implements Observable {

	public static final String FILEBOXES = "fileboxes";

	/** Internal descriptor for Fileboxes */
	static class FileboxDescriptor {
		public String name;
		public String host;
		public int port;
		public Filebox filebox;

		public FileboxDescriptor(String name, String host, int port, Filebox filebox) {
			this.name = name;
			this.host = host;
			this.port = port;
			this.filebox = filebox;
		}

		public boolean equals(Object obj) {
			if ( obj instanceof FileboxDescriptor ) {
				FileboxDescriptor fd = (FileboxDescriptor) obj;
				return 	(name == null ? fd.name == null : name.equals(fd.name)) &&
						(host == null ? fd.host == null : host.equals(fd.host)) &&
						port == fd.port;
			}
			return false;
		}
	}

	final private ArrayList<FileboxDescriptor> fileboxes = new ArrayList<FileboxDescriptor>();

	final protected Observable.Stub obs = new Observable.Stub();

	public FileboxRegistry () {
	}

	public int getFileboxesCount() {
		return fileboxes.size();
	}

	public List<Filebox> getFileboxes() {
		List<Filebox> result = new ArrayList<Filebox>(fileboxes.size());
		for ( FileboxDescriptor desc : fileboxes ) {
			result.add(desc.filebox);
		}
		return Collections.unmodifiableList(result);
	}

	public Filebox getFilebox(int index) {
		return fileboxes.get(index).filebox;
	}

	public void addFilebox(FileboxDescriptor newFilebox) {
		addFilebox(0, newFilebox);
	}

	public void addFilebox(int index, FileboxDescriptor newFilebox) {
		fileboxes.add(index, newFilebox);
		obs.fireIndexedPropertyChange(this, FILEBOXES, index, null, newFilebox.filebox);
	}

	public Filebox removeFilebox(FileboxDescriptor filebox) {
		int index = fileboxes.indexOf(filebox);
		if ( index < 0) return null;
		return removeFilebox(index);
	}

	public Filebox removeFilebox(int index) {
		FileboxDescriptor oldFilebox = fileboxes.remove(index);
		obs.fireIndexedPropertyChange(this, FILEBOXES, index, oldFilebox.filebox, null);
		return oldFilebox.filebox;
	}

	public void addPropertyChangeListener(PropertyChangeListener listener) {
		obs.addPropertyChangeListener(listener);
	}

	public void removePropertyChangeListener(PropertyChangeListener listener) {
		obs.removePropertyChangeListener(listener);
	}

	public void registerFilebox(String name, String host, int port) {

		FileboxDescriptor desc = new FileboxDescriptor(name, host, port, null);
		if ( !fileboxes.contains(desc)) {
			// TODO Retrieve Filebox remote info: deserialized from HTTP json
//				desc.filebox = (Filebox) LocateRegistry.getRegistry(host, port).lookup("filebox");
				addFilebox(desc);
		}
	}

	public void unregisterFilebox(String name, String host, int port) {
		FileboxDescriptor desc = new FileboxDescriptor(name, host, port, null);
		/* Filebox removedFilebox = */ removeFilebox(desc);
	}
}

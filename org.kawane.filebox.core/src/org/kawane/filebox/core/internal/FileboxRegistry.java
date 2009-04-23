package org.kawane.filebox.core.internal;

import java.beans.PropertyChangeListener;
import java.rmi.AccessException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.kawane.filebox.core.IFilebox;
import org.kawane.filebox.core.IFileboxRegistry;
import org.kawane.filebox.core.IObservable;
import org.kawane.services.Service;
import org.kawane.services.ServiceRegistry;


@Service(classes={IFileboxRegistry.class})
public class FileboxRegistry implements IObservable, IFileboxRegistry {

	public static final String FILEBOXES = "fileboxes";

	private static Logger logger = Logger.getLogger(FileboxRegistry.class.getName());


	/** Internal descriptor for Fileboxes */
	static class FileboxDescriptor {
		public String name;
		public String host;
		public int port;
		public IFilebox filebox;

		public FileboxDescriptor(String name, String host, int port, IFilebox filebox) {
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

	final protected IObservable.Stub obs = new IObservable.Stub();

	public FileboxRegistry () {
	}

	public int getFileboxesCount() {
		return fileboxes.size();
	}

	public List<IFilebox> getFileboxes() {
		List<IFilebox> result = new ArrayList<IFilebox>(fileboxes.size());
		for ( FileboxDescriptor desc : fileboxes ) {
			result.add(desc.filebox);
		}
		return Collections.unmodifiableList(result);
	}

	public IFilebox getFilebox(int index) {
		return fileboxes.get(index).filebox;
	}

	public void addFilebox(FileboxDescriptor newFilebox) {
		addFilebox(0, newFilebox);
	}

	public void addFilebox(int index, FileboxDescriptor newFilebox) {
		fileboxes.add(index, newFilebox);
		obs.fireIndexedPropertyChange(this, FILEBOXES, index, null, newFilebox.filebox);
	}

	public IFilebox removeFilebox(FileboxDescriptor filebox) {
		int index = fileboxes.indexOf(filebox);
		if ( index < 0) return null;
		return removeFilebox(index);
	}

	public IFilebox removeFilebox(int index) {
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
			try {
				desc.filebox = (IFilebox) LocateRegistry.getRegistry(host, port).lookup("filebox");
				addFilebox(desc);
				ServiceRegistry.instance.register(IFilebox.class, desc.filebox);
			} catch (AccessException e) {
				logger.log(Level.WARNING, "Can't register filebox.", e);
			} catch (RemoteException e) {
				logger.log(Level.WARNING, "Can't register filebox.", e);
			} catch (NotBoundException e) {
				logger.log(Level.WARNING, "Can't register filebox.", e);
			}
		}
	}

	public void unregisterFilebox(String name, String host, int port) {
		FileboxDescriptor desc = new FileboxDescriptor(name, host, port, null);
		IFilebox removedFilebox = removeFilebox(desc);
		if ( removedFilebox != null ) {
			ServiceRegistry.instance.unregister(IFilebox.class, removedFilebox);
		}
	}
}

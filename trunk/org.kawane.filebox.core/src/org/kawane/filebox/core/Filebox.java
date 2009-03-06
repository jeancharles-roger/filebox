package org.kawane.filebox.core;

import java.io.File;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.kawane.filebox.core.discovery.IConnectionListener;
import org.kawane.filebox.core.discovery.IServiceDiscovery;
import org.kawane.filebox.core.internal.Activator;
import org.kawane.services.IServiceListener;
import org.kawane.services.ServiceRegistry;
import org.kawane.services.advanced.Inject;

public class Filebox extends Observable implements IFilebox {

	private static Logger logger = Logger.getLogger(Activator.class.getName());
	
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
	
	final protected Preferences preferences;
	
	protected String name;
	protected String host;
	protected int port;
	
	protected boolean connected = false;
	protected Registry registry = null;
	
	private IServiceDiscovery serviceDiscovery;
	
	public Filebox(File configurationFile) {
		preferences = new Preferences(configurationFile);
		String preferencesName = preferences.getName();
		this.name = preferencesName == null ? "Me" : preferencesName;
		
		this.port = preferences.getPort();
		this.host = "localhost";
	}
	
	@Inject
	public void setServiceDiscovery(IServiceDiscovery serviceDiscovery) {
		this.serviceDiscovery = serviceDiscovery;
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
		getObservable().fireIndexedPropertyChange(FILEBOXES, index, null, newFilebox);
	}
	
	public IFilebox removeFilebox(IFilebox filebox) {
		int index = fileboxes.indexOf(filebox);
		return removeFilebox(index);
	}
	
	public IFilebox removeFilebox(int index) {
		IFilebox oldFilebox = fileboxes.remove(index);
		getObservable().fireIndexedPropertyChange(FILEBOXES, index, oldFilebox, null);
		return oldFilebox;
	}
	
	public void clearFileboxes() {
		while ( !fileboxes.isEmpty() ) removeFilebox(0);
	}
	
	public Preferences getPreferences() {
		return preferences;
	}
	
	
	public String getHost() throws RemoteException{
		return host;
	}
	
	public void setHost(String host) {
		String oldValue = this.host;
		this.host = host;
		getObservable().firePropertyChange(HOST, oldValue, host);
	}
	
	public int getPort() throws RemoteException{
		return port;
	}
	
	public void setPort(int port) {
		int oldValue = this.port;
		this.port = port;
		getObservable().firePropertyChange(PORT, oldValue, port);
	}
	
	public String getName() throws RemoteException{
		return name;
	}
	
	public void setName(String name) {
		String oldValue = this.name;
		this.name = name;
		getObservable().firePropertyChange(NAME, oldValue, name);
	}
	
	/** connects this to fileboxes network */
	// TODO throws Exception
	public void connect() {
		if ( connected ) return;
		
		// publish object on rmi 
		try {
			registry = LocateRegistry.createRegistry(getPort());
			UnicastRemoteObject.exportObject(this, getPort());
			registry.rebind("filebox", this);
		} catch (RemoteException e) {
			logger.log(Level.SEVERE, "Can't connect Filebox", e);
		}

		serviceDiscovery.connect(name, port, new IConnectionListener () {
			public void connected(IServiceDiscovery serviceDiscovery) {
				connected = true;
				setHost(serviceDiscovery.getHostname());
			}
			public void disconnected(IServiceDiscovery serviceDiscovery) {}
		});
	}
	
	/** disconnects this from fileboxes network */
	public void disconnect() {
		if ( !connected ) return;
		
		// remove object from rmi 
		try {
			UnicastRemoteObject.unexportObject(this, true);
			UnicastRemoteObject.unexportObject(registry, true);
			registry.unbind("filebox");
			registry = null;
		} catch (RemoteException e) {
			logger.log(Level.SEVERE, "Can't disconnect Filebox", e);
		} catch (NotBoundException e) {
			logger.log(Level.SEVERE, "Can't disconnect Filebox", e);
		}

		
		serviceDiscovery.disconnect( new IConnectionListener () {
			public void connected(IServiceDiscovery serviceDiscovery) {}
			public void disconnected(IServiceDiscovery serviceDiscovery) {
				setHost("localhost");
				connected = false;
			}
		});
		
	}
	
	/** @return true if the filebox is connected. */
	public boolean isConnected() throws RemoteException {
		return connected;
	}
}

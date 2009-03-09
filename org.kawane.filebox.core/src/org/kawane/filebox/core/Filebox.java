package org.kawane.filebox.core;

import java.beans.PropertyChangeListener;
import java.io.File;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.kawane.filebox.core.discovery.IConnectionListener;
import org.kawane.filebox.core.discovery.IServiceDiscovery;
import org.kawane.filebox.core.internal.Activator;
import org.kawane.services.advanced.Inject;

public class Filebox implements IFilebox, IObservable {

	private static Logger logger = Logger.getLogger(Activator.class.getName());
	
	final protected Preferences preferences;
	
	protected String name;
	protected String host;
	protected int port;
	
	final protected FileboxNetwork network = new FileboxNetwork();
	
	private IServiceDiscovery serviceDiscovery;

	protected boolean connected = false;
	protected Registry registry = null;

	final protected IObservable.Stub obs = new IObservable.Stub();
	
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
		obs.firePropertyChange(this, HOST, oldValue, host);
	}
	
	public int getPort() throws RemoteException{
		return port;
	}
	
	public void setPort(int port) {
		int oldValue = this.port;
		this.port = port;
		obs.firePropertyChange(this, PORT, oldValue, port);
	}
	
	public String getName() throws RemoteException{
		return name;
	}
	
	public void setName(String name) {
		String oldValue = this.name;
		this.name = name;
		obs.firePropertyChange(this, NAME, oldValue, name);
	}
	
	public FileboxNetwork getNetwork() {
		return network;
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

	public void addPropertyChangeListener(PropertyChangeListener listener) {
		obs.addPropertyChangeListener(listener);
	}

	public void removePropertyChangeListener(PropertyChangeListener listener) {
		obs.removePropertyChangeListener(listener);
	}
	
	
}

package org.kawane.filebox.core;

import java.io.File;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.kawane.filebox.core.discovery.IFileboxServiceListener;
import org.kawane.filebox.core.discovery.IServiceDiscovery;
import org.kawane.filebox.core.internal.Activator;

public class Filebox extends Observable implements IFilebox {

	public static final String FILEBOXES = "fileboxes";
	
	final private List<IFilebox> fileboxes = new ArrayList<IFilebox>();
	final private IFileboxServiceListener serviceListener = new IFileboxServiceListener(){
		
		public void serviceAdded(IFilebox service) {
			addFilebox(service);
		}
	
		public void serviceRemoved(IFilebox service) {
			removeFilebox(service);
		}
	};
	
	final protected Preferences preferences;
	
	protected String name;
	protected String host;
	protected int port;
	protected final Map<String, String> properties = new HashMap<String, String>();
	protected boolean connected = false;
	
	public Filebox(File configurationFile) {
		preferences = new Preferences(configurationFile);
		String preferencesName = preferences.getName();
		this.name = preferencesName == null ? "Me" : preferencesName;
		
		this.port = preferences.getPort();
		this.host = "localhost";
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
	public void connect() {
		if ( connected ) return;
		IServiceDiscovery serviceDiscovery = Activator.getInstance().getServiceDiscovery();
		for ( IFilebox oneFilebox : serviceDiscovery.getServices() ) {
			addFilebox(oneFilebox);
		}
		serviceDiscovery.addServiceListener(serviceListener);
		connected = true;
		setHost(serviceDiscovery.getHostname());
	}
	
	/** disconnects this from fileboxes network */
	public void disconnect() {
		if ( !connected ) return;
		IServiceDiscovery serviceDiscovery = Activator.getInstance().getServiceDiscovery();
		serviceDiscovery.removeServiceListener(serviceListener);
		clearFileboxes();
		setHost("localhost");
		connected = false;
	}
	
	/** @return true if the filebox is connected. */
	public boolean isConnected() throws RemoteException {
		return connected;
	}
}

package org.kawane.filebox.core;

import java.beans.PropertyChangeListener;

import org.kawane.filebox.core.discovery.ConnectionListener;
import org.kawane.filebox.core.discovery.ServiceDiscovery;

public class Filebox implements  Observable {

	public static final String NAME = "name";
	public static final String HOST = "host";
	public static final String PORT = "port";
	public static final String PROPERTIES = "properties";

	final protected Observable.Stub obs = new Observable.Stub();

	final private Preferences preferences = Globals.getPreferences();

	protected String name;
	protected String host;
	protected int port;

	protected boolean connected = false;

	public Filebox() {
		String preferencesName = preferences.getName();
		this.name = preferencesName == null ? "Me" : preferencesName;

		this.port = preferences.getPort();
		this.host = "localhost";
	}


	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		String oldValue = this.host;
		this.host = host;
		obs.firePropertyChange(this, HOST, oldValue, host);
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		int oldValue = this.port;
		this.port = port;
		obs.firePropertyChange(this, PORT, oldValue, port);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		String oldValue = this.name;
		this.name = name;
		obs.firePropertyChange(this, NAME, oldValue, name);
	}

	/** connects this to fileboxes network */
	public void connect() {
		if ( connected ) return;
		Globals.getHttpServer().start();
		Globals.getServiceDiscovery().connect(getName(), getPort(), new ConnectionListener () {
			public void connected(ServiceDiscovery serviceDiscovery) {
				connected = true;
				setHost(serviceDiscovery.getHostname());
			}
			public void disconnected(ServiceDiscovery serviceDiscovery) {}
		});

	}

	/** disconnects this from fileboxes network */
	public void disconnect() {
		if ( !connected ) return;
		Globals.getHttpServer().stop();
		Globals.getServiceDiscovery().disconnect(new ConnectionListener() {
			public void connected(ServiceDiscovery serviceDiscovery) {}
			public void disconnected(ServiceDiscovery serviceDiscovery) {
				setHost("localhost");
				connected = false;
			}
		});
	}

	/** @return true if the filebox is connected. */
	public boolean isConnected() {
		return connected;
	}

	public void addPropertyChangeListener(PropertyChangeListener listener) {
		obs.addPropertyChangeListener(listener);
	}

	public void removePropertyChangeListener(PropertyChangeListener listener) {
		obs.removePropertyChangeListener(listener);
	}
}

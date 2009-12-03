package org.kawane.filebox.core;

import java.beans.PropertyChangeListener;

import org.kawane.filebox.core.discovery.ConnectionListener;
import org.kawane.filebox.core.discovery.ServiceDiscovery;

public class Filebox implements  Observable {

	public static final String NAME = "name";
	public static final String HOST = "host";
	public static final String PORT = "port";
	public static final String STATE = "state";
	public static final String PROPERTIES = "properties";

	public static final int PENDING = 0;
	public static final int CONNECTED = 1;
	public static final int DISCONNECTED = 2;

	final protected Observable.Stub obs = new Observable.Stub();

	final private Preferences preferences;

	protected String name;
	protected String host;
	protected int port;

	
	protected int state = DISCONNECTED;

	public Filebox(Preferences preferences) {
		this.preferences = preferences;
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
	public void connect(final ConnectionListener listener) {
		if ( getState() != DISCONNECTED ) return;
		setState(PENDING);
		Globals.getHttpServer().start();
		Globals.getServiceDiscovery().connect(getName(), getPort(), new ConnectionListener () {
			public void connected(ServiceDiscovery serviceDiscovery) {
				setHost(serviceDiscovery.getHostname());
				setState(CONNECTED);
				if(listener != null){
					listener.connected(serviceDiscovery);
				}
			}
			public void disconnected(ServiceDiscovery serviceDiscovery) {}
		});

	}

	/** disconnects this from fileboxes network */
	public void disconnect(final ConnectionListener listener) {
		if ( getState() != CONNECTED ) return;
		setState(PENDING);
		Globals.getHttpServer().stop();
		Globals.getServiceDiscovery().disconnect(new ConnectionListener() {
			public void connected(ServiceDiscovery serviceDiscovery) {}
			public void disconnected(ServiceDiscovery serviceDiscovery) {
				setHost("localhost");
				setState(DISCONNECTED);
				if(listener != null){
					listener.disconnected(serviceDiscovery);
				}
			}
		});
	}
	
	/** Disconnect and re-connect filebox */
	public void reconnect() {
		if (getState() == Filebox.CONNECTED) {
			disconnect(new ConnectionListener() {
				public void disconnected(ServiceDiscovery serviceDiscovery) {
					connect(null);
				}
				public void connected(ServiceDiscovery serviceDiscovery) {
				}
			});
		}
	}
	
	private void setState(int state) {
		int oldValue = this.state;
		this.state = state;
		obs.firePropertyChange(this, STATE, oldValue, state);
	}

	/** @return the filebox state, PENDING, CONNECTED, DISCONNECTED. */
	public int getState() {
		return state;
	}

	public void addPropertyChangeListener(PropertyChangeListener listener) {
		obs.addPropertyChangeListener(listener);
	}

	public void removePropertyChangeListener(PropertyChangeListener listener) {
		obs.removePropertyChangeListener(listener);
	}
}

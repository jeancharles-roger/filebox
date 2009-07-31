package org.kawane.filebox.core.discovery;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceEvent;
import javax.jmdns.ServiceInfo;
import javax.jmdns.ServiceListener;
import javax.jmdns.ServiceTypeListener;

import org.kawane.filebox.core.Filebox;
import org.kawane.filebox.core.Globals;

public class JmDNSServiceDiscovery implements ServiceListener, ServiceDiscovery, ServiceTypeListener {

	private static Logger logger = Logger.getLogger(JmDNSServiceDiscovery.class.getName());

	Map<String, Filebox> fileBoxes = new HashMap<String, Filebox>();

	private JmDNS dns;
	private ServiceInfo serviceInfo;
	private String name;
	private int port;
	private Object waitInitialization = new Object();

	public String getName() {
		return name;
	}

	public int getPort() {
		return port;
	}

	public String getHostname() {
		return dns.getHostName();
	}

	public void connect(String lname, int lport,
			final ConnectionListener listener) {
		this.name = lname;
		this.port = lport;
		synchronized (waitInitialization) {
			Thread thread = new Thread() {
				@Override
				public void run() {
					try {
						serviceInfo = ServiceInfo.create(FILEBOX_TYPE, name, port, "");
						dns.registerService(serviceInfo);
						if ( listener != null ) listener.connected(JmDNSServiceDiscovery.this);
					} catch (IOException e) {
						logger.log(Level.SEVERE, "An Error Occured", e);
					}
				}
			};
			thread.start();
		}
	}

	public void disconnect(final ConnectionListener listener) {
		synchronized (waitInitialization) {
			Thread thread = new Thread() {
				@Override
				public void run() {
					if (serviceInfo != null) {
						dns.unregisterService(serviceInfo);
						serviceInfo = null;
					}
					if(listener != null) {
						listener.disconnected(JmDNSServiceDiscovery.this);
					}
				}
			};
			thread.start();
		}
	}

	public void start() {
		try {
			dns = JmDNS.create();
			dns.addServiceTypeListener(JmDNSServiceDiscovery.this);
		} catch (Throwable e) {
			logger.log(Level.SEVERE, "An Error Occured", e);
		}
	}

	public void stop() {
		if (dns != null) {
			// synchronized (waitInitialization) {
			// TODO is this close method call really nesessary in zeroconf
			// protocol: http://www.zeroconf.org/
			dns.close();
			// }
		}
	}

	public void serviceTypeAdded(ServiceEvent event) {
		if (event.getType().equals(FILEBOX_TYPE)) {
			dns.addServiceListener(FILEBOX_TYPE, JmDNSServiceDiscovery.this);
		}
	}

	public void serviceAdded(final ServiceEvent event) {
		if (event.getInfo() == null) {
			Thread thread = new Thread(new Runnable() {
				public void run() {
					ServiceInfo info = dns.getServiceInfo(event.getType(), event.getName());
					while (info == null) {
						dns.requestServiceInfo(event.getType(), event.getName());
						info = dns.getServiceInfo(event.getType(), event.getName());
					}
					Globals.getFileboxRegistry().registerFilebox(event.getName(), info.getHostAddress(), info.getPort());
				}
			});
			// this thread forces to resolve the service.
			thread.start();
		} else {
			// register the service now
			Globals.getFileboxRegistry().registerFilebox(event.getName(), event.getInfo().getHostAddress(), event.getInfo().getPort());
		}
	}

	public void serviceRemoved(ServiceEvent event) {
		ServiceInfo info = event.getDNS().getServiceInfo(FILEBOX_TYPE, event.getName());
		Globals.getFileboxRegistry().unregisterFilebox(event.getName(), info.getHostAddress(), info.getPort());
	}

	public void serviceResolved(ServiceEvent event) {
		// do nothing
	}
}

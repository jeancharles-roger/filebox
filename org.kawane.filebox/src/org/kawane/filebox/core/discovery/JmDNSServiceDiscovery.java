package org.kawane.filebox.core.discovery;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceEvent;
import javax.jmdns.ServiceInfo;
import javax.jmdns.ServiceListener;
import javax.jmdns.ServiceTypeListener;

import org.kawane.filebox.core.Globals;

public class JmDNSServiceDiscovery implements ServiceListener, ServiceDiscovery, ServiceTypeListener {

	private static Logger logger = Logger.getLogger(JmDNSServiceDiscovery.class.getName());

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
					try {
						if (serviceInfo != null) {
							dns.unregisterService(serviceInfo);
							serviceInfo = null;
						}
						if(listener != null) {
							listener.disconnected(JmDNSServiceDiscovery.this);
						}
					} catch (Exception e) {
						logger.log(Level.SEVERE, "An Error Occured", e);
					}
				}
			};
			thread.start();
		}
	}

	public void start() {
		try {
			dns = JmDNS.create();
			dns.addServiceListener(FILEBOX_TYPE, JmDNSServiceDiscovery.this);
			dns.registerServiceType(FILEBOX_TYPE);
			dns.addServiceTypeListener(JmDNSServiceDiscovery.this);
			ServiceInfo[] serviceInfos = dns.list(FILEBOX_TYPE);
			for (ServiceInfo serviceInfo : serviceInfos) {
				serviceAdded(serviceInfo.getType(), serviceInfo.getName(), serviceInfo);
			}
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
			dns.removeServiceListener(FILEBOX_TYPE, JmDNSServiceDiscovery.this);
			dns.addServiceListener(FILEBOX_TYPE, JmDNSServiceDiscovery.this);
		}
	}

	public void serviceAdded(final ServiceEvent event) {
		serviceAdded(event.getType(), event.getName(), event.getInfo());
	}

	private void serviceAdded(final String type, final String name, final ServiceInfo info) {
		if (info == null) {
			Thread thread = new Thread(new Runnable() {
				public void run() {
					ServiceInfo info = dns.getServiceInfo(type, name);
					while (info == null) {
						dns.requestServiceInfo(type, name);
						info = dns.getServiceInfo(type, name);
					}
					Globals.getFileboxRegistry().registerFilebox(name, info.getHostAddress(), info.getPort());
				}
			});
			// this thread forces to resolve the service.
			thread.start();
		} else {
			// register the service now
			Globals.getFileboxRegistry().registerFilebox(name, info.getHostAddress(), info.getPort());
		}
	}

	public void serviceRemoved(ServiceEvent event) {
		ServiceInfo info = event.getDNS().getServiceInfo(FILEBOX_TYPE, event.getName());
		Globals.getFileboxRegistry().unregisterFilebox(event.getName(), info.getHostAddress(), info.getPort());
	}

	public void serviceResolved(ServiceEvent event) {
		// do nothing
		
	}

	public void printServices() {
		dns.printServices();
//		ServiceInfo[] serviceInfos = dns.list(FILEBOX_TYPE);
//		for (ServiceInfo serviceInfo : serviceInfos) {
//			serviceAdded(serviceInfo.getType(), serviceInfo.getName(), serviceInfo);
//		}
	}
}

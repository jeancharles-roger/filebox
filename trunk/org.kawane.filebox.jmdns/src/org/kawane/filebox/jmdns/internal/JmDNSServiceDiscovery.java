package org.kawane.filebox.jmdns.internal;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceEvent;
import javax.jmdns.ServiceInfo;
import javax.jmdns.ServiceListener;
import javax.jmdns.ServiceTypeListener;

import org.kawane.filebox.core.IFilebox;
import org.kawane.filebox.core.IFileboxRegistry;
import org.kawane.filebox.core.discovery.IConnectionListener;
import org.kawane.filebox.core.discovery.IServiceDiscovery;
import org.kawane.services.IServiceRegistry;
import org.kawane.services.Service;
import org.kawane.services.advanced.Inject;

@Service(value=IServiceDiscovery.class, depends=IFileboxRegistry.class)
public class JmDNSServiceDiscovery implements ServiceListener,
		IServiceDiscovery, ServiceTypeListener {

	private static Logger logger = Logger.getLogger(JmDNSServiceDiscovery.class
			.getName());

	Map<String, IFilebox> fileBoxes = new HashMap<String, IFilebox>();

	private JmDNS dns;
	private ServiceInfo serviceInfo;
	private String name;
	private int port;
	private Object waitInitialization = new Object();
	private IFileboxRegistry fileboxRegistry;

	public String getName() {
		return name;
	}

	public int getPort() {
		return port;
	}

	public String getHostname() {
		return dns.getHostName();
	}

	@Inject
	public int setFileboxRegistry(IFileboxRegistry registry) {
		this.fileboxRegistry = registry;
		return IServiceRegistry.DEPENDENCY_RESOLVED;
	}

	public void connect(String lname, int lport,
			final IConnectionListener listener) {
		this.name = lname;
		this.port = lport;
		synchronized (waitInitialization) {
			Thread thread = new Thread() {
				@Override
				public void run() {
					try {
						serviceInfo = ServiceInfo.create(FILEBOX_TYPE, name,
								port, "");
						dns.registerService(serviceInfo);
						listener.connected(JmDNSServiceDiscovery.this);
					} catch (IOException e) {
						logger.log(Level.SEVERE, "An Error Occured", e);
					}
				}
			};
			thread.start();
		}
	}

	public void disconnect(final IConnectionListener listener) {
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
			// dns.close();
			// }
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.kawane.filebox.core.IServiceDiscovery#getServices()
	 */
	public Collection<IFilebox> getServices() {
		return fileboxRegistry.getFileboxes();
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
					fileboxRegistry.registerFilebox(event.getName(), info.getHostAddress(), info.getPort());
				}
			});
			// this thread forces to resolve the service.
			thread.start();
		} else {
			// register the service now
			fileboxRegistry.registerFilebox(event.getName(), event.getInfo().getHostAddress(), event.getInfo().getPort());
		}
	}

	public void serviceRemoved(ServiceEvent event) {
		ServiceInfo info = event.getDNS().getServiceInfo(FILEBOX_TYPE, event.getName());
		fileboxRegistry.unregisterFilebox(event.getName(), info.getHostAddress(), info.getPort());
	}

	public void serviceResolved(ServiceEvent event) {
		// do nothing
	}
}

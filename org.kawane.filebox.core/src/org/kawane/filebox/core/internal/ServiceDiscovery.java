package org.kawane.filebox.core.internal;

import java.io.IOException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceEvent;
import javax.jmdns.ServiceInfo;
import javax.jmdns.ServiceListener;

import org.kawane.filebox.core.IFilebox;
import org.kawane.filebox.core.discovery.IFileboxServiceListener;
import org.kawane.filebox.core.discovery.IServiceDiscovery;

public class ServiceDiscovery implements ServiceListener, IServiceDiscovery {

	private static Logger logger = Logger.getLogger(ServiceDiscovery.class.getName());
	
	private JmDNS dns;
	private ServiceInfo serviceInfo;
	private String name;
	private Collection<IFileboxServiceListener> listeners = new HashSet<IFileboxServiceListener>();
	private Map<String, String> properties;
	private int port;
	private Object waitInitialization = new Object();

	public ServiceDiscovery(String name, int port, Map<String, String> properties) {
		this.name = name;
		this.port = port;
		this.properties = properties;
	}

	public String getName() {
		return name;
	}

	public int getPort() {
		return port;
	}

	public Map<String, String> getProperties() {
		return properties;
	}

	public String getHostname() {
		return dns.getHostName();
	}

	public void apply(String name, int port, Map<String, String> properties) {
		this.name = name;
		this.port = port;
		this.properties = properties;
		if(dns ==null) {
			start();
		} else {
			synchronized (waitInitialization) {
				Thread thread = new Thread() {
					@Override
					public void run() {
						try {
							dns.unregisterService(serviceInfo);
							dns.registerService(serviceInfo);
						} catch (IOException e) {
							logger.log(Level.SEVERE, "An Error Occured", e);
						}
					}
				};
				thread.start();
			}
		}
	}

	public void start() {
		Timer timer = new Timer();
		TimerTask task = new TimerTask() {
			public void run() {
				try {
					synchronized (waitInitialization) {
						dns = JmDNS.create();
						serviceInfo = ServiceInfo.create(FILEBOX_TYPE, name, port, FILEBOX_WEIGHT, FILEBOX_PRIORITY, new Hashtable<String, String>(
								properties));
						dns.addServiceListener(FILEBOX_TYPE, ServiceDiscovery.this);
						dns.registerService(serviceInfo);
//						// first call of it is always slow
//						getServices();
					}
				} catch (Throwable e) {
					logger.log(Level.SEVERE, "An Error Occured", e);
				}
			}
		};
		timer.schedule(task, 500);
	}

	public void stop() {
		if (dns != null) {
			//			synchronized (waitInitialization) {
			//TODO is this close method call really nesessary in zeroconf protocol: http://www.zeroconf.org/
			//				dns.close();
			//			}
		}
	}

	/* (non-Javadoc)
	 * @see org.kawane.filebox.core.IServiceDiscovery#getServices()
	 */
	public Collection<IFilebox> getServices() {
		Collection<IFilebox> services = new ArrayList<IFilebox>();
		if (dns != null) {
			ServiceInfo[] servicesInfo = dns.list(FILEBOX_TYPE);
			for (ServiceInfo serviceInfo : servicesInfo) {
				IFilebox fileboxService = createFileboxService(serviceInfo);
				services.add(fileboxService);
			}
		}
		return services;
	}

	/* (non-Javadoc)
	 * @see org.kawane.filebox.core.IServiceDiscovery#addServiceListener(org.kawane.filebox.core.discovery.IFileboxServiceListener)
	 */
	public void addServiceListener(IFileboxServiceListener listener) {
		listeners.add(listener);
	}

	/* (non-Javadoc)
	 * @see org.kawane.filebox.core.IServiceDiscovery#removeServiceListener(org.kawane.filebox.core.discovery.IFileboxServiceListener)
	 */
	public void removeServiceListener(IFileboxServiceListener listener) {
		listeners.remove(listener);
	}

	private IFilebox createFileboxService(ServiceInfo serviceInfo) {
		Map<String, String> properties = new HashMap<String, String>();
		@SuppressWarnings("unchecked")
		Enumeration<String> propertyNames = serviceInfo.getPropertyNames();
		while (propertyNames.hasMoreElements()) {
			String propertyName = propertyNames.nextElement();
			properties.put(propertyName, serviceInfo.getPropertyString(propertyName));
		}
		try  {
			
			
//			IFilebox fileboxService = new DistantFilebox(serviceInfo.getName(), serviceInfo.getHostAddress(), serviceInfo.getPort());
//			String url = "rmi://localhost" + ":" + serviceInfo.getPort() + "/" + serviceInfo.getName();
			//String url = serviceInfo.getName();
			IFilebox fileboxService = (IFilebox) LocateRegistry.getRegistry(serviceInfo.getHostAddress(), serviceInfo.getPort()).lookup(serviceInfo.getName());
			
			return fileboxService;
		} catch (RemoteException e) {
			logger.log(Level.SEVERE, "Can't connect Filebox", e);
			return null;
		} catch (NotBoundException e) {
			logger.log(Level.SEVERE, "Can't connect Filebox", e);
			return null;
		}
	}

	public void serviceAdded(ServiceEvent event) {
		System.out.println("*********************************************************");
		// the service is added but not resolved, not interesting for our application
		System.out.println("a service have been added" + event);

		if (event.getInfo() != null) {
			System.out.println("A service has been added: " + event.getInfo().getName() + " on " + event.getInfo().getHostAddress());
		} else {
			event.getDNS().requestServiceInfo(event.getType(), event.getName());
		}
	}

	public void serviceRemoved(ServiceEvent event) {
		System.out.println("*********************************************************");
		System.out.println("A service has been Removed: " + event);
		HashSet<IFileboxServiceListener> listenersCopy = new HashSet<IFileboxServiceListener>(listeners);
		for (IFileboxServiceListener listener : listenersCopy) {
			listener.serviceRemoved(createFileboxService(event.getInfo()));
		}
	}

	public void serviceResolved(ServiceEvent event) {
		System.out.println("*********************************************************");
		System.out.println("A service has been resolved: " + event);
		HashSet<IFileboxServiceListener> listenersCopy = new HashSet<IFileboxServiceListener>(listeners);
		for (IFileboxServiceListener listener : listenersCopy) {
			listener.serviceAdded(createFileboxService(event.getInfo()));
		}
	}

}

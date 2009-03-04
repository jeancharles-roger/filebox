package org.kawane.filebox.core.internal;

import java.io.IOException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
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
import org.kawane.filebox.core.discovery.IConnectionListener;
import org.kawane.filebox.core.discovery.IServiceDiscovery;
import org.kawane.services.ServiceRegistry;

public class ServiceDiscovery implements ServiceListener, IServiceDiscovery {

	private static Logger logger = Logger.getLogger(ServiceDiscovery.class.getName());
	
	HashMap<ServiceInfo, IFilebox> fileBoxes = new HashMap<ServiceInfo, IFilebox>();
	
	private JmDNS dns;
	private ServiceInfo serviceInfo;
	private String name;
	private Map<String, String> properties;
	private int port;
	private Object waitInitialization = new Object();

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

	public void connect(String lname, int lport, Map<String, String> lproperties, final IConnectionListener listener) {
		this.name = lname;
		this.port = lport;
		this.properties = lproperties;
		synchronized (waitInitialization) {
			Thread thread = new Thread() {
				@Override
				public void run() {
					try {
						serviceInfo = ServiceInfo.create(FILEBOX_TYPE, name, port, FILEBOX_WEIGHT, FILEBOX_PRIORITY, new Hashtable<String, String>(
								properties));
						dns.registerService(serviceInfo);
						listener.connected(ServiceDiscovery.this);
					} catch (IOException e) {
						logger.log(Level.SEVERE, "An Error Occured", e);
					}
				}
			};
			thread.start();
		}
	}
	
	public void disconnect() {
		dns.unregisterService(serviceInfo);
	}

	public void start() {
		Timer timer = new Timer();
		TimerTask task = new TimerTask() {
			public void run() {
				try {
					synchronized (waitInitialization) {
						dns = JmDNS.create();
						dns.addServiceListener(FILEBOX_TYPE, ServiceDiscovery.this);
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
		if(logger.isLoggable(Level.FINEST)) {
			StringBuffer out = new StringBuffer();
			out.append("*********************************************************\n");
			// the service is added but not resolved, not interesting for our application
			out.append("a service have been added" + event);
			out.append('\n');
			if (event.getInfo() != null) {
				out.append("A service has been added: " + event.getInfo().getName() + " on " + event.getInfo().getHostAddress());
				out.append('\n');
			}
			logger.finest(out.toString());
		}
		if (event.getInfo() == null){
				event.getDNS().requestServiceInfo(event.getType(), event.getName());
		}else{
			// register !!
			logger.finest("registered");
		}
	}

	public void serviceRemoved(ServiceEvent event) {
		if(logger.isLoggable(Level.FINEST)) {
			StringBuffer out = new StringBuffer();
			out.append("*********************************************************");
			out.append('\n');
			out.append("A service has been Removed: " + event);
			out.append('\n');
			logger.finest(out.toString());
		}
		ServiceRegistry.instance.unregister(fileBoxes.get(event.getInfo()));
	}

	public void serviceResolved(ServiceEvent event) {
		if(logger.isLoggable(Level.FINEST)) {
			StringBuffer out = new StringBuffer();
			out.append("*********************************************************");
			out.append('\n');
			out.append("A service has been resolved: " + event);
			out.append('\n');
			logger.finest(out.toString());
		}
		IFilebox filebox = createFileboxService(event.getInfo());
		fileBoxes.put(event.getInfo(), filebox);
		ServiceRegistry.instance.register(IFilebox.class,filebox);
	}

}

package org.kawane.filebox.core.internal;

import java.io.File;
import java.rmi.Naming;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;

import org.kawane.filebox.core.LocalFilebox;
import org.kawane.filebox.core.discovery.IServiceDiscovery;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.service.log.LogService;
import org.osgi.util.tracker.ServiceTracker;

public class Activator implements BundleActivator {

	protected static final String CONFIG_FILENAME = "filebox.properties";

	static private Activator instance;
	
	private ServiceTracker logTracker;
	private ServiceDiscovery serviceDiscovery;

	protected File configurationFile;
	
	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		instance = this;
		logTracker = new ServiceTracker(context, LogService.class.getName(), null);
		logTracker.open();
		
		// configuration file
		configurationFile = context.getDataFile(CONFIG_FILENAME);
		
		// properties associated with the profile
		HashMap<String, String> properties = new HashMap<String, String>();
		
		// initialize filebox application
		LocalFilebox filebox = new LocalFilebox(configurationFile);
		context.registerService(LocalFilebox.class.getName(), filebox, null);
	
		// publish object on rmi 
		UnicastRemoteObject.exportObject(filebox, filebox.getPort());
		Naming.rebind(ServiceDiscovery.FILEBOX_TYPE, filebox);
		
//		properties.put(filebox.getStatus().getClass().getSimpleName(), filebox.getStatus().toString());
		serviceDiscovery = new ServiceDiscovery(filebox.getName(), IServiceDiscovery.DEFAULT_PORT, properties);
		// automatically connect to the network for now
		serviceDiscovery.start();
		context.registerService(IServiceDiscovery.class.getName(), serviceDiscovery, null);
	}

	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		instance = null;
		serviceDiscovery.stop();
	}
	
	static public Activator getInstance() {
		return instance;
	}
	
	public ServiceDiscovery getServiceDiscovery() {
		return serviceDiscovery;
	}
	
	public LogService getLogger() {
		LogService logger = (LogService) logTracker.getService();
		return logger;
	}
	

}

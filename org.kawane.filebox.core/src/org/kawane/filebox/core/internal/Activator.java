package org.kawane.filebox.core.internal;

import java.util.HashMap;

import org.kawane.filebox.core.Contact;
import org.kawane.filebox.core.FileboxApplication;
import org.kawane.filebox.core.discovery.IServiceDiscovery;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.service.log.LogService;
import org.osgi.util.tracker.ServiceTracker;

public class Activator implements BundleActivator {

	static private Activator instance;
	private ServiceTracker logTracker;
	private ServiceDiscovery serviceDiscovery;

	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		instance = this;
		logTracker = new ServiceTracker(context, LogService.class.getName(), null);
		logTracker.open();
		// properties associated with the profile
		HashMap<String, String> properties = new HashMap<String, String>();
		
		// initialize filebox application
		FileboxApplication fileboxApplication = new FileboxApplication("Loulou");
		context.registerService(FileboxApplication.class.getName(), fileboxApplication, null);
		
		Contact me = fileboxApplication.getMe();
		properties.put(me.getStatus().getClass().getSimpleName(), me.getStatus().toString());
		serviceDiscovery = new ServiceDiscovery(me.getName(), IServiceDiscovery.DEFAULT_PORT, properties);
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

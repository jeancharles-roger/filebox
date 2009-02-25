package org.kawane.filebox.core.internal;

import java.util.Collection;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import org.kawane.filebox.core.discovery.FileboxService;
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
		// TODO get name, properties and port that come with preference or FileboxApplication let JC do this
		serviceDiscovery = new ServiceDiscovery("nom du contact courant", IServiceDiscovery.DEFAULT_PORT, properties);
		serviceDiscovery.start();
		context.registerService(IServiceDiscovery.class.getName(), serviceDiscovery, null);
		Timer timer = new Timer();
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				Collection<FileboxService> services = serviceDiscovery.getServices();
				for (FileboxService service : services) {
					System.out.println("service: " + service.getName());
				}
			}
		}, 0, 1000);
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

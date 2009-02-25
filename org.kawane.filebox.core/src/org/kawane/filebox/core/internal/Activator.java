package org.kawane.filebox.core.internal;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.service.log.LogService;
import org.osgi.util.tracker.ServiceTracker;

public class Activator implements BundleActivator {

	static private Activator instance;
	private ServiceTracker logTracker;

	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		instance = this;
		logTracker = new ServiceTracker(context, LogService.class.getName(), null);
		logTracker.open();
	}

	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		instance = null;
	}
	
	static public Activator getInstance() {
		return instance;
	}
	
	
	public LogService getLogger() {
		LogService logger = (LogService) logTracker.getService();
		return logger;
	}
	

}

package org.kawane.filebox.ui.internal;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.service.log.LogService;
import org.osgi.util.tracker.ServiceTracker;

public class Activator implements BundleActivator {

	static private Activator instance;
	private ServiceTracker logTracker;
	private BundleContext context;

	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		instance = this;
		logTracker = new ServiceTracker(context, LogService.class.getName(), null);
		logTracker.open();
		this.context = context;
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
	
	public BundleContext getContext() {
		return context;
	}
	
	public LogService getLogger() {
		LogService logger = (LogService) logTracker.getService();
		return logger;
	}

}

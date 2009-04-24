package org.kawane.services.internal;

import org.kawane.services.IServiceRegistry;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.service.packageadmin.PackageAdmin;
import org.osgi.util.tracker.ServiceTracker;

public class Activator implements BundleActivator {

	static private Activator instance;
	private ServiceTracker packageAdminTracker;

	public void start(BundleContext context) throws Exception {
		instance = this;
		packageAdminTracker = new ServiceTracker(context, PackageAdmin.class.getName(), null);
		packageAdminTracker.open();
		// may initialize now the service registry
		ServiceFactory.createService();
		IServiceRegistry.instance.getClass();
	}

	public void stop(BundleContext context) throws Exception {
	}

	public static Activator getInstance() {
		return instance;
	}
	
	public PackageAdmin getPackageAdmin() {
		PackageAdmin packageAdmin = (PackageAdmin) packageAdminTracker.getService();
		return packageAdmin;
	}
	
}

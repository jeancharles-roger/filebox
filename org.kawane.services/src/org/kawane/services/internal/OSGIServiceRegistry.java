package org.kawane.services.internal;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.service.packageadmin.PackageAdmin;

public class OSGIServiceRegistry extends JavaServiceRegistry {

	private PackageAdmin packageAdmin;

	public OSGIServiceRegistry(PackageAdmin packageAdmin) {
		this.packageAdmin = packageAdmin;
	}
	
	public void register(Class<?> serviceClass, Object service) {
		super.register(serviceClass, service);
		Bundle bundle = packageAdmin.getBundle(service.getClass());
		BundleContext context = bundle.getBundleContext();
		context.registerService(serviceClass.getName(), service, null);
	}
}

package org.kawane.services.internal;

import java.util.logging.Logger;

import org.kawane.services.ServiceRegistry;
import org.osgi.service.packageadmin.PackageAdmin;


public class ServiceFactory {
	private static final String CORE_SERVICE_REGISTRY = "org.kawane.services.core";

	private static Logger logger = Logger.getLogger(ServiceFactory.class.getName());

	private static ServiceRegistry serviceRegistry;

	synchronized public static ServiceRegistry createService() {
		if (serviceRegistry == null) {
			if(!Boolean.getBoolean(CORE_SERVICE_REGISTRY)) {
				try {
						// try to know if we are in osgi environnement
						ServiceFactory.class.getClassLoader().loadClass("org.osgi.framework.Bundle");
						PackageAdmin packageAdmin = Activator.getInstance().getPackageAdmin();
						if (packageAdmin != null) {
							ServiceRegistry osgiserviceRegistry = new OSGIServiceRegistry(packageAdmin);
							serviceRegistry = osgiserviceRegistry;
							return serviceRegistry;
						}
				} catch (Throwable e) {
					// we are not in osgi
					logger.fine("Not in osgi mode");
				}
			}
			serviceRegistry = new JavaServiceRegistry();
		}
		return serviceRegistry;
	}

}

package org.kawane.services.internal;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.kawane.services.ServiceRegistry;
import org.osgi.service.packageadmin.PackageAdmin;

public class ServiceFactory {
	private static Logger logger = Logger.getLogger(ServiceFactory.class.getName());
	
	private static ServiceRegistry serviceRegistry;

	synchronized public static ServiceRegistry createService() {
			try {
				// try to know if we are in osgi environnement
				ServiceFactory.class.getClassLoader().loadClass("org.osgi.framework.Bundle");
				PackageAdmin packageAdmin = Activator.getInstance().getPackageAdmin();
				if(packageAdmin != null) {
					ServiceRegistry osgiserviceRegistry = new OSGIServiceRegistry(packageAdmin);
					serviceRegistry = osgiserviceRegistry;
				}
			} catch (Throwable e) {
				// we are not in osgi
				logger.fine("Not in osgi mode");
			}
		if(serviceRegistry == null) {
			serviceRegistry = new JavaServiceRegistry();
		}
		return serviceRegistry;
	}

}

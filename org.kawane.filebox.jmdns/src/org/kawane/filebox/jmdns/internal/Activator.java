package org.kawane.filebox.jmdns.internal;

import org.kawane.filebox.core.discovery.IServiceDiscovery;
import org.kawane.services.ServiceRegistry;
import org.kawane.services.advanced.ServiceInjector;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class Activator implements BundleActivator {
	
	private IServiceDiscovery serviceDiscovery;
	
	public void start(BundleContext context) throws Exception {
		serviceDiscovery = new JmDNSServiceDiscovery();
		new ServiceInjector(serviceDiscovery);
		
//		JSLP discovery implementation
//		serviceDiscovery = new JSLPServiceDiscovery();
		// Start to listening services
		serviceDiscovery.start();
		ServiceRegistry.instance.register(IServiceDiscovery.class, serviceDiscovery);
	}

	public void stop(BundleContext context) throws Exception {
		serviceDiscovery.disconnect(null);
		serviceDiscovery.stop();
	}
}

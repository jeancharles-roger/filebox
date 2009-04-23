package org.kawane.filebox.jmdns.internal;

import org.kawane.filebox.core.discovery.IServiceDiscovery;
import static org.kawane.services.advanced.ServiceManager.*;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class Activator implements BundleActivator {

	private IServiceDiscovery serviceDiscovery;

	public void start(BundleContext context) throws Exception {
		serviceDiscovery = new JmDNSServiceDiscovery();
		inject(serviceDiscovery);

		// Start to listening services
		serviceDiscovery.start();
		register(serviceDiscovery);
	}

	public void stop(BundleContext context) throws Exception {
		serviceDiscovery.disconnect(null);
		serviceDiscovery.stop();
	}
}

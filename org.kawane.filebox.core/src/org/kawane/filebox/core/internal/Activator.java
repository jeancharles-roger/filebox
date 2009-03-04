package org.kawane.filebox.core.internal;

import java.io.File;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.osgi.service.datalocation.Location;
import org.kawane.filebox.core.Filebox;
import org.kawane.filebox.core.discovery.IServiceDiscovery;
import org.kawane.services.ServiceRegistry;
import org.kawane.services.advanced.ServiceInjector;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

public class Activator implements BundleActivator {
	private static Logger logger = Logger.getLogger(Activator.class.getName());

	protected static final String CONFIG_FILENAME = "filebox.properties";

	private ServiceDiscovery serviceDiscovery;

	protected File configurationFile;

	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		ServiceReference[] serviceReferences = context.getServiceReferences(Location.class.getName(), Location.CONFIGURATION_FILTER);
		if (serviceReferences != null) {
			for (ServiceReference serviceReference : serviceReferences) {
				Location configLocation = (Location) context.getService(serviceReference);
				configurationFile = new File(configLocation.getURL().getFile(), CONFIG_FILENAME);
			}
		}
		// configuration file
		if (configurationFile == null) {
			configurationFile = context.getDataFile(CONFIG_FILENAME);
		}

		// configuration file
		configurationFile = context.getDataFile(CONFIG_FILENAME);

		// properties associated with the profile
		HashMap<String, String> properties = new HashMap<String, String>();


		
		// initialize filebox application
		Filebox filebox = new Filebox(configurationFile);
		//		context.registerService(Filebox.class.getName(), filebox, null);
		ServiceRegistry.instance.register(Filebox.class, filebox);
		new ServiceInjector(filebox);
		// publish object on rmi 
		try {
			Registry registry = LocateRegistry.createRegistry(filebox.getPort());
			UnicastRemoteObject.exportObject(filebox, filebox.getPort());
			registry.rebind(filebox.getName(), filebox);
		} catch (RemoteException e) {
			logger.log(Level.SEVERE, "Can't connect Filebox", e);
		}
//		properties.put(filebox.getStatus().getClass().getSimpleName(), filebox.getStatus().toString());
		serviceDiscovery = new ServiceDiscovery(filebox.getName(), filebox.getPort(), properties);
		// automatically connect to the network for now
		serviceDiscovery.start();
		ServiceRegistry.instance.register(IServiceDiscovery.class, serviceDiscovery);
		
	}

	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		serviceDiscovery.stop();
	}

}

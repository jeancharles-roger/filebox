package org.kawane.filebox.core.internal;

import java.io.File;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.kawane.filebox.core.Filebox;
import org.kawane.filebox.core.discovery.IServiceDiscovery;
import org.kawane.services.ServiceRegistry;
import org.kawane.services.advanced.ServiceInjector;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

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
		String configurationProperty = System.getProperty("osgi.configuration.area");
		if(configurationProperty == null || configurationProperty.length() ==0) {
			configurationProperty = System.getProperty("osgi.syspath");
		}
		if(configurationProperty != null & configurationProperty.length() !=0) {
			configurationProperty = configurationProperty.replace("file:", "");
			configurationFile = new File(configurationProperty, CONFIG_FILENAME);
		}
		// configuration file
		if (configurationFile == null) {
			// create on folder where the process run
			configurationFile = new File(CONFIG_FILENAME);
		}

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

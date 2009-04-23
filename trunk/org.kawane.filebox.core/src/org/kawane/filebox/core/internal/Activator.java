package org.kawane.filebox.core.internal;

import static org.kawane.services.advanced.ServiceManager.manage;

import java.io.File;

import org.kawane.filebox.core.Filebox;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class Activator implements BundleActivator {
	protected static final String CONFIG_FILENAME = "filebox.properties";

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
		if(configurationProperty != null && configurationProperty.length() !=0) {
			configurationProperty = configurationProperty.replace("file:", "");
			configurationFile = new File(configurationProperty, CONFIG_FILENAME);
		}
		// configuration file
		if (configurationFile == null) {
			// create on folder where the process run
			configurationFile = new File(CONFIG_FILENAME);
		}

		manage(new FileboxRegistry());

		// initialize filebox application
		manage(new Filebox(configurationFile));

	}

	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
	}

}

package org.kawane.filebox.ui.internal;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class Activator implements BundleActivator {

	private Application application;
	
	private boolean stopping = false;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext
	 * )
	 */
	public void start(final BundleContext context) throws Exception {
		Thread thread = new Thread("UI Thread") {
			@Override
			public void run() {
				// UI Thread
				application = new Application() {
					@Override
					public void stop() {
						if(!stopping) {
							stopping = true;
							super.stop();
							closeFramework(context);
						}
					}

				};
				application.start();
			}
		};
		thread.start();
	}

	public void closeFramework(final BundleContext context) {
		Bundle systemBundle = null;
		Bundle[] bundles = context.getBundles();
		for (Bundle bundle : bundles) {
			if (bundle.getSymbolicName().startsWith("org.eclipse.osgi")) {
				systemBundle = bundle;
				break;
			}
		}
		if (systemBundle != null) {
			try {
				systemBundle.stop();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		try {
			Thread.sleep(200);
			System.exit(0);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		application.stop();
	}

}

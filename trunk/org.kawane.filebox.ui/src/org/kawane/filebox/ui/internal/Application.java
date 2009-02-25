package org.kawane.filebox.ui.internal;

import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.kawane.filebox.core.FileboxApplication;
import org.kawane.filebox.ui.FileboxMainComposite;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;
import org.osgi.service.log.LogService;

public class Application implements IApplication {
	private static LogService logger = Activator.getInstance().getLogger();
	
	private Display display;
	
	public Object start(IApplicationContext context) throws Exception {
		// this the way to retrieve command line option
		//Object args = context.getArguments().get(IApplicationContext.APPLICATION_ARGS);
		// TODO I don't know how to do this without global var if you a better idea
		BundleContext bundleContext = Activator.getInstance().getContext();
		
		
		display = Display.getDefault();
		logger.log(LogService.LOG_INFO, "Start file box ui");
		
		// our first window
		Shell shell = new Shell(display);
		shell.setLayout(new FillLayout());
		shell.setSize(300, 300);
		shell.setText("FileBox");
		
		FileboxMainComposite composite = new FileboxMainComposite(shell, SWT.NONE);
		retrieveFileboxApplication(bundleContext, composite);
			
		shell.open();
		context.applicationRunning();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
		if(!display.isDisposed()) {
            display.dispose ();
		}
		logger.log(LogService.LOG_INFO, "Stop file box ui");
		return null;
	}

	private void retrieveFileboxApplication(final BundleContext bundleContext, final FileboxMainComposite composite) throws InvalidSyntaxException {
		// retrieve filebox application from osgi service registry
		ServiceListener serviceListener = new ServiceListener() {
			public void serviceChanged(ServiceEvent event) {
				if(event.getType() == ServiceEvent.REGISTERED) {
					FileboxApplication application = (FileboxApplication)bundleContext.getService(event.getServiceReference());
					composite.setApplication(application);
				} else if(event.getType() == ServiceEvent.UNREGISTERING){
					composite.setApplication(null);
				}
			}
		};
		
		String filter = "(" + Constants.OBJECTCLASS + "="+ FileboxApplication.class.getName() +")";
		ServiceReference[] serviceReferences = bundleContext.getServiceReferences(null,filter);
		if(serviceReferences != null) {
			for (ServiceReference serviceReference : serviceReferences) {
				serviceListener.serviceChanged(new ServiceEvent(ServiceEvent.REGISTERED,serviceReference));
			}
		}
		bundleContext.addServiceListener(serviceListener, filter);
	}

	public void stop() {
		if(display != null && !display.isDisposed()) {
			display.dispose();
		}
	}

}

package org.kawane.filebox.ui.internal;

import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.events.ShellListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Decorations;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tray;
import org.eclipse.swt.widgets.TrayItem;
import org.kawane.filebox.core.Filebox;
import org.kawane.filebox.ui.FileboxMainComposite;
import org.kawane.filebox.ui.MenuManager;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;
import org.osgi.service.log.LogService;

public class Application implements IApplication {

	/** Shared resources instances. */
	protected Resources resources;

	private static LogService logger = Activator.getInstance().getLogger();

	protected Filebox filebox;

	private Display display;
	private FileboxMainComposite composite;

	public Shell getActiveShell() {
		return display.getActiveShell();
	}

	public Filebox getFilebox() {
		return filebox;
	}

	protected void setFileBox(Filebox filebox) {
		this.filebox = filebox;
		composite.setFilebox(filebox);
	}

	public Object start(IApplicationContext context) throws Exception {
		// this the way to retrieve command line option
		//Object args = context.getArguments().get(IApplicationContext.APPLICATION_ARGS);
		// TODO I don't know how to do this without global var if you a better idea
		BundleContext bundleContext = Activator.getInstance().getContext();

		display = Display.getDefault();
		logger.log(LogService.LOG_INFO, "Start file box ui");
		resources = Resources.getInstance();

		// our first window
		Shell shell = new Shell(display);
		shell.setImage(resources.getImage("filebox.png"));
		shell.setLayout(new FillLayout());
		shell.setSize(300, 300);
		shell.setText("FileBox");
		createSystemTray(shell, resources.getImage("filebox-small.png"));

		MenuManager menuManager = new MenuManager(this);
		menuManager.createMenuBar(shell);

		composite = new FileboxMainComposite(shell, SWT.NONE);
		retrieveFilebox(bundleContext, this);

		shell.open();
		context.applicationRunning();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
		if (!display.isDisposed()) {
			display.dispose();
		}

		resources.dispose();
		logger.log(LogService.LOG_INFO, "Stop file box ui");
		return null;
	}

	private void createSystemTray(final Shell shell, Image image) {
		final Tray tray = display.getSystemTray();
		if (tray == null) {
			System.out.println("The system tray is not available");
		} else {
			final TrayItem item = new TrayItem(tray, SWT.NONE);
			item.setToolTipText("SWT TrayItem");
			
			final Menu menu = new Menu(shell, SWT.POP_UP);
			MenuItem quitMenu = new MenuItem(menu, SWT.PUSH);
			quitMenu.setText("Quit");
			quitMenu.addListener(SWT.Selection, new Listener() {
				public void handleEvent(Event event) {
					Application.this.stop();
				}
			});
			final MenuItem showHideMenu = new MenuItem(menu, SWT.PUSH);
			showHideMenu.setText("Hide");
			Listener showHideListener = new Listener() {
				public void handleEvent(Event event) {
					// Show hide filebox
					if(shell.isVisible()) {
						showHideMenu.setText("Show");
						shell.setVisible(false);
					} else {
						showHideMenu.setText("Hide");
						shell.setVisible(true);
					}
					if(event.type == SWT.Close) {
						// do not quit the application when closing the shell
						event.doit = false;
					}
				}
			};
			showHideMenu.addListener(SWT.Selection, showHideListener);
			shell.addListener(SWT.Close, showHideListener);
			menu.setDefaultItem(showHideMenu);
			item.addListener(SWT.Selection,showHideListener);
			item.addListener(SWT.MenuDetect, new Listener() {
				public void handleEvent(Event event) {
					menu.setVisible(true);
				}
			});
			item.setImage(image);
		}
	}

	private void retrieveFilebox(final BundleContext bundleContext, final Application application) throws InvalidSyntaxException {
		// retrieve filebox from osgi service registry
		ServiceListener serviceListener = new ServiceListener() {
			public void serviceChanged(ServiceEvent event) {
				if (event.getType() == ServiceEvent.REGISTERED) {
					Filebox filebox = (Filebox) bundleContext.getService(event.getServiceReference());
					application.setFileBox(filebox);
				} else if (event.getType() == ServiceEvent.UNREGISTERING) {
					application.setFileBox(null);
				}
			}
		};

		String filter = "(" + Constants.OBJECTCLASS + "=" + Filebox.class.getName() + ")";
		ServiceReference[] serviceReferences = bundleContext.getServiceReferences(null, filter);
		if (serviceReferences != null) {
			for (ServiceReference serviceReference : serviceReferences) {
				serviceListener.serviceChanged(new ServiceEvent(ServiceEvent.REGISTERED, serviceReference));
			}
		}
		bundleContext.addServiceListener(serviceListener, filter);
	}

	public void stop() {
		if (display != null && !display.isDisposed()) {
			display.dispose();
		}
	}

}

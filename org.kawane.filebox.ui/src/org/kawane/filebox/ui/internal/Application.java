package org.kawane.filebox.ui.internal;

import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.kawane.filebox.core.Filebox;
import org.kawane.filebox.ui.FileboxMainComposite;
import org.kawane.filebox.ui.MenuManager;
import org.kawane.services.ServiceRegistry;
import org.kawane.services.advanced.ServiceInjector;
import org.osgi.framework.BundleContext;
import org.osgi.service.log.LogService;

public class Application implements IApplication, UIFileboxApplication {

	/** Shared resources instances. */
	protected Resources resources;

	private static LogService logger = Activator.getInstance().getLogger();

	protected Filebox filebox;

	private Display display;
	private FileboxMainComposite composite;

	/* (non-Javadoc)
	 * @see org.kawane.filebox.ui.internal.UIFileboxApplication#getDisplay()
	 */
	public Display getDisplay() {
		return display;
	}
	
	/* (non-Javadoc)
	 * @see org.kawane.filebox.ui.internal.UIFileboxApplication#getActiveShell()
	 */
	public Shell getActiveShell() {
		return display.getActiveShell();
	}

	public Object start(IApplicationContext context) throws Exception {
		// this the way to retrieve command line option
		//Object args = context.getArguments().get(IApplicationContext.APPLICATION_ARGS);
		
		ServiceRegistry.instance.register(UIFileboxApplication.class, this);
		
		display = Display.getDefault();
		logger.log(LogService.LOG_INFO, "Start file box ui");
		resources = Resources.getInstance();

		// our first window
		final Shell shell = new Shell(display);
		shell.setImage(resources.getImage("filebox.png"));
		shell.setLayout(new FillLayout());
		shell.setSize(300, 300);
		shell.setText("FileBox");
		shell.addListener(SWT.Close, new Listener() {
			public void handleEvent(Event event) {
				boolean visible = !shell.isVisible();
				shell.setVisible(visible);
				
				// do not quit the application when closing the shell
				event.doit = false;
			}
		});
		
		MenuManager menuManager = new MenuManager();
		new ServiceInjector(menuManager);
		menuManager.createMenuBar(shell);
		menuManager.createSystemTray(shell);
		
		composite = new FileboxMainComposite(shell, SWT.NONE);
		new ServiceInjector(composite);

		shell.open();
		context.applicationRunning();
		while (!shell.isDisposed()) {
			try {
				if (!display.readAndDispatch()) {
					display.sleep();
				}
			} catch (Throwable e) {
				logger.log(LogService.LOG_ERROR, "Internal Error", e);
			}
		}
		if (!display.isDisposed()) {
			display.dispose();
		}

		resources.dispose();
		logger.log(LogService.LOG_INFO, "Stop file box ui");
		return null;
	}

	/* (non-Javadoc)
	 * @see org.kawane.filebox.ui.internal.UIFileboxApplication#stop()
	 */
	public void stop() {
		if (display != null && !display.isDisposed()) {
			display.dispose();
		}
	}

}

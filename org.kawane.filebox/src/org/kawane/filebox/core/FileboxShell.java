package org.kawane.filebox.core;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.kawane.filebox.core.discovery.JmDNSServiceDiscovery;
import org.kawane.filebox.core.discovery.ServiceDiscovery;
import org.kawane.filebox.core.network.FileboxService;
import org.kawane.filebox.core.network.HttpServer;
import org.kawane.filebox.core.network.NetworkService;
import org.kawane.filebox.ui.FileboxMainComposite;
import org.kawane.filebox.ui.MenuManager;
import org.kawane.filebox.ui.Resources;

public class FileboxShell  {

	private static Logger logger = Logger.getLogger(FileboxShell.class.getName());

	protected static final String CONFIG_FILENAME = "filebox.properties";

	protected File configurationFile;
	
	/** Shared resources instances. */
	protected Resources resources;

	private Display display;

	
	public Display getDisplay() {
		return display;
	}

	public Shell getActiveShell() {
		return display.getActiveShell();
	}

	private void initFileboxCore() {
		configurationFile = new File(CONFIG_FILENAME);
		Globals.setPreferences(new Preferences(configurationFile));
		
		Globals.setLocalFilebox(new Filebox());
	
		Globals.setFileboxRegistry(new FileboxRegistry());
		
		ServiceDiscovery serviceDiscovery = new JmDNSServiceDiscovery();
		serviceDiscovery.start();
		Globals.setServiceDiscovery(serviceDiscovery);
		
		Map<String, NetworkService> networkServices = new HashMap<String, NetworkService>();
		Globals.setNetworkServices(networkServices);
		networkServices.put("filebox", new FileboxService());
		
		Globals.setHttpServer(new HttpServer());
	}

	public void start() {

		initFileboxCore();
		
		Globals.setFileboxShell(this);

		display = Display.getDefault();
		logger.log(Level.FINE, "Start file box ui");
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
		
		menuManager.createMenuBar(shell);
		menuManager.createSystemTray(shell);

		new FileboxMainComposite(shell, SWT.NONE);
		
		shell.open();
		while (!shell.isDisposed()) {
			try {
				if (!display.readAndDispatch()) {
					display.sleep();
				}
			} catch (Throwable e) {
				logger.log(Level.SEVERE, "Internal Error", e);
			}
		}
		if (!display.isDisposed()) {
			display.dispose();
		}

		resources.dispose();
		logger.log(Level.FINE, "Stop file box ui");
	}

	/* (non-Javadoc)
	 * @see org.kawane.filebox.ui.internal.UIFileboxApplication#stop()
	 */
	public void stop() {
		
		HttpServer server = Globals.getHttpServer();
		server.stop();

		ServiceDiscovery serviceDiscovery = Globals.getServiceDiscovery();
		serviceDiscovery.disconnect(null);
		serviceDiscovery.stop();
		
		if (display != null && !display.isDisposed()) {
			display.dispose();
		}
	}

	
	public static void main(String[] args) {
		FileboxShell application = new FileboxShell();
		application.start();
	}
}

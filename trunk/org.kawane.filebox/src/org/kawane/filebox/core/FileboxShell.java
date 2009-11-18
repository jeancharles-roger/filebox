package org.kawane.filebox.core;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
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
import org.kawane.filebox.network.http.HttpServer;
import org.kawane.filebox.network.http.services.FileService;
import org.kawane.filebox.ui.FileboxMainComposite;
import org.kawane.filebox.ui.MenuManager;
import org.kawane.filebox.ui.Resources;
import org.kawane.filebox.webpage.HomePage;

public class FileboxShell implements PropertyChangeListener {

	private static Logger logger = Logger.getLogger(FileboxShell.class.getName());

	protected static final String CONFIG_FILENAME = "filebox.properties";

	protected File configurationFile;

	/** Shared resources instances. */
	protected Resources resources;

	protected FileboxMainComposite mainComposite;
	
	private Display display;

	public Display getDisplay() {
		return display;
	}

	public Shell getActiveShell() {
		return display.getActiveShell();
	}

	public FileboxMainComposite getMainComposite() {
		return mainComposite;
	}
	
	private void initFileboxCore() {
		configurationFile = new File(CONFIG_FILENAME);
		Preferences preferences = new Preferences(configurationFile);
		Globals.setPreferences(preferences);

		Globals.setLocalFilebox(new Filebox());

		Globals.setFileboxRegistry(new FileboxRegistry());

		ServiceDiscovery serviceDiscovery = new JmDNSServiceDiscovery();
		serviceDiscovery.start();
		Globals.setServiceDiscovery(serviceDiscovery);

		HttpServer server = new HttpServer(preferences.getPort());
		server.setService("/", new HomePage(new File("homePage")));
		server.setService("/files", new FileService(new File(preferences.getPublicDir())));
		
		Globals.setHttpServer(server);
		preferences.addPropertyChangeListener(this);
	}

	public void propertyChange(PropertyChangeEvent event) {
		if (Preferences.PORT.equals(event.getPropertyName())) {
			HttpServer server = Globals.getHttpServer();
			server.setPort(Integer.valueOf((String) event.getNewValue()));
			if (server.isRunning()) {
				server.stop();
				server.start();
			}
		} else if (Preferences.PUBLIC_FILE_DIR.equals(event.getPropertyName())) {
			Globals.getHttpServer().setService("/files", new FileService(new File((String) event.getNewValue())));
		}
	}

	public void start() {

		initFileboxCore();

		Globals.setFileboxShell(this);

		display = Display.getDefault();
		logger.log(Level.FINE, "Start file box ui");
		resources = Resources.getInstance();

		// our first window
		final Shell shell = new Shell(display);
		shell.setImage(resources.getImage("filebox-icon-256x256.png"));
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

		mainComposite = new FileboxMainComposite(shell, SWT.NONE);

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

	/*
	 * (non-Javadoc)
	 * 
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

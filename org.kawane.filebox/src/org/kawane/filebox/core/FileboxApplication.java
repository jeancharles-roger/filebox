package org.kawane.filebox.core;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.kawane.filebox.Resources;
import org.kawane.filebox.core.discovery.JmDNSServiceDiscovery;
import org.kawane.filebox.core.discovery.ServiceDiscovery;
import org.kawane.filebox.network.http.HttpServer;
import org.kawane.filebox.network.http.TransferManager;
import org.kawane.filebox.network.http.services.FileService;
import org.kawane.filebox.ui.ContactController;
import org.kawane.filebox.ui.MenuManager;
import org.kawane.filebox.webpage.HomePage;

public class FileboxApplication implements PropertyChangeListener {

	private static Logger logger = Logger.getLogger(FileboxApplication.class.getName());

	protected static final String CONFIG_FILENAME = "filebox.properties";

	protected File configurationFile;

	/** Shared resources instances. */
	protected Resources resources;
	
	private Preferences preferences;
	private TransferManager transferManager;
	private Filebox filebox;
	private FileboxRegistry fileboxRegistry;
	
	
	private Display display;
	private ContactController contactController;
	private Shell contactShell;
	private MenuManager menuManager;
	
	public Filebox getFilebox() {
		return filebox;
	}
	
	public FileboxRegistry getFileboxRegistry() {
		return fileboxRegistry;
	}
	
	public TransferManager getTransferManager() {
		return transferManager;
	}
	
	public Display getDisplay() {
		return display;
	}

	public Shell getActiveShell() {
		return display.getActiveShell();
	}
	
	public Shell getContactShell() {
		return contactShell;
	}
	
	public ContactController getContactController() {
		return contactController;
	}
	
	public MenuManager getMenuManager() {
		return menuManager;
	}
	
	public Preferences getPreferences() {
		return preferences;
	}
	
	private void initFileboxCore() {
		configurationFile = new File(CONFIG_FILENAME);
		preferences = new Preferences(configurationFile);

		filebox = new Filebox(preferences);
		Globals.setLocalFilebox(filebox);
		
		fileboxRegistry = new FileboxRegistry();
		Globals.setFileboxRegistry(fileboxRegistry);

	
		ServiceDiscovery serviceDiscovery = new JmDNSServiceDiscovery();
		serviceDiscovery.start();
		Globals.setServiceDiscovery(serviceDiscovery);

		HttpServer server = new HttpServer(preferences.getPort());
		server.setService("/", new HomePage(new File("homePage")));
		server.setService("/files", new FileService(new File(preferences.getPublicDir())));
		
		Globals.setHttpServer(server);
		preferences.addPropertyChangeListener(this);
		
		transferManager = new TransferManager();
		transferManager.start();
		
		filebox.connect(null);
	}

	public void propertyChange(PropertyChangeEvent event) {
		if (Preferences.PORT.equals(event.getPropertyName())) {
			HttpServer server = Globals.getHttpServer();
			server.setPort(preferences.getPort());
			final Filebox localFilebox = Globals.getLocalFilebox();
			localFilebox.setPort(preferences.getPort());
			localFilebox.reconnect();
		} else if (Preferences.NAME.equals(event.getPropertyName())) {
			final Filebox localFilebox = Globals.getLocalFilebox();
			localFilebox.setName(preferences.getName());
			localFilebox.reconnect();
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

		contactController = new ContactController(this);
		contactShell = contactController.createShell();
		contactController.refreshUI();

		menuManager = new MenuManager();
		menuManager.createMenuBar(contactShell);
		menuManager.createSystemTray(contactShell);


		contactShell.open();
		while (!contactShell.isDisposed()) {
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

		if (display != null && !display.isDisposed()) {
			display.dispose();
		}

		transferManager.stop();
		
		HttpServer server = Globals.getHttpServer();
		server.stop();

		ServiceDiscovery serviceDiscovery = Globals.getServiceDiscovery();
		serviceDiscovery.disconnect(null);
		serviceDiscovery.stop();

		System.out.println("Exiting...");
		System.exit(0);
	}

	public static void main(String[] args) {
		FileboxApplication application = new FileboxApplication();
		application.start();
	}
}

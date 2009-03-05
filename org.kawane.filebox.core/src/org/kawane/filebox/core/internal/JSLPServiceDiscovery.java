package org.kawane.filebox.core.internal;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.kawane.filebox.core.IFilebox;
import org.kawane.filebox.core.discovery.IConnectionListener;
import org.kawane.filebox.core.discovery.IServiceDiscovery;
import org.kawane.services.ServiceRegistry;

import ch.ethz.iks.slp.Advertiser;
import ch.ethz.iks.slp.Locator;
import ch.ethz.iks.slp.ServiceLocationEnumeration;
import ch.ethz.iks.slp.ServiceLocationException;
import ch.ethz.iks.slp.ServiceLocationManager;
import ch.ethz.iks.slp.ServiceType;
import ch.ethz.iks.slp.ServiceURL;

public class JSLPServiceDiscovery implements IServiceDiscovery {

	private static Logger logger = Logger.getLogger(JSLPServiceDiscovery.class.getName());

	private HashMap<ServiceURL, IFilebox> fileBoxes = new HashMap<ServiceURL, IFilebox>();

	private String name;
	private int port;

	private Advertiser advertiser;

	private ServiceURL myService;

	private Locator locator;

	private Timer timer;

	private String hostname;

	public String getName() {
		return name;
	}

	public int getPort() {
		return port;
	}

	public String getHostname() {
		return hostname;
	}

	public void connect(String lname, int lport, final IConnectionListener listener) {
		this.name = lname;
		this.port = lport;
		
		// the service has lifetime 60, that means it will only persist for one minute
		try {
			advertiser = ServiceLocationManager.getAdvertiser(new Locale("en"));
			Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
			String hostAddress = getHostAddress(networkInterfaces);
			myService = new ServiceURL("service:filebox:remote://" + hostAddress + ":" + port, ServiceURL.LIFETIME_PERMANENT);
			// some attributes for the service
			Hashtable<String, Object> properties = new Hashtable<String, Object>();
			properties.put("persistent", Boolean.TRUE);
			properties.put("max-connections", 5);
			
			advertiser.register(myService, new Hashtable<String, Object>(properties));
			listener.connected(this);
		} catch (ServiceLocationException e) {
			logger.log(Level.SEVERE, "An Error Occured", e);
		} catch (SocketException e) {
			logger.log(Level.SEVERE, "An Error Occured", e);
		}

	}

	private String getHostAddress(Enumeration<NetworkInterface> networkInterfaces) {
		String hostAddress = null;
		while(networkInterfaces.hasMoreElements()) {
			NetworkInterface networkInterface = networkInterfaces.nextElement();
			String displayName = networkInterface.getDisplayName();
			if(!displayName.equals("localhost")) {
				Enumeration<InetAddress> inetAddresses = networkInterface.getInetAddresses();
				while(inetAddresses.hasMoreElements()) {
					InetAddress inetAddress = inetAddresses.nextElement();
					if(!inetAddress.isLoopbackAddress()) {
						hostAddress = inetAddress.getHostAddress();
						break;
					}
				}
				break;
			}
		}
		return hostAddress;
	}

	public void disconnect(final IConnectionListener listener) {
		try {
			advertiser.deregister(myService);
			listener.disconnected(this);
		} catch (ServiceLocationException e) {
			logger.log(Level.SEVERE, "An Error Occured", e);
		}
	}

	public void start() {
		try {
			locator = ServiceLocationManager.getLocator(new Locale("en"));
		} catch (ServiceLocationException e) {
			logger.log(Level.SEVERE, "An Error Occured", e);
		}
		timer = new Timer();
		// find all services of type "test" that have attribute "cool=yes"
		TimerTask timerTask = new TimerTask() {
			@Override
			public void run() {
				try {
					locator = ServiceLocationManager.getLocator(new Locale("en"));
					ServiceLocationEnumeration sle = locator.findServices(new ServiceType("service:filebox:remote"), null, null);
					// iterate over the results
					
					HashMap<ServiceURL, IFilebox> fileBoxesTemp = new HashMap<ServiceURL, IFilebox>();
					while (sle.hasMoreElements()) {
						ServiceURL foundService = (ServiceURL) sle.nextElement();
						IFilebox filebox = createFileboxService(foundService);
						fileBoxesTemp.put(foundService, filebox);
					}
					for(ServiceURL key: fileBoxes.keySet()){
						if(!fileBoxesTemp.containsKey(key)) {
							IFilebox filebox = fileBoxes.remove(key);
							ServiceRegistry.instance.unregister(IFilebox.class, filebox);
						}
					}
					for(Entry<ServiceURL,IFilebox> entry: fileBoxesTemp.entrySet()){
						if(!fileBoxes.containsKey(entry.getKey())) {
							fileBoxes.put(entry.getKey(), entry.getValue());
							ServiceRegistry.instance.register(IFilebox.class, entry.getValue());
						}
					}
				} catch (Exception e) {
					logger.log(Level.SEVERE, "An Error Occured", e);
				}
			}
		};
		timer.schedule(timerTask, 0, 500);
	}

	public void stop() {
		timer.cancel();
	}

	private IFilebox createFileboxService(ServiceURL service) {
		try {
			IFilebox fileboxService = (IFilebox) LocateRegistry.getRegistry(service.getHost(), service.getPort()).lookup("filebox");
			return fileboxService;
		} catch (RemoteException e) {
			logger.log(Level.SEVERE, "Can't connect Filebox", e);
			return null;
		} catch (NotBoundException e) {
			logger.log(Level.SEVERE, "Can't connect Filebox", e);
			return null;
		}
	}

}

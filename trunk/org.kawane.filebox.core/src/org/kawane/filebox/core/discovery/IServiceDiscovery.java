package org.kawane.filebox.core.discovery;

import java.util.Map;


public interface IServiceDiscovery {
	
	// dns specifice
    String FILEBOX_TYPE = "_filebox._tcp.local.";
	int FILEBOX_WEIGHT = 10;
	int FILEBOX_PRIORITY = 10;
	
	String getName() ;
	
	int getPort();
	
	Map<String, String> getProperties();
	
	String getHostname();

	void connect(String name, int port, Map<String, String> properties, IConnectionListener listener);
	
	void disconnect(IConnectionListener listener);

}
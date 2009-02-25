package org.kawane.filebox.core.discovery;

import java.util.Collection;
import java.util.Map;


public interface IServiceDiscovery {
	int DEFAULT_PORT = 9999;
	// dns specifice
    String FILEBOX_TYPE = "_filebox._tcp.local";
	int FILEBOX_WEIGHT = 10;
	int FILEBOX_PRIORITY = 10;
	
	String getName() ;
	
	int getPort();
	
	Map<String, String> getProperties();
	
	String getHostname();
	
	Collection<FileboxService> getServices();

	void addServiceListener(IFileboxServiceListener listener);

	void removeServiceListener(IFileboxServiceListener listener);

}
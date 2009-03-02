package org.kawane.filebox.core.discovery;

import java.util.Collection;
import java.util.Map;

import org.kawane.filebox.core.IFilebox;


public interface IServiceDiscovery {
	
	// dns specifice
    String FILEBOX_TYPE = "_filebox._tcp.local.";
	int FILEBOX_WEIGHT = 10;
	int FILEBOX_PRIORITY = 10;
	
	String getName() ;
	
	int getPort();
	
	Map<String, String> getProperties();
	
	String getHostname();
	/**
	 * first call of this method may be slow.
	 * you may use instead listener system please.
	 * @return
	 */
	Collection<IFilebox> getServices();

	void apply(String name, int port, Map<String, String> properties);

	void addServiceListener(IFileboxServiceListener listener);

	void removeServiceListener(IFileboxServiceListener listener);

}
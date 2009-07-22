package org.kawane.filebox.core.discovery;



public interface ServiceDiscovery {

	// dns specifice
    String FILEBOX_TYPE = "_filebox._tcp.local.";
	int FILEBOX_WEIGHT = 10;
	int FILEBOX_PRIORITY = 10;

	String getName() ;

	int getPort();

	String getHostname();

	void connect(String name, int port, ConnectionListener listener);

	void disconnect(ConnectionListener listener);

	void start();

	void stop();

}
package org.kawane.filebox.core.discovery;

public interface ConnectionListener {
	void connected(ServiceDiscovery serviceDiscovery);
	void disconnected(ServiceDiscovery serviceDiscovery);
}

package org.kawane.filebox.core.discovery;

public interface IConnectionListener {
	void connected(IServiceDiscovery serviceDiscovery);
	void disconnected(IServiceDiscovery serviceDiscovery);
}

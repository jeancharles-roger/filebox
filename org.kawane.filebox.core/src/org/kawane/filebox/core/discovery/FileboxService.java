package org.kawane.filebox.core.discovery;

import java.util.Map;

public class FileboxService {
	private String host;
	private int port;
	private String name;
	private Map<String, String> properties;
	
	public FileboxService() {
	}

	public FileboxService(String host, int port, String name, Map<String, String> properties) {
		super();
		this.host = host;
		this.port = port;
		this.name = name;
		this.properties = properties;
	}

	public String getHost() {
		return host;
	}
	public void setHost(String host) {
		this.host = host;
	}
	public int getPort() {
		return port;
	}
	public void setPort(int port) {
		this.port = port;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Map<String, String> getProperties() {
		return properties;
	}
	public void setDescription( Map<String, String> properties) {
		this.properties = properties;
	}
}

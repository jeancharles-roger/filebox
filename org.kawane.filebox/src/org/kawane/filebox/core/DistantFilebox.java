package org.kawane.filebox.core;


/** Distant Fileboxe description */
final public class DistantFilebox {
	
	private String name;
	private String host;
	private int port;

	public DistantFilebox(String name, String host, int port) {
		this.name = name;
		this.host = host;
		this.port = port;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
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

	public String getId() {
		StringBuilder builder = new StringBuilder();
		builder.append(getName());
		builder.append(getHost());
		builder.append(getPort());
		return  builder.toString();
	}
	
	public boolean equals(Object obj) {
		if ( obj instanceof DistantFilebox ) {
			DistantFilebox fd = (DistantFilebox) obj;
			return 	(name == null ? fd.name == null : name.equals(fd.name)) &&
					(host == null ? fd.host == null : host.equals(fd.host)) &&
					port == fd.port;
		}
		return false;
	}
}
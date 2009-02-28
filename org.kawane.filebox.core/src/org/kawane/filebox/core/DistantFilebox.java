/**
 * Filebox developed by Kawane.
 * LGPL License.
 */

package org.kawane.filebox.core;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Jean-Charles Roger
 *
 */
public class DistantFilebox extends UnicastRemoteObject implements IFilebox {

	private static final long serialVersionUID = 1L;

	protected String name;
	
	/** With RMI host is useless for RMI */
	protected String host;
	
	protected int port;
	
	protected final Map<String, String> properties = new HashMap<String, String>();
	
	public DistantFilebox(String name, String host, int port) throws RemoteException {
		super(port);
		this.name = name;
		this.host = host;
		this.port = port;
	}

	
	/* (non-Javadoc)
	 * @see org.kawane.filebox.core.IFilebox#getHost()
	 */
	public String getHost() {
		return host;
	}

	/* (non-Javadoc)
	 * @see org.kawane.filebox.core.IFilebox#getName()
	 */
	public String getName() {
		return name;
	}

	/* (non-Javadoc)
	 * @see org.kawane.filebox.core.IFilebox#getPort()
	 */
	public int getPort() {
		return port;
	}

}

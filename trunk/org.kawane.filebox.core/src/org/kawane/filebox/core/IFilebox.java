/**
 * Filebox developed by Kawane.
 * LGPL License.
 */
package org.kawane.filebox.core;

import java.rmi.Remote;
import java.rmi.RemoteException;


/**
 * Filebox interface. This is the interface used for communication between fileboxes.
 * @author Jean-Charles Roger
 *
 */
public interface IFilebox extends Remote {

	public static final String NAME = "name";
	public static final String HOST = "host";
	public static final String PORT = "port";
	public static final String PROPERTIES = "properties";
	
	public String getName() throws RemoteException;

	public String getHost() throws RemoteException;
	
	public int getPort() throws RemoteException;
	
}

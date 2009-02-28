/**
 * Filebox developed by Kawane.
 * LGPL License.
 */
package org.kawane.filebox.core;

import org.kawane.filebox.core.discovery.IServiceDiscovery;

/**
 * Filebox interface. This is the interface used for communication between fileboxes.
 * @author Jean-Charles Roger
 *
 */
public interface IFilebox {

	public static final String NAME = "name";
	public static final String HOST = "host";
	public static final String PORT = "port";
	public static final String PROPERTIES = "properties";
	
	public String getName();

	public String getHost();
	
	public int getPort();
	
	public class Stub implements IFilebox {
		protected String name;
		
		public Stub(String name) {
			this.name = name;
		}

		public String getHost() {
			return "localhost";
		}

		public String getName() {
			return name;
		}

		public int getPort() {
			return IServiceDiscovery.DEFAULT_PORT;
		}
		
		
	}
	
}

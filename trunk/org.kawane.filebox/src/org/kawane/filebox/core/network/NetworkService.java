/**
 * Filebox developed by Kawane.
 * LGPL License.
 */
package org.kawane.filebox.core.network;

import org.kawane.filebox.core.DistantFilebox;

/**
 * 
 * A {@link NetworkService} is able to handle {@link HttpRequest} to propose a network service. 
 * 
 * @author Jean-Charles Roger
 *
 */
public interface NetworkService {

	/** Handle a request for a given Filebox. */
	public void handleRequest(DistantFilebox source, HttpRequest request, HttpResponse response); 
	
}

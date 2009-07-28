/**
 * Filebox developed by Kawane.
 * LGPL License.
 */
package org.kawane.filebox.core.network;

/**
 * 
 * A {@link NetworkService} is able to handle {@link HttpRequest} to propose a network service. 
 * 
 * @author Jean-Charles Roger
 *
 */
public interface NetworkService {

	/** Handle a request */
	public void handleRequest(HttpRequest request, HttpResponse response); 
	
}

/**
 * Filebox developed by Kawane.
 * LGPL License.
 */
package org.kawane.filebox.core.network;

import java.io.ByteArrayInputStream;

/**
 * @author Jean-Charles Roger
 *
 */
public class FileboxService implements NetworkService {

	
	
	public void handleRequest(HttpRequest request, HttpResponse response) {
		response.setContents(new ByteArrayInputStream("<html><body>Hello world!</body></html>".getBytes()));
	}

}

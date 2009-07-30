/**
 * Filebox developed by Kawane.
 * LGPL License.
 */
package org.kawane.filebox.core.network;

import java.io.BufferedOutputStream;
import java.io.IOException;

import org.kawane.filebox.boost.JBoost;

/**
 * @author Jean-Charles Roger
 *
 */
public class FileboxService implements NetworkService {

	public void handleRequest(HttpRequest request, HttpResponse response) {
		JBoost boost = new JBoost("filebox", 1);
		boost.initializeReading(request.getContents());
		String readString = boost.readString();
		boost.close();
		System.out.println("Read " + readString);
	}

	
	public static void main(String[] args) throws IOException {
		JBoost boost = new JBoost("filebox", 1);
		boost.initializeWriting(new BufferedOutputStream(System.out));
		boost.writeString("Hello Laurent");
		boost.close();
	}
}

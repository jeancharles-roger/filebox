/**
 * Filebox developed by Kawane.
 * LGPL License.
 */
package org.kawane.filebox.core.network;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.net.Socket;

import org.kawane.filebox.boost.JBoost;
import org.kawane.filebox.core.DistantFilebox;

/**
 * @author Jean-Charles Roger
 *
 */
public class FileboxService implements NetworkService {

	public void handleRequest(DistantFilebox source, HttpRequest request, HttpResponse response) {
		JBoost boost = new JBoost("filebox", 1);
		boost.initializeReading(request.getContents());
		String readString = boost.readString();
		// do not close boost
		System.out.println("------------------");
		System.out.println("Filebox service");
		System.out.println("Request: ");
		System.out.println(request);
		System.out.println("Read: ");
		System.out.println(readString);
	}

	
	public static void main(String[] args) throws Exception {
//		URL url = new URL("http://192.168.7.166:9999/filebox");
		
		Socket socket = new Socket("localhost", 9999);
		HttpRequest request = new HttpRequest("/filebox");
		JBoost boost = new JBoost("filebox", 1);
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		boost.initializeWriting(outputStream);
		boost.writeString("une phrase, on va bouffer bordel enfin j'ai la d‚‚‚‚‚‚‚lllllleee\n de mes deux");
		boost.close();

		ByteArrayInputStream contents = new ByteArrayInputStream(outputStream.toByteArray());
		request.setContents(contents);
		request.write(socket.getOutputStream());
		socket.getOutputStream().close();
		
		System.out.println(HttpResponse.read(socket.getInputStream()));
	}
}

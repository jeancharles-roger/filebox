/**
 * Filebox developed by Kawane.
 * LGPL License.
 */
package org.kawane.filebox.network.http.services;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;
import java.net.Socket;

import org.kawane.filebox.json.JSON;
import org.kawane.filebox.json.JSONStreamReader;
import org.kawane.filebox.json.JSONStreamWriter;
import org.kawane.filebox.network.http.HttpRequest;
import org.kawane.filebox.network.http.HttpResponse;
import org.kawane.filebox.network.http.NetworkService;

/**
 * @author Jean-Charles Roger
 *
 */
public class FileboxService implements NetworkService {

	public void handleRequest(HttpRequest request, HttpResponse response) {
//		JBoost boost = new JBoost("filebox", 1);
//		boost.initializeReading(request.getContents());
//		String readString = boost.readString();
		// do not close boost
		String readString = null;
		try {
			JSONStreamReader reader = new JSONStreamReader(new InputStreamReader(request.getContents()));
			int token =reader.next();
			while (token != -1) {
				switch (token) {
				case JSON.VALUE:
					readString = reader.getValue();
					break;

				default:
					break;
				}
				token = reader.next();
			}
			reader.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

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
//		JBoost boost = new JBoost("filebox", 1);
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		JSONStreamWriter writer = new JSONStreamWriter(outputStream);
		writer.beginDocument();
		writer.member("phrase");
		writer.stringValue("une phrase, on va bouffer bordel enfin j'ai la d�������lllllleee\n de mes deux");
		writer.endDocument();
		writer.close();
//		boost.initializeWriting(outputStream);
//		boost.writeString("une phrase, on va bouffer bordel enfin j'ai la d�������lllllleee\n de mes deux");
//		boost.close();
		ByteArrayInputStream contents = new ByteArrayInputStream(outputStream.toByteArray());
		request.setContents(contents);
		request.write(socket.getOutputStream());
		socket.getOutputStream().close();

		System.out.println(HttpResponse.read(socket.getInputStream()));
	}
}

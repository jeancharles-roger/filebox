package org.kawane.filebox.core.network;

import java.io.IOException;
import java.io.InputStream;

public class Http {

	public static final String HTTP_0_9 = "HTTP/0.9";
	public static final String HTTP_1_0 = "HTTP/1.0";
	public static final String HTTP_1_1 = "HTTP/1.1";
	
	public static final String METHOD_GET = "GET";
	public static final String METHOD_POST = "POST";

	
	public static final int CODE_OK = 200;
	public static final int CODE_FORBIDDEN = 403;
	public static final int CODE_NOTFOUND = 404;
	
	public static final String TEXT_OK = "OK";
	public static final String TEXT_FORBIDDEN = "FORBIDDEN";
	public static final String TEXT_NOTFOUND = "NOT FOUND";
	
	public static final String NL = System.getProperty("line.separator");
	
	public static final String HEADER_CONTENTTYPE = "Content-Type";
	public static final String HEADER_SERVER = "Server";
	
	
	
	public static final String SERVER_FILEBOX_1_0 = "Filebox /1.0";
	public static final String TEXT_HTML = "text/html";
	public static final String TEXT_BOOST = "text/boost";

	public static String readLine(InputStream stream) throws IOException {
		StringBuilder builder = new StringBuilder();
		int read = stream.read();
		while ( read >= 0  ) {
			char readChar = (char) read;
			if ( readChar == '\r' ) {
				// consume '\n'
				stream.read();
				break;
			}
			if ( readChar == '\n' ) break; 
			
			builder.append(readChar);
			read = stream.read();
		}
		return builder.toString();
	}
	


}

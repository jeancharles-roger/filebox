package org.kawane.filebox.network.http;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;

public class Http {

	private static final String DEFAULT_ENCODING_NAME = "UTF-8";
	public static final String HTTP_0_9 = "HTTP/0.9";
	public static final String HTTP_1_0 = "HTTP/1.0";
	public static final String HTTP_1_1 = "HTTP/1.1";

	public static final String METHOD_GET = "GET";
	public static final String METHOD_HEAD = "HEAD";
	public static final String METHOD_POST = "POST";
	public static final String METHOD_DELETE = "DELETE";
	public static final String METHOD_MKCOL = "MKCOL";
	public static final String METHOD_COPY = "COPY";
	public static final String METHOD_MOVE = "MOVE";
	public static final String METHOD_SEARCH = "SEARCH";
	public static final String METHOD_SEARCH_CONTENT = "SEARCH";

	public static final int CODE_OK = 200;
	public static final int CODE_CREATED = 201;
	public static final int CODE_FORBIDDEN = 403;
	public static final int CODE_NOTFOUND = 404;
	public static final int CODE_NOTALLOWED = 405;
	public static final int CODE_CONFLICT = 409;

	public static final String TEXT_OK = "OK";
	public static final String TEXT_FORBIDDEN = "FORBIDDEN";
	public static final String TEXT_NOTFOUND = "NOT FOUND";

	public static final String NL = System.getProperty("line.separator");

	public static final String HEADER_CONTENT_TYPE = "Content-Type";
	public static final String HEADER_CONTENT_LENGTH = "Content-Length";
	public static final String HEADER_SERVER = "Server";

	public static final String SERVER_FILEBOX_1_0 = "Filebox /1.0";
	// MIME TYPE
	public static final String TEXT_HTML = "text/html";
	public static final String TEXT_JSON = "text/json";
	public static final String TEXT_CSS = "text/css";
	public static final String TEXT_PLAIN = "text/plain";
	public static final String TEXT_BOOST = "text/boost";

	public static final String IMAGE_PNG = "image/png";

	public static String readLine(InputStream stream) throws IOException {
		StringBuilder builder = new StringBuilder();
		int read = stream.read();
		while (read >= 0) {
			char readChar = (char) read;
			if (readChar == '\r') {
				// consume '\n'
				stream.read();
				break;
			}
			if (readChar == '\n')
				break;

			builder.append(readChar);
			read = stream.read();
		}
		return builder.toString();
	}

	/** 
	 * code copyu from URI class
	 */
	public static String decode(String s) {
		try {
			return URLDecoder.decode(s, DEFAULT_ENCODING_NAME);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return s;
	}

	
	public static String encode(String s) {
		try {
			return URLEncoder.encode(s,DEFAULT_ENCODING_NAME);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return s;
    }

}

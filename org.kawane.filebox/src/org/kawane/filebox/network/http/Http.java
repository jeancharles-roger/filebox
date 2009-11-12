package org.kawane.filebox.network.http;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CoderResult;
import java.nio.charset.CodingErrorAction;

public class Http {

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
		if (s == null)
			return s;
		int n = s.length();
		if (n == 0)
			return s;
		if (s.indexOf('%') < 0)
			return s;

		StringBuffer sb = new StringBuffer(n);
		ByteBuffer bb = ByteBuffer.allocate(n);
		CharBuffer cb = CharBuffer.allocate(n);
		CharsetDecoder utf8decoder =  Charset.forName("UTF-8").newDecoder().onMalformedInput(CodingErrorAction.REPLACE).onUnmappableCharacter(
				CodingErrorAction.REPLACE);
		// This is not horribly efficient, but it will do for now
		char c = s.charAt(0);
		boolean betweenBrackets = false;

		for (int i = 0; i < n;) {
			assert c == s.charAt(i); // Loop invariant
			if (c == '[') {
				betweenBrackets = true;
			} else if (betweenBrackets && c == ']') {
				betweenBrackets = false;
			}
			if (c != '%' || betweenBrackets) {
				sb.append(c);
				if (++i >= n)
					break;
				c = s.charAt(i);
				continue;
			}
			bb.clear();
			for (;;) {
				assert (n - i >= 2);
				bb.put(decode(s.charAt(++i), s.charAt(++i)));
				if (++i >= n)
					break;
				c = s.charAt(i);
				if (c != '%')
					break;
			}
			bb.flip();
			cb.clear();
			utf8decoder.reset();
			CoderResult cr = utf8decoder.decode(bb, cb, true);
			assert cr.isUnderflow();
			cr = utf8decoder.flush(cb);
			assert cr.isUnderflow();
			sb.append(cb.flip().toString());
		}

		return sb.toString();
	}

	private static byte decode(char c1, char c2) {
		return (byte) (((decode(c1) & 0xf) << 4) | ((decode(c2) & 0xf) << 0));
	}

	private static int decode(char c) {
		if ((c >= '0') && (c <= '9'))
			return c - '0';
		if ((c >= 'a') && (c <= 'f'))
			return c - 'a' + 10;
		if ((c >= 'A') && (c <= 'F'))
			return c - 'A' + 10;
		assert false;
		return -1;
	}

}

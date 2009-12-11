package http;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.List;

public class HttpUtil {
	public static final String GET_METHOD = "GET";
	public static final String POST_METHOD = "POST";
	public static final String HEADER_CONTENT_TYPE = "Content-Type";
	public static final String HEADER_CONTENT_DISPOSITION = "Content-Disposition";
	public static final String HEADER_USER_AGENT = "User-Agent";
	public static final String HEADER_AUTHORIZATION = "Authorization";
	public static final String MIME_TYPE_FORM_DATA = "multipart/form-data";
	public static final Charset DEFAULT_ENCODING = Charset.forName("UTF-8");

	public final static String CRLF = "\r\n";
	public final static byte[] CRLF_BYTE = CRLF.getBytes();

	public static String getContentTypeMultipartForm(String boundary) {
		return MIME_TYPE_FORM_DATA + "; boundary=" + boundary;
	}

	public static void writeMultipartFormData(List<Object[]> formFields, OutputStream out, String boundary) throws IOException {
		for (Object[] field : formFields) {
			out.write(("--" + boundary).getBytes());
			out.write(CRLF_BYTE);
			if (field[1] instanceof File) {
				File file = (File) field[1];
				out.write((HEADER_CONTENT_DISPOSITION + ": form-data; name=\"" + field[0] + "\"; filename=\"" + file.getName() + "\"").getBytes());
				out.write(CRLF_BYTE);
				out.write((HEADER_CONTENT_TYPE + ": application/octet-stream").getBytes());
				out.write(CRLF_BYTE);
				out.write(CRLF_BYTE);
				out.flush();
				BufferedInputStream in = new BufferedInputStream(new FileInputStream(file));
				byte[] buf = new byte[4096];
				int read = in.read(buf);
				while (read != -1) {
					out.write(buf, 0, read);
					read = in.read(buf);
				}
				in.close();
			} else {
				out.write((HEADER_CONTENT_DISPOSITION + ": form-data; name=\"" + field[0] + "\"").getBytes());
				out.write(CRLF_BYTE);
				out.write(CRLF_BYTE);
				out.write((field[1]).toString().getBytes());
			}
			out.write(CRLF_BYTE);
		}
		out.write(("--" + boundary + "--").getBytes());
		out.write(CRLF_BYTE);
		out.flush();

	}
	
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
	
	public static void methodHeader(OutputStream out, String method,String urlPath) throws IOException {
		out.write(method.getBytes(DEFAULT_ENCODING));
		out.write(" ".getBytes(DEFAULT_ENCODING) );
		out.write(urlPath.getBytes(DEFAULT_ENCODING));
		out.write(" ".getBytes(DEFAULT_ENCODING));
		out.write("HTTP/1.1".getBytes(DEFAULT_ENCODING));
		out.write(CRLF_BYTE);
	}
	
	static public void appendHeader(OutputStream out, String name, String value) throws IOException {
		out.write(name.getBytes(DEFAULT_ENCODING));
		out.write(": ".getBytes(DEFAULT_ENCODING));
		out.write(value.getBytes(DEFAULT_ENCODING));
		out.write(CRLF_BYTE);
	}

}

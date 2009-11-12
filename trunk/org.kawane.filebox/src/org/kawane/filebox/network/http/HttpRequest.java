package org.kawane.filebox.network.http;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

public final class HttpRequest {

	private String http;
	private String method;
	private String url;

	private final Map<String, String> header = new HashMap<String, String>();
	private InputStream contents;

	private String retrievedContents;

	private String contextURL;
	private String servicePath;
	private Map<String, String[]> parameters;

	public static HttpRequest read(InputStream stream) throws Exception {

		String http = Http.HTTP_0_9;
		String method;
		String url;
		Map<String, String> header = new HashMap<String, String>();
		Map<String, String[]> parameters = new HashMap<String, String[]>();

		// first line contains 'method url version'
		String[] commands = Http.readLine(stream).split(" ");
		if (commands.length >= 2) {
			method = commands[0];
			url = commands[1];
			if (commands.length >= 3)
				http = commands[2];
		} else {
			return null;
		}
		if (url != null) {
			int indexOfParam = url.lastIndexOf('?');
			if (indexOfParam >= 0) {
				String params = url.substring(indexOfParam + 1);
				url = Http.decode(url.substring(0, indexOfParam));
				decodeParameter(params, parameters);
				System.out.println(params);
			} else {
				url = Http.decode(url);
			}
		}

		String line = Http.readLine(stream);
		while (line != null && line.length() > 0) {
			String[] info = line.split(":");
			if (info.length == 2) {
				header.put(info[0].trim(), info[1].trim());
			}
			line = Http.readLine(stream);
		}
		HttpRequest request = new HttpRequest(http, method, url, header, parameters, line == null ? null : stream);

		String contentType = header.get("Content-Type");
		if (contentType != null && contentType.equals("application/x-www-form-urlencoded")) {
			line = request.getRetrievedContents();
			if (line != null && line.length() > 0) {
				decodeParameter(line, parameters);
			}
		}
		return request;
	}

	public static void decodeParameter(String params, Map<String, String[]> parameters) {
		String[] paramSplit = params.split("&");
		for (String param : paramSplit) {
			int indexEqual = param.indexOf('=');
			if (indexEqual > 0) {
				String name = Http.decode(param.substring(0, indexEqual));
				String value = Http.decode(param.substring(indexEqual + 1));
				String[] values = parameters.get(name);
				if (values == null) {
					values = new String[] { value };
				} else {
					String[] result = new String[values.length + 1];
					System.arraycopy(values, 0, result, 0, values.length);
					result[values.length] = value;
					values = result;
				}
				parameters.put(name, values);
			}
		}
	}

	public HttpRequest(String url) {
		this(Http.METHOD_GET, url, null);
	}

	public HttpRequest(String url, Map<String, String[]> parameters) {
		this(Http.METHOD_GET, url, parameters);
	}

	public HttpRequest(String method, String url, Map<String, String[]> parameters) {
		this(method, url, parameters, null);
	}

	public HttpRequest(String method, String url, Map<String, String[]> parameters, InputStream contents) {
		this(method, url, null, parameters, contents);
	}

	public HttpRequest(String method, String url, Map<String, String> header, Map<String, String[]> parameters, InputStream contents) {
		this(Http.HTTP_1_1, method, url, header, parameters, contents);
	}

	public HttpRequest(String http, String method, String url, Map<String, String> header, Map<String, String[]> parameters, InputStream contents) {
		this.http = http;
		this.method = method;
		this.url = url;
		if (header != null)
			this.header.putAll(header);
		this.contents = contents;
		this.parameters = parameters;
	}

	/** @return request version */
	public String getHttp() {
		return http;
	}

	public void setHttp(String http) {
		this.http = http;
	}

	/** @return request method */
	public String getMethod() {
		return method;
	}

	public void setMethod(String method) {
		this.method = method;
	}

	/** @return request url. */
	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	/** @return a {@link Map} with header content. */
	public Map<String, String> getHeader() {
		return header;
	}

	/**
	 * @return a {@link InputStream} for the request contents. If null there is
	 *         no content.
	 */
	public InputStream getContents() {
		return contents;
	}

	public void setContents(InputStream contents) {
		this.contents = contents;
	}

	public String getRetrievedContents() {
		if (retrievedContents == null && contents != null) {
			try {
				StringBuffer buffer = new StringBuffer();
				String lengthStr = header.get(Http.HEADER_CONTENT_LENGTH);
				int bufSize = 1024;
				if (lengthStr != null) {
					try {
						bufSize = Integer.parseInt(lengthStr);
					} catch (NumberFormatException e) {
						e.printStackTrace();
					}
				}
				byte[] buf = new byte[bufSize];
				int read = -1;
				if (contents.available() > 0)
					read = contents.read(buf);
				while (read != -1) {
					buffer.append(new String(buf, 0, read, "utf-8"));
					if (contents.available() > 0) {
						read = contents.read(buf);
					} else {
						break;
					}
				}
				retrievedContents = buffer.toString();
			} catch (IOException e) {
				// TODO handle error
			}
		}
		return retrievedContents;
	}

	/** @return true if request is valid (HTTP structure only ) */
	public boolean isValid() {
		if (!Http.HTTP_0_9.equals(http) && !Http.HTTP_1_0.equals(http) && !Http.HTTP_1_1.equals(http)) {
			return false;
		}
		if (!Http.METHOD_GET.equals(method) && !Http.METHOD_POST.equals(method)) {
			return false;
		}
		return true;
	}

	public void write(OutputStream stream) throws Exception {
		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(stream));
		writer.append(prepareHeader());
		writer.flush();
		if (contents != null) {
			byte[] buffer = new byte[1024];
			int read = contents.read(buffer);
			while (read >= 0) {
				stream.write(buffer);
				read = contents.read(buffer);
			}
		}
		writer.flush();
	}

	private String prepareHeader() {
		StringBuilder response = new StringBuilder();
		response.append(method);
		response.append(" ");
		response.append(url);
		if (!Http.HTTP_0_9.equals(http)) {
			response.append(" ");
			response.append(http);
		}
		response.append(Http.NL);
		for (Entry<String, String> entry : header.entrySet()) {
			response.append(entry.getKey());
			response.append(": ");
			response.append(entry.getValue());
			response.append(Http.NL);
		}
		response.append(Http.NL);
		return response.toString();
	}

	@Override
	public String toString() {
		StringBuilder buffer = new StringBuilder();
		buffer.append(method);
		buffer.append(" ");
		buffer.append(url);
		if (parameters != null && parameters.size() > 0) {
			buffer.append("?");
			Iterator<Entry<String, String[]>> i = parameters.entrySet().iterator();
			while (i.hasNext()) {
				Entry<String, String[]> param = i.next();
				buffer.append(param.getKey());
				buffer.append("=");
				String[] values = param.getValue();
				for (int j = 0; j < values.length; j++) {
					String value = values[j];
					buffer.append(value);
					if (j + 1 < values.length) {
						buffer.append(",");
					}
				}
				if (i.hasNext()) {
					buffer.append("&");
				} else {
					break;
				}
			}
		}
		if (!Http.HTTP_0_9.equals(http)) {
			buffer.append(" ");
			buffer.append(http);
		}
		buffer.append(Http.NL);
		for (Entry<String, String> entry : header.entrySet()) {
			buffer.append(entry.getKey());
			buffer.append(": ");
			buffer.append(entry.getValue());
			buffer.append(Http.NL);
		}
		if (contents != null) {
			buffer.append(Http.NL);
			buffer.append("<Some contents>");
		}
		return buffer.toString();
	}

	public void setContextURL(String contextURL) {
		this.contextURL = contextURL;
	}

	public String getContextURL() {
		return contextURL;
	}

	public void setServicePath(String servicePath) {
		this.servicePath = servicePath;
	}

	public String getServicePath() {
		return servicePath;
	}

	public String[] getParameter(String name) {
		return parameters.get(name);
	}

	public String getStringParameter(String name) {
		String[] value = parameters.get(name);
		if (value != null && value.length > 0) {
			return value[0];
		}
		return null;
	}

	public Map<String, String> getParameters() {
		return null;
	}
}

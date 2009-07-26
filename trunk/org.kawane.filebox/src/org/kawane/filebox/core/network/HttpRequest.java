package org.kawane.filebox.core.network;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public final class HttpRequest {

	private final String http;
	private final String method;
	private final String url;
	
	private final Map<String, String> header = new HashMap<String, String>();
	private final BufferedReader contents;
	
	private String retrievedContents;
	
	public static HttpRequest readRequest(InputStream stream) throws Exception { 
		BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
		
		String http = Http.HTTP_0_9;
		String method;
		String url;
		Map<String, String> header = new HashMap<String, String>();
		
		// first line contains 'method url version'
		String [] commands = reader.readLine().split(" ");
		if ( commands.length >= 2 ) {
			method = commands[0];
			url = commands[1];
			if ( commands.length >= 3 ) http = commands[2];
		} else {
			return null;
		}
		
		String line = reader.readLine();
		while (line != null && line.length() > 0 ) {
			String [] info = line.split(":");
			if ( info.length == 2 ) {
				header.put(info[0].trim(), info[1].trim());
			}
			line = reader.readLine();
		}
		
		return new HttpRequest(http, method, url, header, line == null ? null : reader);
	}

	private HttpRequest(String http, String method, String url, Map<String, String> header,	BufferedReader contents) {
		this.http = http;
		this.method = method;
		this.url = url;
		if ( header != null) this.header.putAll(header);
		this.contents = contents;
	}
	
	/** @return request version */
	public String getHttp() {
		return http;
	}

	/** @return request method */
	public String getMethod() {
		return method;
	}

	/** @return request url. */
	public String getUrl() {
		return url;
	}

	/** @return a {@link Map} with header content. */
	public Map<String, String> getHeader() {
		return header;
	}

	/** @return a {@link BufferedReader} for the request contents. If null there is no content. */
	public BufferedReader getContents() {
		return contents;
	}
	
	public String getRetrievedContents() {
		if ( retrievedContents == null && contents != null ) {
			try {
				StringBuilder buffer = new StringBuilder();
				String line = contents.readLine();
				while ( line != null ) {
					buffer.append(line);
					buffer.append(Http.NL);
					line = contents.readLine();
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
		if ( 	!Http.HTTP_0_9.equals(http) && 
				!Http.HTTP_1_0.equals(http) && 
				!Http.HTTP_1_1.equals(http) ) {
			return false;
		}
		if ( 	!Http.METHOD_GET.equals(method) && 
				!Http.METHOD_POST.equals(method)) {
			return false;
		}
		return true;
	}
	
	@Override
	public String toString() {
		StringBuilder buffer = new StringBuilder();
		buffer.append(method);
		buffer.append(" ");
		buffer.append(url);
		if ( !Http.HTTP_0_9.equals(http) ) {
			buffer.append(" ");
			buffer.append(http);
		}
		buffer.append(Http.NL);
		for ( Entry<String, String> entry : header.entrySet() ) {
			buffer.append(entry.getKey());
			buffer.append(": ");
			buffer.append(entry.getValue());
			buffer.append(Http.NL);
		}
		if ( contents != null ) {
			buffer.append(Http.NL);
			buffer.append("<Some contents>");
		}
		return buffer.toString();
	}
}

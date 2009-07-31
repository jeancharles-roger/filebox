package org.kawane.filebox.core.network;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public final class HttpRequest {

	private String http;
	private String method;
	private String url;
	
	private final Map<String, String> header = new HashMap<String, String>();
	private InputStream contents;
	
	private String retrievedContents;
	
	public static HttpRequest read(InputStream stream) throws Exception { 
		
		String http = Http.HTTP_0_9;
		String method;
		String url;
		Map<String, String> header = new HashMap<String, String>();
		
		// first line contains 'method url version'
		String [] commands = Http.readLine(stream).split(" ");
		if ( commands.length >= 2 ) {
			method = commands[0];
			url = commands[1];
			if ( commands.length >= 3 ) http = commands[2];
		} else {
			return null;
		}
		
		String line = Http.readLine(stream);
		while (line != null && line.length() > 0 ) {
			String [] info = line.split(":");
			if ( info.length == 2 ) {
				header.put(info[0].trim(), info[1].trim());
			}
			line = Http.readLine(stream);
		}
		return new HttpRequest(http, method, url, header, line == null ? null : stream);
	}

	public HttpRequest(String url) {
		this(Http.METHOD_GET, url);
	}
	
	public HttpRequest(String method, String url) {
		this(method, url, null);
	}
	
	public HttpRequest(String method, String url, InputStream contents) {
		this(method, url, null, contents);
	}
	
	public HttpRequest(String method, String url, Map<String, String> header, InputStream contents) {
		this(Http.HTTP_1_1, method, url, header, contents);
	}
		
	public HttpRequest(String http, String method, String url, Map<String, String> header, InputStream contents) {
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

	/** @return a {@link InputStream} for the request contents. If null there is no content. */
	public InputStream getContents() {
		return contents;
	}
	
	public void setContents(InputStream contents) {
		this.contents = contents;
	}
	
	public String getRetrievedContents() {
		if ( retrievedContents == null && contents != null ) {
			try {
				BufferedReader reader = new BufferedReader(new InputStreamReader(contents));
				StringBuilder buffer = new StringBuilder();
				String line = reader.readLine();
				while ( line != null ) {
					buffer.append(line);
					buffer.append(Http.NL);
					line = reader.readLine();
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
	
	public void write(OutputStream stream) throws Exception {
		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(stream));
		writer.append(prepareHeader());
		writer.flush();
		if ( contents != null ) {
			byte [] buffer = new byte[1024];
			int read = contents.read(buffer);
			while (read >= 0 ) {
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
		if ( !Http.HTTP_0_9.equals(http) ) {
			response.append(" ");
			response.append(http);
		}
		response.append(Http.NL);
		for ( Entry<String, String> entry : header.entrySet() ) {
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

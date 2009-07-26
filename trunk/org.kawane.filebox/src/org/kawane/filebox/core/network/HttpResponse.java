package org.kawane.filebox.core.network;

import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class HttpResponse {

	private final String http;
	private int code = Http.CODE_OK;
	private String text = Http.TEXT_OK;
	
	private final Map<String, String> header = new HashMap<String, String>();
	private InputStream contents;
	
	public HttpResponse() {
		this(Http.HTTP_1_1);
	}
	
	public HttpResponse(String http) {
		this.http = http;
		header.put(Http.HEADER_SERVER, Http.SERVER_FILEBOX_1_0);
	}

	public String getHttp() {
		return http;
	}

	public int getCode() {
		return code;
	}

	public void setCode(int code) {
		this.code = code;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public Map<String, String> getHeader() {
		return header;
	}

	public InputStream getContents() {
		return contents;
	}

	public void setContents(InputStream contents) {
		this.contents = contents;
	}
	
	public void writeResponse(OutputStream stream) throws Exception {
		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(stream));
		writer.append(prepareHeader());
		byte [] buffer = new byte[1024];
		int read = contents.read(buffer);
		while (read >= 0 ) {
			stream.write(buffer);
			read = contents.read(buffer);
		}
	}
	
	private String prepareHeader() {
		StringBuilder response = new StringBuilder();
		response.append(http);
		response.append(" ");
		response.append(code);
		response.append(" ");
		response.append(text);
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
		return prepareHeader();
	}
	
}

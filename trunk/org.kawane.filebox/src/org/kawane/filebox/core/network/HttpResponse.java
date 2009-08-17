package org.kawane.filebox.core.network;

import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class HttpResponse {

	private String http = Http.HTTP_1_1;
	private int code = Http.CODE_OK;
	private String text = Http.TEXT_OK;

	private final Map<String, String> header = new HashMap<String, String>();
	private InputStream contents;

	public static HttpResponse read(InputStream stream) throws Exception {

		String http = Http.HTTP_0_9;
		int code;
		String text;
		Map<String, String> header = new HashMap<String, String>();

		// first line contains 'method url version'
		String [] commands = Http.readLine(stream).split(" ");
		if ( commands.length >= 3 ) {
			http = commands[0];
			code = Integer.parseInt(commands[1]);
			text = commands[2];
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
		return new HttpResponse(http, code, text, header, line == null ? null : stream);
	}


	public HttpResponse() {
		this(Http.HTTP_1_1, Http.CODE_OK, Http.TEXT_OK);
	}

	public HttpResponse(String http, int code, String text) {
		this(http, code, text, null, null);
	}

	public HttpResponse(String http, int code, String text, InputStream contents) {
		this(http, code, text, null, contents);
	}

	public HttpResponse(String http, int code, String text, Map<String, String> header,	InputStream contents) {
		this.http = http;
		this.code = code;
		this.text = text;
		this.header.put(Http.HEADER_CONTENT_TYPE, Http.TEXT_HTML);
		this.header.put(Http.HEADER_SERVER, Http.SERVER_FILEBOX_1_0);
		if ( header != null) this.header.putAll(header);
		this.contents = contents;
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

	public void write(OutputStream stream) throws Exception {
		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(stream));
		writer.append(prepareHeader());
		if ( contents != null ) {
			byte [] buffer = new byte[1024];
			int read = contents.read(buffer);
			while (read >= 0 ) {
				stream.write(buffer, 0, read);
				read = contents.read(buffer);
			}
		}
	}

	private String prepareHeader() {
		StringBuilder response = new StringBuilder();
		response.append(http);
		response.append(" ");
		response.append(code);
		response.append(" ");
		response.append(text);
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
		return prepareHeader();
	}

}

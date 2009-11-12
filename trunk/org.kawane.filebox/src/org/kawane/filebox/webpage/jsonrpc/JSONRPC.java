package org.kawane.filebox.webpage.jsonrpc;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringWriter;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.kawane.filebox.json.JSON;
import org.kawane.filebox.json.JSONStreamReader;
import org.kawane.filebox.json.JSONStreamWriter;
import org.kawane.filebox.network.http.Http;
import org.kawane.filebox.network.http.HttpRequest;
import org.kawane.filebox.network.http.HttpResponse;
import org.kawane.filebox.network.http.NetworkService;

public class JSONRPC implements NetworkService {
	private static Logger logger = Logger.getLogger(JSONRPC.class.getName());

	private File homeDir;


	public JSONRPC(File homeDir) {
		this.homeDir = homeDir;
	}

	public void handleRequest(HttpRequest request, HttpResponse response) {
		if (request.getMethod().equals(Http.METHOD_POST)) {
			post(request, response);
		} else if (request.getMethod().equals(Http.METHOD_GET)) {
			get(request, response);
		}
	}

	public void get(HttpRequest request, HttpResponse response) {
		String url = request.getUrl();
		File file = new File(homeDir, url);
		if (file.isDirectory()) {
			// list json
			listAll(response, file);
		} else {
			if (file.isDirectory()) {
				file = new File(file, "index.html");
			}
			if (file.exists()) {
				response.getHeader().put(Http.HEADER_CONTENT_TYPE, getMimeType(file));
				response.getHeader().put(Http.HEADER_CONTENT_LENGTH, String.valueOf(file.length()));
				try {
					response.setContents(new BufferedInputStream(new FileInputStream(file)));
				} catch (FileNotFoundException e) {
					logger.log(Level.SEVERE, "An Error Occured", e);
				}
			} else {
				response.setCode(Http.CODE_NOTFOUND);
			}
		}
	}
	
	public void post(HttpRequest request, HttpResponse response) {
		// create the snippet
		InputStream contents = request.getContents();
		byte[] b = new byte[1024];
		StringBuffer sb = new StringBuffer();
		try {
			int read = contents.read(b);
			while (read >= 0) {
				sb.append(new String(b, 0, read));
				if(contents.available() > 0) {
					read = contents.read(b);
				} else {
					break;
				}
			}
			try {
				File file = new File(homeDir, request.getUrl());
				if(file.exists() || file.isDirectory()) {
					// error already exists
					response.setContents(new ByteArrayInputStream("{}".getBytes()));
				} else {
					FileWriter writer = new FileWriter(file);
					writer.write(sb.toString());
					writer.close();
					response.setContents(new ByteArrayInputStream("{}".getBytes()));
				}
			} catch (Exception e) {
				logger.log(Level.SEVERE, "An Error Occured", e);
			}
			if (logger.isLoggable(Level.FINEST)) {
				logger.finest(request.getUrl()+ " : " +sb.toString());
			}
		} catch (IOException e) {
			logger.log(Level.SEVERE, "An Error Occured", e);
		}
	}
	
	private String getMimeType(File file) {
		if (file.getName().endsWith(".html")) {
			return Http.TEXT_HTML;
		}
		if (file.getName().endsWith(".css")) {
			return Http.TEXT_CSS;
		}
		if (file.getName().endsWith(".png")) {
			return Http.IMAGE_PNG;
		}
		return Http.TEXT_HTML;
	}

	protected void list(HttpResponse response, File file, Map<String, String> query) {
		StringWriter contentList = new StringWriter();
		JSONStreamWriter writer = new JSONStreamWriter(contentList);
		writer.beginDocument();
		writer.member("list");
		writer.beginArray();
		for (File child : file.listFiles()) {
			if (select(child, query)) {
				writer.writeJSON(getContent(child).toString());
			}
		}
		writer.endArray();
		writer.endDocument();
		writer.flush();
		response.setContents(new ByteArrayInputStream(contentList.toString().getBytes()));
	}

	private boolean select(File child, Map<String, String> query) {
		if (child.isFile() && !child.getName().endsWith(".html") && !child.getName().endsWith(".htm")) {
			boolean select = false;
			try {
				JSONStreamReader r = new JSONStreamReader(new FileReader(child));
				int token = r.next();
				String compareValue = null;
				loop: while (r.next() != -1) {
					switch (token) {
					case JSON.MEMBER:
						compareValue = query.get(r.getName());
						break;
					case JSON.VALUE:
						if (compareValue != null) {
							if (r.getValue() != null && r.getValue().matches(compareValue)) {
								select = true;
							} else {
								select = false;
								break loop;
							}
						}
						break;
					default:
						compareValue = null;
					}
					token = r.next();
				}
				r.close();
			} catch (IOException e) {
				logger.log(Level.SEVERE, "An Error Occured", e);
				return false;
			}
			return select;
		} else {
			return false;
		}
	}

	private void listAll(HttpResponse response, File file) {
		StringWriter contentList = new StringWriter();
		JSONStreamWriter writer = new JSONStreamWriter(contentList);
		writer.beginDocument();
		writer.member("list");
		writer.beginArray();
		for (File child : file.listFiles()) {
			if (child.isFile() && !child.getName().endsWith(".html") && !child.getName().endsWith(".htm")) {
				writer.writeJSON(getContent(child).toString());
			}
		}
		writer.endArray();
		writer.endDocument();
		writer.flush();
		response.setContents(new ByteArrayInputStream(contentList.toString().getBytes()));
	}

	private StringBuffer getContent(File child) {

		StringBuffer sb = new StringBuffer();
		try {
			Reader reader = new BufferedReader(new FileReader(child));
			char[] cbuf = new char[1024];
			int read = reader.read(cbuf);
			while (read != -1) {
				sb.append(cbuf, 0, read);
				read = reader.read(cbuf);
			}
			reader.close();
		} catch (IOException e) {
			logger.log(Level.SEVERE, "An Error Occured", e);
		}
		return sb;
	}

}

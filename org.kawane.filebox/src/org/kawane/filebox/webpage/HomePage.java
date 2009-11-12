package org.kawane.filebox.webpage;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.kawane.filebox.network.http.Http;
import org.kawane.filebox.network.http.HttpRequest;
import org.kawane.filebox.network.http.HttpResponse;
import org.kawane.filebox.network.http.NetworkService;

public class HomePage implements NetworkService {
	private static Logger logger = Logger.getLogger(HomePage.class.getName());
	private SnippetPage snippet;
	private File homeDir;

	public HomePage(File homeDir) {
		snippet = new SnippetPage(homeDir);
		this.homeDir = homeDir;
	}

	public void handleRequest(HttpRequest request, HttpResponse response) {
		String url = request.getUrl();
		if (url.startsWith(SnippetPage.URL)) {
			snippet.handleRequest(request, response);
		} else {
			if (request.getMethod().equals(Http.METHOD_POST)) {
				post(request, response);
			} else if (request.getMethod().equals(Http.METHOD_GET)) {
				get(request, response);
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
	public void get(HttpRequest request, HttpResponse response) {
		String url = request.getUrl();
		File file = new File(homeDir, url);
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
}

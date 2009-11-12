package org.kawane.filebox.network.http;

import java.io.File;

import org.kawane.filebox.network.http.services.FileService;

public class TestHTTPServer {

	public static void main(String[] args) {
		HttpServer server = new HttpServer(9090);
		server.setService("/", new FileService(new File("/home/laurent")));
		server.start();
	}
}

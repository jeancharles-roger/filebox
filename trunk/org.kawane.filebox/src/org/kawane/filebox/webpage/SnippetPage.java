package org.kawane.filebox.webpage;

import java.io.File;

import org.kawane.filebox.network.http.HttpRequest;
import org.kawane.filebox.network.http.HttpResponse;
import org.kawane.filebox.network.http.NetworkService;
import org.kawane.filebox.webpage.jsonrpc.JSONRPC;

public class SnippetPage implements NetworkService {

	public static final String URL = "/snippet";

	private JSONRPC jsonrpc;

	public SnippetPage(File homeDir) {
		this.jsonrpc = new JSONRPC(homeDir);
	}

	public void handleRequest(HttpRequest request, HttpResponse response) {
		jsonrpc.handleRequest(request, response);
	}

}

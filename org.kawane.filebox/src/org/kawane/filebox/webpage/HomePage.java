package org.kawane.filebox.webpage;

import java.io.File;

import org.kawane.filebox.core.network.HttpRequest;
import org.kawane.filebox.core.network.HttpResponse;
import org.kawane.filebox.core.network.NetworkService;

public class HomePage implements NetworkService {
	private SnippetPage snippet;
	private FileService fileService;

	public HomePage(File homeDir) {
		snippet = new SnippetPage(homeDir);
		fileService = new FileService(homeDir);
	}

	public void handleRequest(HttpRequest request, HttpResponse response) {
		String url = request.getUrl();
		if (url.startsWith(SnippetPage.URL)) {
			snippet.handleRequest(request, response);
		} else {
			fileService.handleRequest(request, response);
		}
	}
}

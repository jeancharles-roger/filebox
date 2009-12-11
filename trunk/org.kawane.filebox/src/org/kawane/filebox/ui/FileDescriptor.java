/**
 * Filebox developed by Kawane.
 * LGPL License.
 */

package org.kawane.filebox.ui;

import org.kawane.filebox.core.DistantFilebox;
import org.kawane.filebox.network.http.Http;

public class FileDescriptor {
	private final DistantFilebox filebox;
	private final String name;
	private final String path;
	private final boolean directory;
	private final String mime;
	private final long size;
	
	public FileDescriptor(DistantFilebox filebox, String name, String path, boolean directory, String mime, long size) {
		this.filebox = filebox;
		this.name = name;
		this.path = path;
		this.directory = directory;
		this.mime = mime;
		this.size = size;
	}
	
	public DistantFilebox getFilebox() {
		return filebox;
	}

	public String getName() {
		return name;
	}

	public String getPath() {
		return path;
	}
	
	public boolean isDirectory() {
		return directory;
	}

	public String getMime() {
		return mime;
	}

	public long getSize() {
		return size;
	}
	
	public String getCompleteURL() {
		StringBuilder url = new StringBuilder();
		url.append("http://");
		url.append(filebox.getHost());
		url.append(":");
		url.append(filebox.getPort());
		url.append(getPathURL());
		return url.toString();
	}
	
	public String getPathURL() {
		StringBuilder url = new StringBuilder();
		url.append("/files");
		url.append(Http.encode(path));
		url.append(Http.encode(name));
		return url.toString();
	}
	
}
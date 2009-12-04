/**
 * Filebox developed by Kawane.
 * LGPL License.
 */

package org.kawane.filebox.network.http;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import org.kawane.filebox.core.DistantFilebox;
import org.kawane.filebox.core.ErrorHandler;

public class Transfer {
	
	public final static int IDLE 		= 0;
	public final static int STARTED 	= 1;
	public final static int DONE 		= 2;
	public final static int ERROR 		= 3;
	
	private final DistantFilebox filebox;
	private final String url;
	private final File file;
	private final boolean upload;
	private final TransferMonitor monitor;
	
	private int length;
	private int done;
	private int state = IDLE;
	
	private InputStream inputStream;
	private OutputStream outputStream;
	private ErrorHandler errorHandler = ErrorHandler.Stub;
	
	public Transfer(DistantFilebox filebox, String url, File file, boolean upload, TransferMonitor monitor) {
		this.filebox = filebox;
		this.url = url;
		this.file = file;
		this.upload = upload;
		this.monitor = monitor == null ? TransferMonitor.empty : monitor;
	}
	
	public DistantFilebox getFilebox() {
		return filebox;
	}
	
	public String getUrl() {
		return url;
	}
	
	public File getFile() {
		return file;
	}
	
	public boolean isUpload() {
		return upload;
	}
	
	public ErrorHandler getErrorHandler() {
		return errorHandler;
	}
	
	public void setErrorHandler(ErrorHandler errorHandler) {
		this.errorHandler = errorHandler == null ? ErrorHandler.Stub : errorHandler;
	}
	
	public void start() {
		try {
			if ( upload ) {
				// not implemented yet
			} else {
				Socket socket = new Socket(filebox.getHost(), filebox.getPort());
				HttpRequest request = new HttpRequest(Http.encode(url));
				request.write(socket.getOutputStream());
				HttpResponse response = HttpResponse.read(socket.getInputStream());
				if ( response.getCode() != Http.CODE_OK ) {
					getErrorHandler().handleError(ErrorHandler.ERROR, "Http Error code " + response.getCode());
					return;
				}
				
				inputStream = new BufferedInputStream(response.getContents());
				outputStream = new BufferedOutputStream(new FileOutputStream(file));
				
				String contentLength = response.getHeader().get(Http.HEADER_CONTENT_LENGTH);
				length = contentLength == null ? -1 : Integer.parseInt(contentLength);
				state = STARTED;
				getMonitor().started(this, length);				
 			}
		} catch (Exception e) {
			state = ERROR;
			getErrorHandler().handleError(ErrorHandler.ERROR, e);
		}
	}
	
	public void transfer(int size) {
		try {
			int count = 1;
			int read = inputStream.read();
			while ( read != -1 ) {
				outputStream.write(read);
				if ( count++ >= size ) break;
				read = inputStream.read();
			}
			
			done += count;
			monitor.worked(this, done, length >= 0 ? length - done : -1);
			
			// end of file isn't reached, return.
			if ( read != -1 ) return;

			// end of file is reached, end transfer.
			inputStream.close();
			outputStream.close();
			
			state = DONE;
			getMonitor().done(this);
			
		} catch (Exception e ) {
			state = ERROR;
			getErrorHandler().handleError(ErrorHandler.ERROR, e);
		}
	}
	
	public int getDone() {
		return done;
	}
	
	public int getLength() {
		return length;
	}
	
	public int getState() {
		return state;
	}
	
	public TransferMonitor getMonitor() {
		return monitor;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((filebox == null) ? 0 : filebox.hashCode());
		result = prime * result + ((url == null) ? 0 : url.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Transfer other = (Transfer) obj;
		if (filebox == null) {
			if (other.filebox != null)
				return false;
		} else if (!filebox.equals(other.filebox))
			return false;
		if (url == null) {
			if (other.url != null)
				return false;
		} else if (!url.equals(other.url))
			return false;
		return true;
	}
	
}
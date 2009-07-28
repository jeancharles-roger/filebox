/**
 * Filebox developed by Kawane.
 * LGPL License.
 */

package org.kawane.filebox.core.network;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.kawane.filebox.core.Globals;
import org.kawane.filebox.core.Preferences;

/**
 * @author Jean-Charles Roger
 *
 */
public class HttpServer implements Runnable {

	private static final int THREADS_POOL_SIZE = 3;
	
//	private Filebox filebox = Globals.getLocalFilebox();
	private Preferences preferences = Globals.getPreferences();
	
	private ServerSocket serverSocket;
	private ExecutorService executors = Executors.newFixedThreadPool(THREADS_POOL_SIZE);
	private Thread internalThread = new Thread(this, "HttpServer");
	
	private boolean running = false;
	
	protected void initializeServer() {
		try {
			serverSocket = new ServerSocket(preferences.getPort());
			serverSocket.setSoTimeout(250);
		} catch (IOException e) {
			// TOD check errors
		}
	}
	
	public synchronized boolean isRunning() { return running; }
	
	
	public void start() {
		initializeServer();
		running = true;
		internalThread.start();
	}
	
	public synchronized void stop() {
		running = false;
		executors.shutdown();
	}
	
	public void run() {
		while (isRunning()) {
			try {
				final Socket socket = serverSocket.accept();
				executors.execute(new Runnable() {
					public void run() {
						handleSocket(socket);
					}
				});
			} catch (IOException e) {
				// do nothing 
			}
		}
	}
	
	/**
	 * Ran in a thread from the executor pools.
	 * It handles a socket when it receives it.
	 */
	protected void handleSocket(Socket socket) {
		try {
			HttpRequest request = HttpRequest.readRequest(socket.getInputStream());
			System.out.println(request);
			HttpResponse response = new HttpResponse();
			response.setContents(new ByteArrayInputStream("<html><body>Hello world!</body></html>".getBytes()));
			response.writeResponse(socket.getOutputStream());
			socket.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
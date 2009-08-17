/**
 * Filebox developed by Kawane.
 * LGPL License.
 */

package org.kawane.filebox.core.network;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.kawane.filebox.core.Globals;
import org.kawane.filebox.core.Preferences;

/**
 * @author Jean-Charles Roger
 *
 */
public class HttpServer implements Runnable {

	private static Logger logger = Logger.getLogger(HttpServer.class.getName());

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
			logger.log(Level.SEVERE, "An Error Occured when initializing server", e);
		}
	}

	public synchronized boolean isRunning() {
		return running;
	}

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
				logger.finest("Accepted socket: " + socket);
				executors.execute(new Runnable() {
					public void run() {
						handleSocket(socket);
					}
				});
			} catch (SocketTimeoutException e) {
				// do nothing
			} catch (IOException e) {
				logger.log(Level.SEVERE, "An Error Occured", e);
			}
		}
	}

	/**
	 * Ran in a thread from the executor pools.
	 * It handles a socket when it receives it.
	 */
	protected void handleSocket(Socket socket) {
		try {
			HttpRequest request = HttpRequest.read(socket.getInputStream());
			NetworkService service = null;
			if (request != null) {
				String[] fragments = request.getUrl().split("/");
				if (fragments.length > 0) {
					int i = 0;
					while (i < fragments.length && fragments[i].length() == 0)
						i++;
					service = Globals.getNetworkServices().get(fragments[i++]);
				}
				if (service == null) {
					// get default service
					service = Globals.getNetworkServices().get("/");
				}
			}
			HttpResponse response = new HttpResponse();
			if (service != null) {
				service.handleRequest(request, response);
			} else {
				response.setCode(Http.CODE_FORBIDDEN);
				response.setText(Http.TEXT_FORBIDDEN);
			}
			response.write(socket.getOutputStream());

		} catch (Exception e) {
			logger.log(Level.SEVERE, "An Error Occured", e);
		}
		try {
			socket.close();
		} catch (IOException e) {
			logger.log(Level.SEVERE, "An Error Occured", e);
		}
	}
}

/**
 * Filebox developed by Kawane.
 * LGPL License.
 */

package org.kawane.filebox.network.http;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Jean-Charles Roger
 *
 */
public class HttpServer implements Runnable {

	private static Logger logger = Logger.getLogger(HttpServer.class.getName());
	
	private static int PORT = 8080;

	private static final int THREADS_POOL_SIZE = 3;

	private Map<String, NetworkService> networksServices = new HashMap<String, NetworkService>();
	private ServerSocket serverSocket;
	private ExecutorService executors;
	private Thread internalThread;

	private boolean running = false;

	private int port = PORT;
	
	public HttpServer(int port) {
		this.port = port;
	}
	
	public HttpServer() {
	}
	
	public void setPort(int port) {
		this.port = port;
	}

	private void openSocket() {
		try {
			serverSocket = new ServerSocket(port);
			serverSocket.setSoTimeout(250);
		} catch (IOException e) {
			// TODO check errors
			logger.log(Level.SEVERE, "An Error Occured when initializing server", e);
		}
	}

	private void closeSocket() {
		try {
			serverSocket.close();
		} catch (IOException e) {
			// TODO check errors
			logger.log(Level.SEVERE, "An Error Occured when closing server", e);
		}
	}

	public synchronized boolean isRunning() {
		return running;
	}

	public void start() {
		openSocket();
		executors = Executors.newFixedThreadPool(THREADS_POOL_SIZE);
		internalThread = new Thread(this, "HttpServer Thread");

		running = true;
		internalThread.start();
	}

	public synchronized void stop() {
		running = false;
		closeSocket();
		executors.shutdown();
		executors = null;
	}

	public void setService(String fragment, NetworkService service) {
		networksServices.put(fragment, service);
	}
	
	public void removeService(String fragment) {
		networksServices.remove(fragment);
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
			} catch (SocketException e) {
				// do nothing
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
				String completeURL = request.getUrl();
				String url = completeURL;
				service = networksServices.get(url);
				request.setContextURL("/");
				request.setServicePath(url);
				while(service == null) {
					int index = url.lastIndexOf("/");
					if (index >= 0) {
						url = url.substring(0, index);
						service = networksServices.get(url.substring(0, index));
						if(service != null) {
							request.setContextURL(completeURL.substring(index, completeURL.length()));
							request.setServicePath(url);
						}
					} else {
							// get default service
							service = networksServices.get("/");
							request.setContextURL(completeURL);
							request.setServicePath("/");
							break;
					}
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

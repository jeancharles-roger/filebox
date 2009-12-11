/**
 * Filebox developed by Kawane.
 * LGPL License.
 */

package org.kawane.filebox.network.http;

import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.kawane.filebox.core.DistantFilebox;
import org.kawane.filebox.core.ErrorHandler;
import org.kawane.filebox.core.Observable;

/**
 * <p>Manages all file transfers for Filebox.</p> 
 * @author Jean-Charles Roger
 */
public class TransferManager implements Runnable, Observable {

	private static Logger logger = Logger.getLogger(HttpServer.class.getName());

	private final Observable.Stub obs = new Observable.Stub();
	private final Thread thread = new Thread(this, "Transfer Manager Thread");
	private ErrorHandler errorHandler = ErrorHandler.Stub;

	/** Stores all transfers */
	private final List<Transfer> transferList = new ArrayList<Transfer>();
	
	/** Stores only done ones */
	private final Set<Transfer> stoppedTransferSet = new HashSet<Transfer>();
	
	private boolean started = false;

	public ErrorHandler getErrorHandler() {
		return errorHandler;
	}
	
	public void setErrorHandler(ErrorHandler errorHandler) {
		this.errorHandler = errorHandler == null ? ErrorHandler.Stub : errorHandler;
	}
	
	public List<Transfer> getTransferList() {
		return Collections.unmodifiableList(transferList);
	}
	
	public synchronized void startDownload(DistantFilebox filebox, String url, File destinationFile, TransferMonitor monitor) {
		Transfer transfer = new Transfer(filebox, url, destinationFile, false, monitor);
		transfer.setErrorHandler(errorHandler);
		
		int index = transferList.indexOf(transfer);
		if ( index != -1 ) {
			Transfer oldTransfer = transferList.get(index);
			if ( oldTransfer.getState() != Transfer.STARTED ) {
				transferList.remove(oldTransfer);
				transferList.add(transfer);
			}
		} else {
			transferList.add(transfer);
		}
	}
	
	public synchronized void startUpload(String host, String port, String url, File content, TransferMonitor monitor) {
		// not implemented yet
	}
	
	public void start() {
		started = true;
		thread.start();
	}
	
	public synchronized void stop() {
		this.started = false;
	}
	
	public synchronized boolean isStarted() {
		return started;
	}
	
	public void run() {
 		while (isStarted()) {
 			boolean allDone = true;
			synchronized (this) {
				try {
					Iterator<Transfer> iterator = transferList.iterator();
					while (iterator.hasNext()) {
						Transfer transfer = iterator.next();
						switch ( transfer.getState() ) {
						case Transfer.IDLE:
							allDone = false;
							transfer.start();
							obs.firePropertyChange(this, "started", null, transfer);
							break;
							
						case Transfer.STARTED:
							allDone = false;
							transfer.transfer(2048);
							break;
							
						case Transfer.DONE:
							if ( !stoppedTransferSet.contains(transfer) ) {
								// send property change when transfer is done
								stoppedTransferSet.add(transfer);
								obs.firePropertyChange(this, "done", null, transfer);
							}
							break;
						case Transfer.ERROR:
							if ( !stoppedTransferSet.contains(transfer) ) {
								// send property change when transfer is done
								stoppedTransferSet.add(transfer);
								obs.firePropertyChange(this, "error", null, transfer);
							}
							break;
							
						}
					}
				} catch (Throwable e ) {
					logger.log(Level.SEVERE, "Exception thrown", e);
				}
 			}
			if ( allDone ) {
				try {
					Thread.sleep(250);
				} catch (InterruptedException e) {
					// do nothing
				}
			}
		}
	}
	
	public void addPropertyChangeListener(PropertyChangeListener listener) {
		obs.addPropertyChangeListener(listener);
	}

	public void removePropertyChangeListener(PropertyChangeListener listener) {
		obs.removePropertyChangeListener(listener);
	}
}

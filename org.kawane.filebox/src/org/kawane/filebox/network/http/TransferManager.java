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

import org.kawane.filebox.core.DistantFilebox;
import org.kawane.filebox.core.ErrorHandler;
import org.kawane.filebox.core.Observable;

/**
 * <p>Manages all file transfers for Filebox.</p> 
 * @author Jean-Charles Roger
 */
public class TransferManager implements Runnable, Observable {

	private final Observable.Stub obs = new Observable.Stub();
	private final Thread thread = new Thread(this);
	private ErrorHandler errorHandler = ErrorHandler.Stub;

	/** Stores all transfers */
	private final List<Transfer> transferList = new ArrayList<Transfer>();
	
	/** Stores only done ones */
	private final Set<Transfer> doneTransferSet = new HashSet<Transfer>();
	
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
		if ( !transferList.contains(transfer) )	transferList.add(transfer);
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
			synchronized (this) {
				Iterator<Transfer> iterator = transferList.iterator();
				while (iterator.hasNext()) {
					Transfer transfer = iterator.next();
					switch ( transfer.getState() ) {
					case Transfer.IDLE:
						transfer.start();
						obs.firePropertyChange(this, "started", null, transfer);
						break;
						
					case Transfer.STARTED:
						transfer.transfer(2048);
						break;
						
					case Transfer.DONE:
						if ( !doneTransferSet.contains(transfer) ) {
							// send property change when transfer is done
							doneTransferSet.add(transfer);
							obs.firePropertyChange(this, "done", null, transfer);
						}
						break;
						
					}
				}
			}

			if ( transferList.isEmpty() ) {
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

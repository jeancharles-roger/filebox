package org.kawane.filebox.network.http;

/** Monitor a transfert */
public interface TransferMonitor {

	void started();
	void done();
	
	/** Empty monitor, it does nothing. */
	public static TransferMonitor empty = new TransferMonitor() {
		public void started() { /* do nothing */ }
		public void done() { /* do nothing */ }
	};
}

package org.kawane.filebox.network.http;

/** Monitor a transfert */
public interface TransferMonitor {

	void started(Transfer transfer, int remaining);
	void worked(Transfer transfer, int done, int remaining);
	void done(Transfer transfer);
	
	/** Empty monitor, it does nothing. */
	public static TransferMonitor empty = new TransferMonitor() {
		public void started(Transfer transfer, int remaining) { /* do nothing */ }
		public void worked(Transfer transfer, int done, int remaining) { /* do nothing */ }
		public void done(Transfer transfer) { /* do nothing */ }
	};
}

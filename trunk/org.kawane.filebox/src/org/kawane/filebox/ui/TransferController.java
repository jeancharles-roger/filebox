package org.kawane.filebox.ui;

import java.text.DecimalFormat;
import java.util.HashMap;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.kawane.filebox.network.http.Transfer;
import org.kawane.filebox.network.http.TransferManager;
import org.kawane.filebox.network.http.TransferMonitor;

public class TransferController {

	private final Display display;
	private final TransferManager transferManager;
	
	private Composite composite;
	private Table downloadTable;
	
	private final DecimalFormat format = new DecimalFormat("%");
	private final HashMap<Transfer, TableItem> items = new HashMap<Transfer, TableItem>();
	private final TransferMonitor transferMonitor = new TransferMonitor() {

		public void started(final Transfer transfer, int remaining) {
			display.asyncExec(new Runnable() {
				public void run() {
					TableItem item = new TableItem(downloadTable, SWT.NONE);
					items.put(transfer, item);
					refreshTransferItem(transfer,item, transfer.getDone(), transfer.getLength());
				};
			});
		}

		public void worked(final Transfer transfer, int done, int remaining) {
			display.asyncExec(new Runnable() {
				public void run() {
					TableItem item = items.get(transfer);
					if ( item != null ) {
						refreshTransferItem(transfer,item, transfer.getDone(), transfer.getLength());
					}
				};
			});
		}
		
		public void done(final Transfer transfer ) {
			display.asyncExec(new Runnable() {
				public void run() {
					TableItem item = items.get(transfer);
					if ( item != null ) {
						refreshTransferItem(transfer,item, transfer.getDone(), transfer.getLength());
					}
				};
			});
		}
		
	};
	
	public TransferController(Display display, TransferManager transferManager) {
		this.display = display;
		this.transferManager = transferManager;
	}
	
	public Composite getComposite() {
		return composite;
	}
	
	public Composite createComposite(Composite parent) {
		composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new FillLayout());
		
		downloadTable = new Table(composite, SWT.BORDER | SWT.FULL_SELECTION);
		downloadTable.setLinesVisible(true);
		return composite;
	}
	
	public void refreshUI() {
		downloadTable.removeAll();
		for ( Transfer transfer : transferManager.getTransferList() ) {
			TableItem item = new TableItem(downloadTable, SWT.NONE);
			refreshTransferItem(transfer,item, transfer.getDone(), transfer.getLength());
		}
	}
	
	
	private void refreshTransferItem(Transfer transfer, TableItem item, int done, int remaining) {
		StringBuilder builder = new StringBuilder();
		builder.append(transfer.getFilebox().getName());
		builder.append(":/files");
		builder.append(transfer.getUrl());
		
		builder.append(" (size ");
		if ( transfer.getLength() >= 0 ) {
			builder.append(Utils.displaySize(transfer.getLength()));
		} else {
			builder.append("unknown");
		}
		builder.append(")");
		builder.append(" (rate ");
		if ( transfer.getLength() >= 0 ) {
			builder.append(transfer.getByteRate());
			builder.append(" ko/s");
		} else {
			builder.append("unknown");
		}
		builder.append(")");
		
		
		builder.append(": ");
		switch(transfer.getState()) {
		case Transfer.IDLE:
			builder.append("idle");
			break;
		case Transfer.STARTED:
			if ( remaining >= 0 ) {
				float ratio = ((float) done) / (float) (transfer.getLength());
				builder.append(format.format(ratio));
			} else {
				builder.append(done);
			}
			break;
		case Transfer.DONE:
			builder.append("done");
			break;
		case Transfer.ERROR:
			builder.append("error");
			break;
			
		}
		item.setText(builder.toString());
	}
	
	public TransferMonitor getTransferMonitor() {
		return transferMonitor;
	}
	
	public boolean updateModel(Event event) {
		return false;
	}
	
	
	
}

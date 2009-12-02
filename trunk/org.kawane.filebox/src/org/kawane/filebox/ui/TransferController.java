package org.kawane.filebox.ui;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.kawane.filebox.network.http.Transfer;
import org.kawane.filebox.network.http.TransferManager;

public class TransferController {

	private final TransferManager transferManager;
	
	private final Display display;
	private Shell shell;
	private Listener shellListener = new Listener() {
		public void handleEvent(Event event) {
			switch( event.type ) {
			case SWT.Close:
				boolean visible = !shell.isVisible();
				shell.setVisible(visible);
				
				// do not quit the application when closing the shell
				event.doit = false;
				break;
			case SWT.Dispose:
				break;
			}
		}
	};

	private Table downloadTable;
	
	PropertyChangeListener transferManagerListener = new PropertyChangeListener() {
		public void propertyChange(PropertyChangeEvent evt) {
			display.asyncExec(new Runnable() {
				public void run() {
					refreshUI();
				}
			});
		}
	};
	
	public TransferController(Display display, TransferManager transferManager) {
		this.display = display;
		this.transferManager = transferManager;
		this.transferManager.addPropertyChangeListener(transferManagerListener);
	}
	
	public Shell getShell() {
		return shell;
	}
	
	public Shell createShell() {
		shell = new Shell(display);
		shell.setLayout(new FillLayout());
		shell.setSize(300, 200);
		
		shell.addListener(SWT.Close, shellListener);
		shell.addListener(SWT.Dispose, shellListener);
	
		downloadTable = new Table(shell, SWT.BORDER | SWT.FULL_SELECTION);
		downloadTable.setLinesVisible(true);
		return shell;
	}
	
	public void refreshUI() {
		downloadTable.removeAll();
		for ( Transfer transfer : transferManager.getTransferList() ) {
			StringBuilder builder = new StringBuilder();
			builder.append(transfer.getFilebox().getName());
			builder.append(":/files");
			builder.append(transfer.getUrl());
			builder.append("(");
			switch(transfer.getState()) {
			case Transfer.IDLE:
				builder.append("idle");
				break;
			case Transfer.STARTED:
				builder.append("started");
				break;
			case Transfer.DONE:
				builder.append("done");
				break;
			case Transfer.ERROR:
				builder.append("error");
				break;
				
			}
			builder.append(")");
			
			TableItem item = new TableItem(downloadTable, SWT.NONE);
			item.setText(builder.toString());
		}
	}
	
	public boolean updateModel(Event event) {
		return false;
	}
	
	
	
}

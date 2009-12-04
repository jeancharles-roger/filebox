package org.kawane.filebox.ui;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.kawane.filebox.network.http.Transfer;
import org.kawane.filebox.network.http.TransferManager;

public class TransferController {

	private final TransferManager transferManager;
	
	private final Display display;
	private Composite composite;
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

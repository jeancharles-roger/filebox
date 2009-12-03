package org.kawane.filebox.ui;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.InputStreamReader;
import java.net.Socket;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.dnd.URLTransfer;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.kawane.filebox.Resources;
import org.kawane.filebox.core.DistantFilebox;
import org.kawane.filebox.core.Filebox;
import org.kawane.filebox.core.FileboxRegistry;
import org.kawane.filebox.core.Preferences;
import org.kawane.filebox.json.JSON;
import org.kawane.filebox.json.JSONStreamReader;
import org.kawane.filebox.mime.MimeTypeDatabase;
import org.kawane.filebox.network.http.Http;
import org.kawane.filebox.network.http.HttpRequest;
import org.kawane.filebox.network.http.HttpResponse;
import org.kawane.filebox.network.http.TransferManager;

public class ContactController {

	private static class FileDescriptor {
		final boolean directory;
		final String mime;
		final String name;
		final long size;
		
		FileDescriptor(boolean directory, String mime, String name, long size) {
			this.directory = directory;
			this.mime = mime;
			this.name = name;
			this.size = size;
		}
	}

	private final Filebox filebox;
	private final FileboxRegistry registry;
	private final Preferences preferences;
	private final TransferManager transferManager;
	private final MimeTypeDatabase mimeTypeDatabase = new MimeTypeDatabase();
	private DecimalFormat numberFormat = new DecimalFormat("0.###");

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
				filebox.removePropertyChangeListener(propertiesListener);
				registry.removePropertyChangeListener(propertiesListener);
				break;
			}
		}
	};
	
	/** Shared resources instances. */
	private Resources resources = Resources.getInstance();

	private Composite contactComposite;
	
	private Composite meComposite;
	private Label meLabel;
	private Combo statusCombo;
	private Listener statusComboListener = new Listener(){
		public void handleEvent(Event event) {
			updateModel(event);
		}
	};

	private Table contactsTable;
	private TableColumn statusFileboxColumn;
	private TableColumn nameFileboxColumn;
	private TableColumn hostFileboxColumn;
	private Listener contactsTableListener = new Listener() {
		public void handleEvent(Event e) {
			
			switch (e.type) {
			case SWT.SetData:
				TableItem item = (TableItem)e.item;
				int index = contactsTable.indexOf(item);
				if ( registry.getFileboxesCount() < index ) break;
				
				DistantFilebox distantFilebox = registry.getFilebox(index);
				item.setData(distantFilebox);
				//item.setImage(0, resources.getImage(distantFilebox.isConnected() ? "connected.png" : "disconnected.png"));
				item.setImage(0, resources.getImage("connected.png"));
				item.setText(1, distantFilebox.getName());
				item.setText(2, distantFilebox.getHost());
				break;
			case SWT.Resize:
				resizeContactTable();
				break;
			case SWT.Selection:
				updateModel(e);
				break;
			case SWT.Dispose:
				e.widget.removeListener(SWT.SetData, this);
				e.widget.removeListener(SWT.Resize, this);
				e.widget.removeListener(SWT.Selection, this);
				e.widget.removeListener(SWT.Dispose, this);
			}
		}
	};
	
	private Group filesComposite;
	private Composite pathComposite;
	private Button upButton;
	private Listener upButtonListener = new Listener() {
		public void handleEvent(Event e) {
			switch (e.type) {
			case SWT.Selection:
				updateModel(e);
				break;
			case SWT.Dispose:
				e.widget.removeListener(SWT.Selection, this);
				e.widget.removeListener(SWT.Dispose, this);
				break;
			}
		}
	};
	private Label pathLabel;
	private Table filesTable;
	private TableColumn iconFileColumn;
	private TableColumn nameFileColumn;
	private TableColumn sizeFileColumn;

	private Listener filesTableListener = new Listener() {
		public void handleEvent(Event e) {
			
			switch (e.type) {
			case SWT.SetData:
				TableItem item = (TableItem)e.item;
				int index = filesTable.indexOf(item);
				FileDescriptor fileDescriptor = fileList.get(index);
				String icon = "folder.png";
				if ( !fileDescriptor.directory ) {
					icon = mimeTypeDatabase.searchIconByMime(fileDescriptor.mime);
				}
				item.setImage(0, resources.getImage(icon));
				item.setText(1, fileDescriptor.name);
				item.setText(2, displaySize(fileDescriptor.size));
				break;
			case SWT.Resize:
				resizeFilesTable();
				break;
			case SWT.MouseDoubleClick:
				updateModel(e);
				break;
			case SWT.Dispose:
				e.widget.removeListener(SWT.SetData, this);
				e.widget.removeListener(SWT.Dispose, this);
				break;
			case SWT.MenuDetect:
				createFileTableContextMenu(e);
			}
		}
	};

	
	private final HashMap<DistantFilebox, String> fileboxPathes = new HashMap<DistantFilebox, String>();
	private final List<FileDescriptor> fileList = new ArrayList<FileDescriptor>();
	
	private PropertyChangeListener propertiesListener = new PropertyChangeListener() {
		public void propertyChange(final PropertyChangeEvent evt) {
			shell.getDisplay().asyncExec(new Runnable(){
				public void run() {
					refreshUI();
				}
			});
		}
	};
	
	public ContactController(Display display, Filebox filebox, FileboxRegistry registry, Preferences preferences, TransferManager transferManager) {
		this.display = display;
		this.filebox = filebox;
		this.registry = registry;
		this.preferences = preferences;
		this.transferManager = transferManager;
	}

	protected void createFileTableContextMenu(Event e) {
		Menu menu = new Menu(e.display.getActiveShell());
		MenuItem item = new MenuItem(menu, SWT.PUSH);
		item.setText("Copy URL to Clipboard");
		item.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				try {
					Clipboard clipboard = new Clipboard(display);
					String url = getSelectedFileURL();
					clipboard.setContents(new Object[]{url,url}, new Transfer[]{URLTransfer.getInstance(), TextTransfer.getInstance()});
				} catch (Throwable ex) {
					ex.printStackTrace();
				}
			}
		});
		menu.setVisible(true);
	}

	public Shell getShell() {
		return shell;
	}
	
	private void resizeContactTable() {
		final float hostRatio = 0.4f;
		final int statusSize = 20;
		final int margin = 10;
		int width = contactsTable.getSize().x - margin - statusSize;
		int hostSize = (int) (hostRatio * width);
		statusFileboxColumn.setWidth( statusSize );
		nameFileboxColumn.setWidth( width - hostSize - statusSize);
		hostFileboxColumn.setWidth(hostSize);
	}

	
	public Shell createShell() {

		shell = new Shell(display);
		shell.setLayout(new GridLayout(1,false));
		shell.setImage(resources.getImage("filebox-icon-256x256.png"));
		shell.setSize(300, 300);
		shell.setText("FileBox");
		shell.addListener(SWT.Close, shellListener);
		shell.addListener(SWT.Dispose, shellListener);
		
		contactComposite = new Composite(shell, SWT.NONE);
		contactComposite.setLayout(new GridLayout(1,false));
		contactComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		meComposite = new Composite(contactComposite, SWT.NONE);
		meComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		meComposite.setLayout(new GridLayout(2,false));

		// my name label
		meLabel = new Label(meComposite, SWT.NONE);
		meLabel.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
		meLabel.setText("Me");

		// status combo
		statusCombo = new Combo(meComposite, SWT.READ_ONLY);
		statusCombo.setLayoutData(new GridData(SWT.END, SWT.CENTER, true, false));
		statusCombo.setItems( new String[] { "On line", "Off line" } );
		statusCombo.select(1);
		statusCombo.addListener(SWT.Selection, statusComboListener);

		// a separator
		Label separator = new Label(contactComposite, SWT.SEPARATOR | SWT.HORIZONTAL);
		separator.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

		// contacts label
		Label contactsLabel = new Label(contactComposite, SWT.NONE);
		contactsLabel.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, false));
		contactsLabel.setText("Contacts" + ":");

		// contacts table
		contactsTable = new Table(contactComposite,  SWT.MULTI | SWT.VIRTUAL | SWT.BORDER | SWT.FULL_SELECTION);
		contactsTable.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		contactsTable.setLinesVisible(true);
		statusFileboxColumn = new TableColumn(contactsTable, SWT.CENTER);
		nameFileboxColumn = new TableColumn(contactsTable, SWT.NONE);
		hostFileboxColumn = new TableColumn(contactsTable, SWT.RIGHT);
		contactsTable.addListener(SWT.SetData, contactsTableListener);
		contactsTable.addListener(SWT.Resize, contactsTableListener);
		contactsTable.addListener(SWT.Selection, contactsTableListener);
		contactsTable.addListener(SWT.Dispose, contactsTableListener);
		contactsTable.setItemCount(0);
		resizeContactTable();
		
		filebox.addPropertyChangeListener(propertiesListener);
		registry.addPropertyChangeListener(propertiesListener);
		return shell;
	}
	
	private boolean isFileTableVisible() {
		return filesComposite != null && !filesComposite.isDisposed();
	}
	
	private void showFileTable() {
		if ( isFileTableVisible() ) return;
		
		filesComposite = new Group(shell, SWT.NONE);
		GridLayout gridLayout = new GridLayout(1, false);
		gridLayout.marginWidth = 0;
		gridLayout.marginHeight = 0;
		gridLayout.marginTop = 0;
		gridLayout.marginBottom = 0;
		gridLayout.marginLeft = 0;
		gridLayout.marginRight = 0;
		filesComposite.setLayout(gridLayout);
		filesComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		filesComposite.setText("Files");

		pathComposite = new Composite(filesComposite, SWT.NONE);
		pathComposite.setLayout(new GridLayout(2, false));
		pathComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		
		upButton = new Button(pathComposite, SWT.PUSH);
		upButton.setText("Up");
		upButton.addListener(SWT.Selection, upButtonListener);
		upButton.addListener(SWT.Dispose, upButtonListener);
		GridData gridData = new GridData(SWT.FILL, SWT.CENTER, false, false);
		upButton.setLayoutData(gridData);
		
		pathLabel = new Label(pathComposite, SWT.NONE);
		pathLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		
		filesTable = new Table(filesComposite, SWT.VIRTUAL | SWT.BORDER | SWT.FULL_SELECTION);
		filesTable.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		filesTable.addListener(SWT.SetData, filesTableListener);
		filesTable.addListener(SWT.Resize, filesTableListener);
		filesTable.addListener(SWT.MouseDoubleClick, filesTableListener);
		filesTable.addListener(SWT.Dispose, filesTableListener);
		filesTable.addListener(SWT.MenuDetect, filesTableListener);
		
		iconFileColumn = new TableColumn(filesTable, SWT.NONE);
		iconFileColumn.setWidth(20);

		nameFileColumn = new TableColumn(filesTable, SWT.NONE);
		nameFileColumn.setWidth(100);
		
		sizeFileColumn = new TableColumn(filesTable, SWT.NONE);
		
		shell.setLayout(new GridLayout(2, true));
		shell.layout();
		
		shell.setSize(shell.getSize().x * 2, shell.getSize().y);
	}
	
	private void resizeFilesTable() {
		iconFileColumn.setWidth(20);
		sizeFileColumn.setWidth(100);
		nameFileColumn.setWidth(filesTable.getSize().x - 125);
	}


	private void hideFileTable() {
		if ( !isFileTableVisible() ) return;
		filesComposite.dispose();
		filesComposite = null;
		filesTable = null;
		shell.setLayout(new GridLayout(1, false));
		shell.layout();

		shell.setSize(shell.getSize().x / 2, shell.getSize().y);
	}
	
	private void fillFiles() {
		DistantFilebox selected = getSelectedFilebox();
		String path = fileboxPathes.get(selected);
		if ( path == null ) path = "/";
		
		fileList.clear();
		
		if ( selected != null ) {
//			StringBuffer request = new StringBuffer();
//			request.append("http://");
//			request.append(selected.getHost());
//			request.append(":");
//			request.append(selected.getPort());
//			request.append("/files");
//			request.append(Http.encode(path.trim()));
//			request.append("?format=json");
			
			try { 
				Socket socket = new Socket(selected.getHost(), selected.getPort());
				HttpRequest request = new HttpRequest("/files" + Http.encode(path.trim()));
				request.write(socket.getOutputStream());
				HttpResponse response = HttpResponse.read(socket.getInputStream());
				JSONStreamReader reader = new JSONStreamReader(new InputStreamReader(response.getContents(), "UTF-8"));
				boolean directory = false;
				String type = null;
				String name = null;
				long size = 0;
				int token = reader.next();
				while ( token > 0 ) {
					switch (token) {
					case JSON.MEMBER:
						
						if ( reader.getName().equals("directory") ) {
							token = reader.next();
							directory = reader.getBoolean();
							
						} else if ( reader.getName().equals("mime") ) {
							token = reader.next();
							type = reader.getValue();
							
						} else if ( reader.getName().equals("name") ) {
							token = reader.next();
							name = reader.getValue();
							
						} else if ( reader.getName().equals("size") ) {
							token = reader.next();
							size = reader.getLong();
						}
						break;
					case JSON.END_OBJECT:
							fileList.add(new FileDescriptor(directory, type, name, size));
						break;
						}
					token = reader.next();
				}
			} catch (Exception e) {
				// TODO handle errors.
				e.printStackTrace();
			}
		}
	}
	
	private String displaySize(long length) {
		String unit = " B";
		double l = length;
		if (l >= 1024) {
			unit = " KB";
			l = l / 1024;
		}
		if (l >= 1024) {
			unit = " MB";
			l = l / 1024;
		}
		if (l >= 1024) {
			unit = " GB";
			l = l / 1024;
		}
		return numberFormat.format(l) + unit;
	}

	
	public void refreshUI() {
		meLabel.setText(filebox.getName());
		meLabel.getParent().layout();
		statusCombo.setEnabled(filebox.getState() != Filebox.PENDING);
		if ( filebox.getState() != Filebox.PENDING )  {
			statusCombo.select(filebox.getState() - 1);
			if ( filebox.getState() == Filebox.CONNECTED ) {
				showFileTable();
			} else {
				hideFileTable();
			}
		}
		meComposite.layout();
		
		contactsTable.clearAll();
		contactsTable.setItemCount(registry.getFileboxesCount());
		
		if ( isFileTableVisible() ) {
			String path = fileboxPathes.get(getSelectedFilebox());
			if ( path == null ) path = "/";
			pathLabel.setText(path);
			
			filesTable.clearAll();
			filesTable.setItemCount(fileList.size());
		}
	}
	
	public boolean updateModel(Event event) {
		if ( statusCombo == event.widget ) {
			if ( statusCombo.getSelectionIndex()+1 == filebox.getState() ) return false;
			
			if ( statusCombo.getSelectionIndex() == 0 ) {
				filebox.connect(null);
			} else {
				filebox.disconnect(null);
			}
			return true;
		}
		
		if ( contactsTable == event.widget ) {
			fillFiles();
			refreshUI();
			return true;
		}

		if ( upButton == event.widget) {
			DistantFilebox selectedFilebox = getSelectedFilebox();
			String path = fileboxPathes.get(selectedFilebox);
			if ( path == null || path.equals("/") ) return false;
			
			int index = path.substring(0, path.length() - 1).lastIndexOf('/');
			path = path.substring(0, index +1 );
			fileboxPathes.put(selectedFilebox, path);
			fillFiles();
			refreshUI();
			return true;
		}
		
		if ( filesTable == event.widget ) {
			switch (event.type) {
			case SWT.MouseDoubleClick:
				FileDescriptor file = fileList.get(filesTable.getSelectionIndex());
					DistantFilebox selectedFilebox = getSelectedFilebox();
					String path = fileboxPathes.get(selectedFilebox);
					if ( path == null ) path = "/";
				String url = path + file.name;

				if ( file.directory ) {
					fileboxPathes.put(selectedFilebox, url + "/");
					fillFiles();
					refreshUI();
					return true;
				} else {
					File destinationFile = new File(preferences.getPublicDir(), file.name);
					if ( !destinationFile.getParentFile().exists() ) destinationFile.getParentFile().mkdirs();
					transferManager.startDownload(selectedFilebox, "/files" + url, destinationFile, null);
					return false;
				}
			}
		}
		return false;
	}
	
	public String getSelectedFileURL() {
		FileDescriptor file = fileList.get(filesTable.getSelectionIndex());
		DistantFilebox selectedFilebox = getSelectedFilebox();
		String path = fileboxPathes.get(selectedFilebox);
		if ( path == null ) path = "/";
		String url = Http.encode(path + file.name);
		return "http://" + selectedFilebox.getHost() + ":" + selectedFilebox.getPort()+ "/files" + url;
	}
	
	
	public DistantFilebox getSelectedFilebox() {
		int index = contactsTable.getSelectionIndex();
		if ( index < 0 || index > (registry.getFileboxesCount() - 1) ) return null;
		return registry.getFilebox(index);
	}

}

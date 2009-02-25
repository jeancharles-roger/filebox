package org.kawane.filebox.ui.internal;

import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.kawane.filebox.core.internal.FileboxApplication;
import org.osgi.service.log.LogService;

public class Application implements IApplication {
	private static LogService logger = Activator.getInstance().getLogger();
	
	private Display display;
	
	public Object start(IApplicationContext context) throws Exception {
		// this the way to retrieve command line option
		//Object args = context.getArguments().get(IApplicationContext.APPLICATION_ARGS);
		
		display = Display.getDefault();
		logger.log(LogService.LOG_INFO, "Start file box ui");
		
		// our first window
		Shell shell = new Shell(display);
		shell.setLayout(new FillLayout());
		shell.setSize(300, 300);
		shell.setText("FileBox");
		
		FileboxApplication application = new FileboxApplication("Mezos");
		FileboxMainComposite composite = new FileboxMainComposite(shell, SWT.NONE);
		composite.setApplication(application);
			
		shell.open();
		context.applicationRunning();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
		if(!display.isDisposed()) {
            display.dispose ();
		}
		logger.log(LogService.LOG_INFO, "Stop file box ui");
		return null;
	}

	public void stop() {
		if(display != null && !display.isDisposed()) {
			display.dispose();
		}
	}

}

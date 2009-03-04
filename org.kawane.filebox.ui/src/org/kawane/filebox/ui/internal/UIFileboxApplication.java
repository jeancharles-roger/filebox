package org.kawane.filebox.ui.internal;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

public interface UIFileboxApplication {

	Display getDisplay();

	Shell getActiveShell();

	void start();
	
	void stop();

}
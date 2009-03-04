package org.kawane.filebox.ui.internal;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

public interface UIFileboxApplication {

	public abstract Display getDisplay();

	public abstract Shell getActiveShell();

	public abstract void stop();

}
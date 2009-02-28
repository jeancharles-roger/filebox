package org.kawane.filebox.core.discovery;

import org.kawane.filebox.core.IFilebox;

public interface IFileboxServiceListener {
	void serviceAdded(IFilebox service); 
	void serviceRemoved(IFilebox service);
}

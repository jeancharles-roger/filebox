package org.kawane.filebox.core.discovery;

public interface IFileboxServiceListener {
	void serviceAdded(FileboxService service); 
	void serviceRemoved(FileboxService service);
}

package org.kawane.filebox.core;

import java.util.List;

public interface IFileboxRegistry {

	public void registerFilebox(String name, String host, int port);
	
	public void unregisterFilebox(String name, String host, int port);
	
	public List<IFilebox> getFileboxes();
	
}

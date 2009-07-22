package org.kawane.filebox.core;

import java.util.HashMap;
import java.util.Map;

public class Globals {

	public static final String PREFERENCES_ID = "preferences";

	public static final String FILEBOX_ID = "filebox";
	public static final String FILEBOX_REGISTRY_ID = "filebox.registry";
	public static final String FILEBOX_SHELL_ID = "filebox.shell";
	
	static final private Map<Class<?>, Map<String, Object>> globals = new HashMap<Class<?>, Map<String,Object>>();
	
	public static <T> T get(String id, Class<T> clazz) {
		if ( clazz == null || id == null ) return null;
		Map<String, Object> classSet = globals.get(clazz);
		if ( classSet == null) return null;
		return clazz.cast(classSet.get(id));
	}
	
	public static <T> void set(String id, Class<T> clazz, T object) {
		if ( clazz == null || id == null )  return;
		Map<String, Object> classSet = globals.get(clazz);
		if ( classSet == null) {
			classSet = new HashMap<String, Object>();
			globals.put(clazz, classSet);
		}
		classSet.put(id, object);
	}
	
	protected static void setPreferences(Preferences preferences) {
		set(PREFERENCES_ID, Preferences.class, preferences);
	}
	
	public static Preferences getPreferences() {
		return get(PREFERENCES_ID, Preferences.class);
	}
	
	protected static void setLocalFilebox(Filebox filebox) {
		set(FILEBOX_ID, Filebox.class, filebox);
	}
	
	public static Filebox getLocalFilebox() {
		return get(FILEBOX_ID, Filebox.class);
	}
	
	protected static void setFileboxRegistry(FileboxRegistry fileboxRegistry) {
		set(FILEBOX_REGISTRY_ID, FileboxRegistry.class, fileboxRegistry);
	}
	
	public static FileboxRegistry getFileboxRegistry() {
		return get(FILEBOX_REGISTRY_ID, FileboxRegistry.class);
	}
	
	protected static void setFileboxShell(Application fileboxShell) {
		set(FILEBOX_SHELL_ID, Application.class, fileboxShell);
	}
	
	public static Application getFileboxShell() {
		return get(FILEBOX_SHELL_ID, Application.class);
	}
	
	
}

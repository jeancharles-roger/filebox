package org.kawane.filebox.core;

import java.util.HashMap;
import java.util.Map;

import org.kawane.filebox.core.discovery.ServiceDiscovery;
import org.kawane.filebox.core.network.HttpServer;
import org.kawane.filebox.core.network.NetworkService;

public class Globals {

	public static final String PREFERENCES_ID = "preferences";

	public static final String FILEBOX_ID = "filebox";
	public static final String FILEBOX_REGISTRY_ID = "filebox.registry";
	public static final String FILEBOX_SHELL_ID = "filebox.shell";
	
	public static final String SERVICE_DISCOVERY_ID = "service.discovery";

	public static final String HTTP_SERVER_ID = "http.server";
	public static final String NETWORK_SERVICE_ID = "network.service";

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
	
	protected static void setFileboxShell(FileboxShell fileboxShell) {
		set(FILEBOX_SHELL_ID, FileboxShell.class, fileboxShell);
	}
	
	public static FileboxShell getFileboxShell() {
		return get(FILEBOX_SHELL_ID, FileboxShell.class);
	}
	
	protected static void setServiceDiscovery(ServiceDiscovery fileboxShell) {
		set(SERVICE_DISCOVERY_ID, ServiceDiscovery.class, fileboxShell);
	}
	
	public static ServiceDiscovery getServiceDiscovery() {
		return get(SERVICE_DISCOVERY_ID, ServiceDiscovery.class);
	}

	protected static void setHttpServer(HttpServer httpServer) {
		set(HTTP_SERVER_ID, HttpServer.class, httpServer);
	}
	
	public static HttpServer getHttpServer() {
		return get(HTTP_SERVER_ID, HttpServer.class);
	}

	protected static void setNetworkServices(Map<String, NetworkService> httpServer) {
		set(NETWORK_SERVICE_ID, Map.class, httpServer);
	}
	
	
	@SuppressWarnings("unchecked")
	public static Map<String, NetworkService> getNetworkServices() {
		return get(NETWORK_SERVICE_ID, Map.class);
	}
}

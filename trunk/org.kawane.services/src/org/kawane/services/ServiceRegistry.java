package org.kawane.services;
import java.util.Collection;

import org.kawane.services.internal.ServiceFactory;



public interface ServiceRegistry {
	
	ServiceRegistry instance = ServiceFactory.createService();
	/**
	 * register a service
	 * @param <T>
	 * @param serviceClass
	 * @param service
	 */
	void register(Object service);
	/**
	 * register a service
	 * @param <T>
	 * @param serviceClass
	 * @param service
	 */
	void register(Class<?> serviceClass, Object service);
	/**
	 * unregister a service
	 * @param <T>
	 * @param serviceClass
	 * @param service
	 */
	void unregister(Object service);
	/**
	 * unregister a service
	 * @param <T>
	 * @param serviceClass
	 * @param service
	 */
	void unregister(Class<?> serviceClass, Object service);
	/**
	 * Return the first service corresponding to this class.
	 * @param <T>
	 * @param serviceClass
	 * @param context
	 * @return
	 */
	<T> T getService(Class<T> serviceClass);
	/**
	 * Return a list of service
	 * @param <T>
	 * @param serviceClass
	 * @param context
	 * @return
	 */
	<T> Collection<T> getServices(Class<T> serviceClass);
	/**
	 * Add a service listener
	 * @param <T>
	 * @param serviceClass
	 * @param listener
	 */
	<T>void addListener(Class<T> serviceClass, IServiceListener<T> listener);
	/**
	 * Add a service listener
	 * @param <T>
	 * @param serviceClass
	 * @param listener
	 */
	<T>void addListener(Class<T> serviceClass, IServiceListener<T> listener, boolean sendExistingServices);
	/**
	 * Remove a service listener
	 * @param <T>
	 * @param serviceClass
	 * @param listener
	 */
	<T>void removeListener(Class<T> serviceClass, IServiceListener<T> listener);
}

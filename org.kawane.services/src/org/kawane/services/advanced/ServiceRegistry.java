package org.kawane.services.advanced;

import java.util.Collection;

import org.kawane.services.IServiceListener;
import org.kawane.services.IServiceRegistry;

/**
 * Service Registry can be used to use a simple way to exchange services in a Java Virtual Machine.
 *
 * These packages include an OSGI implementation that include OSGI service.
 * This Service registry can also be used in a non OSGI environnment.
 *
 * You can define the java system property "org.kawane.services.core" to true, if you want to force to use the
 * core java service registry instead of using OSGI service registry even if you are using an OSGI framework.
 *
 * @author <a href="maito:legoff.laurent@gmail.com">Laurent Le Goff </a>
 */
final public class ServiceRegistry {
	final static private IServiceRegistry registry = IServiceRegistry.instance;


	/**
	 * register a service
	 * @param <T>
	 * @param serviceClass
	 * @param service
	 */
	final static public void manage(Object service) {
		registry.manage(service);
	}
	/**
	 * register a service
	 * @param <T>
	 * @param serviceClass
	 * @param service
	 */
	final static public void unmanage(Object service) {
		registry.unmanage(service);
	}
	/**
	 * register a service
	 * @param <T>
	 * @param serviceClass
	 * @param service
	 */
	final static public void register(Object service) {
		registry.register(service);
	}

	/**
	 * register a service
	 * @param <T>
	 * @param serviceClass
	 * @param service
	 */
	final static public void register(Object service, Class<?> serviceClass) {
		registry.register(service, serviceClass);
	}

	/**
	 * unregister a service
	 * @param <T>
	 * @param serviceClass
	 * @param service
	 */
	final static public void unregister(Object service) {
		registry.unregister(service);
	}

	/**
	 * unregister a service
	 * @param <T>
	 * @param serviceClass
	 * @param service
	 */
	final static public void unregister(Object service, Class<?> serviceClass) {
		registry.unregister(service, serviceClass);
	}

	/**
	 * Return the first service corresponding to this class.
	 * @param <T>
	 * @param serviceClass
	 * @return
	 */
	final static public <T> T getService(Class<T> serviceClass) {
		return registry.getService(serviceClass);
	}

	/**
	 * Return the first service corresponding to this class.
	 * Use a context object to determine ClassLoader in particular execution environment like OSGI.
	 * @param <T>
	 * @param serviceClass
	 * @param context
	 * @return
	 */
	final static public <T> T getService(Class<T> serviceClass, Object context) {
		return registry.getService(serviceClass, context);
	}

	/**
	 * Return the number of services that implement or extend this class
	 *
	 * @param serviceClass
	 * @param context
	 * @return
	 */
	final static public int getServicesCount(Class<?> serviceClass) {
		return registry.getServicesCount(serviceClass);
	}

	/**
	 * Return a list of service
	 * @param <T>
	 * @param serviceClass
	 * @return
	 */
	final static public <T> Collection<T> getServices(Class<T> serviceClass) {
		return registry.getServices(serviceClass);
	}

	/**
	 * Return a list of service
	 * Use a context object to determine ClassLoader in particular execution environment like OSGI.
	 * @param <T>
	 * @param serviceClass
	 * @param context
	 * @return
	 */
	final static public <T> Collection<T> getServices(Class<T> serviceClass, Object context) {
		return registry.getServices(serviceClass, context);
	}

	/**
	 * Add a service listener
	 * @param <T>
	 * @param serviceClass
	 * @param listener
	 */
	final static public <T> void addServiceListener(Class<T> serviceClass, IServiceListener<T> listener) {
		registry.addServiceListener(serviceClass, listener);
	}

	/**
	 * Add a service listener
	 * @param <T>
	 * @param serviceClass
	 * @param listener
	 */
	final static public <T> void addServiceListener(Class<T> serviceClass, IServiceListener<T> listener, boolean sendExistingServices) {
		registry.addServiceListener(serviceClass, listener, sendExistingServices);
	}

	/**
	 * Remove a service listener
	 * @param <T>
	 * @param serviceClass
	 * @param listener
	 */
	final static public <T> void removeServiceListener(Class<T> serviceClass, IServiceListener<T> listener) {
		registry.removeServiceListener(serviceClass, listener);
	}
}

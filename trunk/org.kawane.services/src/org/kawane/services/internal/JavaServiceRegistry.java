package org.kawane.services.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.kawane.services.IServiceListener;
import org.kawane.services.Service;
import org.kawane.services.ServiceRegistry;

class JavaServiceRegistry implements ServiceRegistry {
	private Map<Class<?>, Collection<IServiceListener<?>>> listeners = new HashMap<Class<?>, Collection<IServiceListener<?>>>();

	private Map<Class<?>, Collection<Object>> services = new HashMap<Class<?>, Collection<Object>>();

	public JavaServiceRegistry() {
	}

	@SuppressWarnings("unchecked")
	public <T> T getService(Class<T> serviceClass) {
		Collection<Object> list;
		synchronized (services) {
			list = services.get(serviceClass);
		}
		if (list != null) {
			synchronized (list) {
				return (T) list.iterator().next();
			}
		}
		return null;
	}
	
	public  int getServicesCount(Class<?> serviceClass) {
		return getServices(serviceClass).size();
	}

	@SuppressWarnings("unchecked")
	public <T> Collection<T> getServices(Class<T> serviceClass) {
		Collection<Object> list;
		synchronized (services) {
			list = services.get(serviceClass);
		}
		if (list != null) {
			synchronized (list) {
				return new ArrayList<T>((Collection<T>) list);
			}
		}
		return new ArrayList<T>();
	}

	public void register(Object service) {
		Service serviceAnnotation = service.getClass().getAnnotation(Service.class);
		for (Class<?> serviceClass : serviceAnnotation.classes()) {
			register(serviceClass, serviceClass);
		}
	}

	public void register(Class<?> serviceClass, Object service) {
		if (!serviceClass.isInstance(service)) {
			throw new ClassCastException("Service does not implement or extend service class: " + service);
		}
		Collection<Object> list;
		synchronized (services) {
			list = services.get(serviceClass);
			if (list == null) {
				list = new ArrayList<Object>();
				services.put(serviceClass, list);
			}
		}
		synchronized (list) {
			list.add(service);
		}
		fireNotifyAddService(serviceClass, service);
	}

	public void unregister(Object service) {
		if (service == null)
			return;
		Service serviceAnnotation = service.getClass().getAnnotation(Service.class);
		for (Class<?> serviceClass : serviceAnnotation.classes()) {
			unregister(serviceClass, service);
		}
	}

	public void unregister(Class<?> serviceClass, Object service) {
		Collection<Object> list;
		synchronized (services) {
			list = services.get(serviceClass);
		}
		if (list != null) {
			synchronized (list) {
				list.remove(service);
				Collection<IServiceListener<?>> serviceListeners;
				synchronized (listeners) {
					serviceListeners = new HashSet<IServiceListener<?>>(listeners.get(serviceClass));
				}
				if (serviceListeners != null) {
					fireNotifyRemoveService(serviceClass, service);
				}
			}
		}
	}

	private void fireNotifyAddService(final Class<?> serviceClass, final Object service) {
		Thread thread = new Thread("Notify new Service") {
			@SuppressWarnings("unchecked")
			@Override
			public void run() {
				Collection<IServiceListener<?>> list;
				synchronized (listeners) {
					list = listeners.get(serviceClass);
				}
				if (list != null) {
					synchronized (list) {
						list = new HashSet<IServiceListener<?>>(list);
					}
					for (IServiceListener serviceListener : list) {
						serviceListener.serviceAdded(serviceClass, service);
					}
				}
			}
		};
		thread.start();
	}

	private void fireNotifyRemoveService(final Class<?> serviceClass, final Object service) {
		Thread thread = new Thread("Notify remove Service") {
			@SuppressWarnings("unchecked")
			@Override
			public void run() {
				Collection<IServiceListener<?>> list;
				synchronized (listeners) {
					list = listeners.get(serviceClass);
				}
				if (list != null) {
					synchronized (list) {
						list = new HashSet<IServiceListener<?>>(list);
					}
					for (IServiceListener serviceListener : list) {
						serviceListener.serviceRemoved(serviceClass, service);
					}
				}
			}
		};
		thread.start();
	}

	public <T> void addListener(Class<T> serviceClass, IServiceListener<T> listener) {
		addListener(serviceClass, listener, false);
	}

	public <T> void addListener(Class<T> serviceClass, IServiceListener<T> listener, boolean sendExistingServices) {
		Collection<IServiceListener<?>> list;
		synchronized (listeners) {
			list = listeners.get(serviceClass);
			if (list == null) {
				list = new ArrayList<IServiceListener<?>>();
				listeners.put(serviceClass, list);
			}
		}
		if (sendExistingServices) {
			for (T service : getServices(serviceClass)) {
				listener.serviceAdded(serviceClass, service);
			}
		}
		synchronized (list) {
			list.add(listener);
		}
	}

	public <T> void removeListener(Class<T> serviceClass, IServiceListener<T> listener) {
		Collection<IServiceListener<?>> list;
		synchronized (listeners) {
			list = listeners.get(serviceClass);
		}
		if (list != null) {
			synchronized (list) {
				list.remove(listener);
				if (list.isEmpty()) {
					synchronized (listeners) {
						listeners.remove(serviceClass);
					}
				}
			}
		}
	}

}

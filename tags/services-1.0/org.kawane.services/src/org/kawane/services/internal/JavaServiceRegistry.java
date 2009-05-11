package org.kawane.services.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.kawane.services.IServiceListener;
import org.kawane.services.IServiceRegistry;
import org.kawane.services.Service;
import org.kawane.services.advanced.ManagedService;

class JavaServiceRegistry implements IServiceRegistry {
	private Map<Class<?>, Collection<IServiceListener<?>>> listeners = new HashMap<Class<?>, Collection<IServiceListener<?>>>();

	private Map<Class<?>, Set<Object>> services = new HashMap<Class<?>, Set<Object>>();

	public JavaServiceRegistry() {
	}

	public void manage(Object service) {
		Collection<Service> serviceClasses = Util.getServicesClasses(service.getClass(), new HashSet<Service>(1));
		for (Service serviceClass : serviceClasses) {
			ManagedService.manage(serviceClass, service);
		}
	}

	public void unmanage(Object service) {
		ManagedService.unmanage(service);
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
	public <T> T getService(Class<T> serviceClass, Object context) {
		return getService(serviceClass);
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

	public <T> Collection<T> getServices(Class<T> serviceClass, Object context) {
		return getServices(serviceClass);
	}

	public void register(Object service) {
		for (Service serviceClass : Util.getServicesClasses(service.getClass(), new HashSet<Service>(1))) {
			register(service, serviceClass.value());
		}
	}

	public void register(Object service, Class<?> serviceClass) {
		if (!serviceClass.isInstance(service)) {
			throw new ClassCastException("Service does not implement or extend service class: " + service);
		}
		Set<Object> list;
		synchronized (services) {
			list = services.get(serviceClass);
			if (list == null) {
				list = new HashSet<Object>();
				services.put(serviceClass, list);
			}
		}
		boolean notify;
		synchronized (list) {
			notify = list.add(service);
		}
		if(notify) {
			fireNotifyAddService(serviceClass, service);
		}
	}

	public void unregister(Object service) {
		if (service == null)
			return;
		for (Service serviceClass : Util.getServicesClasses(service.getClass(), new HashSet<Service>(1))) {
			unregister(service, serviceClass.value());
		}
	}

	public void unregister(Object service, Class<?> serviceClass) {
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

	public <T> void addServiceListener(Class<T> serviceClass, IServiceListener<T> listener) {
		addServiceListener(serviceClass, listener, false);
	}

	public <T> void addServiceListener(Class<T> serviceClass, IServiceListener<T> listener, boolean sendExistingServices) {
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

	public <T> void removeServiceListener(Class<T> serviceClass, IServiceListener<T> listener) {
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

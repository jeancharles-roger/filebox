package org.kawane.services.advanced;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.kawane.services.IServiceListener;
import org.kawane.services.ServiceRegistry;


@SuppressWarnings("unchecked")
public class ServiceInjector  implements IServiceListener{
	private static Logger logger = Logger.getLogger(ServiceInjector.class.getName());
	
	Map<Method, Class<?>> methodToClass = new HashMap<Method, Class<?>>();
	WeakHashMap<Object, Object> alreadyBound = new WeakHashMap<Object,Object>();
	private Map<Method, Integer> methodsCount = new HashMap<Method, Integer>();
	
	private ServiceRegistry serviceRegistry;
	private Reference<?> ref;
	
	boolean async;

	public ServiceInjector(Object object) {
		this(object, false);
	}
	public ServiceInjector(Object object, boolean async) {
		serviceRegistry = ServiceRegistry.instance;
		this.async = async;
		this.ref = new WeakReference<Object>(object);
		methodToClass = analyse(object);
		inject(object);
		if(methodToClass != null) {
			for (Entry<Method, Class<?>> entry : methodToClass.entrySet()) {
				Class<?> serviceClass = entry.getValue();
				serviceRegistry.addListener(serviceClass, this);
			}
		}
	}
	
	public ServiceInjector(Object object, Map<Method, Class<?>> methodToClass, boolean async) {
		serviceRegistry = ServiceRegistry.instance;
		this.async = async;
		this.ref = new WeakReference<Object>(object);
		this.methodToClass = methodToClass;
		inject(object);
		// register listener
		if(methodToClass != null) {
			for (Entry<Method, Class<?>> entry : methodToClass.entrySet()) {
				Class<?> serviceClass = entry.getValue();
				serviceRegistry.addListener(serviceClass, this);
			}
		}
	}

	synchronized private void inject(Object object) {
		if(methodToClass == null) return;
		// effectivly inject services	
		Set<Entry<Method, Class<?>>> entries = methodToClass.entrySet();
		for (Iterator<Entry<Method, Class<?>>> i = entries.iterator(); i.hasNext();) {
			Entry<Method, Class<?>> entry = i.next();
			Method method = entry.getKey();
			Class<?> serviceClass = entry.getValue();
			Inject inject = method.getAnnotation(Inject.class);
			if (inject == null || inject.max() > 1 || inject.max() == -1) {
				Integer methodCount = getMethodCound(method);
				Collection<?> services = serviceRegistry.getServices(serviceClass);
				for (Object service : services) {
					if(!alreadyBound.containsKey(service)) {
						if(inject == null || inject.max() != -1 && methodCount >= inject.max()) {
							methodsCount.remove(method);
							methodCount = -1;
							i.remove();
							break;
						}
						methodCount++;
						executeMethodInThread(object, method, service, async);
					}
				}
				if(inject != null  && inject.max() != -1 && methodCount != -1) {
					methodsCount.put(method, methodCount);
				}
			} else {
				Object service = serviceRegistry.getService(serviceClass);
				try {
					if (service != null) {
						i.remove();
						executeMethodInThread(object, method, service, async);
					}
				} catch (Throwable e) {
					logger.log(Level.SEVERE, "An Error Occured", e);
				}
			}
		}
		if(methodToClass.size() == 0) {
			clear();
		}
	}

	private void clear() {
		// remove all listener
		for(Entry<Method, Class<?>> entry: methodToClass.entrySet()) {
			serviceRegistry.removeListener(entry.getValue(), this);
		}
		methodToClass = null;
		alreadyBound = null;
		methodsCount = null;
		serviceRegistry = null;
		ref = null;
	}

	private Integer getMethodCound(Method method) {
		Integer count = methodsCount.get(method);
		if(count == null) {
			return 0;
		}
		return count;
	}

	private void executeMethodInThread(final Object object, final Method method, final Object service, boolean async) {
		Runnable runnable = new Runnable() {
			public void run() {
				try {
					method.invoke(object, service);
				} catch (Throwable e) {
					logger.log(Level.SEVERE, "An Error Occured", e);
				}
			}
			
		};
		if(async) {
			Thread thread = new Thread(runnable, "Inject service");
			thread.start();
		} else {
			runnable.run();
		}
	}

	private Map<Method, Class<?>> analyse(Object object) {
		Map<Method, Class<?>> map = new HashMap<Method, Class<?>>();
		Class<? extends Object> class1 = object.getClass();
		Method[] methods = class1.getMethods();
		for (Method method : methods) {
			Inject inject = method.getAnnotation(Inject.class);
			if (inject != null) {
				if (method.getParameterTypes().length == 1) {
					map.put(method, method.getParameterTypes()[0]);
				} else {
					throw new IllegalArgumentException("An inject method must have only one argument");
				}
			} 
		}
		return map;
	}

	public void serviceAdded(Class serviceClass, Object service) {
		Object object = ref.get();
		if(object != null) {
			inject(object);
		} else {
			clear();
		}
	}

	public void serviceRemoved(Class serviceClass, Object service) {
		
	}

}

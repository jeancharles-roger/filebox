package org.kawane.services.advanced;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.kawane.services.IServiceListener;
import org.kawane.services.ServiceRegistry;
import org.kawane.services.internal.Util;

@SuppressWarnings("unchecked")
public class ServiceManager implements IServiceListener {

	private static Logger logger = Logger.getLogger(ServiceManager.class.getName());

	Map<Method, Class<?>> methodToClass = new HashMap<Method, Class<?>>();

	private ServiceRegistry serviceRegistry;

	private Class<?> managedClass;

	private Collection<Class<?>> servicesClassToRegister;

	private Object instance = null;

	ServiceManager(Object instance) {
		this.instance = instance;
		managedClass = instance.getClass();
		this.servicesClassToRegister = Util.getServicesClasses(instance.getClass());
		init(analyse(managedClass));
	}

	ServiceManager(Object instance, Class<?>... serviceClassToRegister) {
		this.instance = instance;
		managedClass = instance.getClass();
		this.servicesClassToRegister = Arrays.asList(serviceClassToRegister);
		init(analyse(managedClass));
	}

	ServiceManager(Object instance, Class<?> managedClass, Map<Method, Class<?>> methodToClass, Class<?>... serviceClassToRegister) {
		this.managedClass = managedClass;
		this.servicesClassToRegister = Arrays.asList(serviceClassToRegister);
		this.instance = instance;
		init(methodToClass);
	}

	ServiceManager(Class<?> managedClass) {
		this.managedClass = managedClass;
		this.servicesClassToRegister = Util.getServicesClasses(managedClass);
		init(analyse(managedClass));
	}

	ServiceManager(Class<?> managedClass, Class<?>... serviceClassToRegister) {
		this.managedClass = managedClass;
		this.servicesClassToRegister = Arrays.asList(serviceClassToRegister);
		init(analyse(managedClass));
	}

	private void init(Map<Method, Class<?>> methodToClass) {
		this.methodToClass = methodToClass;
		serviceRegistry = ServiceRegistry.instance;
		if (verifyCondition()) {
			instanciateObject();
		} else {
			for (Entry<Method, Class<?>> entry : methodToClass.entrySet()) {
				Class<?> serviceClass = entry.getValue();
				serviceRegistry.addListener(serviceClass, this, true);
			}
		}
	}

	public void serviceAdded(Class serviceClass, Object service) {
		if (verifyCondition()) {
			instanciateObject();
		}
	}

	private boolean verifyCondition() {
		for (Entry<Method, Class<?>> entry : methodToClass.entrySet()) {
			Method method = entry.getKey();
			Inject inject = method.getAnnotation(Inject.class);
			int servicesCount = serviceRegistry.getServicesCount(entry.getValue());
			if (inject != null && servicesCount < inject.min()) {
				return false;
			}
		}
		return true;
	}

	private void instanciateObject() {
		try {
			if (instance == null) {
				instance = managedClass.newInstance();
			}
			inject(instance, methodToClass, true);
			if (servicesClassToRegister != null) {
				for (Class<?> serviceClass : servicesClassToRegister) {
					serviceRegistry.register(serviceClass, instance);
				}
			}
		} catch (Throwable e) {
			logger.log(Level.SEVERE, "An Error Occured", e);
		}
		// remove all listener
		for (Entry<Method, Class<?>> entry : methodToClass.entrySet()) {
			serviceRegistry.removeListener(entry.getValue(), this);
		}
		// clear instance variable
		serviceRegistry = null;
		methodToClass = null;
		managedClass = null;
		servicesClassToRegister = null;
		instance = null;
	}

	public void serviceRemoved(Class serviceClass, Object service) {

	}

	private Map<Method, Class<?>> analyse(Class<?> managedClass) {
		Method[] methods = managedClass.getMethods();
		for (Method method : methods) {
			Inject inject = method.getAnnotation(Inject.class);
			if (inject != null) {
				if (method.getParameterTypes().length == 1) {
					methodToClass.put(method, method.getParameterTypes()[0]);
				} else {
					throw new IllegalArgumentException("An inject method must have only one argument");
				}
			}
		}
		return methodToClass;
	}

	static public void register(Object object) {
		ServiceRegistry.instance.register(object);
	}

	static public void register(Class<?> serviceClass, Object service) {
		ServiceRegistry.instance.register(serviceClass, service);
	}

	static public void inject(Object object) {
		new ServiceInjector(object);
	}

	static public void inject(Object object, boolean async) {
		new ServiceInjector(object, async);
	}

	static public void inject(Object object, Map<Method, Class<?>> methodToClass, boolean async) {
		new ServiceInjector(object, methodToClass, async);
	}

	/**
	 * register + inject
	 * @param instance
	 */
	static public void manage(Object instance) {
		new ServiceManager(instance);
	}

	/**
	 * instanciate + register + inject
	 * @param instance
	 */
	static public void manage(Class<?> managedClass) {
		new ServiceManager(managedClass);
	}

	/**
	 * register + inject
	 * @param instance
	 */
	static public void manage(Object instance, Class<?>... serviceClassToRegister) {
		new ServiceManager(instance, serviceClassToRegister);
	}

	/**
	 * instanciate + register + inject
	 * @param instance
	 */
	static public void manage(Class<?> managedClass, Class<?>... serviceClassToRegister) {
		new ServiceManager(managedClass, serviceClassToRegister);
	}

	/**
	 * instanciate + register + inject
	 * instance may be null
	 * @param instance
	 */
	static public void manage(Object instance, Class<?> managedClass, Map<Method, Class<?>> methodToClass, Class<?>... serviceClassToRegister) {
		new ServiceManager(instance, managedClass, methodToClass, serviceClassToRegister);
	}

}

package org.kawane.services.advanced;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.kawane.services.IServiceListener;
import org.kawane.services.Service;
import org.kawane.services.ServiceRegistry;


@SuppressWarnings("unchecked")
public class ServiceManager implements IServiceListener {

	private static Logger logger = Logger.getLogger(ServiceManager.class.getName());

	Map<Method, Class<?>> methodToClass = new HashMap<Method, Class<?>>();

	private ServiceRegistry serviceRegistry;

	private Class<?> managedClass;

	private Class<?> [] servicesClassToRegister;

	private Object instance=null;

	public ServiceManager(Object instance, Class<?> managedClass) {
		this.managedClass = managedClass;
		this.instance = instance;
		Service annotation = managedClass.getAnnotation(Service.class);
		if(annotation != null) {
			this.servicesClassToRegister = annotation.classes();
		}
		init(analyse(managedClass));
	}

	public ServiceManager(Object instance, Class<?> managedClass, Class<?> ... serviceClassToRegister) {
		this.managedClass = managedClass;
		this.instance = instance;
		this.servicesClassToRegister = serviceClassToRegister;
		init(analyse(managedClass));
	}

	public ServiceManager(Object instance, Class<?> managedClass, Map<Method, Class<?>> methodToClass, Class<?> ... serviceClassToRegister) {
		this.managedClass = managedClass;
		this.servicesClassToRegister = serviceClassToRegister;
		this.instance = instance;
		init(methodToClass);
	}
	public ServiceManager(Class<?> managedClass) {
		this.managedClass = managedClass;
		Service annotation = managedClass.getAnnotation(Service.class);
		if(annotation != null) {
			this.servicesClassToRegister = annotation.classes();
		}
		init(analyse(managedClass));
	}

	public ServiceManager(Class<?> managedClass, Class<?> ... serviceClassToRegister) {
		this.managedClass = managedClass;
		this.servicesClassToRegister = serviceClassToRegister;
		init(analyse(managedClass));
	}

	public ServiceManager(Class<?> managedClass, Map<Method, Class<?>> methodToClass, Class<?> ... serviceClassToRegister) {
		this.managedClass = managedClass;
		this.servicesClassToRegister = serviceClassToRegister;
		init(methodToClass);
	}

	private void init(Map<Method, Class<?>> methodToClass) {
		this.methodToClass = methodToClass;
		serviceRegistry = ServiceRegistry.instance;
		if(verifyCondition()) {
			instanciateObject();
		} else {
			for (Entry<Method, Class<?>> entry : methodToClass.entrySet()) {
				Class<?> serviceClass = entry.getValue();
				serviceRegistry.addListener(serviceClass, this, true);
			}
		}
	}

	public void serviceAdded(Class serviceClass, Object service) {
		if(verifyCondition()) {
			instanciateObject();
		}
	}

	private boolean verifyCondition() {
		for(Entry<Method, Class<?>> entry: methodToClass.entrySet()) {
			Method method = entry.getKey();
			Inject inject = method.getAnnotation(Inject.class);
			int servicesCount = serviceRegistry.getServicesCount(entry.getValue());
			if(inject != null && servicesCount < inject.min()) {
				return false;
			}
		}
		return true;
	}

	private void instanciateObject() {
		try {
			if(instance == null) {
				instance = managedClass.newInstance();
			}
			new ServiceInjector(instance, methodToClass, true);
			if(servicesClassToRegister != null) {
				for (Class<?> serviceClass : servicesClassToRegister) {
					serviceRegistry.register(serviceClass, instance);
				}
			}
		} catch (Throwable e) {
			logger.log(Level.SEVERE, "An Error Occured", e);
		}
		// remove all listener
		for(Entry<Method, Class<?>> entry: methodToClass.entrySet()) {
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

}

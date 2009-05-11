package org.kawane.services.advanced;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.kawane.services.IServiceListener;
import org.kawane.services.IServiceRegistry;
import org.kawane.services.Service;
import org.kawane.services.internal.Util;

@SuppressWarnings("unchecked")
final public class ManagedService implements IServiceListener{

	static private Map<Object, Collection<ManagedService>> managedServices = Collections.synchronizedMap(new HashMap<Object, Collection<ManagedService>>());

	private static Logger logger = Logger.getLogger(ManagedService.class.getName());

	final private Service managedService;
	final private Object service;
	private Collection<Class<?>> dependsServices = new HashSet<Class<?>>(0);

	private ManagedService(Service managedService, Object service) {
		this.managedService = managedService;
		this.service = service;
	}

	static public void manage(Service managedService, Object service) {
		ManagedService managed = new ManagedService(managedService, service);
		Collection<ManagedService> services = managedServices.get(service);
		if(services == null) {
			services = new ArrayList<ManagedService>();
			managedServices.put(service, services);
		}
		services.add(managed);
		Collection<Class<?>> dependencyClasses = new HashSet<Class<?>>();
		if(managedService != null && managedService.depends().length > 0) {
			for (Class<?> cl: managedService.depends()) {
				dependencyClasses.add(cl);
			}
		} else {
			if(managedService != null) {
				ServiceRegistry.register(service, managedService.value());
			}
		}
		for (Method m : Util.analyseAnnotations(service.getClass(), Inject.class)) {
			dependencyClasses.add(m.getParameterTypes()[0]);
		}
		for (Method m : Util.analyseAnnotations(service.getClass(), Reject.class)) {
			dependencyClasses.add(m.getParameterTypes()[0]);
		}
		for (Class<?> cl : dependencyClasses) {
			ServiceRegistry.addServiceListener(cl, managed, true);
		}
		if(dependencyClasses.size() == 0) {
			services.remove(managed);
			if(services.size() == 0) {
				managedServices.remove(service);
			}
		}
	}

	static public void unmanage(Object service) {
		Collection<ManagedService> services = managedServices.remove(service);
		if(services != null && services.size() != 0) {
			Collection<Class<?>> dependencyClasses = new HashSet<Class<?>>();
			for (ManagedService s : services) {
				if(s.managedService != null) {
					for(Class<?> cl: s.managedService.depends()) {
						dependencyClasses.add(cl);
					}
				}
				for (Method m : Util.analyseAnnotations(service.getClass(), Inject.class)) {
					dependencyClasses.add(m.getParameterTypes()[0]);
				}
				for (Method m : Util.analyseAnnotations(service.getClass(), Reject.class)) {
					dependencyClasses.add(m.getParameterTypes()[0]);
				}
				for (Class<?> cl : dependencyClasses) {
					ServiceRegistry.removeServiceListener(cl, s);
				}
			}
		}
		ServiceRegistry.unregister(service);
	}

	public void serviceAdded(Class serviceClass, Object s) {
		if(injectService(serviceClass, s) && managedService != null) {
			dependsServices.add(serviceClass);
		}
		if(managedService != null && resolved()) {
			// register the service
			ServiceRegistry.register(service, managedService.value());
		}
	}

	public void serviceRemoved(Class serviceClass, Object s) {
		if( /* not */  ! rejectService(serviceClass, s) && managedService != null) {
			dependsServices.remove(serviceClass);
		}
		if(managedService != null && !resolved()) {
			// unregister the service
			ServiceRegistry.unregister(service, managedService.value());
		}
	}

	private boolean injectService(Class serviceClass, Object s) {
		Collection<Method> rejectMethods = Util.analyseAnnotations(service.getClass(), Inject.class);
		for (Method method : rejectMethods) {
			if(method.getParameterTypes()[0] == serviceClass) {
				try {
					return (Integer)method.invoke(service, s) == IServiceRegistry.DEPENDENCY_RESOLVED;
				} catch(Throwable e) {
					logger.log(Level.SEVERE, "An Error Occured", e);
				}
			}
		}
		return true;
	}

	private boolean rejectService(Class serviceClass, Object s) {
		// if no reject is defined: return false when there is no service left of given class.
		Collection<Method> rejectMethods = Util.analyseAnnotations(service.getClass(), Reject.class);
		for (Method method : rejectMethods) {
			if(method.getParameterTypes()[0] == serviceClass) {
				try {
					return (Integer)method.invoke(service, s) == IServiceRegistry.DEPENDENCY_RESOLVED;
				} catch(Throwable e) {
					logger.log(Level.SEVERE, "An Error Occured", e);
				}
			}
		}
		return ServiceRegistry.getServicesCount(serviceClass) >  0;
	}

	private boolean resolved() {
		for(Class<?> depends: managedService.depends()) {
			if(!dependsServices.contains(depends)) {
				return false;
			}
		}
		return true;
	}


}

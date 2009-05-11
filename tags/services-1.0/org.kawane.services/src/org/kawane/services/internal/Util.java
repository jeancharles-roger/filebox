package org.kawane.services.internal;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;

import org.kawane.services.Service;
import org.kawane.services.Services;

public class Util {
	static public Collection<Service> getServicesClasses(Class<?> cl, Collection<Service> serviceClasses) {
		// TODO make a cache
		Service serviceAnnotation = cl.getAnnotation(Service.class);
		if (serviceAnnotation != null) {
			serviceClasses.add(serviceAnnotation);
		}
		if (cl.getSuperclass() != null) {
			getServicesClasses(cl.getSuperclass(), serviceClasses);
		}
		Services servicesAnnotation = cl.getAnnotation(Services.class);
		if (servicesAnnotation != null) {
			Service[] value = servicesAnnotation.value();
			if (value != null) {
				for (Service service : value) {
					serviceClasses.add(service);
				}
			}
		}
		return serviceClasses;
	}

	static public Collection<Method> analyseAnnotations(Class<?> clazz, Class<? extends Annotation> annotationType) {
		// TODO make a cache
		Collection<Method> methods = new ArrayList<Method>(0);
		Method[] allMethods = clazz.getMethods();
		for (Method method : allMethods) {
			Annotation annotation= method.getAnnotation(annotationType);
			if (annotation != null) {
				if (method.getParameterTypes().length == 1 && method.getReturnType() == Integer.TYPE) {
					methods.add(method);
				} else {
					throw new IllegalArgumentException(method + ": An inject method must have only one argument and must return an integer");
				}
			}
		}
		return methods;
	}
}

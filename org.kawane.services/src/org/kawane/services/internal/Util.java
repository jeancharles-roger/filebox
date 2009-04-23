package org.kawane.services.internal;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

import org.kawane.services.Service;

public class Util {
	static public Collection<Class<?>> getServicesClasses(Class<?> cl) {
		Service serviceAnnotation = cl.getAnnotation(Service.class);
		if (serviceAnnotation != null) {
			Collection<Class<?>> serviceClasses = new HashSet<Class<?>>(1);
			Class<?>[] classes = serviceAnnotation.classes();
			if (classes != null) {
				for (Class<?> class1 : classes) {
					serviceClasses.add(class1);
				}
				return serviceClasses;
			}
			if (cl.getSuperclass() != null) {
				serviceClasses.addAll(getServicesClasses(cl.getSuperclass()));
			}
		} else {
			if (cl.getSuperclass() != null) {
				return getServicesClasses(cl.getSuperclass());
			}
		}
		return Collections.emptyList();
	}
}

package org.kawane.services;

public interface IServiceListener<T> {
	void serviceAdded(Class<T> serviceClass, T service);
	void serviceRemoved(Class<T> serviceClass, T service);
}

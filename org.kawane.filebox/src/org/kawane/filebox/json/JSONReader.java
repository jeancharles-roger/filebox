package org.kawane.filebox.json;

import java.io.IOException;

public interface JSONReader {

	int next() throws IOException;

	String getName();

	int getValueType();

	String getValue();

	boolean getBoolean();

	int getInteger();

	long getLong();

	float getFloat();

	double getDouble();


	/**
	 * Possible values are JSON_START_DOCUMENT, JSON_START_OBJECT, JSON_START_ARRAY
	 * @return
	 */
	int getContext();

	void close() throws IOException;

}
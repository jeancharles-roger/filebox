package org.kawane.filebox.json;

public interface JSONHandler {

	void beginDocument();

	void endDocument();

	void beginObject();

	void endObject();

	void beginArray();

	void endArray();

	void member(String name);

	void stringValue(String value);

	void numberValue(String value);

	void booleanValue(boolean bool);

	void nullValue();

	void value(String value);

}

package org.kawane.filebox.json;


public interface JSONWriter {
	void beginDocument();
	void endDocument();
	void beginObject();
	void endObject();
	void beginArray();
	void endArray();
	void writeMember(String s);
	void writeString(String s);
	void writeBoolean(boolean bool);
	void writeDouble(double d);
	void writeFloat(float f);
	void writeInteger(int i);
	void writeLong(long i);
	void writeNull();
	void writeValue(String value);
}
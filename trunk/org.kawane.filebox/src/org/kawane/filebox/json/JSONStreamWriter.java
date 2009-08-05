package org.kawane.filebox.json;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.Stack;

public class JSONStreamWriter implements JSONHandler {

	private PrintWriter writer;

	private String tabPattern = "\t";
	private int indent = 0;
	private char lastChar = 0;
	private Stack<Integer> context = new Stack<Integer>();

	private boolean firstMember;

	private boolean firstArrayValue;

	static final private int OBJECT = 0;
	static final private int ARRAY = 1;

	public JSONStreamWriter(Writer writer) {
		this.writer = new PrintWriter(new BufferedWriter(writer));
	}

	public JSONStreamWriter(OutputStream stream) {
		this.writer = new PrintWriter(new BufferedWriter(new OutputStreamWriter(stream)));
	}

	private void printIndent() {
		for (int i = 0; i < indent; i++) {
			writer.print(tabPattern);
		}
	}

	private void print(String s) {
		if (lastChar == '\n') {
			printIndent();
		}
		writer.print(s);
		if (s.length() > 0) {
			lastChar = s.charAt(s.length() - 1);
		}
	}

	private void println() {
		writer.println();
		lastChar = '\n';
	}

	private void println(String s) {
		if (lastChar == '\n') {
			printIndent();
		}
		writer.println(s);
		lastChar = '\n';
	}

	public void beginDocument() {
		context.push(OBJECT);
		print("{");
		indent++;
		firstMember = true;
	}

	public void endDocument() {
		endObject();
	}

	public void beginObject() {
		writeComma();
		context.push(OBJECT);
		print("{");
		indent++;
		firstMember = true;
	}

	public void endObject() {
		context.pop();
		indent--;
		println();
		println("}");
		firstMember = false;
		firstArrayValue = false;
	}

	public void beginArray() {
		writeComma();
		context.push(ARRAY);
		print("[");
		firstArrayValue = true;
	}

	public void endArray() {
		context.pop();
		print("]");
		firstMember = false;
		firstArrayValue = false;
	}

	public void member(String s) {
		if (firstMember) {
			firstMember = false;
		} else {
			print(",");
		}
		println();
		print("\"");
		print(s);
		print("\"");
		print(": ");

	}

	public void booleanValue(boolean bool) {
		value(String.valueOf(bool));
	}

	public void doubleValue(double d) {
		value(String.valueOf(d));
	}

	public void floatValue(float f) {
		value(String.valueOf(f));
	}


	public void integerValue(int i) {
		value(String.valueOf(i));
	}


	public void longValue(long i) {
		value(String.valueOf(i));
	}


	public void stringValue(String s) {
		print("\"");
		value(s);
		print("\"");
	}

	public void nullValue() {
		value("null");
	}

	public void value(String value) {
		writeComma();
		print(value);
	}

	private void writeComma() {
		if (context.peek() == ARRAY) {
			if (firstArrayValue) {
				firstArrayValue = false;
			} else {
				print(", ");
			}
		}
	}

	public void numberValue(String value) {
		value(value);
	}

	public void flush() {
		writer.flush();
	}

	public void close() throws IOException {
		writer.close();
	}

}

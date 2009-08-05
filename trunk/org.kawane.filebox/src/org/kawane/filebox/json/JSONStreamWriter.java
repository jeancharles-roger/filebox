package org.kawane.filebox.json;

import java.io.BufferedWriter;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.Stack;

public class JSONStreamWriter implements JSONWriter {

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
		this.writer = new PrintWriter(new BufferedWriter(writer), true);
	}
	public JSONStreamWriter(OutputStream stream) {
		this.writer = new PrintWriter(new BufferedWriter(new OutputStreamWriter(stream)), true);
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

	@Override
	public void beginDocument() {
		context.push(OBJECT);
		print("{");
		indent++;
		firstMember = true;
	}

	@Override
	public void endDocument() {
		endObject();
	}

	@Override
	public void beginObject() {
		writeComma();
		context.push(OBJECT);
		print("{");
		indent++;
		firstMember = true;
	}

	@Override
	public void endObject() {
		context.pop();
		indent--;
		println();
		println("}");
		firstMember = false;
		firstArrayValue = false;
	}

	@Override
	public void beginArray() {
		writeComma();
		context.push(ARRAY);
		print("[");
		firstArrayValue = true;
	}

	@Override
	public void endArray() {
		context.pop();
		print("]");
		firstMember = false;
		firstArrayValue = false;
	}

	@Override
	public void writeMember(String s) {
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

	@Override
	public void writeBoolean(boolean bool) {
		writeValue(String.valueOf(bool));
	}

	@Override
	public void writeDouble(double d) {
		writeValue(String.valueOf(d));
	}

	@Override
	public void writeFloat(float f) {
		writeValue(String.valueOf(f));
	}

	@Override
	public void writeInteger(int i) {
		writeValue(String.valueOf(i));
	}

	@Override
	public void writeLong(long i) {
		writeValue(String.valueOf(i));
	}

	@Override
	public void writeString(String s) {
		print("\"");
		writeValue(s);
		print("\"");
	}

	public void writeNull() {
		writeValue("null");
	}

	public void writeValue(String value) {
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
}

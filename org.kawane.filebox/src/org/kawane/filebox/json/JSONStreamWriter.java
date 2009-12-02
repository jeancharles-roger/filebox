package org.kawane.filebox.json;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.util.Stack;

public class JSONStreamWriter implements JSONHandler {

	static private Charset DEFAULT_ENCODING = Charset.forName("UTF-8");

	private PrintWriter writer;

	private String tabPattern = "\t";
	private int indent = 0;
	private char lastChar = 0;
	private Stack<Integer> context = new Stack<Integer>();

	private boolean firstMember;

	private boolean firstArrayValue;

	private CharsetEncoder encoder;

	static final private int OBJECT = 0;
	static final private int ARRAY = 1;

	public JSONStreamWriter(Writer writer) {
		this.writer = new PrintWriter(new BufferedWriter(writer));
	}

	public JSONStreamWriter(OutputStream stream) {
		this.writer = new PrintWriter(new BufferedWriter(new OutputStreamWriter(stream, DEFAULT_ENCODING)));
	}

	public JSONStreamWriter(OutputStream stream, String charsetName) {
		Charset charset = Charset.forName(charsetName);
		this.writer = new PrintWriter(new BufferedWriter(new OutputStreamWriter(stream, charset)));
	}

	public JSONStreamWriter(OutputStream stream, Charset charset) {
		this.writer = new PrintWriter(new BufferedWriter(new OutputStreamWriter(stream, charset)));
		setCharset(charset);
	}

	public void setCharset(String charsetName) {
		if (charsetName != null) {
			Charset charset = Charset.forName(charsetName);
			encoder = charset.newEncoder();
		} else {
			encoder = null;
		}
	}

	public void setCharset(Charset charset) {
		if (charset != null) {
			encoder = charset.newEncoder();
		} else {
			encoder = null;
		}
	}

	public CharsetEncoder getEncoder() {
		if (encoder == null) {
			encoder = DEFAULT_ENCODING.newEncoder();
		}
		return encoder;
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

	public void byteValue(byte b) {
		value(String.valueOf(b));
	}

	public void charValue(char b) {
		value(String.valueOf((int) b));
	}

	public void shortValue(short b) {
		value(String.valueOf(b));
	}

	public void longValue(long i) {
		value(String.valueOf(i));
	}

	public void stringValue(String s) {
		writeComma();
		print("\"");
		escapeAllValue(s);
		print("\"");
	}
	
	public void stringValueNoEscape(String s) {
		writeComma();
		print("\"");
		print(s);
		print("\"");
	}

	public void nullValue() {
		value("null");
	}

	public void writeJSON(String value) {
		value(value);
	}

	private void escapeAllValue(String value) {
		StringReader r = new StringReader(value);
		char[] c = new char[1];
		try {
			int read = r.read(c);
			encoder = getEncoder();
			while (read != -1) {
				switch (c[0]) {
				case '"':
					writer.print('\\');
					writer.print(c[0]);
					break;
				case '\b':
					writer.print('\\');
					writer.print('b');
					break;
				case '\f':
					writer.print('\\');
					writer.print('f');
					break;
				case '\n':
					writer.print('\\');
					writer.print('n');
					break;
				case '\r':
					writer.print('\\');
					writer.print('r');
					break;
				}
				if (encoder.canEncode(c[0])) {
					writer.append(c[0]);
				} else {
					int codePointAt = Character.codePointAt(c, 0);
					StringBuilder buf = new StringBuilder(Integer.toHexString(codePointAt).toUpperCase());
					while(buf.length() < 4) {
						buf.insert(0, '0');
					}
					writer.append(buf.toString());
				}
				read = r.read(c);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
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
	
	public static void main(String[] args) {
		int codePointAt = Character.codePointAt(new char[]{'ù'}, 0);
		StringBuilder buf = new StringBuilder(Integer.toHexString(codePointAt).toUpperCase());
		while(buf.length() < 4) {
			buf.insert(0, '0');
		}
		System.out.println("\\u" + buf.toString());
	}

}

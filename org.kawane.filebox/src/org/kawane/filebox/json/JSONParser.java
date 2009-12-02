package org.kawane.filebox.json;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.Stack;

public class JSONParser implements JSON {

	public static final int EOF = -1;
	public static final int MAIN = 0;
	public static final int OBJECT = 1;
	public static final int ARRAY = 2;
	public static final int VALUE = 3;
	static private Charset DEFAULT_ENCODING = Charset.forName("UTF-8");


	public static int defaultCharBufferSize = 8192;

	private Reader in;

	private Stack<Integer> contexts = new Stack<Integer>();

	private StringBuffer sb = new StringBuffer();

	private char[] buf;

	private int cursor = -1;

	private int length = 0;
	private JSONHandler handler;

	public JSONParser(InputStream in) {
		this(new InputStreamReader(in, DEFAULT_ENCODING), defaultCharBufferSize);
	}

	public JSONParser(InputStream in, int bufferSize) {
		this(new InputStreamReader(in, DEFAULT_ENCODING), bufferSize);
	}
	
	public JSONParser(Reader in) {
		this(in, defaultCharBufferSize);
	}

	public JSONParser(Reader reader, int bufferSize) {
		this.in = reader;
		buf = new char[bufferSize];
	}

	private char eat() throws IOException {
		if (cursor == -1 || cursor >= length) {
			length = in.read(buf);
			cursor = 0;
			if (length == -1 || length == 0) {
				throw new EOFException();
			}
		}
		return buf[cursor++];
	}

	/* (non-Javadoc)
	 * @see org.kawane.filebox.boost.JSONReader#next()
	 */
	public void parse(JSONHandler handler) throws IOException {
		this.handler = handler;
		int context = MAIN;
		try {
			while (context != EOF) {
				switch (context) {
				case MAIN:
					context = parseDocument();
					break;
				case OBJECT:
					context = parseMember();
					break;
				case ARRAY:
				case VALUE:
					context = parseValue();
					break;
				}
			}
		} catch (EOFException e) {

		}
	}

	private int parseDocument() throws IOException {
		char c;
		while ((c = eat()) != -1) {
			if (c == '{') {
				handler.beginDocument();
				return contexts.push(OBJECT);
			}
		}
		return -1;
	}

	private int parseValue() throws IOException {
		char c;
		while ((c = eat()) != -1) {
			switch (c) {
			case '{':
				handler.beginObject();
				return contexts.push(OBJECT);
			case '"':
				handler.stringValue(parseString());
				return contexts.peek();
			case '[':
				handler.beginArray();
				return contexts.push(ARRAY);
			case ']':
				handler.endArray();
				contexts.pop();
				return contexts.peek();
			default:
				if (Character.isDigit(c) || c == '-') {
					// it may be a number
					handler.numberValue(parseNumber(c));
					return contexts.peek();
				} else if (Character.isLetter(c)) {
					// keyword
					handleKeyword(parseIdentifier(c));
					return contexts.peek();
				}
			}
		}
		return -1;
	}

	private String parseNumber(char c) throws IllegalArgumentException, IOException {
		return parseIdentifier(c);
	}

	private String parseIdentifier(char c) throws IllegalArgumentException, IOException {
		sb.setLength(0);
		while (c != -1) {
			switch (c) {
			case ',':
				return sb.toString();
			case '}':
			case ']':
				cursor--;
				return sb.toString();
			}
			if (Character.isWhitespace(c)) {
				return sb.toString();
			}
			sb.append(c);
			c = eat();
		}
		return null;
	}

	private void handleKeyword(String value) {
		if (TRUE.equalsIgnoreCase(value) || FALSE.equalsIgnoreCase(value)) {
			handler.booleanValue(Boolean.parseBoolean(value));
		} else if (NULL.equalsIgnoreCase(value)) {
			handler.nullValue();
		} else {
			handler.value(value);
		}
	}

	private int parseMember() throws IOException {
		char c;
		while ((c = eat()) != -1) {
			switch (c) {
			case '}':
				contexts.pop();
				if (contexts.isEmpty()) {
					handler.endDocument();
					return EOF;
				} else {
					handler.endObject();
					return contexts.peek();
				}
			case '"':
				handler.member(parseString());
				break;
			case ':':
				return VALUE;
			}
		}
		return -1;
	}

	private String parseString() throws IOException {
		char c;
		sb.setLength(0);
		while ((c = eat()) != -1) {
			switch (c) {
			case '"':
				return sb.toString();
			case '\\':
				c = parseSpecialChar();
				break;
			}
			sb.append(c);
		}
		return null;
	}

	private char parseSpecialChar() throws IOException, IllegalArgumentException {
		char c = eat();
		switch (c) {
		case 'u':
			// unicode four hex digit
			char[] buf = new char[4];
			for (int i = 0; i < 4; i++) {
				buf[i] = eat();
			}
			return Character.toChars(Integer.valueOf(new String(buf), 16))[0];
		case 't':
			return '\t';
		case '\b':
			return '\b';
		case '\f':
			return '\f';
		case '\n':
			return '\n';
		case '\r':
			return '\r';
		default:
			//			case '"':
			//			case '\\':
			//			case '/':
			return c;
		}
	}

	public void close() throws IOException {
		in.close();
	}

}

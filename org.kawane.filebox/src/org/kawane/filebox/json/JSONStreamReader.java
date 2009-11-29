package org.kawane.filebox.json;

import java.io.EOFException;
import java.io.IOException;
import java.io.Reader;
import java.util.Stack;

public class JSONStreamReader implements JSON {

	public static int defaultCharBufferSize = 8192;

	private Reader in;

	private Stack<String> objects = new Stack<String>();
	private Stack<Integer> contexts = new Stack<Integer>();

	private int currentToken = 0;

	private String name;

	private String value;

	private int type;

	private StringBuffer sb = new StringBuffer();

	private char[] buf;

	private int cursor = -1;

	private int length = 0;

	public JSONStreamReader(Reader in) {
		this(in, defaultCharBufferSize);
	}

	public JSONStreamReader(Reader reader, int bufferSize) {
		this.in = reader;
		buf = new char[bufferSize];
	}

	private char eat() throws IOException {
		if(cursor == -1 || cursor >= length) {
			length = in.read(buf);
			cursor = 0;
			if(length == -1 /*|| length == 0*/) {
				throw new EOFException();
			}
		}
		return buf[cursor++];
	}

	/* (non-Javadoc)
	 * @see org.kawane.filebox.boost.JSONReader#next()
	 */
	public int next() throws IOException {
		int token = -1;
		value = null;
		type = -1;
		switch (currentToken) {
		case START:
			token = parseDocument();
			break;
		case START_DOCUMENT:
		case START_OBJECT:
			token = parseMember();
			break;
		case MEMBER:
		case START_ARRAY:
			token = parseValue();
			break;
		case END_OBJECT:
		case END_ARRAY:
		case VALUE:
			switch (contexts.peek()) {
			case START_OBJECT:
			case START_DOCUMENT:
				token = parseMember();
				break;
			case START_ARRAY:
				token = parseValue();
				break;
			}
			break;
		}
		currentToken = token;
		return token;
	}

	private int parseDocument() throws IOException {
		char c;
		while ((c = eat()) != -1) {
			if (c == '{') {
				contexts.push(START_DOCUMENT);
				return START_DOCUMENT;
			}
		}
		return -1;
	}

	private int parseValue() throws IOException {
		char c;
		while ((c = eat()) != -1) {
			switch (c) {
			case '{':
				objects.push(name);
				contexts.push(START_OBJECT);
				return START_OBJECT;
			case '"':
				value = parseString();
				type = STRING_TYPE;
				return VALUE;
			case '[':
				contexts.push(START_ARRAY);
				return START_ARRAY;
			case ']':
				contexts.pop();
				return END_ARRAY;
			default:
				if (Character.isDigit(c) || c == '-') {
					// it may be a number
					value = parseNumber(c);
					type = NUMBER_TYPE;
					return VALUE;
				} else if (Character.isLetter(c)){
					// keyword
					value = parseIdentifier(c);
					if (TRUE.equalsIgnoreCase(value) || FALSE.equalsIgnoreCase(value)) {
						type = BOOLEAN_TYPE;
					} else if (NULL.equalsIgnoreCase(value)) {
						value = null;
						type = NULL_TYPE;
					} else {
						type = UNKNOWN_TYPE;
					}
					return VALUE;
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
				cursor --;
				return sb.toString();
			}
			if(Character.isWhitespace(c)) {
				return sb.toString();
			}
			sb.append(c);
			c = eat();
		}
		return null;
	}

	private int parseMember() throws IOException {
		char c;
		while ((c = eat()) != -1) {
			switch (c) {
			case '}':
				if (objects.isEmpty()) {
					currentToken = END_DOCUMENT;
					return END_DOCUMENT;
				} else {
					name = objects.pop();
					contexts.pop();
					return END_OBJECT;
				}
			case '"':
				name = parseString();
				break;
			case ':':
				return MEMBER;
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

	public String getName() {
		return name;
	}

	public int getValueType() {
		return type;
	}

	public String getValue() {
		return value;
	}

	public boolean getBoolean() {
		return Boolean.valueOf(value);
	}

	public double getDouble() {
		return Double.valueOf(value);
	}

	public float getFloat() {
		return Float.valueOf(value);
	}

	public int getInteger() {
		return Integer.valueOf(value);
	}

	public long getLong() {
		return Long.valueOf(value);
	}

	public int getContext() {
		return contexts.peek();
	}

	public void close() throws IOException {
		in.close();
	}


}

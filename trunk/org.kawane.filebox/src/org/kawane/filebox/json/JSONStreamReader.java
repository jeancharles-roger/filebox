package org.kawane.filebox.json;

import java.io.IOException;
import java.io.Reader;
import java.util.Stack;

public class JSONStreamReader implements JSONConstants, JSONReader{

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

	static public JSONReader create(Reader reader) {
		return new JSONStreamReader(reader);
	}

	private char eat() throws IOException {
		if(cursor == -1 || cursor >= length) {
				length = in.read(buf);
				cursor = 0;
				if(length == -1 || length == 0) {
					return (char)-1;
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
		case JSON_START:
			token = parseDocument();
			break;
		case JSON_START_DOCUMENT:
		case JSON_START_OBJECT:
			token = parseMember();
			break;
		case JSON_MEMBER:
		case JSON_START_ARRAY:
			token = parseValue();
			break;
		case JSON_END_OBJECT:
		case JSON_END_ARRAY:
		case JSON_VALUE:
			switch (contexts.peek()) {
			case JSON_START_OBJECT:
			case JSON_START_DOCUMENT:
				token = parseMember();
				break;
			case JSON_START_ARRAY:
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
				contexts.push(JSON_START_DOCUMENT);
				return JSON_START_DOCUMENT;
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
				contexts.push(JSON_START_OBJECT);
				return JSON_START_OBJECT;
			case '"':
				value = parseString();
				type = JSON_STRING_TYPE;
				return JSON_VALUE;
			case '[':
				contexts.push(JSON_START_ARRAY);
				return JSON_START_ARRAY;
			case ']':
				contexts.pop();
				return JSON_END_ARRAY;
			default:
				if (Character.isDigit(c) || c == '-') {
					// it may be a number
					value = parseNumber(c);
					type = JSON_NUMBER_TYPE;
					return JSON_VALUE;
				} else if (Character.isLetter(c)){
					// keyword
					value = parseIdentifier(c);
					if (TRUE.equalsIgnoreCase(value) || FALSE.equalsIgnoreCase(value)) {
						type = JSON_BOOLEAN_TYPE;
					} else if (NULL.equalsIgnoreCase(value)) {
						value = null;
						type = JSON_NULL_TYPE;
					} else {
						type = JSON_UNKNOWN_TYPE;
					}
					return JSON_VALUE;
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
					currentToken = JSON_END_DOCUMENT;
					return JSON_END_DOCUMENT;
				} else {
					name = objects.pop();
					contexts.pop();
					return JSON_END_OBJECT;
				}
			case '"':
				name = parseString();
				break;
			case ':':
				return JSON_MEMBER;
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

	/* (non-Javadoc)
	 * @see org.kawane.filebox.boost.JSONReader#getName()
	 */
	public String getName() {
		return name;
	}

	/* (non-Javadoc)
	 * @see org.kawane.filebox.boost.JSONReader#getValueType()
	 */
	public int getValueType() {
		return type;
	}

	/* (non-Javadoc)
	 * @see org.kawane.filebox.boost.JSONReader#getValue()
	 */
	public String getValue() {
		return value;
	}
	@Override
	public boolean getBoolean() {
		return Boolean.valueOf(value);
	}
	@Override
	public double getDouble() {
		return Double.valueOf(value);
	}
	@Override
	public float getFloat() {
		return Float.valueOf(value);
	}
	@Override
	public int getInteger() {
		return Integer.valueOf(value);
	}
	@Override
	public long getLong() {
		return Long.valueOf(value);
	}
	/* (non-Javadoc)
	 * @see org.kawane.filebox.boost.JSONReader#getContext()
	 */
	public int getContext() {
		return contexts.peek();
	}

	/* (non-Javadoc)
	 * @see org.kawane.filebox.boost.JSONReader#close()
	 */
	public void close() throws IOException {
		in.close();
	}


}

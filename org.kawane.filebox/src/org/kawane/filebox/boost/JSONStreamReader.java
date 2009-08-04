package org.kawane.filebox.boost;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Stack;

public class JSONStreamReader {

	static final public int JSON_END_DOCUMENT = -1;
	static final public int JSON_START = 0;
	static final public int JSON_START_DOCUMENT = 1;
	static final public int JSON_START_OBJECT = 2;
	static final public int JSON_END_OBJECT = 3;
	static final public int JSON_MEMBER = 4;
	static final public int JSON_VALUE = 5;
	static final public int JSON_START_ARRAY = 6;
	static final public int JSON_END_ARRAY = 7;

	static final public int JSON_STRING_TYPE = 1;
	static final public int JSON_NUMBER_TYPE = 2;
	static final public int JSON_BOOLEAN_TYPE = 3;
	static final public int JSON_NULL_TYPE = 4;
	static final public int JSON_UNKNOWN_TYPE = 5;

	// KEYWORD
	static final public String TRUE = "true";
	static final public String FALSE = "false";
	static final public String NULL = "null";

	private Reader in;

	private Stack<String> objects = new Stack<String>();
	private Stack<Integer> contexts = new Stack<Integer>();

	private int currentToken = 0;

	private String name;

	private Object value;

	private int type;

	private char charMemento = 0;
	private StringBuffer sb = new StringBuffer();

	public JSONStreamReader(Reader reader) {
		this.in = reader;
	}

	static public JSONStreamReader create(Reader reader) {
		return new JSONStreamReader(new BufferedReader(reader));
	}

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

	private char eat() throws IOException {
		if(charMemento != 0) {
			char recordChar = charMemento;
			charMemento = 0;
			return recordChar;
		}
		return (char) in.read();
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
					if (TRUE.equalsIgnoreCase((String) value) || FALSE.equalsIgnoreCase((String) value)) {
						value = Boolean.valueOf((String) value);
						type = JSON_BOOLEAN_TYPE;
					} else if (NULL.equalsIgnoreCase((String) value)) {
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

	private Object parseNumber(char c) throws IllegalArgumentException, IOException {
		return parseIdentifier(c);
	}

	private Object parseIdentifier(char c) throws IllegalArgumentException, IOException {
		sb.setLength(0);
		while (c != -1) {
			switch (c) {
			case ',':
				return sb.toString();
			case '}':
			case ']':
				charMemento = c;
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

	public String getName() {
		return name;
	}

	public int getValueType() {
		return type;
	}

	public Object getValue() {
		return value;
	}
	/**
	 * Possible values are JSON_START_DOCUMENT, JSON_START_OBJECT, JSON_START_ARRAY
	 * @return
	 */
	public int getContext() {
		return contexts.peek();
	}

	public void close() throws IOException {
		in.close();
	}

	public static void main(String[] args) {
		File folder = new File("jsonTests");
		for (File file : folder.listFiles()) {
			if(file.isFile() && file.getName().endsWith(".json")) {
				try {
					JSONStreamReader reader = JSONStreamReader.create(new FileReader(file));
					int token = reader.next();
					String tab = "";
					while(token != -1) {
						switch (token) {
						case JSON_START_OBJECT:
						case JSON_START_DOCUMENT:
							tab+="\t";
							System.out.print("{");
							break;
						case JSON_START_ARRAY:
							System.out.print("[");
							break;
						case JSON_END_ARRAY:
							System.out.print("]");
							break;
						case JSON_END_OBJECT:
						case JSON_END_DOCUMENT:
							tab = tab.substring(1);
							System.out.println();
							System.out.print(tab);
							System.out.print("}");
							break;
						case JSON_MEMBER:
							System.out.println();
							System.out.print(tab);
							System.out.print("\"" + reader.getName() + "\" : ");
							break;
						case JSON_VALUE:
							if(reader.getValueType() == JSON_STRING_TYPE) {
								System.out.print("\"");
								System.out.print(reader.getValue());
								System.out.print("\"");
							} else {
								System.out.print(reader.getValue());
							}
							System.out.print(", ");
							break;
						default:
							break;
						}
						token = reader.next();
					}
					reader.close();
				} catch (Exception e) {
					e.printStackTrace();
				}

			}
		}
	}


}

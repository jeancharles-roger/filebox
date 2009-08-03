package org.kawane.filebox.boost;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import org.kawane.filebox.core.ErrorHandler;

/**
 * <p>
 * A {@link JSONBoost} is able to serialize and deserialize {@link BoostObject} to
 * text files in UTF-8 encoding.
 * </p>
 *
 * <b>Writing example</b>
 *
 * <pre>
 * JBoost boost = new JBoost(&quot;Cob&quot;, 1);
 * FileOutputStream fileStream = new FileOutputStream(file);
 * boost.initializeWritin(fileStream);
 * boost.writeObject(this);
 * boost.close();
 * </pre>
 *
 * <b>Reading example</b>
 *
 * <pre>
 * JBoost boost = new JBoost(&quot;Cob&quot;, 1);
 * FileInputStream fileStream = new FileInputStream(file);
 * boost.initializeReading(fileStream);
 * Model result = (Model) boost.readObject();
 * boost.close();
 * </pre>
 *
 *
 * JSON grammar:
 *
 * object:
 *     --> {}
 *     --> { members }
 * members:
 *     -->  pair
 *     -->  pair , members
 * pair:
 *     -->  string : value
 * array:
 *     -->  []
 *     -->  [ elements ]
 * elements:
 *     -->  value
 *     -->  value , elements
 * value:
 *     -->  string
 *     -->  number
 *     -->  object
 *     -->  array
 *     -->  true
 *     -->  false
 *     -->  null

 * string:
 *     -->  ""
 *     -->  " chars "
 * chars:
 *     -->  char
 *     -->  char chars
 * char:
 *     -->  any-Unicode-character-
 *     -->      except-"-or-\-or-
 *     -->      control-character
 *     -->  \"
 *     -->  \\
 *     -->  \/
 *     -->  \b
 *     -->  \f
 *     -->  \n
 *     -->  \r
 *     -->  \t
 *     -->  \ u four-hex-digits
 * number:
 *     -->  int
 *     -->  int frac
 *     -->  int exp
 *     -->  int frac exp
 * int:
 *     -->  digit
 *     -->  digit1-9 digits
 *     -->  - digit
 *     -->  - digit1-9 digits
 * frac:
 *     -->  . digits
 * exp:
 *     -->  e digits
 * digits:
 *     -->  digit
 *     -->  digit digits
 * e:
 *     -->  e
 *     -->  e+
 *     -->  e-
 *     -->  E
 *     -->  E+
 *     -->  E-


 * @author Laurent Le Goff (legoff.laurent@gmail.com)
 *
 */
public class JSONBoost implements Boost {

	/**
	 * <p>
	 * Writing target.
	 * </p>
	 */
	protected Writer writer;

	/**
	 * <p>
	 * Reading source.
	 * </p>
	 */
	protected Reader reader;

	/**
	 * <p>
	 * This {@link Map} store all objects that have to be serialized.
	 * </p>
	 */
	// protected final Map<BoostObject, Integer> objectsToSave = new
	// HashMap<BoostObject, Integer>();
	/**
	 * <p>
	 * This {@link ArrayList} store the object indexes through file while
	 * <b>reading</b>.
	 * </p>
	 */
	protected final ArrayList<BoostObject> readObjetIndex = new ArrayList<BoostObject>();

	/**
	 * <p>
	 * This {@link Map} store the object indexes through file while
	 * <b>writing</b>.
	 * </p>
	 */
	protected final LinkedHashMap<BoostObject, Integer> writeObjetIndex = new LinkedHashMap<BoostObject, Integer>();

	/**
	 * <p>
	 * This {@link Map} store the class indexes through file.
	 * </p>
	 */
	protected final HashMap<Class<? extends BoostObject>, Integer> classIndex = new HashMap<Class<? extends BoostObject>, Integer>();

	/**
	 * <p>
	 * This {@link String} will be printed in the file header as serial Type.
	 * </p>
	 */
	protected final String type;

	/**
	 * <p>
	 * This integer will be printed in the file header as serial Version.
	 * </p>
	 */
	protected final int version;

	/**
	 * <p>
	 * Stores the version of file that is currently loading.
	 */
	protected int fileVersion;

	/**
	 * <p>
	 * Handles errors.
	 * </p>
	 */
	protected ErrorHandler errorHandler = ErrorHandler.Stub;

	/**
	 * <p>
	 * Creates a {@link JSONBoost} instance for a special type and version.
	 * </p>
	 *
	 * @param type
	 *            type of file to read.
	 * @param version
	 *            version reference.
	 */
	public JSONBoost(String type, int version) {
		this.type = type;
		this.version = version;
	}

	/**
	 * <p>
	 * The current file version that is currently loading. Accessible only after
	 * {@link #initializeReading(InputStream)}.
	 * </p>
	 *
	 * @return the current file version.
	 */
	public int getFileVersion() {
		return fileVersion;
	}

	public int getVersion() {
		return version;
	}

	/* (non-Javadoc)
	 * @see com.geensys.tinymodel.Boost#getErrorHandler()
	 */
	public ErrorHandler getErrorHandler() {
		return errorHandler;
	}

	/* (non-Javadoc)
	 * @see com.geensys.tinymodel.Boost#setErrorHandler(com.geensys.tinymodel.ErrorHandler)
	 */
	public void setErrorHandler(ErrorHandler errorHandler) {
		this.errorHandler = errorHandler;
	}

	public void initializeWriting(OutputStream stream) {
		writeObjetIndex.clear();
		classIndex.clear();
		try {
			this.writer = new BufferedWriter(new OutputStreamWriter(stream, getEncoding()));
		} catch (UnsupportedEncodingException e) {
			return;
		}
		writeHeader();
	}

	public void initializeReading(InputStream stream) {
		readObjetIndex.clear();
		classIndex.clear();
		try {
			this.reader = new BufferedReader(new InputStreamReader(stream, getEncoding()));
		} catch (UnsupportedEncodingException e) {
			return;
		}
		// init token reader
		basicReadChar();
		readHeader();
	}

	public void close() {
		if (writer != null) {
			try {
				writer.close();
			} catch (IOException e) {
				errorHandler.handleError(ErrorHandler.ERROR, "I/O exception: " + e.getMessage());
			} finally {
				writer = null;
			}
		}

		if (reader != null) {
			try {
				reader.close();
			} catch (IOException e) {
				errorHandler.handleError(ErrorHandler.ERROR, "I/O exception: " + e.getMessage());
			}
			reader = null;
		}
	}

	/**
	 * @return the {@link JSONBoost} encoding
	 */
	protected String getEncoding() {
		return "UTF-8";
	}

	/**
	 * @return the {@link JSONBoost} locale
	 */
	protected Locale getLocale() {
		return Locale.ENGLISH;
	}

	/**
	 * @return This {@link String} is printed in all JBoost files.
	 */
	protected String getFormatMagic() {
		return "GeensysBoost";
	}

	/**
	 * @return This integer identifies JBoost version.
	 */
	protected int getFormatVersion() {
		return 1;
	}

	protected void writeHeader() {
		basicWriteString(getFormatMagic() + " " + getFormatVersion() + " ");
		writeString(type);
		writeInt(version);
		fileVersion = version;
	}

	protected void readHeader() {
		String magic = nextToken();
		if (!getFormatMagic().equals(magic)) {
			errorHandler.handleError(ErrorHandler.FATAL_ERROR, "Wrong format type: " + magic + ", it should be " + getFormatMagic());
			return;
		}
		readToken(" ");

		int formatVersion = readInt();
		if (getFormatVersion() != formatVersion) {
			errorHandler
					.handleError(ErrorHandler.FATAL_ERROR, "Wrong format version: " + version + ", it should be " + getFormatVersion());
			return;
		}

		String currentType = readString();
		if (!type.equals(currentType)) {
			errorHandler.handleError(ErrorHandler.FATAL_ERROR, "Wrong file type: " + currentType + ", it should be " + type);
			return;
		}
		fileVersion = readInt();
	}

	/* (non-Javadoc)
	 * @see com.geensys.tinymodel.Boost#writeBoolean(boolean)
	 */
	public void writeBoolean(boolean b) {
		basicWriteString(b ? "t " : "f ");
	}

	/* (non-Javadoc)
	 * @see com.geensys.tinymodel.Boost#readBoolean()
	 */
	public boolean readBoolean() {
		boolean result = nextToken().equals("t");
		readToken(" ");
		return result;
	}

	/* (non-Javadoc)
	 * @see com.geensys.tinymodel.Boost#writeDouble(double)
	 */
	public void writeDouble(double d) {
		basicWriteString(String.valueOf(d));
		basicWriteString(" ");
	}

	/* (non-Javadoc)
	 * @see com.geensys.tinymodel.Boost#readDouble()
	 */
	public double readDouble() {
		double result = Double.valueOf(nextToken());
		readToken(" ");
		return result;
	}

	/* (non-Javadoc)
	 * @see com.geensys.tinymodel.Boost#writeFloat(float)
	 */
	public void writeFloat(float f) {
		basicWriteString(String.valueOf(f));
		basicWriteString(" ");
	}

	/* (non-Javadoc)
	 * @see com.geensys.tinymodel.Boost#readFloat()
	 */
	public float readFloat() {
		float result = Float.valueOf(nextToken());
		readToken(" ");
		return result;
	}

	/* (non-Javadoc)
	 * @see com.geensys.tinymodel.Boost#writeInt(int)
	 */
	public void writeInt(int i) {
		basicWriteString(String.valueOf(i));
		basicWriteString(" ");
	}

	/* (non-Javadoc)
	 * @see com.geensys.tinymodel.Boost#readInt()
	 */
	public int readInt() {
		int result = Integer.valueOf(nextToken());
		readToken(" ");
		return result;
	}

	/* (non-Javadoc)
	 * @see com.geensys.tinymodel.Boost#writeLong(long)
	 */
	public void writeLong(long l) {
		basicWriteString(String.valueOf(l));
		basicWriteString(" ");
	}

	/* (non-Javadoc)
	 * @see com.geensys.tinymodel.Boost#readLong()
	 */
	public long readLong() {
		long result = Long.valueOf(nextToken());
		readToken(" ");
		return result;
	}

	/* (non-Javadoc)
	 * @see com.geensys.tinymodel.Boost#writeBooleanArray(boolean[])
	 */
	public void writeBooleanArray(boolean[] array) {
		if (array == null) {
			basicWriteString("0 ");
			return;
		}
		writeInt(array.length);
		for (int i = 0; i < array.length; i++) {
			writeBoolean(array[i]);
		}
	}

	/* (non-Javadoc)
	 * @see com.geensys.tinymodel.Boost#readBooleanArray()
	 */
	public boolean[] readBooleanArray() {
		int size = readInt();
		boolean[] result = new boolean[size];
		for (int i = 0; i < size; i++) {
			result[i] = readBoolean();
		}
		return result;
	}

	/* (non-Javadoc)
	 * @see com.geensys.tinymodel.Boost#writeDoubleArray(double[])
	 */
	public void writeDoubleArray(double[] array) {
		if (array == null) {
			basicWriteString("0 ");
			return;
		}
		writeInt(array.length);
		for (int i = 0; i < array.length; i++) {
			writeDouble(array[i]);
		}
	}

	/* (non-Javadoc)
	 * @see com.geensys.tinymodel.Boost#readDoubleArray()
	 */
	public double[] readDoubleArray() {
		int size = readInt();
		double[] result = new double[size];
		for (int i = 0; i < size; i++) {
			result[i] = readInt();
		}
		return result;

	}

	/* (non-Javadoc)
	 * @see com.geensys.tinymodel.Boost#writeFloatArray(float[])
	 */
	public void writeFloatArray(float[] array) {
		if (array == null) {
			basicWriteString("0 ");
			return;
		}
		writeInt(array.length);
		for (int i = 0; i < array.length; i++) {
			writeFloat(array[i]);
		}
	}

	/* (non-Javadoc)
	 * @see com.geensys.tinymodel.Boost#readFloatArray()
	 */
	public float[] readFloatArray() {
		int size = readInt();
		float[] result = new float[size];
		for (int i = 0; i < size; i++) {
			result[i] = readFloat();
		}
		return result;

	}

	/* (non-Javadoc)
	 * @see com.geensys.tinymodel.Boost#writeIntArray(int[])
	 */
	public void writeIntArray(int[] array) {
		if (array == null) {
			basicWriteString("0 ");
			return;
		}
		writeInt(array.length);
		for (int i = 0; i < array.length; i++) {
			writeInt(array[i]);
		}
	}

	/* (non-Javadoc)
	 * @see com.geensys.tinymodel.Boost#readIntArray()
	 */
	public int[] readIntArray() {
		int size = readInt();
		int[] result = new int[size];
		for (int i = 0; i < size; i++) {
			result[i] = readInt();
		}
		return result;

	}

	/* (non-Javadoc)
	 * @see com.geensys.tinymodel.Boost#writeLongArray(long[])
	 */
	public void writeLongArray(long[] array) {
		if (array == null) {
			basicWriteString("0 ");
			return;
		}
		writeInt(array.length);
		for (int i = 0; i < array.length; i++) {
			writeLong(array[i]);
		}
	}

	/* (non-Javadoc)
	 * @see com.geensys.tinymodel.Boost#readLongArray()
	 */
	public long[] readLongArray() {
		int size = readInt();
		long[] result = new long[size];
		for (int i = 0; i < size; i++) {
			result[i] = readLong();
		}
		return result;

	}

	/* (non-Javadoc)
	 * @see com.geensys.tinymodel.Boost#writeObjectCollection(java.util.Collection)
	 */
	public <T extends BoostObject> void writeObjectCollection(Collection<T> collection) {
		writeInt(collection.size());
		for (T oneObject : collection) {
			writeObject(oneObject);
		}
	}

	/* (non-Javadoc)
	 * @see com.geensys.tinymodel.Boost#readObjectList(java.lang.Class)
	 */
	public <T extends BoostObject> List<T> readObjectList(Class<T> objectClass) {
		int size = readInt();
		List<T> result = new ArrayList<T>(size);
		for (int i = 0; i < size; i++) {
			result.add(readObject(objectClass));
		}
		return result;
	}

	/* (non-Javadoc)
	 * @see com.geensys.tinymodel.Boost#writeObject(com.geensys.tinymodel.BoostObject)
	 */
	public void writeObject(BoostObject obj) {

		if (obj == null) {
			basicWriteString("n ");
			return;
		}

		basicWriteString("{");
		if (writeObjetIndex.containsKey(obj)) {
			// object already been serialized
			basicWriteString(writeObjetIndex.get(obj).toString());
		} else {

			// first serialization for object
			// use the objectIndex size as new object index
			int index = writeObjetIndex.size();
			writeObjetIndex.put(obj, index);

			basicWriteString(String.valueOf(index));
			basicWriteString(" ");
			writeClass(obj.getClass());
			obj.writeToBoost(this);

		}
		basicWriteString("} ");
	}

	/* (non-Javadoc)
	 * @see com.geensys.tinymodel.Boost#readObject(java.lang.Class)
	 */
	public <T extends BoostObject> T readObject(Class<T> objectClass) {
		String token = nextToken();

		// case of null.
		if (token.equals("n")) {
			return null;
		}

		// case of a reference
		if ( token.charAt(0) == '!' ) {

			// get size from token: '![size]'
			int size = Integer.valueOf(token.substring(1));

			// consume next token: ':'
			readToken(":");

			String result = readNCharacters(size);

			// consume space after string.
			readToken(" ");

			return resolveReference(result, objectClass);
		}


		// lastDelimiter must be must be equals to "{".

		token = nextToken();
		int objectId = Integer.valueOf(token);

		token = nextToken();

		T result = null;
		if (token.equals(" ")) {
			Class<? extends T> objectRealClass = readClass(objectClass);
			try {
				result = objectRealClass.newInstance();
			} catch (InstantiationException e) {
				errorHandler.handleError(ErrorHandler.FATAL_ERROR, "Instanciation error: " + e.getMessage());
				return result;
			} catch (IllegalAccessException e) {
				errorHandler.handleError(ErrorHandler.FATAL_ERROR, "Illegal Access error: " + e.getMessage());
				return result;
			}

			readObjetIndex.add(result);
			// writeObjetIndex.put(result, objectId);
			result.readFromBoost(this);
			readToken("}");

		} else /* token must be '}' */{
			// searches for object.
			result = objectClass.cast(readObjetIndex.get(objectId));

			if (result == null) {
				// object already been encounters, the objectIndex must contain
				// it
				errorHandler.handleError(ErrorHandler.FATAL_ERROR, "Object id " + objectId + " doesn't exist");
				return null;
			}
		}
		readToken(" ");
		return result;
	}

	protected void writeClass(Class<? extends BoostObject> oneClass) {
		if (classIndex.containsKey(oneClass)) {
			// class already been serialized
			basicWriteString("[");
			basicWriteString(classIndex.get(oneClass).toString());
			basicWriteString("] ");
			return;
		}
		// first serialization for class
		// use the classIndex size as new class index
		int index = classIndex.size();
		classIndex.put(oneClass, index);

		// searches in mapping for class name, if none uses the simple name.
		String className = getClassBoostNameMap().get(oneClass);
		if (className == null) {
			className = oneClass.getCanonicalName();
		}

		basicWriteString("[");
		basicWriteString(String.valueOf(index));
		basicWriteString(" ");
		basicWriteString(className);
		basicWriteString("] ");
	}

	//@SuppressWarnings("unchecked")
	@SuppressWarnings("unchecked")
	protected <T extends BoostObject> Class<? extends T> readClass(Class<T> parentClass) {
		Class<? extends BoostObject> result = null;
		readToken("[");
		int classId = Integer.valueOf(nextToken());
		if (classIndex.containsValue(classId)) {
			// searches for class.
			for (Entry<Class<? extends BoostObject>, Integer> entry : classIndex.entrySet()) {
				if (entry.getValue() == classId) {
					result = entry.getKey();
					break;
				}
			}
		} else {
			readToken(" ");

			String boostClassName = nextToken();
			result = findClass(boostClassName);
			classIndex.put(result, classIndex.size());

		}

		if (!parentClass.isAssignableFrom(result)) {
			String simpleName = null;
			if(result != null) {
				simpleName = result.getSimpleName();
			}
			errorHandler.handleError(ErrorHandler.FATAL_ERROR, "Excepting for class " + parentClass.getSimpleName()
					+ " but read incompatible class " + simpleName + ".");
			return null;
		}

		readToken("]");
		readToken(" ");
		return (Class<? extends T>) result;
	}

	/* (non-Javadoc)
	 * @see com.geensys.tinymodel.Boost#writeString(java.lang.String)
	 */
	public void writeString(String stringValue) {
		basicWriteString("s");
		basicWriteString(String.valueOf(stringValue.length()));
		basicWriteString(":");
		basicWriteString(stringValue);
		basicWriteString(" ");
	}

	/* (non-Javadoc)
	 * @see com.geensys.tinymodel.Boost#readString()
	 */
	public String readString() {
		// reads token: 's[size]'
		String stringSize = nextToken();
		int size = Integer.valueOf(stringSize.substring(1));

		// consume next token: ':'
		readToken(":");

		String result = readNCharacters(size);

		// consume space after string.
		readToken(" ");
		return result;
	}

	/**
	 * <p>
	 * Reads exactly the given number of characters.
	 * </p>
	 * @param size number of character to read
	 * @return the read {@link String}.
	 */
	protected String readNCharacters(int size) {
		// consume all characters of the string, and build it
		StringBuilder buffer = new StringBuilder();
		for (int i = 0; i < size; i++) {
			buffer.append(lookAheadChar);
			basicReadChar();
		}
		return buffer.toString();
	}

	/* (non-Javadoc)
	 * @see com.geensys.tinymodel.Boost#writeEnum(T)
	 */
	public <T extends Enum<T>> void writeEnum(T enumValue) {
		String enumLiteral = enumValue.name();
		basicWriteString("#");
		basicWriteString(String.valueOf(enumLiteral.length()));
		basicWriteString(":");
		basicWriteString(enumLiteral);
		basicWriteString(" ");
	}

	/* (non-Javadoc)
	 * @see com.geensys.tinymodel.Boost#readEnum(java.lang.Class)
	 */
	public <T extends Enum<T>> T readEnum(Class<T> enumClass) {
		return Enum.valueOf(enumClass, readString());
	}

	protected void basicWriteString(String value) {
		try {
			writer.write(value);
		} catch (IOException e) {
			errorHandler.handleError(ErrorHandler.FATAL_ERROR, "I/O error: " + e.getMessage());
		}
	}

	protected void readToken(String exceptedToken) {
		String token = nextToken();
		if (!token.equals(exceptedToken)) {
			errorHandler.handleError(ErrorHandler.FATAL_ERROR, "Expecting '" + exceptedToken + "' but read '" + token + "'");
		}
	}

	protected char lookAheadChar = '\0';

	/**
	 * <p>
	 * Reads the next token from stream. It considers the tokens separated by
	 * {@value #Separators}. One separator is one token, nothing is lost,
	 * everything in the buffer will appear as tokens.
	 * </p>
	 * <p>
	 * For example, the stream <code>0.001 [1 AClass] s3:token</code>, will
	 * gives the next tokens:
	 * <ul>
	 * <li>'<code>0.001</code>', '<code> </code>', '<code>[</code>', '
	 * <code>1</code>', '<code> </code>', '<code>AClass</code>', '<code>]</code>
	 * ', '<code> </code>', '<code>s3</code>', '<code>:</code>', '
	 * <code>token</code>'
	 * </ul>
	 *
	 * @return the next token in the stream.
	 */
	protected String nextToken() {
		StringBuilder tokenBuffer = new StringBuilder();
		while (true) {
			switch (lookAheadChar) {
			case '[': case ']': case '{': case '}': case ' ': case ':':
				if (tokenBuffer.length() == 0) {
					tokenBuffer.append(lookAheadChar);
					basicReadChar();
				}
				return tokenBuffer.toString();
			default:
				tokenBuffer.append(lookAheadChar);
				basicReadChar();
			}
		}
	}

	protected void basicReadChar() {
		try {
			lookAheadChar = (char) reader.read();
		} catch (IOException e) {
			errorHandler.handleError(ErrorHandler.FATAL_ERROR, "I/O error: " + e.getMessage());
		}
	}

	@SuppressWarnings("unchecked")
	protected Class<? extends BoostObject> findClass(String boostName) {
		String className = boostName;
		if (getClassBoostNameMap().containsKey(boostName)) {
			className = getClassBoostNameMap().get(boostName);
		}
		try {
			return (Class<? extends BoostObject>) getClass().getClassLoader().loadClass(className);
		} catch (ClassNotFoundException e) {
			errorHandler.handleError(ErrorHandler.FATAL_ERROR, "Class not found error: " + e.getMessage());
		}
		return null;
	}

	protected Map<Class<?>, String> getClassBoostNameMap() {
		return Collections.emptyMap();
	}

	protected <T extends BoostObject> T  resolveReference(String reference, Class<T> objectClass) {
		errorHandler.handleError(ErrorHandler.ERROR, "This Boost implementation doesn't handle references (ref: " + reference + ").");
		return null;
	}

}


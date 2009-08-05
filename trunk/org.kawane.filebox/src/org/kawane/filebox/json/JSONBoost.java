package org.kawane.filebox.json;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Collection;
import java.util.List;

import org.kawane.filebox.boost.Boost;
import org.kawane.filebox.boost.BoostObject;
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

	private JSONStreamReader reader;
	private ErrorHandler errorHandler = ErrorHandler.Stub;
	private JSONStreamWriter writer;

	public void initializeWriting(OutputStream stream) {
		this.writer = new JSONStreamWriter(stream);
	}

	public void initializeReading(InputStream stream) {
		this.reader = new JSONStreamReader(new InputStreamReader(stream));
		// init document
		try {
			reader.next();
		} catch (Exception e) {
			getErrorHandler().handleError(ErrorHandler.ERROR, e);
		}
	}

	@Override
	public ErrorHandler getErrorHandler() {
		return errorHandler;
	}

	@Override
	public int getFileVersion() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean readBoolean() {
		try {
			// read member
			reader.next();
			// read the value
			reader.next();
		} catch (IOException e) {
			getErrorHandler().handleError(ErrorHandler.ERROR, e);
		}
		return reader.getBoolean();
	}

	@Override
	public boolean[] readBooleanArray() {
		try {
			// read the member
			reader.next();
			int step = 10;
			boolean [] bools =new boolean[step];
			int i = 0;
			int token = reader.next();
			while(token != JSONConstants.JSON_END_ARRAY) {
				token = reader.next();
				if(i >= bools.length) {
					boolean [] temp = new boolean[bools.length + step];
					System.arraycopy(bools, 0, temp, 0,bools.length);
					bools = temp;
				}
				bools[i] = reader.getBoolean();
				i++;
			}
			boolean [] temp = new boolean[i];
			System.arraycopy(bools, 0, temp, 0, i);
			return temp;
		} catch (IOException e) {
			getErrorHandler().handleError(ErrorHandler.ERROR, e);
		}
		return null;
	}

	@Override
	public double readDouble() {
		try {
			// read member
			reader.next();
			// read the value
			reader.next();
		} catch (IOException e) {
			getErrorHandler().handleError(ErrorHandler.ERROR, e);
		}
		return reader.getDouble();
	}

	@Override
	public double[] readDoubleArray() {
		try {
			// read the member
			reader.next();
			int step = 10;
			double [] dbls =new double[step];
			int i = 0;
			int token = reader.next();
			while(token != JSONConstants.JSON_END_ARRAY) {
				token = reader.next();
				if(i >= dbls.length) {
					double [] temp = new double[dbls.length + step];
					System.arraycopy(dbls, 0, temp, 0,dbls.length);
					dbls = temp;
				}
				dbls[i] = reader.getDouble();
				i++;
			}
			double [] temp = new double[i];
			System.arraycopy(dbls, 0, temp, 0, i);
			return temp;
		} catch (IOException e) {
			getErrorHandler().handleError(ErrorHandler.ERROR, e);
		}
		return null;
	}

	@Override
	public <T extends Enum<T>> T readEnum(Class<T> enumClass) {
		 return Enum.valueOf(enumClass, readString());
	}

	@Override
	public float readFloat() {
		try {
			// read member
			reader.next();
			// read the value
			reader.next();
		} catch (IOException e) {
			getErrorHandler().handleError(ErrorHandler.ERROR, e);
		}
		return reader.getFloat();
	}

	@Override
	public float[] readFloatArray() {
		try {
			// read the member
			reader.next();
			int step = 10;
			float [] dbls =new float[step];
			int i = 0;
			int token = reader.next();
			while(token != JSONConstants.JSON_END_ARRAY) {
				token = reader.next();
				if(i >= dbls.length) {
					float [] temp = new float[dbls.length + step];
					System.arraycopy(dbls, 0, temp, 0,dbls.length);
					dbls = temp;
				}
				dbls[i] = reader.getFloat();
				i++;
			}
			float [] temp = new float[i];
			System.arraycopy(dbls, 0, temp, 0, i);
			return temp;
		} catch (IOException e) {
			getErrorHandler().handleError(ErrorHandler.ERROR, e);
		}
		return null;
	}

	@Override
	public int readInt() {
		try {
			// read member
			reader.next();
			// read the value
			reader.next();
		} catch (IOException e) {
			getErrorHandler().handleError(ErrorHandler.ERROR, e);
		}
		return reader.getInteger();
	}

	@Override
	public int[] readIntArray() {
		try {
			// read the member
			reader.next();
			int step = 10;
			int [] dbls =new int[step];
			int i = 0;
			int token = reader.next();
			while(token != JSONConstants.JSON_END_ARRAY) {
				token = reader.next();
				if(i >= dbls.length) {
					int [] temp = new int[dbls.length + step];
					System.arraycopy(dbls, 0, temp, 0,dbls.length);
					dbls = temp;
				}
				dbls[i] = reader.getInteger();
				i++;
			}
			int [] temp = new int[i];
			System.arraycopy(dbls, 0, temp, 0, i);
			return temp;
		} catch (IOException e) {
			getErrorHandler().handleError(ErrorHandler.ERROR, e);
		}
		return null;
	}

	@Override
	public long readLong() {
		try {
			// read member
			reader.next();
			// read the value
			reader.next();
		} catch (IOException e) {
			getErrorHandler().handleError(ErrorHandler.ERROR, e);
		}
		return reader.getLong();
	}

	@Override
	public long[] readLongArray() {
		try {
			// read the member
			reader.next();
			int step = 10;
			long [] dbls =new long[step];
			int i = 0;
			int token = reader.next();
			while(token != JSONConstants.JSON_END_ARRAY) {
				token = reader.next();
				if(i >= dbls.length) {
					long [] temp = new long[dbls.length + step];
					System.arraycopy(dbls, 0, temp, 0,dbls.length);
					dbls = temp;
				}
				dbls[i] = reader.getInteger();
				i++;
			}
			long [] temp = new long[i];
			System.arraycopy(dbls, 0, temp, 0, i);
			return temp;
		} catch (IOException e) {
			getErrorHandler().handleError(ErrorHandler.ERROR, e);
		}
		return null;
	}

	@Override
	public <T extends BoostObject> T readObject(Class<T> objectClass) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T extends BoostObject> List<T> readObjectList(Class<T> objectClass) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String readString() {
		try {
			// read member
			reader.next();
			// read the value
			reader.next();
		} catch (IOException e) {
			getErrorHandler().handleError(ErrorHandler.ERROR, e);
		}
		return reader.getValue();
	}

	@Override
	public void setErrorHandler(ErrorHandler errorHandler) {
		this.errorHandler  = errorHandler;
	}

	public void tag(String s) {
		writer.writeMember(s);
	}

	@Override
	public void writeBoolean(boolean b) {
		writer.writeBoolean(b);
	}

	@Override
	public void writeBooleanArray(boolean[] array) {
		writer.beginArray();
		for (boolean b : array) {
			writer.writeBoolean(b);
		}
		writer.endArray();
	}

	@Override
	public void writeDouble(double d) {
		writer.writeDouble(d);
	}

	@Override
	public void writeDoubleArray(double[] array) {
		writer.beginArray();
		for (double d : array) {
			writer.writeDouble(d);
		}
		writer.endArray();
	}

	@Override
	public <T extends Enum<T>> void writeEnum(T enumValue) {
		writer.writeString(enumValue.name());
	}

	@Override
	public void writeFloat(float f) {
		writer.writeFloat(f);
	}

	@Override
	public void writeFloatArray(float[] array) {
		writer.beginArray();
		for (float f : array) {
			writer.writeFloat(f);
		}
		writer.endArray();
	}

	@Override
	public void writeInt(int i) {
		writer.writeInteger(i);
	}

	@Override
	public void writeIntArray(int[] array) {
		writer.beginArray();
		for (int i : array) {
			writer.writeInteger(i);
		}
		writer.endArray();
	}

	@Override
	public void writeLong(long l) {
		writer.writeLong(l);
	}

	@Override
	public void writeLongArray(long[] array) {
		writer.beginArray();
		for (long l : array) {
			writer.writeLong(l);
		}
		writer.endArray();
	}


	@Override
	public void writeString(String s) {
		writer.writeString(s);
	}

	@Override
	public void writeObject(BoostObject obj) {
		// TODO Auto-generated method stub

	}

	@Override
	public <T extends BoostObject> void writeObjectCollection(Collection<T> collection) {
		// TODO Auto-generated method stub

	}

}

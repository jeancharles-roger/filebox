package org.kawane.filebox.boost;

import java.io.InputStream;
import java.util.Collection;
import java.util.List;

import org.kawane.filebox.core.ErrorHandler;

/**
 * <p>
 * A class that inmplements {@link Boost} is able to serialize and deserialize {@link BoostObject} to
 * text files in UTF-8 encoding.
 * </p>
 * 
 * <b>Writing example using {@link JBoost} implementation</b>
 * 
 * <pre>
 * Boost boost = new JBoost(&quot;Cob&quot;, 1);
 * FileOutputStream fileStream = new FileOutputStream(file);
 * boost.initializeWritin(fileStream);
 * boost.writeObject(this);
 * boost.close();
 * </pre>
 * 
 * <b>Reading example using {@link JBoost} implementation</b>
 * 
 * <pre>
 * Boost boost = new JBoost(&quot;Cob&quot;, 1);
 * FileInputStream fileStream = new FileInputStream(file);
 * boost.initializeReading(fileStream);
 * Model result = (Model) boost.readObject();
 * boost.close();
 * </pre>
 * 
 * @author Jean-Charles Roger (jeancharles.roger@geensys.com)
 * 
 */
public interface Boost {

	public ErrorHandler getErrorHandler();

	public void setErrorHandler(ErrorHandler errorHandler);

	/**
	 * <p>
	 * The current file version that is currently loading. Accessible only after
	 * {@link #initializeReading(InputStream)}.
	 * </p>
	 * 
	 * @return the current file version.
	 */
	public int getFileVersion();

	public void writeBoolean(boolean b);

	public boolean readBoolean();

	public void writeDouble(double d);

	public double readDouble();

	public void writeFloat(float f);

	public float readFloat();

	public void writeInt(int i);

	public int readInt();

	public void writeLong(long l);

	public long readLong();

	public void writeBooleanArray(boolean[] array);

	public boolean[] readBooleanArray();

	public void writeDoubleArray(double[] array);

	public double[] readDoubleArray();

	public void writeFloatArray(float[] array);

	public float[] readFloatArray();

	public void writeIntArray(int[] array);

	public int[] readIntArray();

	public void writeLongArray(long[] array);

	public long[] readLongArray();

	/**
	 * <p>
	 * Writes a collection of {@link BoostObject} in the writer.
	 * </p>
	 * 
	 * @param collection
	 *            {@link Collection} to write.
	 */
	public <T extends BoostObject> void writeObjectCollection(Collection<T> collection);

	public <T extends BoostObject> List<T> readObjectList(Class<T> objectClass);

	public void writeObject(BoostObject obj);

	public <T extends BoostObject> T readObject(Class<T> objectClass);

	public void writeString(String stringValue);

	public String readString();

	public <T extends Enum<T>> void writeEnum(T enumValue);

	public <T extends Enum<T>> T readEnum(Class<T> enumClass);

}
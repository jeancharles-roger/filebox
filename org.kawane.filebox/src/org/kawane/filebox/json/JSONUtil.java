package org.kawane.filebox.json;

import java.lang.reflect.Field;
import java.util.logging.Level;
import java.util.logging.Logger;

public class JSONUtil {
	private static Logger logger = Logger.getLogger(JSONUtil.class.getName());

	static final public Object get(Object o, Field field) {
		try {
			boolean accessible = field.isAccessible();
			field.setAccessible(true);
			Object value = field.get(o);
			field.setAccessible(accessible);
			return value;
		} catch (IllegalArgumentException e) {
			logger.log(Level.SEVERE, "An Error Occured", e);
		} catch (IllegalAccessException e) {
			logger.log(Level.SEVERE, "An Error Occured", e);
		}
		return null;
	}

	static final public boolean getBoolean(Object o, Field field) {
		try {
			boolean accessible = field.isAccessible();
			field.setAccessible(true);
			boolean value = field.getBoolean(o);
			field.setAccessible(accessible);
			return value;
		} catch (IllegalArgumentException e) {
			logger.log(Level.SEVERE, "An Error Occured", e);
		} catch (IllegalAccessException e) {
			logger.log(Level.SEVERE, "An Error Occured", e);
		}
		return false;
	}

	static final public byte getByte(Object o, Field field) {
		try {
			boolean accessible = field.isAccessible();
			field.setAccessible(true);
			byte value = field.getByte(o);
			field.setAccessible(accessible);
			return value;
		} catch (IllegalArgumentException e) {
			logger.log(Level.SEVERE, "An Error Occured", e);
		} catch (IllegalAccessException e) {
			logger.log(Level.SEVERE, "An Error Occured", e);
		}
		return 0;
	}

	static final public char getChar(Object o, Field field) {
		try {
			boolean accessible = field.isAccessible();
			field.setAccessible(true);
			char value = field.getChar(o);
			field.setAccessible(accessible);
			return value;
		} catch (IllegalArgumentException e) {
			logger.log(Level.SEVERE, "An Error Occured", e);
		} catch (IllegalAccessException e) {
			logger.log(Level.SEVERE, "An Error Occured", e);
		}
		return 0;
	}

	static final public double getDouble(Object o, Field field) {
		try {
			boolean accessible = field.isAccessible();
			field.setAccessible(true);
			double value = field.getDouble(o);
			field.setAccessible(accessible);
			return value;
		} catch (IllegalArgumentException e) {
			logger.log(Level.SEVERE, "An Error Occured", e);
		} catch (IllegalAccessException e) {
			logger.log(Level.SEVERE, "An Error Occured", e);
		}
		return 0;
	}

	static final public float getFloat(Object o, Field field) {
		try {
			boolean accessible = field.isAccessible();
			field.setAccessible(true);
			float value = field.getFloat(o);
			field.setAccessible(accessible);
			return value;
		} catch (IllegalArgumentException e) {
			logger.log(Level.SEVERE, "An Error Occured", e);
		} catch (IllegalAccessException e) {
			logger.log(Level.SEVERE, "An Error Occured", e);
		}
		return 0;
	}

	static final public int getInt(Object o, Field field) {
		try {
			boolean accessible = field.isAccessible();
			field.setAccessible(true);
			int value = field.getInt(o);
			field.setAccessible(accessible);
			return value;
		} catch (IllegalArgumentException e) {
			logger.log(Level.SEVERE, "An Error Occured", e);
		} catch (IllegalAccessException e) {
			logger.log(Level.SEVERE, "An Error Occured", e);
		}
		return 0;
	}

	static final public long getLong(Object o, Field field) {
		try {
			boolean accessible = field.isAccessible();
			field.setAccessible(true);
			long value = field.getLong(o);
			field.setAccessible(accessible);
			return value;
		} catch (IllegalArgumentException e) {
			logger.log(Level.SEVERE, "An Error Occured", e);
		} catch (IllegalAccessException e) {
			logger.log(Level.SEVERE, "An Error Occured", e);
		}
		return 0;
	}

	static final public short getShort(Object o, Field field) {
		try {
			boolean accessible = field.isAccessible();
			field.setAccessible(true);
			short value = field.getShort(o);
			field.setAccessible(accessible);
			return value;
		} catch (IllegalArgumentException e) {
			logger.log(Level.SEVERE, "An Error Occured", e);
		} catch (IllegalAccessException e) {
			logger.log(Level.SEVERE, "An Error Occured", e);
		}
		return 0;
	}

	static final public void set(Object o, Field field, Object v) {
		try {
			boolean accessible = field.isAccessible();
			field.setAccessible(true);
			field.set(o, v);
			field.setAccessible(accessible);
		} catch (IllegalArgumentException e) {
			logger.log(Level.SEVERE, "An Error Occured", e);
		} catch (IllegalAccessException e) {
			logger.log(Level.SEVERE, "An Error Occured", e);
		}
	}

	static final public void setInt(Object o, Field field, int v) {
		try {
			boolean accessible = field.isAccessible();
			field.setAccessible(true);
			field.setInt(o, v);
			field.setAccessible(accessible);
		} catch (IllegalArgumentException e) {
			logger.log(Level.SEVERE, "An Error Occured", e);
		} catch (IllegalAccessException e) {
			logger.log(Level.SEVERE, "An Error Occured", e);
		}
	}

	static final public void setLong(Object o, Field field, long v) {
		try {
			boolean accessible = field.isAccessible();
			field.setAccessible(true);
			field.setLong(o, v);
			field.setAccessible(accessible);
		} catch (IllegalArgumentException e) {
			logger.log(Level.SEVERE, "An Error Occured", e);
		} catch (IllegalAccessException e) {
			logger.log(Level.SEVERE, "An Error Occured", e);
		}
	}

	static final public void setShort(Object o, Field field, short v) {
		try {
			boolean accessible = field.isAccessible();
			field.setAccessible(true);
			field.setShort(o, v);
			field.setAccessible(accessible);
		} catch (IllegalArgumentException e) {
			logger.log(Level.SEVERE, "An Error Occured", e);
		} catch (IllegalAccessException e) {
			logger.log(Level.SEVERE, "An Error Occured", e);
		}
	}

	static final public void setFloat(Object o, Field field, float v) {
		try {
			boolean accessible = field.isAccessible();
			field.setAccessible(true);
			field.setFloat(o, v);
			field.setAccessible(accessible);
		} catch (IllegalArgumentException e) {
			logger.log(Level.SEVERE, "An Error Occured", e);
		} catch (IllegalAccessException e) {
			logger.log(Level.SEVERE, "An Error Occured", e);
		}
	}

	static final public void setDouble(Object o, Field field, double v) {
		try {
			boolean accessible = field.isAccessible();
			field.setAccessible(true);
			field.setDouble(o, v);
			field.setAccessible(accessible);
		} catch (IllegalArgumentException e) {
			logger.log(Level.SEVERE, "An Error Occured", e);
		} catch (IllegalAccessException e) {
			logger.log(Level.SEVERE, "An Error Occured", e);
		}
	}

	static final public void setByte(Object o, Field field, byte v) {
		try {
			boolean accessible = field.isAccessible();
			field.setAccessible(true);
			field.setByte(o, v);
			field.setAccessible(accessible);
		} catch (IllegalArgumentException e) {
			logger.log(Level.SEVERE, "An Error Occured", e);
		} catch (IllegalAccessException e) {
			logger.log(Level.SEVERE, "An Error Occured", e);
		}
	}

	static final public void setBoolean(Object o, Field field, boolean v) {
		try {
			boolean accessible = field.isAccessible();
			field.setAccessible(true);
			field.setBoolean(o, v);
			field.setAccessible(accessible);
		} catch (IllegalArgumentException e) {
			logger.log(Level.SEVERE, "An Error Occured", e);
		} catch (IllegalAccessException e) {
			logger.log(Level.SEVERE, "An Error Occured", e);
		}
	}
}

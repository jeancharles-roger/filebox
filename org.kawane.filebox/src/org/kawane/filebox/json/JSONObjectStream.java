package org.kawane.filebox.json;

import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import static org.kawane.filebox.json.JSONUtil.*;

public class JSONObjectStream {

	public static final int PRIMITIVE_INTEGER = 0;
	public static final int PRIMITIVE_LONG = 1;
	public static final int PRIMITIVE_BYTE = 2;
	public static final int PRIMITIVE_SHORT = 3;
	public static final int PRIMITIVE_CHAR = 4;
	public static final int PRIMITIVE_DOUBLE = 5;
	public static final int PRIMITIVE_FLOAT = 6;
	public static final int PRIMITIVE_BOOLEAN = 7;
	public static final int VALUE = 8;
	public static final int STRING = 9;
	public static final int ENUM = 10;
	public static final int COLLECTION = 11;
	public static final int MAP = 12;
	public static final int ARRAY = 13;
	public static final int OBJECT = 14;

	public <T> T deserialize(Reader reader, Class<T> cl) throws Exception {
		T instance = cl.newInstance();
		return deserialize(reader, instance);
	}

	public <T> T deserialize(Reader reader, T o) throws Exception {
		JSONObjectHandler objectHandler = new JSONObjectHandler(o);
		JSONParser parser = new JSONParser(reader);
		parser.parse(objectHandler);
		return o;
	}

	public void serialize(Writer writer, Object o) throws Exception {
		if (o == null)
			return;
		JSONStreamWriter out = new JSONStreamWriter(writer);
		out.beginDocument();
		serializeMembers(o, out);
		out.endDocument();
		out.flush();
	}

	private void serializeMembers(Object o, JSONStreamWriter out) {
		Class<?> clazz = o.getClass();
		List<Field> fields = new ArrayList<Field>();
		collectObjectFields(clazz, fields);
		for (Field field : fields) {
			out.member(field.getName());
			int type = serialisationType(field.getType());
			switch (type) {
			case PRIMITIVE_BOOLEAN:
				out.booleanValue(getBoolean(o, field));
				break;
			case PRIMITIVE_BYTE:
				out.byteValue(getByte(o, field));
				break;
			case PRIMITIVE_CHAR:
				out.charValue(getChar(o, field));
				break;
			case PRIMITIVE_DOUBLE:
				out.doubleValue(getDouble(o, field));
				break;
			case PRIMITIVE_FLOAT:
				out.floatValue(getFloat(o, field));
				break;
			case PRIMITIVE_INTEGER:
				out.integerValue(getInt(o, field));
				break;
			case PRIMITIVE_LONG:
				out.longValue(getLong(o, field));
				break;
			case PRIMITIVE_SHORT:
				out.shortValue(getShort(o, field));
				break;
			default:
				serializeValue(get(o, field), type, field.getType(), out);
			}
		}
	}

	private void serializeCollection(Collection<?> list, JSONStreamWriter out) {
		out.beginArray();
		for (Object object : list) {
			serializeValue(object, serialisationType(object.getClass()), object.getClass(), out);
		}
		out.endArray();
	}

	private void serializeValue(Object value, int type, Class<?> clazz, JSONStreamWriter out)  {
		if (value == null) {
			out.nullValue();
		} else {
			switch (type) {
			case VALUE:
				out.value(String.valueOf(value));
				break;
			case STRING:
				out.stringValue((String) value);
				break;
			case ENUM:
				out.stringValue(((Enum<?>) value).name());
				break;
			case COLLECTION:
				serializeCollection((Collection<?>) value, out);
				break;
			case MAP:
				out.beginObject();
				for (Entry<?, ?> entry : ((Map<?, ?>) value).entrySet()) {
					out.member(String.valueOf(entry.getKey()));
					value = entry.getValue();
					if (entry.getValue() == null) {
						out.nullValue();
					} else {
						serializeValue(value, serialisationType(value.getClass()), value.getClass(), out);
					}
				}
				out.endObject();
				break;
			case ARRAY:
				serializeArray(value, clazz.getComponentType(), out);
				break;
			case OBJECT:
				out.beginObject();
				serializeMembers(value, out);
				out.endObject();
				break;
			}
		}
	}

	private void serializeArray(Object value, Class<?> clazz, JSONStreamWriter out) {
		int type = serialisationType(clazz);
		out.beginArray();
		switch (type) {
		case PRIMITIVE_BOOLEAN:
			for (boolean o : (boolean[]) value) {
				out.booleanValue(o);
			}
			break;
		case PRIMITIVE_BYTE:
			for (byte o : (byte[]) value) {
				out.byteValue(o);
			}
			break;
		case PRIMITIVE_CHAR:
			for (char o : (char[]) value) {
				out.charValue(o);
			}
			break;
		case PRIMITIVE_DOUBLE:
			for (double o : (double[]) value) {
				out.doubleValue(o);
			}
			break;
		case PRIMITIVE_FLOAT:
			for (float o : (float[]) value) {
				out.floatValue(o);
			}
			break;
		case PRIMITIVE_INTEGER:
			for (int o : (int[]) value) {
				out.integerValue(o);
			}
			break;
		case PRIMITIVE_LONG:
			for (long o : (long[]) value) {
				out.longValue(o);
			}
			break;
		case PRIMITIVE_SHORT:
			for (short o : (short[]) value) {
				out.longValue(o);
			}
			break;
		default:
			for (Object object : (Object[]) value) {
				serializeValue(object, type, clazz, out);
			}
		}
		out.endArray();
	}

	static public void collectObjectFields(Class<?> clazz, List<Field> fields) {
		for (Field field : clazz.getDeclaredFields()) {
			if (!Modifier.isStatic(field.getModifiers()) && !Modifier.isTransient(field.getModifiers())) {
				fields.add(field);
			}
		}
		Class<?> superclass = clazz.getSuperclass();
		if (superclass != null) {
			collectObjectFields(superclass, fields);
		}
	}

	static public int serialisationType(Class<?> type) {
		if (type.isArray()) {
			return ARRAY;
		}
		if (Collection.class.isAssignableFrom(type)) {
			return COLLECTION;
		}
		if (Map.class.isAssignableFrom(type)) {
			return MAP;
		}
		if (String.class == type) {
			return STRING;
		}
		// primitive java numbers
		if (Integer.TYPE == type) {
			return PRIMITIVE_INTEGER;
		}
		if (Long.TYPE == type) {
			return PRIMITIVE_LONG;
		}
		if (Byte.TYPE == type) {
			return PRIMITIVE_BYTE;
		}
		if (Short.TYPE == type) {
			return PRIMITIVE_SHORT;
		}
		if (Double.TYPE == type) {
			return PRIMITIVE_DOUBLE;
		}
		if (Float.TYPE == type) {
			return PRIMITIVE_FLOAT;
		}
		// Object numbers
		if (Number.class.isAssignableFrom(type)) {
			return VALUE;
		}
		//		if(Integer.class == type) {
		//			return PRIMITIVE_VALUE;
		//		}
		//		if(Long.class == type) {
		//			return PRIMITIVE_VALUE;
		//		}
		//		if(Byte.class == type ) {
		//			return PRIMITIVE_VALUE;
		//		}
		//		if(Short.class == type ) {
		//			return PRIMITIVE_VALUE;
		//		}
		//		if(Double.class == type) {
		//			return PRIMITIVE_VALUE;
		//		}
		//		if(Float.class == type ) {
		//			return PRIMITIVE_VALUE;
		//		}
		if (Character.class == type) {
			return VALUE;
		}
		if (Character.TYPE == type) {
			return PRIMITIVE_CHAR;
		}
		if (Boolean.class == type) {
			return VALUE;
		}
		if (Boolean.TYPE == type) {
			return PRIMITIVE_BOOLEAN;
		}
		if (Enum.class.isAssignableFrom(type)) {
			return ENUM;
		}
		return OBJECT;
	}

	static class Toto {
		String coucou = "coucou";
		int[] i = { 0, 1, 2 };
		int[][] ii = { { 3, 4, 5 }, { 6, 7, 8 } };
		Titi[] titis = { new Titi(), new Titi() };
	}

	static class Titi {
		String coucou = "coucou";
		int[] i = { 0, 1, 2 };
		int[][] ii = { { 3, 4, 5 }, { 6, 7, 8 } };
	}

	public static void main(String[] args) {
		JSONObjectStream jsonObject = new JSONObjectStream();
		try {
			jsonObject.serialize(new OutputStreamWriter(System.out), new Toto());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}

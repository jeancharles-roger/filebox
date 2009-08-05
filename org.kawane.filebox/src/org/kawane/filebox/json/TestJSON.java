package org.kawane.filebox.json;

import java.io.File;
import java.io.FileReader;
import java.io.PrintWriter;
import java.io.Reader;

public class TestJSON {

	interface JSONFactory {
		JSONReader create(Reader in);
	}

	public static void main(String[] args) {
		JSONFactory factory = new JSONFactory() {
			public JSONReader create(Reader in) {
				return JSONStreamReader.create(in);
			}
		};
		int ntimes = 1;
		for (int i = 0; i < ntimes; i++) {
			System.out.println("time: "+ perf(factory, 1 ));
		}
	}

	private static int perf(JSONFactory factory, int ntimes) {
		int time = 0;
		for (int i = 0; i < ntimes; i++) {
			PrintWriter writer = new PrintWriter(System.out);
			time += parseAndDisplay(factory, writer);
			writer.flush();
		}
		return time;
	}

	private static long parseAndDisplay(JSONFactory factory1, PrintWriter writer) {
		long time = System.currentTimeMillis();
		File folder = new File("jsonTests");
		for (File file : folder.listFiles()) {
			if (file.isFile() && file.getName().endsWith(".json")) {
				try {
					JSONReader reader = factory1.create(new FileReader(file));
					JSONStreamWriter out = new JSONStreamWriter(writer);
					int token = reader.next();
					while (token != -1) {
						switch (token) {
						case JSONConstants.JSON_START_DOCUMENT:
							out.beginDocument();
							break;
						case JSONConstants.JSON_START_OBJECT:
							out.beginObject();
							break;
						case JSONConstants.JSON_START_ARRAY:
							out.beginArray();
							break;
						case JSONConstants.JSON_END_ARRAY:
							out.endArray();
							break;
						case JSONConstants.JSON_END_DOCUMENT:
							out.endDocument();
							break;
						case JSONConstants.JSON_END_OBJECT:
							out.endObject();
							break;
						case JSONConstants.JSON_MEMBER:
							out.writeMember(reader.getName());
							break;
						case JSONConstants.JSON_VALUE:
							switch (reader.getValueType()) {
							case JSONConstants.JSON_STRING_TYPE:
								out.writeString(reader.getValue());
								break;
							case JSONConstants.JSON_BOOLEAN_TYPE:
								out.writeBoolean(reader.getBoolean());
								break;
							case JSONConstants.JSON_NULL_TYPE:
								out.writeNull();
								break;
							case JSONConstants.JSON_NUMBER_TYPE:
								out.writeValue(reader.getValue());
								break;
							}
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
		return System.currentTimeMillis() - time;
	}

}

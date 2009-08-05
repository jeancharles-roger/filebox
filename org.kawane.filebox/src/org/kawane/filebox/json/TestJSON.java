package org.kawane.filebox.json;

import java.io.File;
import java.io.FileReader;
import java.io.PrintWriter;

public class TestJSON {

	public static void main(String[] args) {
		int ntimes = 10;
		for (int i = 0; i < ntimes; i++) {
			System.out.println("time: "+ perf(1000 ));
		}
	}

	private static int perf(int ntimes) {
		int time = 0;
		for (int i = 0; i < ntimes; i++) {
			PrintWriter writer = new PrintWriter(System.out);
			time += parseAndDisplay(writer);
			writer.flush();
		}
		return time;
	}

	private static long parseAndDisplay(PrintWriter writer) {
		long time = System.currentTimeMillis();
		File folder = new File("jsonTests");
		for (File file : folder.listFiles()) {
			if (file.isFile() && file.getName().endsWith(".json")) {
				try {
					JSONStreamReader reader = new JSONStreamReader(new FileReader(file));
					JSONStreamWriter out = new JSONStreamWriter(writer);
					int token = reader.next();
					while (token != -1) {
						switch (token) {
						case JSON.START_DOCUMENT:
							out.beginDocument();
							break;
						case JSON.START_OBJECT:
							out.beginObject();
							break;
						case JSON.START_ARRAY:
							out.beginArray();
							break;
						case JSON.END_ARRAY:
							out.endArray();
							break;
						case JSON.END_DOCUMENT:
							out.endDocument();
							break;
						case JSON.END_OBJECT:
							out.endObject();
							break;
						case JSON.MEMBER:
							out.writeMember(reader.getName());
							break;
						case JSON.VALUE:
							switch (reader.getValueType()) {
							case JSON.STRING_TYPE:
								out.writeString(reader.getValue());
								break;
							case JSON.BOOLEAN_TYPE:
								out.writeBoolean(reader.getBoolean());
								break;
							case JSON.NULL_TYPE:
								out.writeNull();
								break;
							case JSON.NUMBER_TYPE:
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

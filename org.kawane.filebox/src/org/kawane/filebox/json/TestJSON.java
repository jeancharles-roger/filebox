package org.kawane.filebox.json;

import java.io.File;
import java.io.FileReader;
import java.io.PrintWriter;
import java.io.StringWriter;

public class TestJSON {

	public static void main(String[] args) {
		int ntimes = 10;
		for (int i = 0; i < ntimes; i++) {
			System.out.println("time1: "+ perf1(1000));
			System.out.println("time2: "+ perf2(1000));
		}
	}

	private static int perf1(int ntimes) {
		int time = 0;
		for (int i = 0; i < ntimes; i++) {
			PrintWriter writer = new PrintWriter(new StringWriter());
			time += parseAndDisplay1(writer);
			writer.flush();
		}
		return time;
	}

	private static long parseAndDisplay1(PrintWriter writer) {
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
							out.member(reader.getName());
							break;
						case JSON.VALUE:
							switch (reader.getValueType()) {
							case JSON.STRING_TYPE:
								out.stringValue(reader.getValue());
								break;
							case JSON.BOOLEAN_TYPE:
								out.booleanValue(reader.getBoolean());
								break;
							case JSON.NULL_TYPE:
								out.nullValue();
								break;
							case JSON.NUMBER_TYPE:
								out.numberValue(reader.getValue());
								break;
							}
							break;
						default:
							break;
						}
						token = reader.next();
					}
					reader.close();
					out.flush();
				} catch (Exception e) {
					e.printStackTrace();
				}

			}
		}
		return System.currentTimeMillis() - time;
	}
	private static int perf2(int ntimes) {
		int time = 0;
		for (int i = 0; i < ntimes; i++) {
			PrintWriter writer = new PrintWriter(new StringWriter());
			time += parseAndDisplay2(writer);
			writer.flush();
		}
		return time;
	}

	private static long parseAndDisplay2(PrintWriter writer) {
		long time = System.currentTimeMillis();
		File folder = new File("jsonTests");
		for (File file : folder.listFiles()) {
			if (file.isFile() && file.getName().endsWith(".json")) {
				try {
					JSONParser reader = new JSONParser(new FileReader(file));
					final JSONStreamWriter out = new JSONStreamWriter(writer);
					reader.parse(out);
					reader.close();
					out.flush();
				} catch (Exception e) {
					e.printStackTrace();
				}

			}
		}
		return System.currentTimeMillis() - time;
	}

}

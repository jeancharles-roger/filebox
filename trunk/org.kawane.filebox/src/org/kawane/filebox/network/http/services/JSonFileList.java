package org.kawane.filebox.network.http.services;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.kawane.filebox.json.JSONStreamWriter;
import org.kawane.filebox.network.http.HttpRequest;

public class JSonFileList {

	private static final Charset utf8Charset = Charset.forName("UTF-8");
	
	public String generate(File file, HttpRequest request) {
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		JSONStreamWriter writer = new JSONStreamWriter(stream);
		writer.beginDocument();
		writer.beginArray();
		for (File child : order(file.listFiles())) {
			if (child.isHidden()) continue;

			writer.member("type");
			if ( child.isDirectory() ) {
				writer.charValue('d');
			} else if ( child.isFile() ) {
				writer.charValue('f');
			} else {
				writer.charValue('u');
			}
			
			writer.member("name");
			writer.value(child.getName());
			
			writer.member("size");
			writer.longValue(child.length());
		}
		writer.endArray();
		writer.endDocument();
		try {
			writer.close();
		} catch (IOException e) {
			// can't happen, there is no link to system.
		}
		return new String(stream.toByteArray(), utf8Charset);
	}
	
	private Collection<File> order(File... files) {

		List<File> orderedFiles2 = new ArrayList<File>();
		List<File> orderedFiles = Arrays.asList(files);
		Collections.sort(orderedFiles, new Comparator<File>() {
			public int compare(File f1, File f2) {
				return f1.getName().compareTo(f2.getName());
			}
		});
		for (File file : orderedFiles) {
			if (file.isDirectory()) {
				orderedFiles2.add(file);
			}
		}
		for (File file : orderedFiles) {
			if (!file.isDirectory()) {
				orderedFiles2.add(file);
			}
		}
		return orderedFiles2;
	}
}

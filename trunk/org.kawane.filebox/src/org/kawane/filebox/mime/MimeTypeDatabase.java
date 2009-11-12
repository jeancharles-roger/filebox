package org.kawane.filebox.mime;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.kawane.filebox.network.http.Http;

public class MimeTypeDatabase {
	private Map<String, String> extensions = new HashMap<String, String>();
	private Map<String, String> mimeToIcons = new HashMap<String, String>();
	private Set<String> icons = new HashSet<String>();
	private Set<String> unknownIcons = new HashSet<String>();
	private Set<String> unknownMime = new HashSet<String>();

	public MimeTypeDatabase() {
		URL resource = getClass().getResource("mime-type.info");
		try {
			InputStream stream = resource.openStream();
			loadMimeTypes(stream);
			stream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		resource = getClass().getResource("generics-icons");
		try {
			InputStream stream = resource.openStream();
			loadMimeToIcons(stream);
			stream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		resource = getClass().getResource("icons.txt");
		try {
			InputStream stream = resource.openStream();
			loadIconsList(stream);
			stream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void loadMimeTypes(InputStream in) throws IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(in));
		String readLine = reader.readLine();
		while (readLine != null) {
			String[] split = readLine.split("\\s");
			if (split.length > 1 && split[0].trim().length() > 0) {
				String mime = split[0].trim();
				for (int i = 1; i < split.length; i++) {
					String ext = split[i].trim();
					if (ext.length() > 0) {
						extensions.put(ext, mime);
					}
				}
			}
			readLine = reader.readLine();
		}
	}
	
	private void loadMimeToIcons(InputStream in) throws IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(in));
		String readLine = reader.readLine();
		while (readLine != null) {
			String[] split = readLine.split(":");
			if (split.length == 2 && split[0].trim().length() > 0) {
				mimeToIcons.put(split[0].trim(), split[1].trim());
			}
			readLine = reader.readLine();
		}
	}

	private void loadIconsList(InputStream in) throws IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(in));
		String line = reader.readLine();
		while (line != null) {
			line = line.trim();
			if (line.length() > 0) {
				icons.add(line);
			}
			line = reader.readLine();
		}
	}
	
	public String searchMimeType(File file) {
		String name = file.getName();
		int lastIndexOf = name.lastIndexOf(".");
		if(lastIndexOf != -1) {
			String ext = name.substring(lastIndexOf+1, name.length());
			String mime = searchMimeByExtension(ext);
			if(mime!=null) {
				return mime;
			}
		}
		return Http.TEXT_PLAIN;
	}
	
	public String searchMimeByExtension(String ext) {
		String mime = extensions.get(ext.toLowerCase());
		if(mime == null) {
			if(unknownMime.add(mime)) {
				System.out.println("Unknown mime for extension: "+ ext);
			}
		}
		return mime;
	}
	
	public String searchIconByMime(String mime) {
		if(mime == null) return null;
		String iconName = mimeToIcons.get(mime);
		iconName = searchIcon(iconName);
		if(iconName == null) {
			iconName = searchIcon(mime);
		}
		if(iconName == null) {
			String[] split = mime.split("/");
			if(split.length>0) {
				iconName = searchIcon(split[0]);
			}
		}
		return iconName;
	}
	
	public String searchIcon(String iconName) {
		String ext = ".png";
		if(icons.contains(iconName+ext)) {
			return  iconName + ext;
		}
		String prefix = "source_";
		if(icons.contains(prefix + iconName+ext)) {
			return  prefix + iconName + ext;
		}
		ext = "-x-generic.png";
		if(icons.contains(iconName+ext)) {
			return  iconName + ext;
		}
		return  null;
	}
	
	public String searchIcon(File file) {
		if(file.isDirectory()) {
			return  "folder.png";
		}
		String name = file.getName();
		int lastIndexOf = name.lastIndexOf(".");
		if(lastIndexOf != -1) {
			String ext = name.substring(lastIndexOf+1, name.length()).toLowerCase();
			String icon = null;
			icon = searchIcon(ext);
			if(icon != null) {
				return icon;
			}
			String mime = searchMimeByExtension(ext);
			if(mime != null) {
				icon = searchIconByMime(mime);
			}
			if(icon != null) {
				return icon;
			}
			if(unknownIcons.add(ext)) {
				System.out.println("Unknown icon for Mime "+ mime +" and extension: "+ ext);
			}
		}
		return  "file.png";
	}
	
	public InputStream getIconContent(String icon) {
		if(icons.contains(icon)) {
			return getClass().getResourceAsStream("icons/"+icon);
		}
		return null;
	}

	public static void main(String[] args) {
		System.out.println(Pattern.compile("^core$").matcher("core").find());
		System.out.println(Pattern.compile("tar.gz$").matcher("coucou.tar.gz").find());
		System.out.println("coucou.tar.gz".matches("tar.gz$")); 
//		File mimeFolder = new File("/usr/share/mime-info/");
//		OutputStream out = System.out;
//		for (File file : mimeFolder.listFiles()) {
//			try {
//				BufferedReader reader = new BufferedReader(new FileReader(file));
//				String line = reader.readLine();
//				String mimeName;
//				while (line != null) {
//				   
//					if(line.trim().length() > 0) {
//						if(line.startsWith("\t") || line.startsWith(" ")) {
//							line = line.trim();
//							mimeName=null;
//						} else {
//							mimeName = line.trim();
//						}
//						out.write(line.getBytes());
//					}
//					line = reader.readLine();
//				}
//				reader.close();
//			} catch (IOException e) {
//				e.printStackTrace();
//			}
//		}
	}
}

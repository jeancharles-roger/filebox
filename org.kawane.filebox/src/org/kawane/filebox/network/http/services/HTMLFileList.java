package org.kawane.filebox.network.http.services;

import java.io.File;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import org.kawane.filebox.mime.MimeTypeDatabase;
import org.kawane.filebox.network.http.HttpRequest;

public class HTMLFileList {
	
	private DateFormat dateFormat = new SimpleDateFormat("dd/MM/yy HH:mm:ss");
	private DecimalFormat numberFormat = new DecimalFormat("0.###");
	private MimeTypeDatabase mimeTypeDatabase;

	public HTMLFileList(MimeTypeDatabase mimeTypeDatabase) {
		this.mimeTypeDatabase = mimeTypeDatabase;
	}
	
	public String generate(File file, HttpRequest request) {
		StringBuffer buf = new StringBuffer();
		buf.append("<html>\n");
		buf.append("<head>\n");
		String servicePath = request.getServicePath();
		buf.append("<link type=\"text/css\" rel=\"stylesheet\" href=\"" + servicePath +"?stylesheet=stylesheet.css" + "\">");
		buf.append("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\" />\n");
		buf.append("</head>\n");
		buf.append("<body>\n");
		buf.append("<div class=\"header\">\n");
		buf.append("<table>\n");
		File parentFile = file.getParentFile();
		String url = request.getContextURL();
		buf.append("<p>\n");
		buf.append("<table>\n");
		buf.append("<tr>\n");
		buf.append("<td style=\"width: 16px;	background-repeat: no-repeat;background-image: url('"+servicePath+"?icon="+ "go-home.png');\">\n");
		buf.append("</td>\n");
		buf.append("<td>\n");
		buf.append("<a href=\"");
		buf.append(servicePath);
		buf.append("\">");
		buf.append("Home\n");
		buf.append("</a>\n");
		buf.append("</td>\n");
		if(url.charAt(url.length() - 1) == '/') {
			url = url.substring(0, url.length() - 1);
		}
		if(servicePath.charAt(servicePath.length() - 1) == '/') {
			servicePath = servicePath.substring(0, servicePath.length() - 1);
		}
		if(parentFile != null && parentFile.exists() && !url.equals("/")) {
			buf.append("<td style=\"width: 16px;	background-repeat: no-repeat;background-image: url('"+servicePath+"?icon=" + "go-up.png');\">\n");
			buf.append("</td>\n");
			buf.append("<td>\n");
			buf.append("<a href=\"");
			buf.append(servicePath + url.substring(0, url.lastIndexOf("/")+1));
			buf.append("\">");
			buf.append("parent directory\n");
			buf.append("</a>\n");
			buf.append("</td>\n");
		}
		buf.append("</tr>\n");
		buf.append("</table>\n");
		buf.append("</p>\n");
		
		buf.append("<p>\n");
		buf.append("<form ACTION=\""+ servicePath + url + "\"ENCTYPE=\"multipart/form-data\" METHOD=\"POST\">\n");
		buf.append("Upload a File: ");
		buf.append("<INPUT TYPE=\"FILE\" NAME=\"files\"/>");
		buf.append("<INPUT TYPE=\"submit\"/>");
		buf.append("</form>\n");
		buf.append("</p>\n");
		
		buf.append("<hr/>\n");
		buf.append("<hr/>\n");
		buf.append("<hr/>\n");
		buf.append("</div>\n");
		buf.append("<div class=\"content\">\n");
		buf.append("<table class=\"files\">\n");
		for (File child : order(file.listFiles())) {
			if (child.isHidden())
				continue;
			buf.append("<tr class=\"line\">\n");
			buf.append("<td style=\"width: 16px;	background-repeat: no-repeat;background-image: url('"+request.getServicePath()+"?icon=" +mimeTypeDatabase.searchIcon(child)+"');\">\n");
			buf.append("</td>\n");
			buf.append("<td>\n");
			buf.append("<a href=\"");
			String parentDirectoryURL = request.getUrl();
			buf.append(parentDirectoryURL);
			if(!parentDirectoryURL.endsWith("/")) {
				buf.append("/");
			}
			buf.append(child.getName());
			if (child.isDirectory()) {
				buf.append("/");
			}
			buf.append("\">");
			buf.append(child.getName());
			buf.append("</a>\n");
			buf.append("</td>\n");
			buf.append("<td>\n");
			if (!child.isDirectory()) {
				buf.append(displaySize(child.length()));
			}
			buf.append("</td>\n");
			buf.append("<td>\n");
			buf.append(dateFormat.format(new Date(child.lastModified())));
			buf.append("</td>\n");
//			buf.append("<td>\n");
//			if (child.canRead()) {
//				buf.append("r");
//			}
//			if (child.canWrite()) {
//				buf.append("w");
//			}
//			buf.append("</td>\n");
			buf.append("</tr>");
		}
		buf.append("</table>\n");
		buf.append("</div>\n");
		buf.append("</body>\n");
		buf.append("</html>\n");
		return buf.toString();
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
	
	private String displaySize(long length) {
		String unit = " B";
		double l = length;
		if (l >= 1024) {
			unit = " KB";
			l = l / 1024;
		}
		if (l >= 1024) {
			unit = " MB";
			l = l / 1024;
		}
		if (l >= 1024) {
			unit = " GB";
			l = l / 1024;
		}
		return numberFormat.format(l) + unit;
	}

	public InputStream getStyleSheetContent(String styleSheet) {
		return getClass().getResourceAsStream(styleSheet);
	}
}

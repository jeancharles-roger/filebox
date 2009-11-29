package org.kawane.filebox.network.http.services;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.kawane.filebox.mime.MimeTypeDatabase;
import org.kawane.filebox.network.http.Http;
import org.kawane.filebox.network.http.HttpRequest;
import org.kawane.filebox.network.http.HttpResponse;
import org.kawane.filebox.network.http.NetworkService;
import org.kawane.filebox.search.SplitterInputStream;

public class FileService implements NetworkService {
	private static Logger logger = Logger.getLogger(FileService.class.getName());

	private File homeDir;

	private MimeTypeDatabase mimeTypeDatabase;

	private HTMLFileList htmlFileList;
	private JSonFileList jsonFileList;

	public FileService(File homeDir) {
		this.homeDir = homeDir;
		this.mimeTypeDatabase = new MimeTypeDatabase();
		htmlFileList = new HTMLFileList(mimeTypeDatabase);
		jsonFileList = new JSonFileList();
	}

	public void handleRequest(HttpRequest request, HttpResponse response) {
		if (request.getMethod().equalsIgnoreCase(Http.METHOD_POST)) {
			post(request, response);
		} else if (request.getMethod().equalsIgnoreCase(Http.METHOD_GET)) {
			get(request, response);
		} else if (request.getMethod().equalsIgnoreCase(Http.METHOD_HEAD)) {
			head(request, response);
		} else if (request.getMethod().equalsIgnoreCase(Http.METHOD_DELETE)) {
			delete(request, response);
		} else if (request.getMethod().equalsIgnoreCase(Http.METHOD_MKCOL)) {
			mkcol(request, response);
		} else if (request.getMethod().equalsIgnoreCase(Http.METHOD_COPY)) {
			copy(request, response);
		} else if (request.getMethod().equalsIgnoreCase(Http.METHOD_MOVE)) {
			move(request, response);
		}
		// search
	}

	private void move(HttpRequest request, HttpResponse response) {
		String url = request.getContextURL();
		File file = new File(homeDir, url);
		String destination = request.getHeader().get("Destination");
		File dest = new File(homeDir, destination);
		if (file.exists() && !dest.exists()) {
			if (file.renameTo(dest)) {
				response.setCode(Http.CODE_OK);
			} else {
				// try to copy and delete
				if (copy(file, dest)) {
					if (file.delete()) {
						response.setCode(Http.CODE_OK);
					} else {
						response.setCode(Http.CODE_NOTALLOWED);
					}
				} else {
					response.setCode(Http.CODE_NOTALLOWED);

				}
			}
		} else {
			response.setCode(Http.CODE_NOTALLOWED);
		}
	}

	public void copy(HttpRequest request, HttpResponse response) {
		String url = request.getContextURL();
		File file = new File(homeDir, url);
		String destination = request.getHeader().get("Destination");
		File dest = new File(homeDir, destination);
		if (file.exists()) {
			if (copy(file, dest)) {
				response.setCode(Http.CODE_OK);
			} else {
				response.setCode(Http.CODE_NOTALLOWED);
			}
		}
	}

	private boolean copy(File file, File dest) {
		if (file.isDirectory()) {
			dest.mkdir();
			File[] files = file.listFiles();
			for (File child : files) {
				copy(child, new File(dest, child.getName()));
			}
			return true;
		} else {
			if (dest.isDirectory()) {
				dest = new File(dest, file.getName());
			}
			if (!file.equals(dest)) {
				try {
					OutputStream out = new BufferedOutputStream(new FileOutputStream(dest));
					InputStream in = new BufferedInputStream(new FileInputStream(file));
					byte[] buf = new byte[1024];
					int read = in.read(buf);
					while (read != -1) {
						out.write(buf, 0, read);
						read = in.read(buf);
					}
					in.close();
					out.close();
				} catch (IOException e) {
					e.printStackTrace();
					return false;
				}
			}
		}
		return true;
	}

	public void mkcol(HttpRequest request, HttpResponse response) {
		String url = request.getContextURL();
		File file = new File(homeDir, url);
		if (!file.exists() && file.getParentFile().exists() && file.getParentFile().isDirectory()) {
			if (file.mkdir()) {
				response.setCode(Http.CODE_CREATED);
			} else {
				response.setCode(Http.CODE_NOTALLOWED);
			}
		} else {
			if (file.exists()) {
				response.setCode(Http.CODE_NOTALLOWED);
			} else if (!file.getParentFile().exists()) {
				response.setCode(Http.CODE_CONFLICT);
			} else if (file.getParentFile().isFile()) {
				response.setCode(Http.CODE_FORBIDDEN);
			}
		}
	}

	public void delete(HttpRequest request, HttpResponse response) {
		String url = request.getContextURL();
		File file = new File(homeDir, url);
		if (file.exists()) {
			if (file.delete()) {
				response.setCode(Http.CODE_OK);
			} else {
				response.setCode(Http.CODE_FORBIDDEN);
			}
		} else {
			response.setCode(Http.CODE_NOTALLOWED);
		}
	}

	public void post(HttpRequest request, HttpResponse response) {
		String url = request.getContextURL();
		System.out.println(request);
		File file = new File(homeDir, url);
		if (file.isDirectory()) {
			String contentType = request.getHeader().get(Http.HEADER_CONTENT_TYPE);
			if (contentType != null) {
				String[] split = contentType.split(";");
				if (split.length > 1 && split[0].trim().equals("multipart/form-data")) {
					split = split[1].split("=");
					if (split.length > 1 && split[0].trim().equals("boundary")) {
						String boundary = "--" + split[1].trim();
						System.out.println("Boundary: \n" + boundary);
						SplitterInputStream in = new SplitterInputStream(request.getContents());
						byte[] b = new byte[1024];
						OutputStream out = null;
						try {
							while (in.hasNext()) {
								in.setMarker(boundary.getBytes());
								int read = in.read(b);
								while (read != -1) {
									if (out != null) {
										out.write(b, 0, read);
									} else {
										System.out.write(b, 0, read);
									}
									read = in.read(b);
								}
								if (out != null) {
									out.close();
									out = null;
								}
								Map<String, String> header = readHeader(in);
								String value = header.get("Content-Disposition");
								if (value != null) {
									int lastIndexOf = value.lastIndexOf("filename");
									String filename = value.substring(lastIndexOf, value.length()).trim();
									int indexOf = filename.indexOf('=');
									filename = filename.substring(indexOf + 2, filename.length() - 1);
									filename = new File(filename).getName();
									if (filename.trim().length() > 0) {
										out = new BufferedOutputStream(new FileOutputStream(new File(file, filename)));
									}
								}
								if (!boundary.startsWith("\r\n")) {
									boundary = "\r\n" + boundary;
								}
							}
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
					get(request, response);
					return;
				}
			}
			// create the snippet
			InputStream contents = request.getContents();
			byte[] b = new byte[1024];
			StringBuffer sb = new StringBuffer();
			try {
				int read = contents.read(b);
				while (read >= 0) {
					sb.append(new String(b, 0, read));
					if (contents.available() > 0) {
						read = contents.read(b);
					} else {
						break;
					}
				}
				try {
					System.out.println(request);
					System.out.println(sb);
				} catch (Exception e) {
					logger.log(Level.SEVERE, "An Error Occured", e);
				}
				if (logger.isLoggable(Level.FINEST)) {
					logger.finest(request.getUrl() + " : " + sb.toString());
				}
			} catch (IOException e) {
				logger.log(Level.SEVERE, "An Error Occured", e);
			}
		}
	}

	public void display(InputStream in) {
		byte[] b = new byte[1024];
		try {
			int read = in.read(b);
			while (read != -1) {
				System.out.write(b, 0, read);
				if (in.available() > 0) {
					read = in.read(b);
				} else {
					break;
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private Map<String, String> readHeader(SplitterInputStream in) throws IOException {
		in.setMarker("\r\n\r\n".getBytes());
		String headerString = readString(in);
		HashMap<String, String> header = new HashMap<String, String>();
		if (headerString.trim().length() == 0 || headerString.trim().startsWith("--")) {
			return header;
		}
		String[] headers = headerString.split("\n");
		for (String multiPartHeader : headers) {
			if (multiPartHeader.trim().length() > 0) {
				int indexOf = multiPartHeader.indexOf(":");
				String name = multiPartHeader.substring(0, indexOf);
				String value = multiPartHeader.substring(indexOf + 1, multiPartHeader.length());
				header.put(name, value);
			}
		}
		return header;
	}

	private String readString(SplitterInputStream in) throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		byte[] b = new byte[1024];
		int read = in.read(b);
		while (read != -1) {
			out.write(b, 0, read);
			read = in.read(b);
		}
		return new String(out.toByteArray());
	}

	/**
	 * same as GET but do not send file content
	 * 
	 * @param request
	 * @param response
	 */
	public void head(HttpRequest request, HttpResponse response) {
		String url = request.getContextURL();
		File file = new File(homeDir, url);
		String[] icons = request.getParameter("icon");
		String[] styleSheets = request.getParameter("stylesheet");
		try {
			if (icons != null && icons.length > 0) {
				InputStream iconContent = mimeTypeDatabase.getIconContent(icons[0]);
				if (iconContent != null) {
					iconContent.close();
					response.getHeader().put(Http.HEADER_CONTENT_TYPE, "image/png");
					response.setCode(Http.CODE_OK);
					return;
				}
				response.setCode(Http.CODE_NOTFOUND);
				return;
			}
			if (styleSheets != null && styleSheets.length > 0) {
				InputStream styleSheetsContent = htmlFileList.getStyleSheetContent(styleSheets[0]);
				if (styleSheetsContent != null) {
					styleSheetsContent.close();
					response.setContents(styleSheetsContent);
					response.getHeader().put(Http.HEADER_CONTENT_TYPE, Http.TEXT_CSS);
					response.setCode(Http.CODE_OK);
					return;
				}
				response.setCode(Http.CODE_NOTFOUND);
				return;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (file.isDirectory()) {
			try {
				byte[] bytes;
				bytes = htmlFileList.generate(file, request).getBytes("utf-8");
				response.getHeader().put(Http.HEADER_CONTENT_TYPE, Http.TEXT_HTML);
				response.getHeader().put(Http.HEADER_CONTENT_LENGTH, String.valueOf(bytes.length));
				response.setCode(Http.CODE_OK);
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
			return;
		}
		if (file.exists()) {
			response.getHeader().put(Http.HEADER_CONTENT_TYPE, getMimeType(file));
			response.getHeader().put(Http.HEADER_CONTENT_LENGTH, String.valueOf(file.length()));
			response.setCode(Http.CODE_OK);
		} else {
			response.setCode(Http.CODE_NOTFOUND);
		}
	}

	public void get(HttpRequest request, HttpResponse response) {
		String url = request.getContextURL();
		System.out.println(request);
		File file = new File(homeDir, url);
		String[] icons = request.getParameter("icon");
		String[] styleSheets = request.getParameter("stylesheet");
		String[] formatParameter = request.getParameter("format");
		String format = "html";
		if ( formatParameter != null && formatParameter.length > 0 ) {
			format = formatParameter[0];
		}
		
		if (icons != null && icons.length > 0) {
			InputStream iconContent = mimeTypeDatabase.getIconContent(icons[0]);
			if (iconContent != null) {
				response.setContents(iconContent);
				response.getHeader().put(Http.HEADER_CONTENT_TYPE, "image/png");
				response.setCode(Http.CODE_OK);
				return;
			}
			response.setCode(Http.CODE_NOTFOUND);
			return;
		}
		if (styleSheets != null && styleSheets.length > 0) {
			InputStream styleSheetsContent = htmlFileList.getStyleSheetContent(styleSheets[0]);
			if (styleSheetsContent != null) {
				response.setContents(styleSheetsContent);
				response.getHeader().put(Http.HEADER_CONTENT_TYPE, Http.TEXT_CSS);
				response.setCode(Http.CODE_OK);
				return;
			}
			response.setCode(Http.CODE_NOTFOUND);
			return;
		}
		if (file.isDirectory()) {
			try {
				byte[] bytes;
				if ( "json".equals(format) ) {
					bytes = jsonFileList.generate(file, request).getBytes("utf-8");
					response.getHeader().put(Http.HEADER_CONTENT_TYPE, Http.TEXT_JSON);
					
				} else {
					bytes = htmlFileList.generate(file, request).getBytes("utf-8");
					response.getHeader().put(Http.HEADER_CONTENT_TYPE, Http.TEXT_HTML);
				}
				response.getHeader().put(Http.HEADER_CONTENT_LENGTH, String.valueOf(bytes.length));
				response.setCode(Http.CODE_OK);
				response.setContents(new ByteArrayInputStream(bytes));
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
			return;
		}
		if (file.exists()) {
			response.getHeader().put(Http.HEADER_CONTENT_TYPE, getMimeType(file));
			response.getHeader().put(Http.HEADER_CONTENT_LENGTH, String.valueOf(file.length()));
			response.setCode(Http.CODE_OK);
			try {
				response.setContents(new BufferedInputStream(new FileInputStream(file)));
			} catch (FileNotFoundException e) {
				logger.log(Level.SEVERE, "An Error Occured", e);
			}
		} else {
			response.setCode(Http.CODE_NOTFOUND);
		}
	}

	private String getMimeType(File file) {
		String name = file.getName();
		int lastIndexOf = name.lastIndexOf(".");
		if (lastIndexOf != -1) {
			String ext = name.substring(lastIndexOf + 1, name.length());
			String mime = mimeTypeDatabase.searchMimeByExtension(ext);
			if (mime != null) {
				return mime;
			}
		}
		return Http.TEXT_PLAIN;
	}

}

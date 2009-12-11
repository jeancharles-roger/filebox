import static http.HttpUtil.CRLF_BYTE;
import static http.HttpUtil.HEADER_AUTHORIZATION;
import static http.HttpUtil.HEADER_CONTENT_TYPE;
import static http.HttpUtil.HEADER_USER_AGENT;
import static http.HttpUtil.POST_METHOD;
import static http.HttpUtil.appendHeader;
import static http.HttpUtil.getContentTypeMultipartForm;
import static http.HttpUtil.methodHeader;
import static http.HttpUtil.readLine;
import static http.HttpUtil.writeMultipartFormData;
import http.Base64Coder;
import http.HttpUtil;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

import option.Option;
import option.OptionDefinition;
import option.OptionParser;
import option.OptionValidator;

public class UploadFile {

	public static void uploadFileToProjectURL(String project, File file, String summary, String user, String password, List<String> labels) {
		String host = project + ".googlecode.com/files";
		System.out.println("upload file to: \"https://" + host + "\"");
		System.out.println("Summary: \"" + summary + "\"");
		System.out.println("User: \"" + user + "\"");
		System.out.println("Password: \"" + password + "\"");
		System.out.println("Labels: " + labels);
		String auth = Base64Coder.encodeString(user + ":" + password);
		try {
			URL url = new URL("https://" + host);
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setDoOutput(true);
			connection.setRequestMethod(POST_METHOD);
			connection.addRequestProperty(HEADER_AUTHORIZATION, "Basic " + auth);
			connection.addRequestProperty(HEADER_USER_AGENT, "Kawane java uploader v0.1");
			List<Object[]> formFields = new ArrayList<Object[]>();
			formFields.add(new Object[] { "summary", summary });
			for (String l : labels) {
				formFields.add(new Object[] { "label", l });
			}
			formFields.add(new Object[] { "filename", file });
			String boundary = "----------Googlecode_boundary_reindeer_flotilla";
			connection.addRequestProperty(HEADER_CONTENT_TYPE, getContentTypeMultipartForm(boundary));
			connection.connect();
			OutputStream out = connection.getOutputStream();
			writeMultipartFormData(formFields, out, boundary);
			out.close();

			int responseCode = connection.getResponseCode();
			String responseMessage = connection.getResponseMessage();
			String location = connection.getHeaderField("Location");
			if (responseCode == 201) {
				System.out.println("File has been upload: " + responseMessage);
				System.out.println("Location: " + location);
			} else {
				System.err.println("Error when upload file: " + responseMessage);
				System.err.println("Location: " + location);
			}
			connection.disconnect();
		} catch (Throwable e) {
			System.err.println(e);
		}
	}
	public static void uploadFileToProject(String project, File file, String summary, String user, String password, List<String> labels) {
		String host = project + ".googlecode.com";
		String urlPath = "/files"; 
		System.out.println("upload file to: \"https://" + host + "\"");
		System.out.println("Summary: \"" + summary + "\"");
		System.out.println("User: \"" + user + "\"");
		System.out.println("Password: \"" + password + "\"");
		System.out.println("Labels: " + labels);
		String auth = Base64Coder.encodeString(user + ":" + password);
		try {
			SSLSocketFactory socketFactory = (SSLSocketFactory)SSLSocketFactory.getDefault();
	        SSLSocket socket = (SSLSocket)socketFactory.createSocket(host, 443);
	        SSLSession session = socket.getSession();
	        System.out.println("SSL Connection valid: " + session.isValid());
	        System.out.println("SSL protocol: " +session.getProtocol());
	        OutputStream out = socket.getOutputStream();
	        methodHeader(out, HttpUtil.POST_METHOD, urlPath);
	        appendHeader(out, HEADER_AUTHORIZATION, "Basic " + auth);
	        appendHeader(out, HEADER_USER_AGENT, "Kawane java uploader v0.1");
			List<Object[]> formFields = new ArrayList<Object[]>();
			formFields.add(new Object[] { "summary", summary });
			for (String l : labels) {
				formFields.add(new Object[] { "label", l });
			}
			formFields.add(new Object[] { "filename", file });
			String boundary = "----------Googlecode_boundary_reindeer_flotilla";
			appendHeader(out, HEADER_CONTENT_TYPE, getContentTypeMultipartForm(boundary));
			out.write(CRLF_BYTE);
			out.flush();
			writeMultipartFormData(formFields, out, boundary);
			
			InputStream in = socket.getInputStream();
			read(in);
			
			socket.close();
		} catch (Throwable e) {
			e.printStackTrace();
			System.err.println(e);
		}
	}

	static public void read(InputStream in) throws IOException {
//		String http = "HTTP/0.9";
		int responseCode;
		String responseMessage;
		Map<String, String> header = new HashMap<String, String>();

		// first line contains 'method url version'
		String [] commands = readLine(in).split(" ");
		if ( commands.length >= 3 ) {
//			http = commands[0];
			responseCode = Integer.parseInt(commands[1]);
			responseMessage = commands[2];
		} else {
			return;
		}

		String line = readLine(in);
		while (line != null && line.length() > 0 ) {
			String [] info = line.split(":");
			if ( info.length == 2 ) {
				header.put(info[0].trim(), info[1].trim());
			}
			line = readLine(in);
		}
		String location = header.get("Location");
		if (responseCode == 201) {
			System.out.println("File has been upload: " + responseMessage);
			System.out.println("Location: " + location);
		} else {
			System.err.println("Error when upload file: " + responseMessage);
			System.err.println("Location: " + location);
		}
	}
	public static void main(String[] args) {
		OptionParser parser = new OptionParser("java UploadFile -s SUMMARY -p PROJECT [options] FILE");

		parser.newGroup("Project information", null);
		OptionDefinition summaryDef = parser.addOptionDefinition("-s", "--summary", "Short description of the file", true, OptionValidator.required,
				OptionValidator.unique);
		OptionDefinition projectDef = parser.addOptionDefinition("-p", "--project", "Google Code project name", true, OptionValidator.required);
		OptionDefinition labelsDef = parser.addOptionDefinition("-l", "--labels", "An optional list of comma-separated labels to attach to the file",
				true);
		parser.newGroup("Authentication", "Use the password provided by the website: http://code.google.com/hosting/settings");
		OptionDefinition userDef = parser
				.addOptionDefinition("-u", "--user", "Your Google Code username", true, OptionValidator.required, OptionValidator.unique);
		OptionDefinition passwordDef = parser.addOptionDefinition("-w", "--password", "Your Google Code password", true, OptionValidator.required,
				OptionValidator.unique);
		List<Option> mainsOptions = parser.parseArgs(args);
		if (parser.isValid()) {
			if (mainsOptions.size() != 1) {
				parser.printError("You have to specify one file", System.err);
				parser.printUsage(System.err);
				return;
			}
			File file = mainsOptions.get(0).getFileValue();
			String summary = summaryDef.getOptionsValue().get(0);
			String user = userDef.getOptionsValue().get(0);
			String password = passwordDef.getOptionsValue().get(0);
			for (String project : projectDef.getOptionsValue()) {
				uploadFileToProjectURL(project, file, summary, user, password, labelsDef.getOptionsValue());
			}
		}

	}
}

package org.kawane.filebox.boost;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.UnknownHostException;

public class TestSocket {
	public static void main(String[] args) {
		Socket socket;
		try {
			socket = new Socket("localhost", 9999);
			OutputStream out = socket.getOutputStream();
			OutputStreamWriter writer = new OutputStreamWriter(out);
			writer.write("GET /filebox");
			writer.write("\n");
			writer.write("Server: filebox");
			writer.write("\n");
			writer.write("\n");
			writer.write("GeensysBoost 1 s7:filebox 1 s13:Hello Laurent)");
			writer.write("\n");
			writer.flush();

			Thread.sleep(1000);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

	}
}

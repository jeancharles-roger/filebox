import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;


public class ScreenCaptureServer {
	private static DatagramSocket socket;
	private static Display display;
	private static Shell shell;
	private static Canvas canvas;
	protected static Image image;

	public static void main(String[] args) {
		try {
			socket = new DatagramSocket(4444, InetAddress.getLocalHost());
			socket.setReceiveBufferSize(300000);
			Thread thread = new Thread(new Runnable() {
				
				@Override
				public void run() {
					while(true) {
						receiveImage();
					}
				}
			});
			thread.start();
			display = new Display();
			shell = new Shell(display);
			shell.setLayout(new FillLayout());
			ScrolledComposite sc = new ScrolledComposite(shell, SWT.V_SCROLL | SWT.H_SCROLL);
	        canvas = new Canvas(sc, SWT.DOUBLE_BUFFERED);
	        sc.setContent(canvas);
	        canvas.setBounds(display.getBounds());
	        canvas.addPaintListener(new PaintListener() {
	          public void paintControl(PaintEvent e) {
	        	  if(image != null) {
	        		  e.gc.drawImage(image, 0, 0);
	        	  }
	          }
	        });
			shell.pack();
			shell.open();
			while (!shell.isDisposed()) {
				if (!display.readAndDispatch())
					display.sleep();
			}
			display.dispose();
		} catch (SocketException e) {
			e.printStackTrace();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
	}

	protected static void receiveImage() {
		try {
			DatagramPacket p = new DatagramPacket(new byte[4], 4);
			socket.receive(p);
			int length = byteArrayToInt(p.getData());
			int count = 0;
			if(length>0) {
				ByteArrayOutputStream out = new ByteArrayOutputStream(length);
				byte[] buf = new byte[socket.getReceiveBufferSize()];
				while(count < length) {
					if(length - count > buf.length) {
						p = new DatagramPacket(buf, buf.length);
					} else {
						p = new DatagramPacket(buf, length - count);
					}
					socket.receive(p);
					out.write(p.getData(), p.getOffset(), p.getLength());
					count += p.getLength();
				}
				ImageLoader loader = new ImageLoader();
				byte[] data = out.toByteArray();
				ByteArrayInputStream stream = new ByteArrayInputStream(data);
				final ImageData[] imageData = loader.load(stream);
				display.asyncExec(new Runnable() {
					public void run() {
						if(imageData.length > 0) {
						image = new Image(display, imageData[0]);
						canvas.redraw();
						}
					}
				});
				stream.close();
				out.close();
				out = null;
				stream = null;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static byte[] intToByteArray(int value) {
        byte[] b = new byte[4];
        for (int i = 0; i < 4; i++) {
            int offset = (b.length - 1 - i) * 8;
            b[i] = (byte) ((value >>> offset) & 0xFF);
        }
        return b;
    }
	
	public static int byteArrayToInt(byte[] b) {
        int value = 0;
        for (int i = 0; i < 4; i++) {
            int shift = (4 - 1 - i) * 8;
            value += (b[i] & 0x000000FF) << shift;
        }
        return value;
    }
}

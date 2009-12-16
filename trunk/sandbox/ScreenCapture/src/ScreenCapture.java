import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.text.DecimalFormat;
import java.text.NumberFormat;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.Transform;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;

public class ScreenCapture {

	private static File videosFolder;
	private static Display display;
	private static Shell shell;
	private static NumberFormat numberFormat = new DecimalFormat("00000");
	private static long count = 0;
	private static int nbFramePerSec = 10;
	private static boolean started = false;
	private static DatagramSocket socket;
	private static OutputStream out;

	public static void main(String[] args) {
		try {
			socket = new DatagramSocket();
			videosFolder = new File("videos");
			display = new Display();
			shell = new Shell(display);
			shell.setLayout(new FillLayout());
			final Button bcapture = new Button(shell, SWT.PUSH);
			final Button ecapture = new Button(shell, SWT.PUSH);
			ecapture.setEnabled(false);
			bcapture.setText("Begin Capture");
			videosFolder = new File("videos");
			bcapture.addListener(SWT.Selection, new Listener() {
				public void handleEvent(Event event) {
					Runnable runnable = new Runnable() {
						@Override
						public void run() {
							if (started) {
								copyArea();
								if (started) {
									display.timerExec(1000 / nbFramePerSec, this);
								}
							}
						}
					};
					started = true;
					display.timerExec(1000 / nbFramePerSec, runnable);
					ecapture.setEnabled(true);
				}
			});
			ecapture.setText("Stop Capture");
			ecapture.addListener(SWT.Selection, new Listener() {

				public void handleEvent(Event event) {
					started = false;
					ecapture.setEnabled(false);
				}
			});
			shell.pack();
			shell.open();
			while (!shell.isDisposed()) {
				if (!display.readAndDispatch())
					display.sleep();
			}
			display.dispose();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void copyArea() {
		float scale = 2;
		Rectangle bounds = display.getBounds();
		final Image image = new Image(display, bounds);
		GC gc = new GC(display);
		gc.copyArea(image, 0, 0);
		gc.dispose();
		Image scaledImage = new Image(display, new Rectangle(bounds.x, bounds.y, (int)(bounds.width / scale), (int) (bounds.height / scale)));
		GC igc = new GC(scaledImage);
		Transform transform = new Transform(display);
		transform.scale(1/scale, 1/scale);
		igc.setTransform(transform);
		igc.drawImage(image, 0, 0);
		igc.dispose();
		transform.dispose();
		sendImage(scaledImage);
		scaledImage.dispose();
		image.dispose();
	}

	private static void sendImage(Image image) {
		ImageLoader loader = new ImageLoader();
		loader.data = new ImageData[]{image.getImageData()};
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		loader.save(out, SWT.IMAGE_JPEG);
		byte[] byteArray = out.toByteArray();
		try {
			byte[] length = intToByteArray(byteArray.length);
			if(byteArray.length > 0 ){
				socket.send(new DatagramPacket(length, 4, InetAddress.getLocalHost(), 4444));
				ByteArrayInputStream in = new ByteArrayInputStream(byteArray);
				byte[] buf = new byte[socket.getSendBufferSize()];
				int read = in.read(buf);
				while(read != -1) {
					socket.send(new DatagramPacket(buf, 0, read, InetAddress.getLocalHost(), 4444));
					read = in.read(buf);
				}
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

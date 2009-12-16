import java.awt.AWTException;
import java.awt.Dimension;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DirectColorModel;
import java.awt.image.IndexColorModel;
import java.awt.image.WritableRaster;
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
import java.util.ArrayDeque;
import java.util.Queue;

import javax.imageio.ImageIO;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.RGB;
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
	private static int nbFramePerSec = 5;
	private static boolean started = false;
	private static DatagramSocket socket;
	private static ByteArrayOutputStream out;
	private static InetAddress remote;
	private static long t;
	private static ImageData imageData;
	private static Image image;
	private static Image scaledImage;
	private static Queue<ImageData> queue = new ArrayDeque<ImageData>();
	private static GC igc;
	private static GC gc;

	public static void main(String[] args) {
		try {
			t = System.currentTimeMillis();
			socket = new DatagramSocket();
			// remote = InetAddress.getByAddress(new
			// byte[]{(byte)192,(byte)168,7,69});
			remote = InetAddress.getLocalHost();
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
								long t = System.currentTimeMillis();
								copyArea();
								if (started) {
									long dt = System.currentTimeMillis() - t;
									long delay = 1000 / nbFramePerSec;
									if (delay - dt < 0) {
										System.err.println("capture image take too much time: " + dt);
										delay = 0;
									} else {
										delay = delay - dt;
									}
									display.timerExec((int) delay, this);
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

	public static void trace(String message) {
		System.out.print("Since ");
		System.out.print(System.currentTimeMillis() - t);
		t = System.currentTimeMillis();
		System.out.print(" :");
		System.out.println(message);
	}
 
	static Toolkit toolkit = Toolkit.getDefaultToolkit();
	static Dimension screenSize = toolkit.getScreenSize();
	static java.awt.Rectangle screenRect = new java.awt.Rectangle(0,0, screenSize.width, screenSize.height);
	static Robot robot;
	private static BufferedImage bufimage;
	
	static {
		try {
			robot = new Robot();
		} catch (AWTException e) {
			e.printStackTrace();
		}
	}
	
	public static void copyArea2() {
		// create screen shot
		try {
			trace("take screen shot");
//			if(bufimage == null) {
				bufimage = robot.createScreenCapture(screenRect);
				bufimage.setAccelerationPriority(1);
//			}
//			bufimage.flush();
//			java.awt.Image img = image.getScaledInstance(100, 100, java.awt.Image.SCALE_REPLICATE);
//			img.setAccelerationPriority(1);
			trace("done");
			trace("convert to jpg");
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			ImageIO.write(bufimage, "jpg", out);
			trace("done");
			trace("send");
			byte[] byteArray = out.toByteArray();
			try {
				byte[] length = intToByteArray(byteArray.length);
				if (byteArray.length > 0) {
					socket.send(new DatagramPacket(length, 4, remote, 4444));
					ByteArrayInputStream in = new ByteArrayInputStream(byteArray);
					byte[] buf = new byte[socket.getSendBufferSize()];
					int read = in.read(buf);
					while (read != -1) {
						socket.send(new DatagramPacket(buf, 0, read, remote, 4444));
						read = in.read(buf);
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			trace("done");
		} catch (Exception e1) {
			e1.printStackTrace();
		}

	}

	public static void copyArea() {
		trace("copy gc to image");
		nbFramePerSec = 5;
		float scale = 1.2f;
		Rectangle bounds = display.getBounds();
		// final Image image = new Image(display, bounds);
		final Image image = getImage(bounds);

		GC gc = getGC();
		gc.copyArea(image, 0, 0);
		trace("End copy");

		Image scaledImage = getScaledImage(scale, bounds);
//		 Image scaledImage = new Image(display, new Rectangle(bounds.x,
//		 bounds.y, (int)(bounds.width / scale), (int) (bounds.height /
//		 scale)));
		GC igc = getScaledImageGC(scaledImage, scale);
		igc.drawImage(image, 0, 0);
		trace("Send image");
		sendImage(scaledImage, (int)(bounds.width/scale), (int)(bounds.height/scale));
		trace("End send");
	}

	private static GC getGC() {
		if (gc == null) {
			gc = new GC(display);
		}
		return gc;
	}

	private static GC getScaledImageGC(Image scaledImage, float scale) {
		if (igc == null) {
			igc = new GC(scaledImage);
			Transform transform = new Transform(display);
			transform.scale(1 / scale, 1 / scale);
			igc.setTransform(transform);
		}
		return igc;
	}

	private static Image getScaledImage(float scale, Rectangle bounds) {
		if (scaledImage == null) {
//			ImageData scaledImageData = new ImageData((int) (bounds.width / scale), (int) (bounds.height / scale), 8, new PaletteData(0x7C00, 0x3E0,
//					0x1F));
//			scaledImage = new Image(display, scaledImageData);
			 scaledImage = new Image(display, new Rectangle(bounds.x,
			 bounds.y, (int)(bounds.width / scale), (int) (bounds.height /
			 scale)));
		}
		return scaledImage;
	}

	private static Image getImage(Rectangle bounds) {
		if (image == null) {
			// ImageData imageData = new ImageData(bounds.width, bounds.height,
			// 16, new PaletteData(0x7C00, 0x3E0, 0x1F));
			// image = new Image(display, imageData);
			image = new Image(display, bounds);
		}
		return image;
	}
	static BufferedImage convertToAWT(ImageData data) {
	    ColorModel colorModel = null;
	    PaletteData palette = data.palette;
	    if (palette.isDirect) {
	      colorModel = new DirectColorModel(data.depth, palette.redMask,
	          palette.greenMask, palette.blueMask);
	      BufferedImage bufferedImage = new BufferedImage(colorModel,
	          colorModel.createCompatibleWritableRaster(data.width,
	              data.height), false, null);
	      WritableRaster raster = bufferedImage.getRaster();
	      int[] pixelArray = new int[3];
	      for (int y = 0; y < data.height; y++) {
	        for (int x = 0; x < data.width; x++) {
	          int pixel = data.getPixel(x, y);
	          RGB rgb = palette.getRGB(pixel);
	          pixelArray[0] = rgb.red;
	          pixelArray[1] = rgb.green;
	          pixelArray[2] = rgb.blue;
	          raster.setPixels(x, y, 1, 1, pixelArray);
	        }
	      }
	      return bufferedImage;
	    } else {
	      RGB[] rgbs = palette.getRGBs();
	      byte[] red = new byte[rgbs.length];
	      byte[] green = new byte[rgbs.length];
	      byte[] blue = new byte[rgbs.length];
	      for (int i = 0; i < rgbs.length; i++) {
	        RGB rgb = rgbs[i];
	        red[i] = (byte) rgb.red;
	        green[i] = (byte) rgb.green;
	        blue[i] = (byte) rgb.blue;
	      }
	      if (data.transparentPixel != -1) {
	        colorModel = new IndexColorModel(data.depth, rgbs.length, red,
	            green, blue, data.transparentPixel);
	      } else {
	        colorModel = new IndexColorModel(data.depth, rgbs.length, red,
	            green, blue);
	      }
	      BufferedImage bufferedImage = new BufferedImage(colorModel,
	          colorModel.createCompatibleWritableRaster(data.width,
	              data.height), false, null);
	      WritableRaster raster = bufferedImage.getRaster();
	      int[] pixelArray = new int[1];
	      for (int y = 0; y < data.height; y++) {
	        for (int x = 0; x < data.width; x++) {
	          int pixel = data.getPixel(x, y);
	          pixelArray[0] = pixel;
	          raster.setPixel(x, y, pixelArray);
	        }
	      }
	      return bufferedImage;
	    }
	  }
	protected static void sendImage(Image image, int width, int height) {
		try {
		ImageLoader loader = new ImageLoader();
			 trace("save Image");
		loader.data = new ImageData[] { image.getImageData() };
		if(out == null) {
			out = new ByteArrayOutputStream(150000);
		} else {
			out.reset();
		}
		loader.save(out, SWT.IMAGE_JPEG);
//			ImageIO.write(convertToAWT(image.getImageData()), "jpg", out);
			 trace("Image saved");
			byte[] byteArray = out.toByteArray();
			byte[] length = intToByteArray(byteArray.length);
			if(byteArrayToInt(length)!= byteArray.length){
				System.err.println("length is not correct: " + byteArrayToInt(length));
				System.err.println("must be: " + byteArray.length);
			} else {
				if (byteArray.length > 0) {
					socket.send(new DatagramPacket(length, 4, remote, 4444));
					ByteArrayInputStream in = new ByteArrayInputStream(byteArray);
					byte[] buf = new byte[socket.getSendBufferSize()];
					int read = in.read(buf);
					while (read != -1) {
						socket.send(new DatagramPacket(buf, 0, read, remote, 4444));
						read = in.read(buf);
					}
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

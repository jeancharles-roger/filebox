/**
 * Filebox developed by Kawane.
 * LGPL License.
 */

package org.kawane.filebox;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.eclipse.swt.SWTException;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

/**
 * @author Jean-Charles Roger
 *
 */
public class Resources {

	protected final static String NO_IMAGE = "missing.gif";
	protected final static String IMAGE_BASEDIR = "ui/images/";
	protected final static String MIME_BASEDIR = "mime/icons/";
	
	final protected Device device;
	private boolean disposed = false;
	
	private final List<String> imageDirectories = new ArrayList<String>();
	
	/** Stores all images that have already been loaded. */
	private final HashMap<String, Image> imageCache = new HashMap<String, Image>();
	
	private Resources(Device device) {
		this.device = device;
		
		imageDirectories.add(IMAGE_BASEDIR);
		imageDirectories.add(MIME_BASEDIR);
		
		loadImage(NO_IMAGE);
	}
	
	public Image getImage(String location) {
		Image image = imageCache.get(location);
		if (image == null) {
			image = loadImage(location);
			if (image == null) return imageCache.get(NO_IMAGE);
			imageCache.put(location, image);
		}
		return image;
	}
	
	private Image loadImage(String imageName) {
		for ( String baseDirectory : imageDirectories ) {
			Image image = loadImage(baseDirectory, imageName);
			if (image != null ) return image;
		}
		return null;
	}
	
	private Image loadImage(String baseDir, String imageName) {
		InputStream stream = getClass().getResourceAsStream (baseDir + imageName);
		if (stream == null) return null;
		Image image = null;
		try {
			image = new Image (device, stream);
		} catch (SWTException ex) {
		} finally {
			try {
				stream.close ();
			} catch (IOException ex) {}
		}
		return image;
	}
	
	public void dispose() {
		for (Image image : imageCache.values())  image.dispose();
		disposed = true;
	}
	
	public boolean isDisposed() {
		return disposed;
	}
	
	private static Resources instance = null;
	
	/** @return the Resources shared instance*/
	public static Resources getInstance() {
		if ( instance == null || instance.isDisposed() ) {
			instance = new Resources(Display.getCurrent());
		}
		return instance;
	}
	
}

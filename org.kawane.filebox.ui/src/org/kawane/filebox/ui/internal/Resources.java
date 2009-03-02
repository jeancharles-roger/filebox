/**
 * Filebox developed by Kawane.
 * LGPL License.
 */

package org.kawane.filebox.ui.internal;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

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
	protected final static String IMAGE_BASEDIR = "images/";
	
	final protected Device device;
	private boolean disposed = false;
	
	/** Stores all images that have already been loaded. */
	private final HashMap<String, Image> imageCache = new HashMap<String, Image>();
	
	private Resources(Device device) {
		this.device = device;
		
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
	
	protected Image loadImage(String imageName) {
		InputStream stream = getClass().getResourceAsStream (IMAGE_BASEDIR + imageName);
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

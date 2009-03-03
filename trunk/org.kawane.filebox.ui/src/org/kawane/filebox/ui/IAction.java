/**
 * Filebox developed by Kawane.
 * LGPL License.
 */
package org.kawane.filebox.ui;

import org.eclipse.swt.graphics.Image;

/**
 * A user action.
 * @author Jean-Charles Roger
 *
 */
public interface IAction {

	/** No special style */
	public static final int STYLE_NONE = 0;
	
	/** Action is a separator */
	public static final int STYLE_SEPARATOR = 1;
	
	/** Action will appears in the main menu bar */
	public static final int STYLE_MENUBAR = 2;
	
	/** Action will appears in the system tray menu */
	public static final int STYLE_MENUTRAY = 4;
		
	/** Action will appears in a popup menu */
	public static final int STYLE_MENUPOPUP = 8;
	
	/** Action will be considered as default action */
	public static final int STYLE_DEFAULTACTION = 16;
	
	/** The action is enable */
	public static final int VISIBILITY_ENABLE = 1;
	
	/** The action is disable */
	public static final int VISIBILITY_DISABLE = 2;
	
	/** The action is hidden */
	public static final int VISIBILITY_HIDDEN = 3;
	
	/** The action's code ends normally */
	public static final int STATUS_OK = 3;
	
	/** The action's code was canceled */
	public static final int STATUS_CANCEL = 3;
	
	/** The action's code gets in error */
	public static final int STATUS_ERROR = 3;
	
	/** Action's label */
	public String getLabel();
	
	/** Action's image */
	public Image getImage();
	
	/** Action's visibility */
	public int getVisibility();
	
	/** 
	 * Code to execute for action 
	 * @return the final status. 
	 * It can be {@link #STATUS_OK}, {@link #STATUS_CANCEL}, {@link #STATUS_ERROR}.
	 */
	public int run();
	
	/** @return true if action has given styme */
	public boolean hasStyle(int style);
	
	
	/**
	 * A stub implementation of interface {@link IAction}, suitable as a base
	 * class for concrete implementations.
	 */
	public class Stub implements IAction {

		protected String label;

		protected Image image;
		
		protected int style;
		
		/** Constructs an Action. */
		public Stub() {
			this("", null, STYLE_NONE);
		}
		
		/** Constructs an Action with given style. */
		public Stub(int style) {
			this("", null, style);
		}
		
		/** Constructs an Action with a given label. */
		public Stub(String label) {
			this(label, null, STYLE_NONE);
		}

		/** Constructs an Action with given label and image. */
		public Stub(String label, Image image) {
			this(label, image, STYLE_NONE);
		}
		
		/** Constructs an Action with given label, image and style. */
		public Stub(String label, Image image, int style) {
			this.label = label;
			this.image = image;
			this.style = style;
		}

		public String getLabel() {
			return label;
		}

		public void setLabel(String label) {
			this.label = label;
		}
		public Image getImage() {
			return image;
		}
		
		public void setImage(Image image) {
			this.image = image;
		}
		
		public int getStyle() {
			return style;
		}
		
		public void setStyle(int style) {
			this.style = style;
		}
		
		public final boolean hasStyle(int styleMask) {
			return (getStyle() & styleMask) != 0;
		}

		public int run() {
			return STATUS_OK;
		}
		
		public int getVisibility() {
			return VISIBILITY_ENABLE;
		}
		
	}
}

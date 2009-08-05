/**
 *  Copyright 2008 GeenSys. All rights reserved.
 */
package org.kawane.filebox.core;

/**
 * <p>
 * Handles errors for models actions;
 * </p>
 *
 * @author Jean-Charles Roger
 *
 */
public interface ErrorHandler {

	static final int WARNING = 0;
	static final int ERROR = 1;
	static final int FATAL_ERROR = 2;

	void handleError(int type, String message);
	void handleError(int type, Exception e);

	/**
	 * <p>
	 * This {@link ErrorHandler} prints errors and warning to console and
	 * throws a {@link RuntimeException} for ERROR and FATAL_ERROR.
	 * </p>
	 */
	public static final ErrorHandler Stub = new ErrorHandler() {
		public void handleError(int type, String message) {
			System.err.println(message);
			switch (type) {
			case ERROR:
			case FATAL_ERROR:
				throw new RuntimeException(message);
			}
		};
		@Override
		public void handleError(int type, Exception e) {
			e.printStackTrace();
			switch (type) {
			case ERROR:
			case FATAL_ERROR:
				throw new RuntimeException(e);
			}
		}
	};



}

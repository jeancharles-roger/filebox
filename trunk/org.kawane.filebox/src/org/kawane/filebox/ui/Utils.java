package org.kawane.filebox.ui;

import java.text.DecimalFormat;

public class Utils {

	final static private DecimalFormat numberFormat = new DecimalFormat("0.###");

	public static String displaySize(long length) {
		String unit = " B";
		double l = length;
		if (l >= 1024) {
			unit = " KB";
			l = l / 1024;
		}
		if (l >= 1024) {
			unit = " MB";
			l = l / 1024;
		}
		if (l >= 1024) {
			unit = " GB";
			l = l / 1024;
		}
		return numberFormat.format(l) + unit;
	}


	public static boolean isWindows() {
		return System.getProperty("os.name").toLowerCase().contains("windows");
	}
	
	public static boolean isMac() {
		return System.getProperty("os.name").toLowerCase().contains("mac");
	
	}
	
	public static boolean isLinux() {
		return System.getProperty("os.name").toLowerCase().contains("linux");
		
	}
}

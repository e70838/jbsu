/*
 * @(#)Radioan.java
 * 
 */
package tk.bocquet.bsu.geometry;

/**
 * Conversion functions from degrees to radian and vice versa.
 * 
 * @author Jean-François Bocquet
 */
public class Radian {
	/**
	 * Convert from degrees to radian.
	 * @param i_deg
	 * @return
	 */
	public static double deg2rad (double i_deg) {
		return i_deg * (java.lang.Math.PI / 180.0);
	}

	/**
	 * Convert from radian to degrees.
	 * @param i_rad
	 * @return
	 */
	public static double rad2deg (double i_rad) {
		return i_rad * (180.0 / java.lang.Math.PI);
	}

}

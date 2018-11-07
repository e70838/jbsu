/*
 * @(#)CoordNm.java
 * 
 */

package tk.bocquet.bsu.geometry;

/**
 * A CoordNm object represent the projection of a geographic location in (x, y), specified in Nm with double precision.
 *
 * @author Jean-François Bocquet
 *
 */
public class CoordNm {
	/**
	 * horizontal coordinate
	 */
	public double x;
	
	/**
	 * @return the x
	 */
	public double getX() {
		return x;
	}

	/**
	 * @param x the x to set
	 */
	public void setX(double x) {
		this.x = x;
	}

	/**
	 * @return the y
	 */
	public double getY() {
		return y;
	}

	/**
	 * @param y the y to set
	 */
	public void setY(double y) {
		this.y = y;
	}

	/**
	 * vertical coordinate. Greater values are below on the screen
	 */
	public double y;
	
	/**
	 * Default constructor returning the projection center.
	 */
	public CoordNm () {
		x = y = 0.0;
	}
	
	/**
	 * Create a location in (x, y) specified in Nm
	 * @param i_x
	 * @param i_y
	 */
	public CoordNm (double i_x, double i_y) {
		x = i_x;
		y = i_y;
	}
}

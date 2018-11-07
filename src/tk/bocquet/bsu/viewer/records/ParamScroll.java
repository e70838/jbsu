/*
 * @(#)ParamScroll.java
 * 
 */

package tk.bocquet.bsu.viewer.records;

/**
 * ParamScroll contains values computed after reading map data and that never change after.
 * 
 * @author Jean-François Bocquet
 *
 */
public class ParamScroll {
	/**
	 * Minimum and Maximum values of the zoom expressed in Nm.
	 */
    public int zoomMin, zoomMax;

    /**
     * Offset to apply to projected points before applying zoom
     */
    public double shiftX, shiftY;

    /**
     * Maximum values of x and y in Nm. (minimum is 0, 0)
     */
    public double xMax, yMax;
}

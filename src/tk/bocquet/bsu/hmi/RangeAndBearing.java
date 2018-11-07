/*
 * @(#)RangeAndBearing.java
 * 
 */

package tk.bocquet.bsu.hmi;

import tk.bocquet.bsu.geometry.CoordDegree;
import tk.bocquet.bsu.geometry.CoordNm;
import tk.bocquet.bsu.geometry.DistanceAndBearing;
import tk.bocquet.bsu.geometry.Projection;
import tk.bocquet.bsu.viewer.records.ParamScroll;


/**
 * A range and bearing is a line that gives the distance between two points and the direction
 * to fly from the first to the second one.
 * 
 * @author Jean-François Bocquet
 *
 */
public class RangeAndBearing {
	/**
	 * Format of a number with two decimal digits
	 */
	private static final java.text.DecimalFormat c_format2 = new java.text.DecimalFormat ("##0.00");
	
	/**
	 * Format of a number with a single decimal digit
	 */
	private static final java.text.DecimalFormat c_format1 = new java.text.DecimalFormat ("##0.0");
	
	/**
	 * Geographical location of the origin of the line
	 */
	private CoordDegree startDegree;
	
	/**
	 * Projected location of the origin of the line
	 */
	private CoordNm startNm;
	
	/**
	 * Geographical location of the destination of the line
	 */
	private CoordDegree destDegree;

	/**
	 * Projected location of the destination of the line
	 */
	private CoordNm destNm;
	
	/**
	 * Projection object
	 */
	private Projection projection;
	
	/**
	 * Scrolling parameters
	 */
	private ParamScroll paramScroll;
	
	/**
	 * Record used to store result of distanceBearing method
	 */
	private DistanceAndBearing distanceAndBearing;
	
	/**
	 * The constructor memorises the origin point and preallocate records
	 * @param i_bp projection
	 * @param i_ps scroll parameters
	 * @param i_point origin point (x, y) in pixels
	 * @param i_rat zoom ratio
	 */
	public RangeAndBearing (Projection i_bp, ParamScroll i_ps, java.awt.Point i_point, double i_rat) {
		this.projection = i_bp;
		this.paramScroll = i_ps;
		this.startNm = new CoordNm ((double)i_point.x / i_rat - this.paramScroll.shiftX,
				(double)i_point.y / i_rat - this.paramScroll.shiftY);
		this.destNm = new CoordNm (this.startNm.x, this.startNm.y);
		this.startDegree = new CoordDegree ();
		this.projection.stereo2geo(this.startNm, this.startDegree);
		this.destDegree = new CoordDegree (this.startDegree);
		this.distanceAndBearing = new DistanceAndBearing();
	}

	/**
	 * Move the destination point
	 * @param i_point destination point (x, y) in pixels
	 * @param i_rat zoom ratio
	 */
	public void move (java.awt.Point i_point, double i_rat) {
		this.destNm.x = (double)i_point.x / i_rat - this.paramScroll.shiftX;
		this.destNm.y = (double)i_point.y / i_rat - this.paramScroll.shiftY;
		this.projection.stereo2geo(this.destNm, this.destDegree);
	}

	/**
	 * Draw the range and bearing line
	 * @param i_hdc drawing context
	 * @param i_rat zoom ratio
	 */
	public void draw (java.awt.Graphics2D i_hdc, double i_rat) {
		int x1 = (int)((this.startNm.x + this.paramScroll.shiftX) * i_rat + 0.5);
		int y1 = (int)((this.startNm.y + this.paramScroll.shiftY) * i_rat + 0.5);
		int x2 = (int)((this.destNm.x + this.paramScroll.shiftX) * i_rat + 0.5);
		int y2 = (int)((this.destNm.y + this.paramScroll.shiftY) * i_rat + 0.5);
		i_hdc.setFont (Resources.g_arial);
		i_hdc.setColor (Resources.g_colors[Resources.c_yellow]);
		i_hdc.setStroke(Resources.g_hPenRoute);
		i_hdc.drawLine(x1, y1, x2, y2);
		this.startDegree.distanceBearing (this.destDegree, this.distanceAndBearing);
		i_hdc.drawString (RangeAndBearing.c_format2.format(this.distanceAndBearing.distance) + " Nm, " + RangeAndBearing.c_format1.format(this.distanceAndBearing.bearing), (x1+x2)/2, (y1+y2)/2);
		// System.out.println ("Draw " + x1 + " " + y1 + " - " + x2 + " " + y2);
	}
}

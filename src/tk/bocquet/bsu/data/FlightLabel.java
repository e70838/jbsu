package tk.bocquet.bsu.data;

import java.awt.geom.Area;

import tk.bocquet.bsu.viewer.records.StateVector;

/**
 * FlightLabel is a small rectangular area attached to a flight giving its callsign, its altitude, its speed, ...
 *
 */
public class FlightLabel {
	/**
	 * Callsign of the flight, immutable
	 */
	public String callsign;
	/**
	 * Wake category : L (light), M (medium), H (heavy)
	 */
	public char wake;
	/**
	 * Distance between the flight and the center of the label in Nm
	 */
	public double x;
	/**
	 * Distance between the flight and the center of the label in Nm
	 */
	public double y;
	/**
	 * Distance between the flight and the center of the label in pixels
	 */
	public int dx;
	/**
	 * Distance between the flight and the center of the label in pixels
	 */
	public int dy;
	/**
	 * width of the label in pixels
	 */
	public int width;
	/**
	 * height of the label in pixels
	 */
	public int height;
	/**
	 * state vector currently associated to the label
	 */
	public StateVector stateVector;
	/**
	 * Flight Id (immutable)
	 */
	public int id;
	/**
	 * Clipping area used to draw the line between the flight and the label.
	 */
	public Area area;

	/**
	 * Constructor
	 */
	public FlightLabel(int id) {
		this.id = id;
		this.x = this.y = 10.0;
	}

	/**
	 * Compute if a position is inside the label
	 * @param x
	 * @param y
	 * @return
	 */
	public boolean contains (int x, int y) {
		return this.dx <= x && this.dy <= y && (this.dx + 2 * this.width) > x && (this.dy + 2 * this.height) > y;
	}

	/**
	 * Draw the label using the affine transformation given as parameter
	 * @param i_hdc
	 * @param i_rat
	 * @param i_sx
	 * @param i_sy
	 */
	public void draw (java.awt.Graphics2D i_hdc, double i_rat, double i_sx, double i_sy) {
		int x = this.dx;
		int y = this.dy;
		java.awt.Shape l_prevClip = i_hdc.getClip();
		i_hdc.drawString (this.callsign, x, y+11);
		StateVector l_sv = this.stateVector;
		i_hdc.drawString (FlightData.c_format03d.format ((l_sv.altitude + 5)/10) + ' ' + l_sv.attitude + ' ' + (l_sv.groundSpeed + 50)/100, x, y + 18 + 11);
		i_hdc.drawString (l_sv.ssr + ' ' + this.wake, x, y + 18 + 18 + 11);
		i_hdc.setClip(this.area);
		int l_x = (int)((l_sv.xy.x + i_sx) * i_rat + 0.5);
		int l_y = (int)((l_sv.xy.y + i_sy) * i_rat + 0.5);
		i_hdc.drawLine (l_x, l_y, this.dx + this.width, this.dy + this.height);
		i_hdc.setClip(l_prevClip);
	}	
}
/*
 * @(#)FlightData.java
 * 
 */
package tk.bocquet.bsu.data;

import java.util.Iterator;

import tk.bocquet.bsu.data.FlightLabel;
import tk.bocquet.bsu.geometry.Projection;
import tk.bocquet.bsu.hmi.Resources;
import tk.bocquet.bsu.viewer.records.StateVector;
import tk.bocquet.bsu.viewer.records.Subtitle;

/**
 * FlightData contain all the state vectors of all flights (gathered by time ticks). It contains also flight labels, controllers list and orders.
 * 
 * @author Jean-François Bocquet
 *
 */
public class FlightData {
	/**
	 * Formats hour, minute and second components of time.
	 */
	static final java.text.DecimalFormat c_format = new java.text.DecimalFormat ("00");
	
	/**
	 * Formats altitude value in flight label
	 */
	static final java.text.DecimalFormat c_format03d = new java.text.DecimalFormat ("000");

	/**
	 * Images used to display flight and meteors
	 */
	javax.swing.ImageIcon flightSymbol, firstMeteorSymbol, secondMeteorSymbol, thirdMeteorSymbol, fourthMeteorSymbol, fifthMeteorSymbol;
	
	/**
	 * Distance between flights and label shall not evolve linearly with zoom.
	 */
	double labelDistRatio;

	/**
	 * All the state vectors of a given time tick are gathered in a TimeTick object, with the associated subtitles.
	 */
	public java.util.Vector<TimeTick> timeTicks;

	/**
	 * Current index in the timeTicks array.
	 */
	int curTimeTick;

	/**
	 * Array of controllers names. Immutable.
	 */
	public String[] controllers;

	/**
	 * Flag needed for first time initialisation.
	 */
	boolean firstDraw;

	/**
	 * Current selected flight label if any.
	 */
	public FlightLabel selectedLabel;
	
	/**
	 * All labels except the selected one. Ordered : latest are above.
	 */
	public java.util.LinkedList<FlightLabel> labels;

	/**
	 * Time formating
	 * @param i_time time expressed in seconds
	 * @return time as String : hh:mm:ss
	 */
	static String formatTime (int i_time) {
		int l_s = i_time % 60;
		i_time = (i_time - l_s)/60;
		int l_m = i_time % 60;
		int l_h = (i_time - l_m)/60;
		return c_format.format(l_h) + ":" + c_format.format(l_m) + ":" + c_format.format(l_s);
	}

	/**
	 * Time of the first timeTick
	 * @return time in seconds
	 */
	public int getFirstTime () {
		return this.timeTicks.firstElement().time;
	}

	/**
	 * Time of the last timeTick
	 * @return time in seconds
	 */
	public int getLastTime () {
		return this.timeTicks.lastElement().time;
	}

	/**
	 * Time of the current timeTick
	 * @return time in seconds
	 */
	public int getCurTime () {
		return this.timeTicks.get(this.curTimeTick).time;
	}

	/**
	 * Select another time tick
	 * @param i_curTimeTick index in the timeTicks array
	 * @return the time of the selected time tick formatted as String
	 */
	public String setCurTimeTick (int i_curTimeTick) {
		this.curTimeTick = i_curTimeTick;
		return formatTime (this.timeTicks.get(this.curTimeTick).time);
	}

	/**
	 * Return the array of state vectors for the current time tick.
	 * @return
	 */
	public StateVector[] currentFlightData () {
		return this.timeTicks.get(this.curTimeTick).stateVectors;
	}

	/**
	 * Return the subtitles for the current time tick.
	 * @return
	 */
	public Subtitle currentSubtitle () {
		return this.timeTicks.get(this.curTimeTick).subtitle;
	}

	/**
	 * Terminates a labelMove. It is put back in the list this.labels.
	 * @param i_rat affine transformation ratio
	 * @param i_sx  affine offset x
	 * @param i_sy  affine offset y
	 */
	public void applyLabelMove(double i_rat, double i_sx, double i_sy) {
		FlightLabel l_fl = this.selectedLabel;
		double x = l_fl.dx + l_fl.width - (l_fl.stateVector.xy.x + i_sx) * i_rat;
		double y = l_fl.dy + l_fl.height - (l_fl.stateVector.xy.y + i_sy) * i_rat;
		l_fl.x = x / this.labelDistRatio;
		l_fl.y = y / this.labelDistRatio;
		this.labels.addLast(this.selectedLabel);
		this.selectedLabel = null;
	}

	/**
	 * Draw all the flights and the associated labels
	 * @param i_hdc
	 * @param i_rat
	 * @param i_sx
	 * @param i_sy
	 * @param i_width
	 */
	public void draw (java.awt.Graphics2D i_hdc, double i_rat, double i_sx, double i_sy, int i_width) {
		if (this.firstDraw) {
			this.firstDraw = false;
			java.awt.font.FontRenderContext l_c = i_hdc.getFontRenderContext();
			java.awt.geom.Rectangle2D l_r = Resources.g_arial.getStringBounds ("000 - 00", l_c);
			int l_minWidth = (int)l_r.getWidth();
			for (FlightLabel l_fl : this.labels) {
				l_r = Resources.g_arial.getStringBounds (l_fl.callsign, l_c);
				int l_w = (int)l_r.getWidth();
				if (l_w < l_minWidth) {
					l_w = l_minWidth;
				}
				l_fl.width = (l_w + 1) / 2;
				l_fl.height = (18 + 18 + 11 + 1) / 2;
			}
		}
		// double l_z = i_width / i_rat;
		// TODO: adapt the formula to the replacement of feet by Nm
		this.labelDistRatio = i_rat; // * java.lang.Math.pow (100.0 / l_z, 1.0 / 3.0);

		i_hdc.setColor(Resources.g_hColorFlight);
		i_hdc.setStroke(Resources.g_hPenFlight);
		StateVector[] l_d = currentFlightData ();
		int l_time = getCurTime();
		for (StateVector l_sv : l_d) {
			int x = (int)((l_sv.xy.x + i_sx) * i_rat + 0.5);
			int y = (int)((l_sv.xy.y + i_sy) * i_rat + 0.5);

			// Update FlightLabel to anticipate its future drawing
			FlightLabel l_fl = l_sv.label;
			l_fl.stateVector = l_sv;
			if (l_fl != selectedLabel) {
				l_fl.dx = (int)(l_fl.x * this.labelDistRatio + 0.5);
				l_fl.dy = (int)(l_fl.y * this.labelDistRatio + 0.5);
				l_fl.dx += x - l_fl.width;
				l_fl.dy += y - l_fl.height;
			}
			l_fl.area = new java.awt.geom.Area((new java.awt.geom.Line2D.Double (x, y, l_fl.dx + l_fl.width, l_fl.dy + l_fl.height)).getBounds());
			l_fl.area.subtract(new java.awt.geom.Area(new java.awt.Rectangle (x-5, y-5, 10, 10)));
			l_fl.area.subtract(new java.awt.geom.Area(new java.awt.Rectangle (l_fl.dx, l_fl.dy, l_fl.width * 2, l_fl.height * 2)));
			
			int X = (int)((l_sv.speedVectorXY.x + i_sx) * i_rat + 0.5);
			int Y = (int)((l_sv.speedVectorXY.y + i_sy) * i_rat + 0.5);
			i_hdc.drawImage(this.flightSymbol.getImage(),
					x - this.flightSymbol.getIconWidth() / 2,
					y - this.flightSymbol.getIconHeight() / 2, null);
			i_hdc.drawLine (x, y, X, Y);
			StateVector l_m = l_sv.previous;
			if (l_m != null) {
				int l_mx = (int)((l_m.xy.x + i_sx) * i_rat + 0.5);
				int l_my = (int)((l_m.xy.y + i_sy) * i_rat + 0.5);
				i_hdc.drawImage(this.firstMeteorSymbol.getImage(),
						l_mx - this.firstMeteorSymbol.getIconWidth() / 2,
						l_my - this.firstMeteorSymbol.getIconHeight() / 2, null);
				l_m = l_m.previous;
				if (l_m != null) {
					l_mx = (int)((l_m.xy.x + i_sx) * i_rat + 0.5);
					l_my = (int)((l_m.xy.y + i_sy) * i_rat + 0.5);
					i_hdc.drawImage(this.secondMeteorSymbol.getImage(),
							l_mx - this.secondMeteorSymbol.getIconWidth() / 2,
							l_my - this.secondMeteorSymbol.getIconHeight() / 2, null);
					l_m = l_m.previous;
					if (l_m != null) {
						l_mx = (int)((l_m.xy.x + i_sx) * i_rat + 0.5);
						l_my = (int)((l_m.xy.y + i_sy) * i_rat + 0.5);
						i_hdc.drawImage(this.thirdMeteorSymbol.getImage(),
								l_mx - this.thirdMeteorSymbol.getIconWidth() / 2,
								l_my - this.thirdMeteorSymbol.getIconHeight() / 2, null);
						l_m = l_m.previous;
						if (l_m != null) {
							l_mx = (int)((l_m.xy.x + i_sx) * i_rat + 0.5);
							l_my = (int)((l_m.xy.y + i_sy) * i_rat + 0.5);
							i_hdc.drawImage(this.fourthMeteorSymbol.getImage(),
									l_mx - this.fourthMeteorSymbol.getIconWidth() / 2,
									l_my - this.fourthMeteorSymbol.getIconHeight() / 2, null);
							l_m = l_m.previous;
							if (l_m != null) {
								l_mx = (int)((l_m.xy.x + i_sx) * i_rat + 0.5);
								l_my = (int)((l_m.xy.y + i_sy) * i_rat + 0.5);
								i_hdc.drawImage(this.fifthMeteorSymbol.getImage(),
										l_mx - this.fifthMeteorSymbol.getIconWidth() / 2,
										l_my - this.fifthMeteorSymbol.getIconHeight() / 2, null);
								l_m = l_m.previous;
							}
						}
					}
				}
			}
		}
		i_hdc.setFont (Resources.g_arial);
		i_hdc.setStroke(Resources.g_hPenLabel);
		for (FlightLabel l_fl : this.labels) {
			if (l_fl.stateVector != null && l_fl.stateVector.time == l_time) {
				l_fl.draw(i_hdc, i_rat, i_sx, i_sy);
			}
		}
		if (this.selectedLabel != null) {
			if (this.selectedLabel.stateVector.time != l_time) {
				int x = (int)((this.selectedLabel.stateVector.xy.x + i_sx) * i_rat + 0.5);
				int y = (int)((this.selectedLabel.stateVector.xy.y + i_sy) * i_rat + 0.5);
				this.selectedLabel.area = new java.awt.geom.Area((new java.awt.geom.Line2D.Double (x, y, this.selectedLabel.dx + this.selectedLabel.width, this.selectedLabel.dy + this.selectedLabel.height)).getBounds());
				this.selectedLabel.area.subtract(new java.awt.geom.Area(new java.awt.Rectangle (x-5, y-5, 10, 10)));
				this.selectedLabel.area.subtract(new java.awt.geom.Area(new java.awt.Rectangle (this.selectedLabel.dx, this.selectedLabel.dy, this.selectedLabel.width * 2, this.selectedLabel.height * 2)));
			}
			selectedLabel.draw(i_hdc, i_rat, i_sx, i_sy);
		}
	}

	/**
	 * Select the label if a label is bellow the cursor.
	 * @param x
	 * @param y
	 */
	public void hotspot (int x, int y) {
		for (Iterator<FlightLabel> l_it = this.labels.descendingIterator() ; l_it.hasNext() ; ) {
			FlightLabel l_l = l_it.next();
			if (l_l.contains (x, y)) {
				selectedLabel = l_l;
				l_it.remove();
				return;
			}
		}
	}

	/**
	 * Read the data to construct all the member variables.
	 * @param i_projection projection
	 * @param labels       all the labels
	 * @param timeTicks    all the time ticks
	 * @param controllers  all the sector names
	 */
	public FlightData (Projection i_projection,
				java.util.LinkedList<FlightLabel> labels,
				java.util.Vector<TimeTick> timeTicks,
				String[] controllers) {
		this.firstDraw = true;
		this.flightSymbol = Resources.createImageIcon("flight.gif");
		this.firstMeteorSymbol = Resources.createImageIcon("meteor.gif");
		this.secondMeteorSymbol = this.thirdMeteorSymbol = this.fourthMeteorSymbol = this.fifthMeteorSymbol
				= Resources.createImageIcon("meteor2.gif");;
		this.selectedLabel = null;
		this.labels = labels;
		this.timeTicks = timeTicks;
		this.curTimeTick = 0;
		this.controllers = controllers;
		System.out.println("Entering FlightData");
		System.out.println("projection " + i_projection.getCenter());
		System.out.println("nb timeTicks " + timeTicks.size());
		System.out.println("nb controllers " + controllers.length);
		System.out.println("nb labels " + labels.size());
		System.out.println("Leaving FlightData");
	}



}

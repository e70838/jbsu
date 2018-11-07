/*
 * @(#)StateVector.java
 * 
 */

package tk.bocquet.bsu.viewer.records;

import tk.bocquet.bsu.data.FlightLabel;
import tk.bocquet.bsu.geometry.CoordNm;

/**
 * 
 * @author Jean-François Bocquet
 */
public class StateVector implements java.lang.Comparable<StateVector> {
	/**
	 * Projected coordinates of the aircraft position
	 */
	public CoordNm xy;

	/**
	 * Projected coordinate of where the aircraft will be in 90s 
	 */
	public CoordNm speedVectorXY;

	/**
	 * @return the xy
	 */
	public CoordNm getXy() {
		return xy;
	}

	/**
	 * @param xy the xy to set
	 */
	public void setXy(CoordNm xy) {
		this.xy = xy;
	}

	/**
	 * @return the speedVectorXY
	 */
	public CoordNm getSpeedVectorXY() {
		return speedVectorXY;
	}

	/**
	 * @param speedVectorXY the speedVectorXY to set
	 */
	public void setSpeedVectorXY(CoordNm speedVectorXY) {
		this.speedVectorXY = speedVectorXY;
	}

	/**
	 * @return the time
	 */
	public int getTime() {
		return time;
	}

	/**
	 * @param time the time to set
	 */
	public void setTime(int time) {
		this.time = time;
	}

	/**
	 * @return the altitude
	 */
	public int getAltitude() {
		return altitude;
	}

	/**
	 * @param altitude the altitude to set
	 */
	public void setAltitude(int altitude) {
		this.altitude = altitude;
	}

	/**
	 * @return the track
	 */
	public int getTrack() {
		return track;
	}

	/**
	 * @param track the track to set
	 */
	public void setTrack(int track) {
		this.track = track;
	}

	/**
	 * @return the groundSpeed
	 */
	public int getGroundSpeed() {
		return groundSpeed;
	}

	/**
	 * @param groundSpeed the groundSpeed to set
	 */
	public void setGroundSpeed(int groundSpeed) {
		this.groundSpeed = groundSpeed;
	}

	/**
	 * @return the ssr
	 */
	public String getSsr() {
		return ssr;
	}

	/**
	 * @param ssr the ssr to set
	 */
	public void setSsr(String ssr) {
		this.ssr = ssr;
	}

	/**
	 * @return the rocd
	 */
	public char getAttitude() {
		return attitude;
	}

	/**
	 * @param attitude the rocd to set
	 */
	public void setAttitude(char attitude) {
		this.attitude = attitude;
	}

	/**
	 * @return the previous
	 */
	public StateVector getPrevious() {
		return previous;
	}

	/**
	 * @param previous the previous to set
	 */
	public void setPrevious(StateVector previous) {
		this.previous = previous;
	}

	/**
	 * Simulation time of these data
	 */
	public int time;

	/**
	 * altitude in 10 feet.
	 */
	public int altitude;

	/**
	 *  Track in 1/60 degree
	 */
	public int track;

	/**
	 * Ground speed in 1/10 knot
	 */
	public int groundSpeed;

	/**
	 * SSR code as a String of four octal digits (0-7) 
	 */
	public String ssr;

	/**
	 * Attitude letter : '_', '-' or '^'
	 */
	public char attitude;

	/**
	 * Rate of climb or descend
	 */
	public short rocd;

	/**
	 * Link to previous position. Useful for meteors
	 */
	public StateVector previous;

	/**
	 * The label contains invariables data (callsign, id)
	 */
	public FlightLabel label;

	/**
	 * The starship operator is missing in Java
	 * @param l left hand side parameter
	 * @param r right hand side parameter
	 * @return -1 when l<r, 0 when l==r, 1 when l>r
	 */
	private static int cmp (int l, int r) {
		return l == r ? 0 : l < r ? -1 : 1;
	}
	
	/**
	 * Full order comparator. Useful for binary search in sorted array of state vectors
	 */
	public int compareTo (StateVector o) {
		return (o.time == time) ? cmp (label.id, o.label.id) : cmp (time, o.time);
	}
}

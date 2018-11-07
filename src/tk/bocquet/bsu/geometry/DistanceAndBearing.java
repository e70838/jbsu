/*
 * @(#)DistanceAndBearing.java
 * 
 */

package tk.bocquet.bsu.geometry;

/**
 * A DistanceAndBearing object represent the distance and the bearing of a location toward another location.
 *
 * @author Jean-François Bocquet
 *
 */
public class DistanceAndBearing {
	/**
	 * Distance in Nm
	 */
	public double distance;
	
	/**
	 * @return the distance
	 */
	public double getDistance() {
		return distance;
	}

	/**
	 * @param distance the distance to set
	 */
	public void setDistance(double distance) {
		this.distance = distance;
	}

	/**
	 * @return the bearing
	 */
	public double getBearing() {
		return bearing;
	}

	/**
	 * @param bearing the bearing to set
	 */
	public void setBearing(double bearing) {
		this.bearing = bearing;
	}

	/**
	 * Bearing in degree
	 */
	public double bearing;
}

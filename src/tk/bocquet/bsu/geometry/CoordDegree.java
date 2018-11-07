/*
 * @(#)CoorDegree.java
 * 
 */

package tk.bocquet.bsu.geometry;

/**
 * A CoordDegree object represents a geographic location in (latitude, longitude), specified in degrees with double precision.
 * 
 * @author Jean-François Bocquet
 */
public class CoordDegree {
	/**
	 * Position of Paris
	 */
	public static final CoordDegree c_Paris = new CoordDegree
		(ParseLatLong.parseLatitude("4852N"), ParseLatLong.parseLongitude("00219E"));

	/**
	 * latitude in degrees. Northern latitudes are positive.
	 */
	public double latitude;
	
	/**
	 * @return the latitude
	 */
	public double getLatitude() {
		return latitude;
	}

	/**
	 * @param latitude the latitude to set
	 */
	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}

	/**
	 * @return the longitude
	 */
	public double getLongitude() {
		return longitude;
	}

	/**
	 * @param longitude the longitude to set
	 */
	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}

	/**
	 * longitude in degrees. Eastern longitude are positive.
	 */
	public double longitude;

	/**
	 * Default constructor which creates the location (0, 0).
	 * This is the intersection of Greenwich meridian and of equator.
	 */
	public CoordDegree () {
		this.latitude = this.longitude = 0.0;
	}
	
	public String toString () {
		return Double.toString(this.latitude) + " - " + Double.toString(this.longitude);
	}
	
	/**
	 * Creates a geographical location with coordinates expressed in degrees.
	 * 
	 * @param latitude latitude in degrees, north is positive
	 * @param longitude longitude in degrees, east is positive
	 */
	public CoordDegree (double latitude, double longitude) {
		this.latitude = latitude;
		this.longitude = longitude;
	}
	
	/**
	 * Copy constructor of a CoordDegree
	 * 
	 * @param coordDegree geographical location to clone
	 */
	public CoordDegree (CoordDegree coordDegree) {
		this.latitude = coordDegree.latitude;
		this.longitude = coordDegree.longitude;
	}

	/**
	 * Translate the point using a distance and a bearing.
	 * @param distance distance
	 * @param bearing  bearing
	 */
	public void translate (double distance, double bearing) {
		double sinDistance = java.lang.Math.sin (Radian.deg2rad(distance / 60));
		double bearingRad = Radian.deg2rad(bearing);
		this.latitude += Radian.rad2deg(java.lang.Math.asin (java.lang.Math.cos(bearingRad) * sinDistance));
		this.longitude += Radian.rad2deg(java.lang.Math.asin (java.lang.Math.sin(bearingRad) * sinDistance) / java.lang.Math.cos (Radian.deg2rad (this.latitude)));
	}
	
	/** 
	 * Compute the distance and the bearing from one geographical point to another one
	 * @param destination destination point
	 * @param result      will receive the computed distance and bearing
	 */
	public void distanceBearing (CoordDegree destination, DistanceAndBearing result) {
		double l_lat1 = Radian.deg2rad(this.latitude);
		double l_long1 = Radian.deg2rad(this.longitude);
		double l_lat2 = Radian.deg2rad(destination.latitude);
		double l_long2 = Radian.deg2rad(destination.longitude);
		result.distance = java.lang.Math.acos (java.lang.Math.sin(l_lat1)*java.lang.Math.sin(l_lat2) + java.lang.Math.cos(l_lat1)*java.lang.Math.cos(l_lat2)*java.lang.Math.cos(l_long1-l_long2));
		result.bearing = Radian.rad2deg(java.lang.Math.acos (java.lang.Math.sin(l_lat2 - l_lat1) / java.lang.Math.sin(result.distance)));
		if (l_long2 < l_long1) result.bearing = 360.0 - result.bearing;
		result.distance = 60.0 * Radian.rad2deg(result.distance);
		if (result.distance < 0.001) result.bearing = 0.0;
	}

	/** 
	 * Compute the distance and the bearing from one geographical point to another one
	 * @param destination destination point
	 * @return distance and bearing
	 */
	public DistanceAndBearing distanceBearing (CoordDegree destination) {
		DistanceAndBearing result = new DistanceAndBearing();
		this.distanceBearing (destination, result);
		return result;
	}
}

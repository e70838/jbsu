/*
 * @(#)Projection.java
 * 
 */

package tk.bocquet.bsu.geometry;

/**
 * A Projection object provides services to convert a geographical
 * location (CoordDegree) to a screen location (CoordNm) and vice versa.
 * 
 * @author Jean-François Bocquet
 *
 */
public class Projection {
	/**
	 * Earth radius expressed in Nm
	 */
	private static final double earthRadius = 3437.746770785;
	
	/**
	 * Projection center stored in radians (instead of degrees).
	 */
	private double latitudeRadians, longitudeRadians;
	
	/**
	 * sin and cos of projection center latitude
	 */
	private double sinusLatitude, cosinusLatitude;

	/**
	 * Default constructor creates a projection centered on Paris
	 */
	public Projection () {
		setCenter (CoordDegree.c_Paris);
	}
	
	/**
	 * Constructor.
	 * @param i_center
	 */
	public Projection (CoordDegree i_center) {
		setCenter (i_center);
	}
	
	/**
	 * Internal procedure called by both constructors to initialise member variables.
	 * @param i_center
	 */
	public void setCenter (CoordDegree i_center) {
		this.latitudeRadians  = Radian.deg2rad (i_center.latitude);
		this.sinusLatitude    = java.lang.Math.sin (this.latitudeRadians);
		this.cosinusLatitude  = java.lang.Math.cos (this.latitudeRadians);
		this.longitudeRadians = Radian.deg2rad (i_center.longitude);
	}
	
	public CoordDegree getCenter () {
		return new CoordDegree(Radian.rad2deg(this.latitudeRadians), Radian.rad2deg(this.longitudeRadians));
	}
	
	/**
	 * Compute the projection of a point.
	 * @param i_point input parameter : geographical point
	 * @param o_feet  result : screen coordinate in Nm
	 */
	public void geo2stereo (CoordDegree i_point, CoordNm o_feet) {
		double l_lat = Radian.deg2rad(i_point.latitude);
		double l_lon = Radian.deg2rad(i_point.longitude);
		double slat = java.lang.Math.sin(l_lat);
		double clat = java.lang.Math.cos (l_lat);
		double cdlon = java.lang.Math.cos (l_lon - longitudeRadians);
		double ratio = 2.0 * earthRadius / (1.0 + slat * sinusLatitude + clat * cosinusLatitude * cdlon);
		o_feet.x = ratio * clat * java.lang.Math.sin (l_lon - longitudeRadians);
		o_feet.y = -ratio * (cosinusLatitude * slat - clat * sinusLatitude * cdlon);
	}

	/**
	 * Convert a screen coordinate in Nm to a geographical point.
	 * @param i_feet  screen coordinate in Nm
	 * @param o_point geographical point
	 */
	public void stereo2geo (CoordNm i_feet, CoordDegree o_point){
		double l_lat = (-i_feet.y * earthRadius * cosinusLatitude + 2 * earthRadius * earthRadius * sinusLatitude)
		/ (i_feet.x * i_feet.x + i_feet.y * i_feet.y + 4 * earthRadius * earthRadius);
		l_lat = java.lang.Math.asin(4.0 * l_lat - sinusLatitude);
		double slat = java.lang.Math.sin(l_lat);
		o_point.latitude = Radian.rad2deg(l_lat);
		o_point.longitude = Radian.rad2deg(longitudeRadians  + java.lang.Math.atan(i_feet.x * (slat + sinusLatitude) / (slat * (2 * earthRadius * cosinusLatitude + i_feet.y * sinusLatitude) + i_feet.y)));
	}

}

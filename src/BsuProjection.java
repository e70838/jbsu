
public class BsuProjection {

	/**
	 * Earth radius expressed in Nm
	 */
	private static final double c_rayonterre = 3437.746770785;
	
	/**
	 * Projection center stored in radians (instead of degrees).
	 */
	private double m_lat0, m_lon0;
	
	/**
	 * sin and cos of projection center latitude
	 */
	private double m_slat0, m_clat0;

	/**
	 * Default constructor creates a projection centred on Paris
	 */
	public BsuProjection () {setCenter (CoordDegree.c_Paris);}
	
	/**
	 * Constructor.
	 * @param i_center
	 */
	public BsuProjection (CoordDegree i_center) {
		setCenter (i_center);
	}
	
	/**
	 * Internal procedure called by both constructors to initialise member variables.
	 * @param i_center
	 */
	private void setCenter (CoordDegree i_center) {
		m_lat0 = BsuUtil.deg2rad (i_center.m_lat);
		m_slat0 = java.lang.Math.sin(m_lat0);
		m_clat0 = java.lang.Math.cos(m_lat0);
		m_lon0 = BsuUtil.deg2rad (i_center.m_long);
	}
	
	/**
	 * Compute the projection of a point.
	 * @param i_point input parameter : geographical point
	 * @param o_feet  result : screen coordinate in Nm
	 */
	public void geo2stereo (CoordDegree i_point, CoordNm o_feet) {
		double l_lat = BsuUtil.deg2rad(i_point.m_lat);
		double l_lon = BsuUtil.deg2rad(i_point.m_long);
		double slat = java.lang.Math.sin(l_lat);
		double clat = java.lang.Math.cos (l_lat);
		double cdlon = java.lang.Math.cos (l_lon - m_lon0);
		double ratio = 2.0 * c_rayonterre / (1.0 + slat * m_slat0 + clat * m_clat0 * cdlon);
		o_feet.m_x = ratio * clat * java.lang.Math.sin (l_lon - m_lon0);
		o_feet.m_y = -ratio * (m_clat0 * slat - clat * m_slat0 * cdlon);
	}

	/**
	 * Convert a screen coordinate in Nm to a geographical point.
	 * @param i_feet  screen coordinate in Nm
	 * @param o_point geographical point
	 */
	public void stereo2geo (CoordNm i_feet, CoordDegree o_point){
		double l_lat = (-i_feet.m_y * c_rayonterre * m_clat0 + 2 * c_rayonterre * c_rayonterre * m_slat0)
		/ (i_feet.m_x * i_feet.m_x + i_feet.m_y * i_feet.m_y + 4 * c_rayonterre * c_rayonterre);
		l_lat = java.lang.Math.asin(4.0 * l_lat - m_slat0);
		double slat = java.lang.Math.sin(l_lat);
		o_point.m_lat = BsuUtil.rad2deg(l_lat);
		o_point.m_long = BsuUtil.rad2deg(m_lon0  + java.lang.Math.atan(i_feet.m_x * (slat + m_slat0) / (slat * (2 * c_rayonterre * m_clat0 + i_feet.m_y * m_slat0) + i_feet.m_y)));
	}

	/**
	 * Compute the distance and the bearing from one geographical point to another one
	 * @param i_point1 origin point
	 * @param i_point2 destination point
	 * @param o_dab    result
	 */
	static void distanceBearing (CoordDegree i_point1, CoordDegree i_point2, DistanceAndBearing o_dab) {
		double l_lat1 = BsuUtil.deg2rad(i_point1.m_lat);
		double l_long1 = BsuUtil.deg2rad(i_point1.m_long);
		double l_lat2 = BsuUtil.deg2rad(i_point2.m_lat);
		double l_long2 = BsuUtil.deg2rad(i_point2.m_long);
		o_dab.m_distance = java.lang.Math.acos (java.lang.Math.sin(l_lat1)*java.lang.Math.sin(l_lat2) + java.lang.Math.cos(l_lat1)*java.lang.Math.cos(l_lat2)*java.lang.Math.cos(l_long1-l_long2));
		o_dab.m_bearing = BsuUtil.rad2deg(java.lang.Math.acos (java.lang.Math.sin(l_lat2 - l_lat1) / java.lang.Math.sin(o_dab.m_distance)));
		if (l_long2 < l_long1) o_dab.m_bearing = 360.0 - o_dab.m_bearing;
		o_dab.m_distance = 60.0 * BsuUtil.rad2deg(o_dab.m_distance);
		if (o_dab.m_distance < 0.001) o_dab.m_bearing = 0.0;
	}

	/**
	 * Compute a new geographical point based on an origin point, a distance and a bearing.
	 * @param io_point   origin point, will hold the result of the computation
	 * @param i_distance distance
	 * @param i_track    bearing
	 */
	static void translate (CoordDegree io_point, double i_distance, double i_track) {
		double l_sdistance = java.lang.Math.sin (BsuUtil.deg2rad(i_distance / 60));
		double l_track = BsuUtil.deg2rad(i_track);
		io_point.m_lat += BsuUtil.rad2deg(java.lang.Math.asin (java.lang.Math.cos(l_track) * l_sdistance));
		io_point.m_long += BsuUtil.rad2deg(java.lang.Math.asin (java.lang.Math.sin(l_track) * l_sdistance) / java.lang.Math.cos (BsuUtil.deg2rad (io_point.m_lat)));
	}
}

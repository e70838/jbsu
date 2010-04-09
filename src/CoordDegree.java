
public class CoordDegree {
	double m_lat, m_long;
	CoordDegree () {
		m_lat = m_long = 0.0;
	}
	CoordDegree (double i_lat, double i_long) {
		m_lat = i_lat;
		m_long = i_long;
	}
	public static final CoordDegree c_Paris = new CoordDegree (BsuUtil.string_to_lat ("6012N"),
			BsuUtil.string_to_long("01105E"));
}

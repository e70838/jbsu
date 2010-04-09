/**
 * 
 */

/**
 * @author Jean-Francois Bocquet
 *
 */
public class StateVector implements java.lang.Comparable<StateVector> {
	public CoordNm m_xy;
	public CoordNm m_XY;
	public int m_time;
	public int m_id;
	public int m_altitude;    // dizaine de pieds
	public int m_track;       // minute d'angle
	public int m_groundSpeed; // dizi'eme de knots
	public String m_ssr;
	public char m_rocd;
	//short m_rocd;
	public StateVector m_prev;
	public StateVector m_next;
	public StateVector m_target;
	public FlightLabel m_label;
	public boolean m_instructed;
	
	private static int cmp (int l, int r) {
		return l == r ? 0 : l < r ? -1 : 1;
	}
	
	public int compareTo (StateVector o) {
		return (o.m_time == m_time) ? cmp (m_id, o.m_id) : cmp (m_time, o.m_time);
	}
}

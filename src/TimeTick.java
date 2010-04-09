/**
 * @author Jean-Francois Bocquet
 *
 */
public class TimeTick {
	/**
	 * Simulation time expressed in seconds since the midnight
	 */
	public int m_time;
	
	/**
	 * The state vectors that were send for this time tick
	 */
	public StateVector[] m_fd;
	
	/**
	 * The subtitles that occured at this time tick
	 */
	public Subtitle m_sub;
	
	/**
	 * Constructor
	 * @param i_time simulation time
	 * @param i_svs vector of state vectors. Will be sorted before converted to array
	 */
	public TimeTick (int i_time, java.util.Vector<StateVector> i_svs) {
		m_time = i_time;
		m_fd = i_svs.toArray(new StateVector[0]);
		m_sub = null;
		java.util.Arrays.sort(m_fd);
	}
	
	/**
	 * Find the state vector for a given callsign. Returns null if not found.
	 * @param i_callsign
	 * @return
	 */
	public StateVector findFlight (String i_callsign) {
		for (StateVector sv : m_fd) {
			if (sv.m_label.m_calls.compareTo(i_callsign) == 0)
				return sv;
		}
		return null;
	}
}

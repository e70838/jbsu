package tk.bocquet.bsu.data;


import tk.bocquet.bsu.viewer.records.StateVector;
import tk.bocquet.bsu.viewer.records.Subtitle;


/**
 * @author Jean-François Bocquet
 *
 */
public class TimeTick {
	/**
	 * Simulation time expressed in seconds since the midnight
	 */
	public int time;
	
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
	 * @return the stateVectors
	 */
	public StateVector[] getStateVectors() {
		return stateVectors;
	}

	/**
	 * @param stateVectors the stateVectors to set
	 */
	public void setStateVectors(StateVector[] stateVectors) {
		this.stateVectors = stateVectors;
	}

	/**
	 * @return the subtitle
	 */
	public Subtitle getSubtitle() {
		return subtitle;
	}

	/**
	 * @param subtitle the subtitle to set
	 */
	public void setSubtitle(Subtitle subtitle) {
		this.subtitle = subtitle;
	}

	/**
	 * The state vectors that were send for this time tick
	 */
	public StateVector[] stateVectors;
	
	/**
	 * The subtitles that occured at this time tick
	 */
	public Subtitle subtitle;
	
	/**
	 * Constructor
	 * @param i_time simulation time
	 * @param i_svs vector of state vectors. Will be sorted before converted to array
	 */
	public TimeTick (int i_time, java.util.Vector<StateVector> i_svs) {
		time = i_time;
		stateVectors = i_svs.toArray(new StateVector[0]);
		subtitle = null;
		java.util.Arrays.sort(stateVectors);
	}
	
	public TimeTick () { }
	
	/**
	 * Find the state vector for a given callsign. Returns null if not found.
	 * @param i_callsign
	 * @return
	 */
	public StateVector findFlight (String i_callsign) {
		for (StateVector sv : stateVectors) {
			if (sv.label.callsign.compareTo(i_callsign) == 0)
				return sv;
		}
		return null;
	}
}

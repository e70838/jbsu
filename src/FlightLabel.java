
public class FlightLabel {
    public String m_calls;
    public char m_wake; // L M or H
    public double m_x, m_y; // en Nm
    public int m_dx, m_dy; // en pixels
    public int m_width, m_height;
    public FlightLabel m_prev;
    public FlightLabel m_next;
    public FlightData m_fd;
    static public FlightLabel g_currentSelectedLabel, g_currentHighlightedLabel = null;
    static public FlightLabel g_firstLabel, g_lastLabel;
    public void move(int i_angle) {
	
    }

	public void raiseLabel () {
		//  if the label is not yet the last of the list
		if (m_next != null) {
			m_next.m_prev = m_prev;
			if (m_prev != null)
				m_prev.m_next = m_next;
			m_prev = FlightLabel.g_lastLabel;
			m_next = null;
			FlightLabel.g_lastLabel.m_next = this;
			FlightLabel.g_lastLabel = this;
		}
	}

	public void setCurrentHighlightedLabel () {
		raiseLabel ();
		FlightLabel.g_currentHighlightedLabel = this;
	}

}

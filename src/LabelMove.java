public class LabelMove {
	static final int c_nbMaxMove = 4000;
	static int[] m_start = new int[c_nbMaxMove];
	static int[] m_angle = new int[c_nbMaxMove];
	static FlightLabel[] m_label = new FlightLabel[c_nbMaxMove];
	static int[] m_prev = new int[c_nbMaxMove];
	static int m_nbMoves = 0;
	static int m_curTime = -1;

	public static void add (FlightLabel i_label, int i_start, int i_angle) {
		if (m_nbMoves == c_nbMaxMove) return;
		// ajouter un test pour v'erifier que les ajouts se font dans l'ordre et que l'heure est multiple de 5
		m_start[m_nbMoves]  = i_start/5;
		m_angle[m_nbMoves]  = i_angle;
		m_label[m_nbMoves]  = i_label;
		m_prev[m_nbMoves]   = 0;
		for (int l_i = 0; l_i < m_nbMoves; l_i++) {
			if (m_label[l_i] == i_label) m_prev[m_nbMoves] = l_i;
		}
		m_nbMoves++;
	}
	public static boolean setTime (int i_timeTick) {
		if (i_timeTick == m_start[m_curTime]) return false;
		if ((m_curTime == -1) || (i_timeTick > m_start[m_curTime])) {
			while ((m_curTime + 1 < m_nbMoves) && (i_timeTick >= m_start[m_curTime + 1])) {
				m_curTime++;
				m_label[m_curTime].move(m_angle[m_curTime]);
			}
		} else {
			while ((m_curTime >= 0) && (i_timeTick < m_start[m_curTime])) {
				if (m_prev[m_curTime] != 0)
					m_label[m_prev[m_curTime]].move(m_angle[m_prev[m_curTime]]);
					m_curTime--;
			}
		}
		return true;

	}

}

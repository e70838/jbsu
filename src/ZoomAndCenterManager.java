public class ZoomAndCenterManager {
	static final int c_nbMaxZoom = 10;
	static int[] m_uTime = new int[c_nbMaxZoom];
	static int[] m_zoomPos = new int[c_nbMaxZoom];
	static int[] m_scrollX = new int[c_nbMaxZoom];
	static int[] m_scrollY = new int[c_nbMaxZoom];
	static int m_nbZooms = 0;
	static int m_curTime = 0;
	static ZoomAndCenter m_zoom;

	static void init (ZoomAndCenter io_zoom) {
		m_zoom = io_zoom;
	}
	public static void add (int i_start, int i_end, int i_zoomStart, int i_zoomEnd, int i_xStart, int i_xEnd, int i_yStart, int i_yEnd) {
		if (m_nbZooms >= c_nbMaxZoom) return;
		m_uTime[m_nbZooms]     = i_start;
		m_zoomPos[m_nbZooms]   = i_zoomStart;
		m_scrollX[m_nbZooms]   = i_xStart;
		m_scrollY[m_nbZooms++] = i_yStart;
		m_uTime[m_nbZooms]     = i_end;
		m_zoomPos[m_nbZooms]   = i_zoomEnd;
		m_scrollX[m_nbZooms]   = i_xEnd;
		m_scrollY[m_nbZooms++] = i_yEnd;
	}
	public static boolean setTime (int i_uSecond) {
		boolean l_changed = false;
		if (m_curTime == 0) {
			m_zoom.m_zoomPos = m_zoomPos[0];
			m_zoom.m_scrollX = m_scrollX[0];
			m_zoom.m_scrollY = m_scrollY[0];
			l_changed = true;
		}
		if (i_uSecond != m_curTime) {
			for (int l_i = 0; l_i < m_nbZooms; l_i += 2) {
				if ((i_uSecond >= m_uTime[l_i]) && (i_uSecond <= m_uTime[l_i+1])) {
					int l_to = (i_uSecond > m_curTime) ? l_i+1 : l_i;
					int l_steps;
					if (m_uTime[l_to] >= i_uSecond)
						l_steps = 1 + m_uTime[l_to] - i_uSecond;
					else
						l_steps = 1 + i_uSecond - m_uTime[l_to];
					if (m_zoomPos[l_to] > m_zoom.m_zoomPos)
						m_zoom.m_zoomPos += (m_zoomPos[l_to] - m_zoom.m_zoomPos) / l_steps;
					else
						m_zoom.m_zoomPos -= (m_zoom.m_zoomPos - m_zoomPos[l_to]) / l_steps;
					m_zoom.m_scrollX += (m_scrollX[l_to] - m_zoom.m_scrollX) / l_steps;
					m_zoom.m_scrollY += (m_scrollY[l_to] - m_zoom.m_scrollY) / l_steps;
					l_changed = true;
				} else if ((i_uSecond >= m_uTime[l_i+1]) && (m_curTime <= m_uTime[l_i+1])) {
					m_zoom.m_zoomPos = m_zoomPos[l_i+1];
					m_zoom.m_scrollX = m_scrollX[l_i+1];
					m_zoom.m_scrollY = m_scrollY[l_i+1];
					l_changed = true;
				} else if ((i_uSecond <= m_uTime[l_i]) && (m_curTime >= m_uTime[l_i])) {
					m_zoom.m_zoomPos = m_zoomPos[l_i];
					m_zoom.m_scrollX = m_scrollX[l_i];
					m_zoom.m_scrollY = m_scrollY[l_i];
					l_changed = true;
				}
			}
		}
		if (l_changed) {
			System.out.println ("New zoom & scroll (" + m_curTime + "->" + i_uSecond + ") -> " +
					m_zoom.m_zoomPos + " " + m_zoom.m_scrollX + " " + m_zoom.m_scrollY);
		}
		m_curTime = i_uSecond;
		return l_changed;
	}
}

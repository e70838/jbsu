
public class FlightData {
	static final java.text.DecimalFormat c_format = new java.text.DecimalFormat ("00");
	static final int c_maxNbLabel = 1000;

	TimeTick[] m_tt;
	public int getNbTimeTick () {return m_tt.length;}

	String[] m_controlers;

	FlightLabel[] m_allLabel;
	int m_maxId;

	int m_curTimeTick = 0;

	public String[] getControlers () {
		return m_controlers;
	}

	static String formatTime (int i_time) {
		int l_s = i_time % 60;
		i_time = (i_time - l_s)/60;
		int l_m = i_time % 60;
		int l_h = (i_time - l_m)/60;
		return c_format.format(l_h) + ":" + c_format.format(l_m) + ":" + c_format.format(l_s);
	}

	/**
	 * Find the TimeTick associated to a given simulation time
	 * @param i_tts  the vector of TimeTick
	 * @param i_time the searched simulation time
	 * @return the TimeTick
	 */
	static TimeTick findTime (java.util.Vector<TimeTick> i_tts, int i_time) {
		int l_low = 0;
		int l_high = i_tts.size() - 1;
		int l_mid;
		while (l_low <= l_high) {
			l_mid = (l_low + l_high) / 2;
			if (i_tts.elementAt(l_mid).m_time < i_time)
				l_low = l_mid + 1;
			else if (i_tts.elementAt(l_mid).m_time > i_time)
				l_high = l_mid - 1;
			else
				return i_tts.elementAt(l_mid);
		}
		throw new Error ("Time tick " + i_time + " not found in FindTime");
	}

	public int getFirstTime () {
		return m_tt[0].m_time;
	}
	
	public int getLastTime () {
		return m_tt[m_tt.length - 1].m_time;
	}
	
	public int getCurTime () {
		return m_tt[m_curTimeTick].m_time;
	}

	public String setCurTimeTick (int i_curTimeTick) {
		m_curTimeTick = i_curTimeTick;
		return formatTime (m_tt[m_curTimeTick].m_time);
	}

	public StateVector[] currentFlightData () {
		return m_tt[m_curTimeTick].m_fd;
	}

	public Subtitle currentSubtitle () {
		return m_tt[m_curTimeTick].m_sub;
	}

	public FlightData (BsuProjection i_projection) {
		try {
			long l_t0 = java.lang.System.currentTimeMillis();
			java.io.FileInputStream l_in_unbuf = new java.io.FileInputStream(BSU.g_dataDir + "state_vector.bin");
			int l_max = l_in_unbuf.available();
			java.io.BufferedInputStream l_in = new java.io.BufferedInputStream(l_in_unbuf);
			if (l_max == 0 || l_max % 34 != 0) {
				String l_buf = "Bad size for state_vector.bin : " + l_max + " is not >0 and multiple of 34";
				javax.swing.JOptionPane.showMessageDialog(null, l_buf, "Assertion Error", javax.swing.JOptionPane.OK_OPTION);
				throw new Error ("Wrong size for state_vector.bin");
			}
			int l_nbRecord = l_max / 34;
			System.out.println ("Size " + l_max + " -> nb record " + l_nbRecord);

			java.util.Vector<String> l_controlers = new java.util.Vector<String>();
			java.util.Vector<TimeTick> l_tts = new java.util.Vector<TimeTick>();
			java.util.Vector<StateVector> l_svs = new java.util.Vector<StateVector>();
			m_allLabel = new FlightLabel[c_maxNbLabel];
			for (int l_i = 0; l_i < c_maxNbLabel; l_i++) {
				m_allLabel[l_i] = new FlightLabel();
				m_allLabel[l_i].m_x = m_allLabel[l_i].m_y = 10.0;
				m_allLabel[l_i].m_prev = l_i == 0 ? null : m_allLabel[l_i - 1];
				m_allLabel[l_i].m_next = (l_i < c_maxNbLabel-1) ? m_allLabel[l_i + 1] : null;
				m_allLabel[l_i].m_fd = null;
			}
			FlightLabel.g_firstLabel = m_allLabel[0];
			m_maxId = 0;

			long l_t1 = java.lang.System.currentTimeMillis();
			int l_previousTime = 0;
			for (int l_i = 0; l_i < l_nbRecord; l_i++) {
				int l_time        = BsuUtil.readInt(l_in);
				if (l_i != 0) {
					if (l_time != l_previousTime) {
						l_tts.add(new TimeTick(l_previousTime, l_svs));
						l_svs.clear();
					}
				}
				l_previousTime = l_time;
				StateVector l_sv = new StateVector();
				l_svs.add (l_sv);
				l_sv.m_time = l_time;
				int l_srecord_m_lat     = BsuUtil.readInt(l_in);
				int l_srecord_m_long    = BsuUtil.readInt(l_in);
				l_sv.m_id          = BsuUtil.readShort(l_in);
				l_sv.m_label       = m_allLabel[l_sv.m_id];
				if (l_sv.m_id > m_maxId) m_maxId = l_sv.m_id;
				l_sv.m_altitude    = BsuUtil.readShort(l_in);
				l_sv.m_track       = BsuUtil.readShort(l_in);
				l_sv.m_groundSpeed = BsuUtil.readShort(l_in);
				short l_srecord_m_ssr   = BsuUtil.readShort(l_in);
				l_sv.m_ssr         = "" + ('0'+((l_srecord_m_ssr & 07000) >> 9))
				+ ('0'+((l_srecord_m_ssr & 0700) >> 6))
				+ ('0'+((l_srecord_m_ssr & 070) >> 3))
				+ ('0'+(l_srecord_m_ssr & 07));
				short l_srecord_m_rocd = BsuUtil.readShort(l_in);
				l_sv.m_rocd        = l_srecord_m_rocd == 0 ? '-' : l_srecord_m_rocd < 0 ? '_' : '^';
				l_sv.m_prev        = null;
				l_sv.m_next        = null;
				l_sv.m_target      = null;
				l_sv.m_instructed  = false;
				m_allLabel[l_sv.m_id].m_wake = BsuUtil.readChar(l_in);
				m_allLabel[l_sv.m_id].m_calls = BsuUtil.readString(l_in, 9);
				CoordDegree l_latlong = new CoordDegree (l_srecord_m_lat / 36000.0, l_srecord_m_long / 36000.0);
				l_sv.m_xy = new CoordNm();
				l_sv.m_XY = new CoordNm();
				i_projection.geo2stereo(l_latlong, l_sv.m_xy);
				BsuProjection.translate(l_latlong, l_sv.m_groundSpeed / (40.0 * 10.0), l_sv.m_track / 60.0);
				i_projection.geo2stereo(l_latlong, l_sv.m_XY);
			}
			l_tts.add(new TimeTick(l_previousTime, l_svs));
			m_tt = l_tts.toArray(new TimeTick[0]);

			FlightLabel.g_lastLabel = m_allLabel[m_maxId];
			FlightLabel.g_firstLabel.m_prev = FlightLabel.g_lastLabel.m_next = null;

			l_in.close();
			long l_t2 = java.lang.System.currentTimeMillis();

			// link flights to meteors
			for (int l_time = 1; l_time < l_tts.size(); l_time++) {
				StateVector[] l_prev = l_tts.elementAt(l_time - 1).m_fd;
				StateVector[] l_cur = l_tts.elementAt(l_time).m_fd;
				int l_iPrev = 0;
				int l_iCur = 0;
				while (l_iCur < l_cur.length) {
					while (l_iPrev < l_prev.length && l_prev[l_iPrev].m_id < l_cur[l_iCur].m_id)
						l_iPrev ++;
					if (l_iPrev >= l_prev.length) break;
					while (l_iCur < l_cur.length && l_cur[l_iCur].m_id < l_prev[l_iPrev].m_id)
						l_iCur ++;
					if (l_iCur >= l_cur.length) break;
					l_prev[l_iPrev].m_next = l_cur[l_iCur];
					l_cur[l_iCur].m_prev = l_prev[l_iPrev];
					l_iPrev++; l_iCur++;
				}
			}

			long l_t3 = java.lang.System.currentTimeMillis();
			l_in_unbuf = new java.io.FileInputStream(BSU.g_dataDir + "event.bin");
			l_in = new java.io.BufferedInputStream(l_in_unbuf);

			@SuppressWarnings("unused")
			int l_simulationStart = l_tts.firstElement().m_time;
			@SuppressWarnings("unused")
			int l_simulationEnd = l_tts.lastElement().m_time;
			long l_t4 = java.lang.System.currentTimeMillis();
			while (l_in.available() > 0) {
				int l_kind = BsuUtil.readInt(l_in);
				if (l_kind == 1) { // subtitle
					int l_start = BsuUtil.readInt(l_in);
					@SuppressWarnings("unused")
					int l_end   = BsuUtil.readInt(l_in);
					String l_calls = BsuUtil.readString(l_in);
					String l_subtitle = BsuUtil.readString(l_in);
					String l_next;
					do {
						l_next = BsuUtil.readString(l_in);
						l_subtitle += l_next;
					} while (l_next.length() > 0);
					String l_cat      = BsuUtil.readString(l_in);

					Subtitle l_s = new Subtitle();
					l_s.m_controler = -1;
					for (int l_i = 0; l_i < l_controlers.size(); l_i++) {
						if (l_controlers.elementAt(l_i).compareTo(l_cat) == 0)
							l_s.m_controler = l_i;
					}
					if (l_s.m_controler == -1) {
						l_controlers.add(l_cat);
						l_s.m_controler = l_controlers.size() - 1;
					}
					l_s.m_sentence = l_subtitle;
					TimeTick l_theTime = findTime(l_tts, l_start);
					l_s.m_next = l_theTime.m_sub;
					l_theTime.m_sub = l_s;
					System.out.println ("subtitle " + l_calls + " : " + l_subtitle + " : " + l_cat);
					//		printf ("DEBUG end e_subtitle\n");fflush(stdout);
				} else if (l_kind == 7) { // titlePage
					int l_start = BsuUtil.readInt(l_in);
					int l_end   = BsuUtil.readInt(l_in);
					int l_id    = BsuUtil.readInt(l_in);
					System.out.println ("titlePage " + l_start + " - " + l_end + " - " + l_id);
					//		TitlePageManager::add (l_start, l_end, l_id);
				} else if (l_kind == 8) { // labelMove
					int l_start = BsuUtil.readInt(l_in);
					int l_angle = BsuUtil.readInt(l_in);
					int l_id    = BsuUtil.readInt(l_in);
					LabelMove.add (m_allLabel[l_id], l_start, l_angle);
					//System.out.println ("labelMove " + l_start + " - " + l_angle + " - " + l_id);
				} else if (l_kind == 6) { // range
					@SuppressWarnings("unused")
					int l_simulationStartTODO = BsuUtil.readInt(l_in);
					l_simulationEnd   = BsuUtil.readInt(l_in);
				} else if (l_kind == 9) { // zoomAndCenter
					int l_start     = BsuUtil.readInt(l_in);
					int l_end       = BsuUtil.readInt(l_in);
					int l_zoomStart = BsuUtil.readInt(l_in);
					int l_zoomEnd   = BsuUtil.readInt(l_in);
					int    l_xStart    = BsuUtil.readInt(l_in);
					int    l_xEnd      = BsuUtil.readInt(l_in);
					int    l_yStart    = BsuUtil.readInt(l_in);
					int    l_yEnd      = BsuUtil.readInt(l_in);
					//ZoomAndCenterManager.add (l_start, l_end, l_zoomStart, l_zoomEnd, l_xStart, l_xEnd, l_yStart, l_yEnd);
				} else {
					int l_time = BsuUtil.readInt(l_in);
					String l_calls = BsuUtil.readString(l_in);
					TimeTick l_theTime = findTime(l_tts, l_time);
					StateVector l_subjectFD = l_theTime.findFlight(l_calls);
					switch (l_kind) {
					case 2 : // select
					{
						String l_target = BsuUtil.readString(l_in);
						StateVector l_targetFD = l_theTime.findFlight(l_target);
						while (l_subjectFD != null) {
							l_subjectFD.m_target = l_targetFD;
							l_subjectFD.m_instructed  = false;
							l_subjectFD = l_subjectFD.m_next;
							if (l_targetFD != null) l_targetFD = l_targetFD.m_next;
						};
					} break;
					case 3 : // deselect
						while (l_subjectFD != null) {
							l_subjectFD.m_target = null;
							l_subjectFD.m_instructed  = false;
							l_subjectFD = l_subjectFD.m_next;
						};
						break;
					case 4 : // endSpacing
						while (l_subjectFD != null) {
							l_subjectFD.m_target = null;
							l_subjectFD.m_instructed  = false;
							l_subjectFD = l_subjectFD.m_next;
						};
						break;
					case 5 : // instruct
						while (l_subjectFD != null) {
							l_subjectFD.m_instructed  = true;
							l_subjectFD = l_subjectFD.m_next;
						};
						break;
					}
				}
			}
			// TODO this block shall be converted later, after some refactoring
			//while (l_nbTime > 0 && l_fd[l_timeIndex[0]].m_time < l_simulationStart) {
			//	l_timeIndex++;
			//	l_nbTime--;
			//}
			//while (l_nbTime > 0 && l_fd[l_timeIndex[l_nbTime-1]].m_time > l_simulationEnd) {
			//	l_nbTime--;
			//}
			m_controlers = l_controlers.toArray(new String[0]);
			long l_t5 = java.lang.System.currentTimeMillis();
			System.out.println ("T1 " + (l_t1 - l_t0));
			System.out.println ("T2 " + (l_t2 - l_t1));
			System.out.println ("T3 " + (l_t3 - l_t2));
			System.out.println ("T4 " + (l_t4 - l_t3));
			System.out.println ("T5 " + (l_t5 - l_t4));
		}
		catch (java.io.IOException e) {
			System.err.println ("Couldn't read from state_vector.bin");
			throw new Error ("IOException when reading state_vector.bin");
		}
	}
}

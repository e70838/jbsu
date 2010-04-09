
public class BsuData {
	static final java.awt.Color[] g_colors = {
		new java.awt.Color(0, 0, 0),
		new java.awt.Color(0xff, 0xff, 0xff),
		new java.awt.Color(0x58, 0x50, 0x50),
		new java.awt.Color(0x60, 0x58, 0x58),
		new java.awt.Color(0x80, 0x78, 0x79),
		new java.awt.Color(0x73, 0x73, 0x73),
		new java.awt.Color(0xA6, 0xA6, 0xA6),
		new java.awt.Color(0x7a, 0x73, 0x73),
		new java.awt.Color(0xf4, 0xf2, 0xd6),
		new java.awt.Color(0xfa, 0xb4, 0x32),
		new java.awt.Color(0x00, 0xc8, 0x00),
		new java.awt.Color(0xff, 0xff, 0x00),
		new java.awt.Color (0x40, 0x40, 0x80)
	};
	static final int c_black = 0, c_white = 1, c_bgrdAll = 2, c_bgrdSelf = 3, c_sectorBorder = 4, c_beacon = 5, c_route = 6, c_ring = 7, c_flight = 8, c_orange = 9, c_green = 10, c_yellow = 11, c_airport = 12;
	java.awt.Color m_hBrushBgrdAll, m_hBrushBgrdSelf, m_hSubtitleBrush;
	static java.awt.Stroke m_hPenRoute, m_hPenRunway, m_hPenILS, m_hPenSectorBorder, m_hPenBeacon, m_hPenLabel, m_hPenOrange, m_hPenGreen, m_hPenFlight, m_hPenLabel2;
	static java.awt.Color m_hColorRoute, m_hColorRunway, m_hColorILS, m_hColorSectorBorder, m_hColorBeacon, m_hColorLabel, m_hColorOrange, m_hColorGreen, m_hColorFlight, m_hColorLabel2;
	static java.awt.Font m_times, m_arial, m_wingdings, m_symbol, m_subfont;

	public void DrawAll (java.awt.Graphics2D i_hdc, FlightData i_fd) {
		DrawMap (i_hdc);
		i_hdc.setColor(m_routesCategories[0].m_hColor);
		i_hdc.setStroke(m_routesCategories[0].m_hPen);
		StateVector[] l_d = i_fd.currentFlightData ();
		for (StateVector l_f : l_d) {
			int x = (int)((l_f.m_xy.m_x + m_ps.m_sx) * m_matrixRat + 0.5);
			int y = (int)((l_f.m_xy.m_y + m_ps.m_sy) * m_matrixRat + 0.5);
			i_hdc.fillArc(x, y, 15, 15, 0, 360);
		}
	}

	public static class JRouteCategory {
		java.awt.Stroke m_hPen;
		java.awt.Color m_hColor;
		short m_nbRoutes;
		JRoute[] m_routes;
		JRouteCategory (java.awt.Stroke m_hPen, java.awt.Color m_hColor) {
			this.m_hPen = m_hPen;
			this.m_hColor = m_hColor;
		}
	}
	public static class JBeaconCategory {
		java.awt.Font m_hFntLabel, m_hFnt;
		java.awt.Color m_color, m_labelColor;
		String m_char;
		short m_nbBeacons;
		JBeacon[] m_beacons;
		JBeaconCategory (java.awt.Font m_hFntLabel, java.awt.Font m_hFnt, java.awt.Color m_color, java.awt.Color m_labelColor, char m_char) {
			this.m_hFntLabel = m_hFntLabel;
			this.m_hFnt = m_hFnt;
			this.m_color = m_color;
			this.m_labelColor = m_labelColor;
			this.m_char = "" + m_char;
		}
	}
	public static class JRoute {
		int m_nbPoints;
		int[] m_pointsX;
		int[] m_pointsY;
		short[] m_pointIndex;
	}
	public static class JBeacon {
		java.awt.Point m_pSymbol, m_pLabel;
		String m_label;
	}
	private static final int c_nbTypesRoutes = 4, c_nbTypesBeacons = 2;
	private short[] m_nbRoutes = new short[c_nbTypesRoutes];
	private short m_nbPoints;
	private short[] m_nbBeacons = new short [c_nbTypesBeacons];
	private short m_totalNbBeacon, m_totalNbRoute;

	private CoordDegree[] m_allLatLong;
	private CoordNm[] m_allProjLatLong;
	private java.awt.Point[] m_allPoints;

	private JBeacon[] m_allBeacons;
	private JRoute[] m_allRoutes;
	private JBeaconCategory[] m_beaconsCategories;
	private JRouteCategory[] m_routesCategories;
	private int m_matrixWidth;
	private int m_matrixHeight;
	private double m_matrixRat;
	
	public ParamScroll m_ps;

	BsuData(BsuProjection bp) {
		System.out.println("InitMap");
		m_hBrushBgrdAll  = g_colors[c_bgrdAll];
		m_hBrushBgrdSelf = g_colors[c_bgrdSelf];
		m_hSubtitleBrush = g_colors[c_yellow];
		m_hPenRoute        = new java.awt.BasicStroke ();
		m_hPenRunway       = new java.awt.BasicStroke ();
		m_hPenILS          = new java.awt.BasicStroke (1.0f, java.awt.BasicStroke.CAP_SQUARE, java.awt.BasicStroke.JOIN_MITER, 1.0f, new float[]{10.0f, 5.0f}, 0.0f);
		m_hPenSectorBorder = new java.awt.BasicStroke ();
		m_hPenBeacon       = new java.awt.BasicStroke ();
		m_hPenLabel        = new java.awt.BasicStroke ();
		m_hPenOrange       = new java.awt.BasicStroke ();
		m_hPenGreen        = new java.awt.BasicStroke ();
		m_hPenFlight       = new java.awt.BasicStroke (2.0f);
		m_hPenLabel2       = new java.awt.BasicStroke (4.0f);

		m_hColorRoute        = g_colors[c_route];
		m_hColorRunway       = g_colors[c_black];
		m_hColorILS          = g_colors[c_route];
		m_hColorSectorBorder = g_colors[c_sectorBorder];
		m_hColorBeacon       = g_colors[c_beacon];
		m_hColorLabel        = g_colors[c_flight];
		m_hColorOrange       = g_colors[c_orange];
		m_hColorGreen        = g_colors[c_green];
		m_hColorFlight       = g_colors[c_flight];
		m_hColorLabel2       = g_colors[c_black];

		m_times     = new java.awt.Font ("Times New Roman", java.awt.Font.PLAIN, 12);
		m_arial     = new java.awt.Font ("Arial", java.awt.Font.PLAIN, 12);
		m_wingdings = new java.awt.Font ("Wingdings", java.awt.Font.PLAIN, 12);
		m_symbol    = new java.awt.Font ("Symbol", java.awt.Font.PLAIN, 12);
		m_subfont   = new java.awt.Font ("Arial", java.awt.Font.PLAIN, 20);

		try {
			java.io.FileInputStream l_in_unbuf = new java.io.FileInputStream(BSU.g_dataDir + "map.bin");
			java.io.BufferedInputStream l_in = new java.io.BufferedInputStream(l_in_unbuf);

			m_routesCategories = new JRouteCategory[c_nbTypesRoutes];
			m_routesCategories[0] = new JRouteCategory(m_hPenRoute, m_hColorRoute);
			m_routesCategories[1] = new JRouteCategory(m_hPenILS, m_hColorILS);
			m_routesCategories[2] = new JRouteCategory(m_hPenRunway, m_hColorRunway);
			m_routesCategories[3] = new JRouteCategory(m_hPenSectorBorder, m_hColorSectorBorder);
			m_beaconsCategories = new JBeaconCategory[c_nbTypesBeacons];
			m_beaconsCategories[0] = new JBeaconCategory(m_arial, m_arial, g_colors[c_beacon], g_colors[c_beacon], '\u2206');
			m_beaconsCategories[1] = new JBeaconCategory(m_arial, m_symbol, g_colors[c_airport], g_colors[c_airport], '\u00c4');

			m_nbPoints = BsuUtil.readShort(l_in);
			m_totalNbBeacon = 0;
			for (int l_i = 0; l_i < c_nbTypesBeacons; l_i++) {
				m_totalNbBeacon += (m_nbBeacons[l_i] = BsuUtil.readShort(l_in));
			}
			m_totalNbRoute = 0;
			for (int l_i = 0; l_i < c_nbTypesRoutes; l_i++) {
				m_totalNbRoute += (m_nbRoutes[l_i] = BsuUtil.readShort(l_in));
			}

			m_allLatLong = new CoordDegree[m_nbPoints];
			m_allProjLatLong = new CoordNm [m_nbPoints];
			m_allPoints      = new java.awt.Point[m_nbPoints];
			for (int i = 0; i < m_nbPoints; i++) {
				int l_lat = BsuUtil.readInt(l_in);
				int l_long = BsuUtil.readInt(l_in);
				m_allLatLong[i] = new CoordDegree(l_lat/3600.0, l_long/3600.0);
				m_allProjLatLong[i] = new CoordNm();
				m_allPoints[i] = new java.awt.Point();
			}
			m_allRoutes  = new JRoute [m_totalNbRoute];
			for (int l_i = 0; l_i < c_nbTypesRoutes; l_i++) {
				m_allRoutes[l_i] = new JRoute();
			}
			for (int l_i = 0, l_offset = 0; l_i < c_nbTypesRoutes; l_i++) {
				m_routesCategories[l_i].m_routes = new JRoute[m_nbRoutes[l_i]];
				m_routesCategories[l_i].m_nbRoutes = m_nbRoutes[l_i];
				for (int j = 0; j < m_nbRoutes[l_i]; j++) {
					m_routesCategories[l_i].m_routes[j] = m_allRoutes[l_offset + j];
				}
				l_offset += m_nbRoutes[l_i];
			}
			for (int l_i = 0; l_i < m_totalNbRoute; l_i++) {
				m_allRoutes[l_i] = new JRoute();
				m_allRoutes[l_i].m_nbPoints = BsuUtil.readShort(l_in);
				m_allRoutes[l_i].m_pointsX = new int[m_allRoutes[l_i].m_nbPoints];
				m_allRoutes[l_i].m_pointsY = new int[m_allRoutes[l_i].m_nbPoints];
				m_allRoutes[l_i].m_pointIndex = new short[m_allRoutes[l_i].m_nbPoints];
				for (int j = 0; j < m_allRoutes[l_i].m_nbPoints; j++) {
					m_allRoutes[l_i].m_pointIndex[j] = BsuUtil.readShort(l_in);
				}
			}

			m_allBeacons = new JBeacon [m_totalNbBeacon];
			for (int l_i = 0, l_offset = 0; l_i < c_nbTypesBeacons; l_i++) {
				m_beaconsCategories[l_i].m_beacons = new JBeacon[m_nbBeacons[l_i]];
				for (int j = 0; j < m_nbBeacons[l_i]; j++) {
					m_beaconsCategories[l_i].m_beacons[j] = m_allBeacons[l_offset + j];			    	
				}
				m_beaconsCategories[l_i].m_nbBeacons = m_nbBeacons[l_i];
				l_offset += m_nbBeacons[l_i];
			}

			for (int l_i = 0; l_i < m_totalNbBeacon; l_i++) {
				m_allBeacons[l_i] = new JBeacon();
				m_allBeacons[l_i].m_pSymbol = new java.awt.Point(0, -7);
				m_allBeacons[l_i].m_pLabel = new java.awt.Point(8, -3);
			}

			for (int l_i = 0; l_i < m_totalNbBeacon; l_i++) {
				m_allBeacons[l_i].m_label = BsuUtil.readString(l_in);
				System.out.println("beacon " + l_i + " = "
						+ m_allBeacons[l_i].m_label);
			}

		}
		catch (java.io.IOException e) {
			System.err.println ("Couldn't read from map.bin");
		}
		// ApplyProjection
		m_ps = new ParamScroll();
		m_ps.m_zoomMin = 2;
		CoordDegree l_l = m_allLatLong[0];
		CoordNm l_xy = m_allProjLatLong[0];
		bp.geo2stereo(l_l, l_xy);
		double l_xMin = m_ps.m_xMax = l_xy.m_x;
		double l_yMin = m_ps.m_yMax = l_xy.m_y;
		for (int l_i = 1; l_i < m_nbPoints; l_i++) {
			l_l = m_allLatLong[l_i];	
			l_xy = m_allProjLatLong[l_i];
			bp.geo2stereo(l_l, l_xy);
			if (l_xy.m_x > m_ps.m_xMax) {
				m_ps.m_xMax = l_xy.m_x;
			} else if (l_xy.m_x < l_xMin) {
				l_xMin = l_xy.m_x;
			}
			if (l_xy.m_y > m_ps.m_yMax) {
				m_ps.m_yMax = l_xy.m_y;
			} else if (l_xy.m_y < l_yMin) {
				l_yMin = l_xy.m_y;
			}
		}

		// origin shift in order to have xMin=yMin=0 and to have a margin of 10% everywhere
		double w = (m_ps.m_xMax - l_xMin) * 1.2;
		double h = (m_ps.m_yMax - l_yMin) * 1.2;
		m_ps.m_sx = -l_xMin + w/12.0;
		m_ps.m_sy = -l_yMin + h/12.0;
		for (int l_i = 0; l_i < m_nbPoints; l_i++) {
			l_xy = m_allProjLatLong[l_i];
			l_xy.m_x += m_ps.m_sx;
			l_xy.m_y += m_ps.m_sy;
		}
		m_ps.m_xMax = w;
		m_ps.m_yMax = h;
		m_ps.m_zoomMax = (int)(java.lang.Math.ceil  (w));
	}

	// called from bsu.cpp:partout
	void UpdateScrollAndZoom (java.awt.Dimension i_area, int i_zoomMax) {
		double l_rat  = (double)i_area.width / (double)i_zoomMax;
		if ( (   m_matrixWidth   == i_area.width)
			 && (m_matrixHeight  == i_area.height)
			 && (m_matrixRat     == l_rat) )
		    	return;
		m_matrixWidth   = i_area.width;
		m_matrixHeight  = i_area.height;
		m_matrixRat     = l_rat;
		for (int l_i = 0; l_i < m_nbPoints; l_i++) {
		    CoordNm l_xy = m_allProjLatLong[l_i];
		    m_allPoints[l_i].x = (int)(l_xy.m_x * l_rat + 0.5);
		    m_allPoints[l_i].y = (int)(l_xy.m_y * l_rat + 0.5);
		  }
		  // copie des points vers les sommets des routes
		  for (int l_i = 0; l_i < m_totalNbRoute; l_i++) {
		     for (int l_j = 0; l_j < m_allRoutes[l_i].m_nbPoints; l_j++) {
			       m_allRoutes[l_i].m_pointsX[l_j] = m_allPoints[m_allRoutes[l_i].m_pointIndex[l_j]].x;
			       m_allRoutes[l_i].m_pointsY[l_j] = m_allPoints[m_allRoutes[l_i].m_pointIndex[l_j]].y;
		     }
		  }
	}

	// called once from bsu.cpp:main_OnMouseMove
	//extern void UpdateLatLong (int, int, HWND, HWND);

	// called from bsu.cpp:main_OnLButtonDown, main_OnMouseMove, main_OnLButtonUp
	//extern void DrawRangeAndBearing (HWND, HWND, int, int, int, int);

	static void ApplyLabelMove () {
	}
	
	public void DrawMap (java.awt.Graphics2D i_hdc) {
		i_hdc.setColor(m_hBrushBgrdAll);
		//System.out.println ("width " + m_matrixWidth + ", height " + m_matrixHeight);
		i_hdc.fillRect(0, 0, m_matrixWidth, m_matrixHeight);
		i_hdc.setColor(m_routesCategories[0].m_hColor);
		i_hdc.setStroke(m_routesCategories[0].m_hPen);
		
		int l_cmptr = 0;
		int l_category = 0;
		for (int l_i = 0; l_i < m_totalNbRoute; l_i++) {
		    if (l_i >= m_routesCategories[l_category].m_nbRoutes + l_cmptr) {
		      l_cmptr += m_routesCategories[l_category].m_nbRoutes;
		      l_category++;
		      i_hdc.setColor(m_routesCategories[l_category].m_hColor);
		      i_hdc.setStroke(m_routesCategories[l_category].m_hPen);
		    }
		    i_hdc.drawPolyline(m_allRoutes[l_i].m_pointsX, m_allRoutes[l_i].m_pointsY, m_allRoutes[l_i].m_nbPoints);
		  }

		  i_hdc.setColor (m_beaconsCategories[0].m_color);
		  i_hdc.setFont (m_beaconsCategories[0].m_hFnt);
		  String l_curChr = m_beaconsCategories[0].m_char;

		  l_cmptr = 0;
		  l_category = 0;
		  for (int l_i = 0; l_i < m_totalNbBeacon; l_i++) {
		    if (l_i >= m_beaconsCategories[l_category].m_nbBeacons + l_cmptr) {
		      l_cmptr += m_beaconsCategories[l_category].m_nbBeacons;
		      l_category++;
		      i_hdc.setColor(m_beaconsCategories[l_category].m_color);
		      i_hdc.setFont(m_beaconsCategories[l_category].m_hFnt);
		      l_curChr = m_beaconsCategories[l_category].m_char;
		    }
		    int x = m_allPoints[l_i].x + m_allBeacons[l_i].m_pSymbol.x;
		    int y = m_allPoints[l_i].y + m_allBeacons[l_i].m_pSymbol.y;
		    i_hdc.drawString (l_curChr, x, y);
		  }

		  l_cmptr = 0;
		  l_category = 0;
		  i_hdc.setColor (m_beaconsCategories[0].m_labelColor);
		  i_hdc.setFont (m_beaconsCategories[0].m_hFntLabel);
		  for (int l_i = 0; l_i < m_totalNbBeacon; l_i++) {
		    if (l_i >= m_beaconsCategories[l_category].m_nbBeacons + l_cmptr) {
		      l_cmptr += m_beaconsCategories[l_category].m_nbBeacons;
		      l_category++;
		      i_hdc.setColor(m_beaconsCategories[l_category].m_labelColor);
		      i_hdc.setFont(m_beaconsCategories[l_category].m_hFntLabel);
		    }
		    int x = m_allPoints[l_i].x + m_allBeacons[l_i].m_pLabel.x;
		    int y = m_allPoints[l_i].y + m_allBeacons[l_i].m_pLabel.y;
		    i_hdc.drawString (m_allBeacons[l_i].m_label, x, y);
		  }
	}
}

package tk.bocquet.bsu.hmi;

/**
 * The class resources creates common graphical resources in static members.
 * 
 * @author Jean-François Bocquet
 *
 */
public class Resources {

	/**
	 * All used colours
	 */
	public static final java.awt.Color[] g_colors = {
		new java.awt.Color(0, 0, 0), // c_black
		new java.awt.Color(0xff, 0xff, 0xff), // c_white
		new java.awt.Color(0x58, 0x50, 0x50), // c_bgrdAll
		new java.awt.Color(0x60, 0x58, 0x58), // c_bgrdSelf
		new java.awt.Color(0x80, 0x78, 0x79), // c_sectorBorder
		new java.awt.Color(0x73, 0x73, 0x73), // c_beacon
		new java.awt.Color(0xA6, 0xA6, 0xA6), // c_route
		new java.awt.Color(0x7a, 0x73, 0x73), // c_ring
		new java.awt.Color(0xf4, 0xf2, 0xd6), // c_flight
		new java.awt.Color(0xfa, 0xb4, 0x32), // c_orange
		new java.awt.Color(0x00, 0xc8, 0x00), // c_green
		new java.awt.Color(0xff, 0xff, 0x00), // c_yellow
		new java.awt.Color (0x40, 0x40, 0x80) // c_airport
	};

	/**
	 * Named index of colours in the array g_colors
	 */
	public static final int c_black = 0, c_white = 1, c_bgrdAll = 2, c_bgrdSelf = 3, c_sectorBorder = 4, c_beacon = 5, c_route = 6, c_ring = 7, c_flight = 8, c_orange = 9, c_green = 10, c_yellow = 11, c_airport = 12;
	
	public static java.awt.Color g_hBrushBgrdAll  = Resources.g_colors[Resources.c_bgrdAll];
	public static java.awt.Stroke g_hPenRoute     = new java.awt.BasicStroke ();
	public static java.awt.Stroke g_hPenLabel     = new java.awt.BasicStroke ();
	public static java.awt.Stroke g_hPenFlight    = new java.awt.BasicStroke (2.0f);
	public static java.awt.Color  g_hColorBeacon  = Resources.g_colors[Resources.c_beacon];
	public static java.awt.Color  g_hColorAirport = Resources.g_colors[Resources.c_airport];
	public static java.awt.Color  g_hColorFlight  = Resources.g_colors[Resources.c_flight];
	public static java.awt.Font   g_arial     = loadATCFont(); // new java.awt.Font ("Arial", java.awt.Font.PLAIN, 12);
	public static java.awt.Font   g_subfont   = new java.awt.Font ("Arial", java.awt.Font.BOLD, 20);
	public static java.awt.Font   g_atcFont    = loadATCFont();

	private static java.awt.Font loadATCFont () {
		try {
			// getClassLoader().
			java.io.InputStream fontStream = (new Resources()).getClass().getResourceAsStream("ADOSdraft1.ttf");
			java.awt.Font font = java.awt.Font.createFont(0, fontStream);
			return font.deriveFont(java.awt.Font.PLAIN, 14);
		} catch (Exception e) {
			System.out.println("Resources exception: " + e.toString());
			throw new Error ("Resources exception" + e.toString());
		}
	}
	
	/**
	 * Create an image from a file stored in the jar
	 * @param i_name
	 * @return
	 */
	public static javax.swing.ImageIcon createImageIcon (String i_name) {
		java.net.URL imgURL = Resources.class.getResource("/images/" + i_name);
		if (imgURL != null) {
	        return new javax.swing.ImageIcon(imgURL, i_name);
	    } else {
	        System.err.println("Couldn't find file: " + i_name);
	        return null;
	    }
	}
}

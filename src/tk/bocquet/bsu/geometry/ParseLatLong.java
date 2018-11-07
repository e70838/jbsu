/*
 * @(#)ParseLatLong.java
 * 
 */

package tk.bocquet.bsu.geometry;

/**
 * ParseLatLong gathers a set of static methods for converting strings to latitude and longitude
 * 
 * @author Jean-François Bocquet
 *
 */
public class ParseLatLong {
	/**
	 * Format of a number with two digits
	 */
	private static final java.text.DecimalFormat c_format02 = new java.text.DecimalFormat ("00");
	/**
	 * Format of a number with three digits
	 */
	private static final java.text.DecimalFormat c_format03 = new java.text.DecimalFormat ("000");

	private static String toDMS (double i_deg, char i_positiv, char i_negativ) {
		char l_letter;
		if (i_deg >= 0.0) {
			l_letter = i_positiv;
		} else {
			l_letter = i_negativ;
			i_deg *= -1;
		}
		// round i_deg
		int l_n = (int)(i_deg * 60.0 * 60.0 + 0.5);

		int l_second = l_n % 60;
		l_n = (l_n - l_second) / 60;
		int l_minute = l_n % 60;
		return ((i_positiv == 'N') ? c_format02.format((l_n-l_minute)/60) : c_format03.format((l_n-l_minute)/60))
		+ c_format02.format(l_minute) + l_letter + c_format02.format(l_second);
	}

	public static String latitudeToString (double i_latitude) {
		return toDMS(i_latitude, 'N', 'S');
	}

	public static String longitudeToString (double i_longitude) {
		return toDMS(i_longitude, 'E', 'W');
	}

	/**
	 * Parse a latitude written as two digits for the degrees, two digits for the minutes, the letter N or S for the orientation
	 * and optionally two digits for the seconds that may be followed by a decimal part.
	 * @param latitude
	 * @return a latitude in degree with double precision
	 */
	static public double parseLatitude (String latitude) {
		int d, m;
		double deg;
		double s = 0.0;
		char c = latitude.charAt(0);
		if (! Character.isDigit(c)) throw new Error ("Bad syntax for latitude: " + latitude);
		d = 10 * (c - '0');
		c = latitude.charAt(1);
		if (! Character.isDigit(c)) throw new Error ("Bad syntax for latitude: " + latitude);
		d += c - '0';
		c = latitude.charAt(2);
		if (! Character.isDigit(c)) throw new Error ("Bad syntax for latitude: " + latitude);
		m = 10 * (c - '0');
		c = latitude.charAt(3);
		if (! Character.isDigit(c)) throw new Error ("Bad syntax for latitude: " + latitude);
		m += c - '0';
		if (latitude.length() > 5) {
			c = latitude.charAt(5);
			if (! Character.isDigit(c)) throw new Error ("Bad syntax for latitude: " + latitude);
			s = 10 * (c - '0');
			c = latitude.charAt(6);
			if (! Character.isDigit(c)) throw new Error ("Bad syntax for latitude: " + latitude);
			s += c - '0';
			if (latitude.length() > 7) {
				if (latitude.charAt(7) != '.') throw new Error ("Bad syntax for latitude: " + latitude);
				c = latitude.charAt(8);
				s += (c - '0') / 10.0;
				if (latitude.length() > 9) {
					c = latitude.charAt(9);
					if (! Character.isDigit(c)) throw new Error ("Bad syntax for latitude: " + latitude);
					s += (c - '0') / 100.0;
				}
			}
		}
		deg = d + m / 60.0 + s / 3600.0;
		c = latitude.charAt(4);
		if (c == 'N') {
			return deg;
		} else if (c == 'S') {
			return -deg;
		} else {
			throw new Error ("Bad syntax for latitude: " + latitude);	
		}
	}

	/**
	 * Parse a longitude written as three digits for the degrees, two digits for the minutes, the letter E or W for the orientation and two digits for the seconds.
	 * @param i_s
	 * @return a longitude in degree with double precision
	 */
	static public double parseLongitude (String longitude) {
		int d, m;
		double deg;
		double s = 0.0;
		char c = longitude.charAt(0);
		if (! Character.isDigit(c)) throw new Error ("Bad syntax for longitude: " + longitude);
		d = 10 * (c - '0');
		c = longitude.charAt(1);
		if (! Character.isDigit(c)) throw new Error ("Bad syntax for longitude: " + longitude);
		d += c - '0';
		c = longitude.charAt(2);
		if (! Character.isDigit(c)) throw new Error ("Bad syntax for longitude: " + longitude);
		d = (d*10) + (c - '0');
		c = longitude.charAt(3);
		if (! Character.isDigit(c)) throw new Error ("Bad syntax for longitude: " + longitude);
		m = 10 * (c - '0');
		c = longitude.charAt(4);
		if (! Character.isDigit(c)) throw new Error ("Bad syntax for longitude: " + longitude);
		m += c - '0';
		if (longitude.length() > 6) {
			c = longitude.charAt(6);
			if (! Character.isDigit(c)) throw new Error ("Bad syntax for longitude: " + longitude);
			s = 10.0 * (c - '0');
			c = longitude.charAt(7);
			if (! Character.isDigit(c)) throw new Error ("Bad syntax for longitude: " + longitude);
			s += c - '0';
			if (longitude.length() > 9) {
				if (longitude.charAt(8) != '.') throw new Error ("Bad syntax for longitude: " + longitude);
				c = longitude.charAt(9);
				if (! Character.isDigit(c)) throw new Error ("Bad syntax for longitude: " + longitude);
				s += (c - '0') / 10.0;
				if (longitude.length() > 10) {
					c = longitude.charAt(10);
					if (! Character.isDigit(c)) throw new Error ("Bad syntax for longitude: " + longitude);
					s += (c - '0') / 100.0;
				}
			}
		}
		deg = d + m / 60.0 + s / 3600.0;
		c = longitude.charAt(5);
		if (c == 'E') {
			return deg;
		} else if (c == 'W') {
			return -deg;
		} else {
			throw new Error ("Bad syntax for longitude: " + longitude);	
		}
	}
}

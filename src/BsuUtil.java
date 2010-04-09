
public class BsuUtil {
	static double deg2rad (double i_deg) {
		return i_deg * (java.lang.Math.PI / 180.0);
	}
	
	static double rad2deg (double i_rad) {
		return i_rad * (180.0 / java.lang.Math.PI);
	}
	
	static double string_to_lat (String i_s) {
		double r = i_s.charAt(3)-'0' + 10 * (i_s.charAt(2)-'0') + 60 * (i_s.charAt(1)-'0') + 600 * (i_s.charAt(0)-'0');
		return r * ((i_s.charAt(4) == 'N') ? 1 : -1) / 60.0;
	}
	
	static double string_to_lat_second (String i_s) {
		double r = i_s.charAt(3)-'0' + 10 * (i_s.charAt(2)-'0') + 60 * (i_s.charAt(1)-'0') + 600 * (i_s.charAt(0)-'0') + (i_s.charAt(5)-'0') / 6.0 + (i_s.charAt(6)-'0') / 60.0;
		return r * ((i_s.charAt(4) == 'N') ? 1 : -1) / 60.0;
	}
	
	static double string_to_long (String i_s) {
		double r = i_s.charAt(4)-'0' + 10 * (i_s.charAt(3)-'0') + 60 * (i_s.charAt(2)-'0') + 600 * (i_s.charAt(1)-'0') + 6000 * (i_s.charAt(0)-'0');
		return r * ((i_s.charAt(5) == 'E') ? 1 : -1) / 60.0;
	}
	
	static double string_to_long_second (String i_s) {
		double r = i_s.charAt(4)-'0' + 10 * (i_s.charAt(3)-'0') + 60 * (i_s.charAt(2)-'0') + 600 * (i_s.charAt(1)-'0') + 6000 * (i_s.charAt(0)-'0') + (i_s.charAt(6)-'0') / 6.0 + (i_s.charAt(7)-'0') / 60.0;
		return r * ((i_s.charAt(5) == 'E') ? 1 : -1) / 60.0;
	}
	
	static short readShort (java.io.InputStream i_in) throws java.io.IOException{
		int byte1, byte2;
		byte1 = i_in.read ();
		byte2 = i_in.read ();
		if (byte1 == -1 || byte2 == -1) {
			throw new java.io.EOFException();
		}
		return (short)((byte2 << 8) + byte1);
	}
	
	static int readInt (java.io.InputStream i_in) throws java.io.IOException{
		int byte1, byte2, byte3, byte4;
		byte1 = i_in.read ();
		byte2 = i_in.read ();
		byte3 = i_in.read ();
		byte4 = i_in.read ();
		if (byte1 == -1 || byte2 == -1 || byte3 == -1 || byte4 == -1) {
			throw new java.io.EOFException();
		}
		return (byte4 << 24) + (byte3 << 16) + (byte2 << 8) + byte1;
	}
	
	static String readString (java.io.InputStream i_in) throws java.io.IOException{
		byte[] l_buf = new byte[1024];
		int i = 0;
		while (true) {
			int b = i_in.read ();
			if (b == -1) {
				throw new java.io.EOFException();
			} else if (b == 0) {
				return new String (l_buf, 0, i);
			} else {
				l_buf[i++] = (byte)b;
			}
		}
	}
	
	static char readChar (java.io.InputStream i_in) throws java.io.IOException{
		int byte1;
		byte1 = i_in.read ();
		if (byte1 == -1) {
			throw new java.io.EOFException();
		}
		return (char)(byte1);
	}
	
	static String readString (java.io.InputStream i_in, int i_length) throws java.io.IOException{
		byte[] l_buf = new byte[i_length];
		if (i_length != i_in.read (l_buf)) {
			throw new java.io.EOFException();
		}
		int l_last = i_length-1;
		while (l_last > 0 && l_buf[l_last] == 0)
			l_last --;
		return new String (l_buf, 0, l_last);
	}
}

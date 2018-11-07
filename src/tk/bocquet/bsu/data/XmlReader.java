package tk.bocquet.bsu.data;

import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

import com.sun.org.apache.xerces.internal.parsers.SAXParser;

import tk.bocquet.bsu.geometry.CoordDegree;
import tk.bocquet.bsu.geometry.CoordNm;
import tk.bocquet.bsu.geometry.ParseLatLong;
import tk.bocquet.bsu.geometry.Projection;
import tk.bocquet.bsu.hmi.Resources;
import tk.bocquet.bsu.viewer.records.ParamScroll;
import tk.bocquet.bsu.viewer.records.StateVector;
import tk.bocquet.bsu.viewer.records.Subtitle;

public class XmlReader extends DefaultHandler {
	static final int IN_TOP_LEVEL = 0;
	static final int IN_TIME_TICK = 1;
	static final int IN_POINTS    = 2;
	static final int IN_ROUTES    = 3;
	static final int IN_ROUTE     = 4;
	static final int IN_TITLE     = 5;
	
	int state;
	StringBuffer stringBuffer;
	java.util.Vector<String> controlers;
	java.util.Vector<TimeTick> timeTicks;
	TimeTick timeTick;
	java.util.Vector<StateVector> stateVectors;
	java.util.LinkedList<FlightLabel> flightLabels;
	java.util.HashMap<String, FlightLabel> allLabels;
	
	java.util.Vector<CoordNm> allProjLatLong;
	java.util.Vector<java.awt.Point> allPoints;
	
	MapData.JBeaconCategory beaconCategory;
	java.util.Vector<MapData.JBeaconCategory> beaconCategories;
	java.util.Vector<String> beacons;
	java.util.HashMap<String, Integer> coordsIndex;
	
	java.util.Vector<Integer> currentRoute;
	java.util.Vector<MapData.JRoute> routes;
	java.util.Vector<MapData.JRouteCategory> routesGroups;
	String currentRouteName;
	MapData.JRouteCategory currentRouteCategory;
	int flightId;

	private String title;
	public String getTitle() {
		return this.title;
	}
	
	private ParamScroll paramScroll;
	public ParamScroll getParamScroll() {
		return this.paramScroll;
	}

	private Projection projection;
	public Projection getProjection() {
		return this.projection;
	}

	private FlightData flightData;
	public FlightData getFlightData() {
		return this.flightData;
	}

	private MapData mapData;
	public MapData getMapData() {
		return this.mapData;
	}
	
	private int time;
	private String sector;
	private String callsign; 

	static public java.awt.Color parseColor(String color) {
		int [] v = new int[6];
		for (int i = 0; i < color.length(); i++) {
			char c = color.charAt(i);
			if (Character.isDigit(c)) {
				v[i] = c - '0';
			} else if ((c >= 'a') && (c <= 'f')) {
				v[i] = 10 + c - 'a';
			} else if ((c >= 'A') && (c <= 'F')) {
				v[i] = 10 + c - 'A';
			} else {
				throw new Error ("Pb parsing color " + color);
			}
		}
		return new java.awt.Color(v[0] * 16 + v[1], v[2] * 16 + v[3], v[4] * 16 + v[5]);
	}
	
	static public int parseTime (String time) {
		int h, m, s, pos;
		char c = time.charAt(0);
		if (! Character.isDigit(c)) {
			throw new Error ("Bad syntax for time: " + time);
		}
		h = c - '0';
		c = time.charAt(1);
		if (Character.isDigit(c)) {
			h = (h * 10) + (c - '0');
			c = time.charAt(2);
			pos = 3;
		} else {
			pos = 2;
		}
		if (c == ':') {
			c = time.charAt(pos++);
		}
		if (! Character.isDigit(c)) {
			throw new Error ("Bad syntax for time: " + time);
		}
		m = c - '0';
		c = time.charAt(pos++);
		if (Character.isDigit(c)) {
			m = (m * 10) + (c - '0');
			c = time.charAt(pos++);
		}
		if (c == ':') {
			c = time.charAt(pos++);
		}
		if (! Character.isDigit(c)) {
			throw new Error ("Bad syntax for time: " + time);
		}
		s = c - '0';
		if (pos == time.length()-1) {
			c = time.charAt(pos++);
			if (! Character.isDigit(c)) {
				throw new Error ("Bad syntax for time: " + time);
			}
			s = (s * 10) + (c - '0');
		} else if (pos != time.length()) {
			throw new Error ("Bad length for time: " + time + "!");			
		}
		return (h * 60 + m) * 60 + s;
	}
	
	public XmlReader(String filename) {
		long l_t0 = java.lang.System.currentTimeMillis();
		SAXParser p = new SAXParser();
		state = IN_TOP_LEVEL;
		p.setContentHandler(this);
		this.projection       = null;
		flightId = 1;
		this.controlers       = new java.util.Vector<String>();
		this.timeTicks        = new java.util.Vector<TimeTick>();
		this.flightLabels     = new java.util.LinkedList<FlightLabel>();
		this.allLabels        = new java.util.HashMap<String, FlightLabel>(400);
		this.stateVectors     = new java.util.Vector<StateVector>();
		
		this.allProjLatLong   = new java.util.Vector<CoordNm>();
		this.allPoints        = new java.util.Vector<java.awt.Point>();
		this.coordsIndex      = new java.util.HashMap<String, Integer>(1000);
		this.beaconCategories = new java.util.Vector<MapData.JBeaconCategory>();
		
		this.routesGroups = new java.util.Vector<MapData.JRouteCategory>();
		try {
			if (filename == null) {
				org.xml.sax.InputSource is = new org.xml.sax.InputSource(new java.io.BufferedInputStream(getClass().getResourceAsStream("/default.xml")));
				p.parse(is);
			} else {
				p.parse(filename);
			}
			System.out.println("Finish parsing");
			this.flightData = new FlightData(this.projection, this.flightLabels, this.timeTicks, this.controlers.toArray(new String[0]));
			this.paramScroll = new ParamScroll();
			this.mapData = new MapData(this.projection, this.allProjLatLong.toArray(new CoordNm[0]),
					this.allPoints.toArray(new java.awt.Point[0]), this.beaconCategories, this.routesGroups.toArray(new MapData.JRouteCategory[0]), this.paramScroll);
		} catch (Exception e) {
			System.out.println("Exception " + e);
			throw (new Error("Exception " + e));
		}
		long l_t1 = java.lang.System.currentTimeMillis();
		System.out.println ("Sax parsing " + (l_t1 - l_t0));
	}

	public int addCoord(Attributes atts) {
		String latitude = atts.getValue("latitude");
		String longitude =atts.getValue("longitude");
		int res = this.allProjLatLong.size();
		if (this.coordsIndex.containsKey(latitude+longitude)) {
			return -this.coordsIndex.get(latitude+longitude).intValue();
		}
		CoordDegree coordDegree = new CoordDegree(ParseLatLong.parseLatitude(latitude), ParseLatLong.parseLongitude(longitude));
		CoordNm coordNm = new CoordNm();
		this.projection.geo2stereo(coordDegree, coordNm);
		this.allProjLatLong.add(coordNm);
		this.allPoints.add(new java.awt.Point());
		this.coordsIndex.put(latitude + longitude, res);
		return res;
	}
	
	public void startElement(String namespaceURI, String localName, String qName, Attributes atts) {
		if (this.projection == null) {
			if (localName.equals("projection")) { // namespaceURI.equals("http://recipes.org") && 
				String latitude  = atts.getValue("latitude");
				String longitude = atts.getValue("longitude");
				System.out.println("Center : " + latitude + " - " + longitude);
				projection = new Projection(new CoordDegree
						(ParseLatLong.parseLatitude (latitude), ParseLatLong.parseLongitude(longitude)));
			} else if (localName.equals("data")) {

			} else if (localName.equals("viewport")) {
			} else if (localName.equals("title")) {
				stringBuffer = new StringBuffer();
				this.state = IN_TITLE;
			} else {
				throw new Error("Unknown localName " + localName);
			}
		} else if (this.state == IN_TOP_LEVEL) {
			if (localName.equals("time_tick")) {
				this.state = IN_TIME_TICK;
				timeTick = new TimeTick();
				this.stateVectors.clear();
				timeTick.time = parseTime(atts.getValue("time"));
			} else if (localName.equals("order")) {
				stringBuffer = new StringBuffer();
				this.time = parseTime(atts.getValue("time"));
				this.sector = atts.getValue("sector");
				this.callsign = atts.getValue("callsign");
			} else if (localName.equals("points")) {
				// System.out.println("start points ...");
				this.state = IN_POINTS;
				try {
					java.io.InputStream fontStream = (new Resources()).getClass().getResourceAsStream(atts.getValue("labelFontName"));
					java.awt.Font font = java.awt.Font.createFont(0, fontStream);
					String styleString = atts.getValue("labelFontStyle");
					int style = styleString.compareTo("PLAIN") == 0 ? java.awt.Font.PLAIN : styleString.compareTo("BOLD") == 0 ? java.awt.Font.BOLD : java.awt.Font.ITALIC; 
					font = font.deriveFont(style, Integer.parseInt(atts.getValue("labelFontSize")));
					java.awt.Color labelColor = parseColor(atts.getValue("labelColor"));
					this.beaconCategory = new MapData.JBeaconCategory(font, labelColor, atts.getValue("symbol"));
				} catch (Exception e) {
					System.out.println("Resources exception: " + e.toString());
					throw new Error ("Resources exception" + e.toString());
				}
				this.beacons = new java.util.Vector<String>();
				// System.out.println("... start points");
			} else if (localName.equals("routes")) {
				@SuppressWarnings("unused")
				String typeRoute = atts.getValue("kind");
				String style = atts.getValue("style");
				java.awt.Stroke stroke;
				if (style.compareToIgnoreCase("solid") == 0) {
					stroke = new java.awt.BasicStroke ();
				} else if (style.compareToIgnoreCase("dashed") == 0) {
					stroke = new java.awt.BasicStroke (1.0f, java.awt.BasicStroke.CAP_SQUARE, java.awt.BasicStroke.JOIN_MITER, 1.0f, new float[]{10.0f, 5.0f}, 0.0f);
				} else {
					throw new Error ("Pb parsing route style " + style);
				}
				java.awt.Color color = parseColor(atts.getValue("color"));
				this.currentRouteCategory = new MapData.JRouteCategory(stroke, color);
				
				this.routes = new java.util.Vector<MapData.JRoute>();
				this.state = IN_ROUTES;
			} else {
				throw new Error("Unknown localName in state IN_TOP_LEVEL " + localName);
			}
		} else if (this.state == IN_TIME_TICK) { // in time_tick
			if (localName.equals("state_vector")) {
				String callsign = atts.getValue("callsign");
				atts.getValue("latitude");
				atts.getValue("longitude");
				String wake = atts.getValue("wake");
				if (wake.length() != 1) {
					throw new Error("Wake must be 1 character");
				}
				FlightLabel flightLabel;
				if (this.allLabels.containsKey(callsign)) {
					flightLabel = this.allLabels.get(callsign);
				} else {
					flightLabel = new FlightLabel(flightId++);
					this.allLabels.put(callsign, flightLabel);
					this.flightLabels.add(flightLabel);
					flightLabel.wake = wake.charAt(0);
					flightLabel.callsign = callsign;
				}
				StateVector stateVector = new StateVector();
				stateVector.time = timeTick.time;
				stateVector.track = (int)(60.0F * Float.parseFloat(atts.getValue("track")));
				stateVector.ssr = atts.getValue("ssr");
				stateVector.altitude = Integer.parseInt(atts.getValue("altitude"));
				stateVector.rocd = Short.parseShort(atts.getValue("rocd"));
				stateVector.attitude = stateVector.rocd == 0 ? '\u0014' : stateVector.rocd < 0 ? '\u0015' : '\u0016';
				stateVector.groundSpeed = (int)(10.0F * Float.parseFloat(atts.getValue("GS")));
				CoordDegree l_latlong = new CoordDegree (ParseLatLong.parseLatitude(atts.getValue("latitude")), ParseLatLong.parseLongitude(atts.getValue("longitude")));
				stateVector.xy = new CoordNm();
				stateVector.speedVectorXY = new CoordNm();
				projection.geo2stereo(l_latlong, stateVector.xy);
				l_latlong.translate(stateVector.groundSpeed / (40.0 * 10.0), stateVector.track / 60.0);
				projection.geo2stereo(l_latlong, stateVector.speedVectorXY);
				stateVector.label = flightLabel;
				stateVector.previous = flightLabel.stateVector;
				flightLabel.stateVector = stateVector;
				this.stateVectors.add(stateVector);
			} else {
				throw new Error("Unknown localName in state IN_TIME_TICK " + localName);
			}
		} else if (this.state == IN_POINTS) { // in points
			if (localName.equals("point")) {
				// System.out.println("start point ...");
				String name = atts.getValue("name");
				int i = addCoord(atts);
				if (i >= 0) { // TODO to be tested
					this.beacons.add(name);
					this.coordsIndex.put(name, i);
				}
				// System.out.println("... start point");
			}
		} else if (this.state == IN_ROUTES) { // in routes
			if (localName.equals("route")) {
				this.currentRoute = new java.util.Vector<Integer>();
				this.currentRouteName = atts.getValue("name");
				this.state = IN_ROUTE;
			} else {
				throw new Error("UnKnown localName in state IN_ROUTES " + localName);
			}
		} else if (this.state == IN_ROUTE) { // in route
			if (localName.equals("point")) {
				int i = this.addCoord(atts);
				this.currentRoute.add(i);
			} else if (localName.equals("beacon")) {
				this.currentRoute.add(this.coordsIndex.get(atts.getValue("name")));
			} else {
				throw new Error("UnKnown localName in state IN_ROUTE " + localName);
			}
		} else {
			throw new Error("StartElement invalid in state " + this.state + " name : " + localName);
		}
	}
	public void endElement(String namespaceURI, String localName, String qName) {
		if (this.state == IN_TOP_LEVEL) {
			if (localName.equals("order")) {
				int i = 0;
				while (this.timeTicks.elementAt(i).time < this.time) {
					i++;
				}
				Subtitle s = new Subtitle();
				s.controller = getController(this.sector);
				s.sentence = this.sector + ' ' + this.callsign + ' ' + stringBuffer;
				s.next = this.timeTicks.elementAt(i).subtitle;
				this.timeTicks.elementAt(i).subtitle = s;
				//System.out.println ("Order " + this.sector + " : " + this.callsign + " : " + order);
				this.stringBuffer = null;
			} else if (localName.equals("viewport")) {
			} else if (localName.equals("data")) {
			} else if (localName.equals("projection")) {
			} else {
				throw new Error("endElement UnKnown localName in state IN_TOP_LEVEL " + localName);
			}
		} else if (this.state == IN_TITLE) {
			this.title = this.stringBuffer.toString();
			this.stringBuffer = null;
			this.state = IN_TOP_LEVEL;
		} else if (this.state == IN_TIME_TICK) {
			if (localName.equals("time_tick")) {
				this.state = IN_TOP_LEVEL;
				this.timeTick.setStateVectors(this.stateVectors.toArray(new StateVector[0]));
				this.timeTicks.add(this.timeTick);
			} else if (localName.equals("state_vector")) {
			} else {
				throw new Error("endElement UnKnown localName in state IN_TIME_TICK " + localName);
			}
		} else if (this.state == IN_POINTS) { // in points
			if (localName.equals("points")) {
				// System.out.println("end points ...");
				this.beaconCategory.beacons = new MapData.JBeacon[this.beacons.size()];
				for (int i = 0; i < this.beaconCategory.beacons.length; i++) {
					MapData.JBeacon beacon = new MapData.JBeacon();
					beacon.pSymbol = new java.awt.Point(-7, -7);
					beacon.pLabel = new java.awt.Point(8, -3);
					beacon.label = this.beacons.elementAt(i);
					this.beaconCategory.beacons[i] = beacon;
				}
				this.beacons = null;
				this.beaconCategories.add(this.beaconCategory);
				this.beaconCategory = null;
				this.state = IN_TOP_LEVEL;
				// System.out.println("...end points");
			} else if (localName.equals("point")) {
			}
		} else if (this.state == IN_ROUTES) { // in routes
			if (localName.equals("routes")) {
				this.state = IN_TOP_LEVEL;
				this.currentRouteCategory.routes = routes.toArray(new MapData.JRoute[0]);
				this.routesGroups.add(this.currentRouteCategory);
				this.currentRouteCategory = null;
			} else {
				throw new Error("endElement UnKnown localName in state IN_ROUTES " + localName);
			}
		} else if (this.state == IN_ROUTE) { // in route
			if (localName.equals("route")) {
				this.state = IN_ROUTES;
				MapData.JRoute route = new MapData.JRoute();
				int nbPoints = this.currentRoute.size();
				route.pointsX = new int[nbPoints];
				route.pointsY = new int[nbPoints];
				route.pointIndex = new java.awt.Point[nbPoints];
				for (int z = 0 ; z < nbPoints; z++) {
					route.pointIndex[z] = this.allPoints.elementAt(Math.abs(this.currentRoute.get(z)));
				}
				this.routes.add(route);
				this.currentRoute = null;
			} else if (localName.equals("point")) {
			} else if (localName.equals("beacon")) {
			} else {
				throw new Error("endElement UnKnown localName in state IN_ROUTE " + localName);
			}
		}
	}
	private int getController(String sector) {
		for (int i = 0; i < this.controlers.size(); i++) {
			if (sector.compareTo(this.controlers.get(i)) == 0) {
				return i;
			}
		}
		this.controlers.add(sector);		
		return this.controlers.size();
	}

	public void characters(char buf[], int offset, int len)
	{
		if (stringBuffer != null) {
			stringBuffer.append(buf, offset, len);
		}
	}	
}

/*
 * @(#)MapData.java
 */

package tk.bocquet.bsu.data;

import tk.bocquet.bsu.geometry.CoordNm;
import tk.bocquet.bsu.geometry.Projection;
import tk.bocquet.bsu.hmi.Resources;
import tk.bocquet.bsu.viewer.records.ParamScroll;

/**
 * MapData contains all the data to draw lines of different colors and thickness and to draw point symbols (airports or navigation points) 
 * 
 * @author Jean-François Bocquet
 *
 */
public class MapData {

	private double previousRatio;

	public static class JRouteCategory {
		java.awt.Stroke hPen;
		java.awt.Color color;
		JRoute[] routes;
		JRouteCategory (java.awt.Stroke i_hPen, java.awt.Color i_hColor) {
			this.hPen = i_hPen;
			this.color = i_hColor;
		}
		JRouteCategory () {}
	}

	public static class JBeaconCategory {
		java.awt.Font hFntLabel;
		java.awt.Color labelColor;
		javax.swing.ImageIcon symbol;
		JBeacon[] beacons;
		JBeaconCategory (java.awt.Font i_hFntLabel, java.awt.Color i_labelColor, String i_image) {
			this.hFntLabel = i_hFntLabel;
			this.labelColor = i_labelColor;
			this.symbol = Resources.createImageIcon(i_image);
		}
		JBeaconCategory() {}
	}

	public static class JRoute {
		int[] pointsX;
		int[] pointsY;
		java.awt.Point[] pointIndex;
	}

	public static class JBeacon {
		/**
		 * Position of the beacon symbol relative to its coordinates
		 */
		java.awt.Point pSymbol;
		/**
		 * Position of the beacon label relative to its coordinates
		 */
		java.awt.Point pLabel;
		/**
		 * label of the beacon
		 */
		String label;
	}

	/**
	 * Projected coordinates of all points, ordered by category
	 */
	public CoordNm[] allProjLatLong;

	/**
	 * Screen coordinates of all points (zoom transformation of CoordNm)
	 */
	public java.awt.Point[] allPoints;

	/**
	 * Beacons are grouped in categories like 'navigation point', 'airport'
	 */
	public JBeaconCategory[] beaconsCategories;

	/**
	 * Lines are grouped in categories like 'navigation route', 'ILS', 'runways', 'dashed lines'
	 */
	public JRouteCategory[] routesCategories;

	/**
	 * Draw the map.
	 * @param i_hdc
	 * @param i_rat
	 */
	public void draw (java.awt.Graphics2D i_hdc, double i_rat) {
		if (i_rat != previousRatio) {
			previousRatio = i_rat;
			for (int l_i = 0; l_i < allProjLatLong.length; l_i++) {
				CoordNm l_xy = allProjLatLong[l_i];
				allPoints[l_i].x = (int)(l_xy.x * i_rat + 0.5);
				allPoints[l_i].y = (int)(l_xy.y * i_rat + 0.5);
			}
			// copy points coordinates into route vertices
			for (int i = 0 ; i < routesCategories.length ; i++) {
				JRoute [] jroutes = routesCategories[i].routes;
				for (int j = 0 ; j < jroutes.length ; j++) {
					JRoute route = jroutes[j];
					for (int k = 0; k < route.pointsX.length; k++) {
						route.pointsX[k] = route.pointIndex[k].x;
						route.pointsY[k] = route.pointIndex[k].y;
						
					}
				}
			}
		}

		for (JRouteCategory l_rc : routesCategories) {
			i_hdc.setColor(l_rc.color);
			i_hdc.setStroke(l_rc.hPen);
			for (JRoute route : l_rc.routes) {
				i_hdc.drawPolyline(route.pointsX, route.pointsY, route.pointsX.length);
			}
		}

		int cmptr = 0;
		for (int l_i = 0; l_i < beaconsCategories.length; l_i++) {
			JBeaconCategory bc = beaconsCategories[l_i];
			javax.swing.ImageIcon l_symbol = bc.symbol;
			int nbBeacons = bc.beacons.length;
			for (int j = 0; j < nbBeacons ; j++) {
				i_hdc.drawImage (l_symbol.getImage(),
						allPoints[cmptr].x + bc.beacons[j].pSymbol.x,
						allPoints[cmptr].y + bc.beacons[j].pSymbol.y, null);
				cmptr ++;
			}
		}
		
		cmptr = 0;
		for (int l_i = 0; l_i < beaconsCategories.length; l_i++) {
			JBeaconCategory bc = beaconsCategories[l_i];
			i_hdc.setColor (bc.labelColor);
			i_hdc.setFont (bc.hFntLabel);
			int nbBeacons = bc.beacons.length;
			for (int j = 0; j < nbBeacons ; j++) {
				i_hdc.drawString (bc.beacons[j].label,
						allPoints[cmptr].x + bc.beacons[j].pLabel.x,
						allPoints[cmptr].y + bc.beacons[j].pLabel.y);
				cmptr ++;
			}
		}
	}

	/**
	 * Constructor.
	 * @param bp
	 * @param allProjLatLong   projected coordinated of all points
	 * @param allPoints        associated screen position
	 * @param beaconCategories drawing styles for points (beacons and airports)
	 * @param routesCategories drawing styles for lines
	 * @param o_ps
	 */
	public MapData(Projection bp,
			CoordNm[] allProjLatLong,
			java.awt.Point[] allPoints,
			java.util.Vector<MapData.JBeaconCategory> beaconCategories,
			JRouteCategory[] routesCategories,
			ParamScroll o_ps) {
		System.out.println("InitMap"); 
		this.allProjLatLong   = allProjLatLong;
		this.allPoints        = allPoints;
		this.routesCategories = routesCategories;
		
		this.beaconsCategories = beaconCategories.toArray(new MapData.JBeaconCategory[0]);
		
		// compute scroll parameters
		o_ps.zoomMin = 2;

		CoordNm l_xy = allProjLatLong[0];
		double l_xMin = o_ps.xMax = l_xy.x;
		double l_yMin = o_ps.yMax = l_xy.y;
		for (int l_i = 1; l_i < allProjLatLong.length; l_i++) {
			l_xy = allProjLatLong[l_i];
			if (l_xy.x > o_ps.xMax) {
				o_ps.xMax = l_xy.x;
			} else if (l_xy.x < l_xMin) {
				l_xMin = l_xy.x;
			}
			if (l_xy.y > o_ps.yMax) {
				o_ps.yMax = l_xy.y;
			} else if (l_xy.y < l_yMin) {
				l_yMin = l_xy.y;
			}
		}

		// origin shift in order to have xMin=yMin=0 and to have a margin of 10% everywhere
		double w = (o_ps.xMax - l_xMin) * 1.2;
		double h = (o_ps.yMax - l_yMin) * 1.2;
		o_ps.shiftX = -l_xMin + w / 12.0;
		o_ps.shiftY = -l_yMin + h / 12.0;
		for (int l_i = 0; l_i < allProjLatLong.length; l_i++) {
			l_xy = allProjLatLong[l_i];
			l_xy.x += o_ps.shiftX;
			l_xy.y += o_ps.shiftY;
		}
		o_ps.xMax = w;
		o_ps.yMax = h;
		o_ps.zoomMax = (int)(java.lang.Math.ceil  (w));
	}	
}

/*
 * @(#)ZoomDlgModeless.java
 * 
 */

package tk.bocquet.bsu.hmi;

/**
 * ZoomDlgModeless represents the graphical panel used to adjust zoom value
 *
 * @author Jean-François Bocquet
 *
 */
class ZoomPanel extends javax.swing.JPanel implements java.awt.event.AdjustmentListener {
	static final long serialVersionUID = -3787957532000147183L;
	javax.swing.JScrollBar scroll;
	javax.swing.JLabel value;
	BSU radar;

	/**
	 * Constructor
	 * @param frame main window
	 * @param i_radar
	 * @param i_zoomMin minimum available zoom value
	 * @param i_zoomMax maximum available zoom value
	 * @param i_tc TimeControler window
	 */
	public ZoomPanel (javax.swing.JFrame frame, BSU i_radar, int i_zoomMin, int i_zoomMax, javax.swing.JDialog i_tc) {
		super (new java.awt.GridLayout (2, 1));
		radar = i_radar;

		javax.swing.JPanel l_top = new javax.swing.JPanel ();
		l_top.setLayout(new javax.swing.BoxLayout(l_top, javax.swing.BoxLayout.X_AXIS));
		l_top.add(new javax.swing.JLabel(i_zoomMin + " Nm"));

		l_top.add(javax.swing.Box.createHorizontalGlue());
		l_top.add(value = new javax.swing.JLabel(Integer.toString(i_radar.zoomAndCenter)));
		l_top.add(javax.swing.Box.createHorizontalGlue());

		l_top.add(new javax.swing.JLabel(i_zoomMax + " Nm "));
		this.add(l_top);

		scroll = new javax.swing.JScrollBar (javax.swing.JScrollBar.HORIZONTAL, i_radar.zoomAndCenter, 0, i_zoomMin, i_zoomMax);
		scroll.setBlockIncrement ((i_zoomMax-i_zoomMin)/20);
		scroll.setBorder (new javax.swing.border.EmptyBorder(3, 1, 0, 0));
		scroll.addAdjustmentListener(this);
		this.add(scroll);
		setVisible(false);
	}

	/**
	 * Callback method when zoom value changes.
	 */
	public void adjustmentValueChanged(java.awt.event.AdjustmentEvent e) {
		javax.swing.JScrollBar s = (javax.swing.JScrollBar)e.getSource();
		int l_oldZoom = radar.zoomAndCenter;
		value.setText(Integer.toString(radar.zoomAndCenter = s.getValue()));
		javax.swing.JViewport l_viewport = radar.scrollPane.getViewport();
		java.awt.Point p = l_viewport.getViewPosition();
		java.awt.Dimension d = l_viewport.getExtentSize();
		// TODO : complete review of this block
		double l_rat = (double)l_oldZoom / radar.zoomAndCenter;
		p.x = (int)(l_rat * p.x + d.width * (l_rat - 1.0) / 2.0);
		if (p.x < 0) p.x = 0;
		p.y = (int)(l_rat * p.y + d.height * (l_rat - 1.0) / 2.0);
		if (p.y < 0) p.y = 0;
		
		radar.area.width = radar.paramScroll.zoomMax * radar.dataWidth / radar.zoomAndCenter;
		radar.area.height = (int)((double)radar.area.width * radar.paramScroll.yMax / radar.paramScroll.xMax);
		radar.setPreferredSize(radar.area);
		l_viewport.setViewPosition(p);
		radar.revalidate();
	}
}
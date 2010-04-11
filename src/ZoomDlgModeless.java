/**
 * 
 */

class ZoomDlgModeless extends javax.swing.JDialog implements java.awt.event.AdjustmentListener {
	static final long serialVersionUID = -3787957532000147183L;
	javax.swing.JScrollBar m_scroll;
	javax.swing.JLabel m_value;
	BSU m_bsu;

	public ZoomDlgModeless (javax.swing.JFrame frame, BSU i_radar, int i_zoomMin, int i_zoomMax, TimeControler i_tc) {
		super (frame, "Zoom");
		m_bsu = i_radar;
		// 2 areas : the top and the bottom
		javax.swing.JPanel l_center = new javax.swing.JPanel (new java.awt.GridLayout (2, 1));

		javax.swing.JPanel l_top = new javax.swing.JPanel ();
		l_top.setLayout(new javax.swing.BoxLayout(l_top, javax.swing.BoxLayout.X_AXIS));
		l_top.add(new javax.swing.JLabel(i_zoomMin + " Nm"));

		l_top.add(javax.swing.Box.createHorizontalGlue());
		l_top.add(m_value = new javax.swing.JLabel(Integer.toString(i_radar.m_zoomAndCenter)));
		l_top.add(javax.swing.Box.createHorizontalGlue());

		l_top.add(new javax.swing.JLabel(i_zoomMax + " Nm "));
		l_center.add(l_top);

		m_scroll = new javax.swing.JScrollBar (javax.swing.JScrollBar.HORIZONTAL, i_radar.m_zoomAndCenter, 0, i_zoomMin, i_zoomMax);
		m_scroll.setBlockIncrement ((i_zoomMax-i_zoomMin)/20);
		m_scroll.setBorder (new javax.swing.border.EmptyBorder(0, 0, 0, 1));
		m_scroll.addAdjustmentListener(this);
		l_center.add(m_scroll);
		this.getContentPane().add(l_center, java.awt.BorderLayout.CENTER);
		java.awt.Rectangle r = i_tc.getBounds();
		r.y += r.height;
		//setResizable(false);
		setBounds(r);
		setVisible(true);
	}

	public void adjustmentValueChanged(java.awt.event.AdjustmentEvent e) {
		javax.swing.JScrollBar s = (javax.swing.JScrollBar)e.getSource();
		int l_oldZoom = m_bsu.m_zoomAndCenter;
		m_value.setText(Integer.toString(m_bsu.m_zoomAndCenter = s.getValue()));
		javax.swing.JViewport l_viewport = m_bsu.m_scrollPane.getViewport();
		java.awt.Point p = l_viewport.getViewPosition();
		java.awt.Dimension d = l_viewport.getExtentSize();
		double l_rat = (double)l_oldZoom / m_bsu.m_zoomAndCenter;
		p.x = (int)(l_rat * p.x + d.width * (l_rat - 1.0) / 2.0);
		if (p.x < 0) p.x = 0;
		p.y = (int)(l_rat * p.y + d.height * (l_rat - 1.0) / 2.0);
		if (p.y < 0) p.y = 0;
		
		m_bsu.m_area.width = m_bsu.m_ps.m_zoomMax * m_bsu.m_dataWidth / m_bsu.m_zoomAndCenter;
		m_bsu.m_area.height = (int)((double)m_bsu.m_area.width * m_bsu.m_ps.m_yMax / m_bsu.m_ps.m_xMax);
		m_bsu.setPreferredSize(m_bsu.m_area);
		l_viewport.setViewPosition(p);
		m_bsu.revalidate();
	}

}
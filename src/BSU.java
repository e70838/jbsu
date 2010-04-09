// Avertissement : Ce programme est prot'eg'e par la loi relative au droit
// d'auteur et par les conventions internationales. Toute reproduction
// ou distribution partielle ou totale de ce fichier, par quelque moyen que
// ce soit, est strictement interdite. Toute personne ne respectant pas
// ces dispositions se rendra coupable du d'elit de contrefacon et sera
// passible des sanctions p'enales pr'evues par la loi.

//import java.io.*;
//import java.util.*;
import java.awt.*;

public class BSU extends javax.swing.JPanel {
	static final long serialVersionUID = 3963266857345701745L;
	static final String g_dataDir = "dataasas/";

	static final String szAppName = "Bsu";
	static ParamScroll g_ps;
	static FlightData g_fd;
	static BsuData g_bd;

	private static ZoomAndCenter g_zoomAndCenter;
	static java.awt.Dimension g_area = new java.awt.Dimension();

	static int g_dataWidth;
	static int g_dataLastWidth, g_dataLastZoomPos;

	public static javax.swing.JFrame g_hMainWindow;
	static BSU g_radar;
	static TimeControler g_tc;
	static ZoomDlgModeless g_hZoomDlgModeless;
	static javax.swing.JScrollPane g_scrollPane;

	static int g_destX, g_destY;

	// petites fonctions

	static void MoveLabel (FlightLabel i_label, int i_angle) {
		// bearing to trigonometric angle
		double l_angle = BsuUtil.deg2rad((double)((360 + 90 - i_angle) % 360));
		i_label.m_x = 10.0*java.lang.Math.cos(l_angle);
		i_label.m_y = -10.0*java.lang.Math.sin(l_angle);
	}

	static void UpdateMainWindowScrollbar (javax.swing.JFrame hwnd, boolean fRedraw) {
		//		g_dataLastWidth  = g_dataWidth;
		//		g_dataLastHeight = g_dataHeight;
		//		java.awt.Dimension r = hwnd.getSize ();
		//		if (r.height != g_dataHeight) {
		//			javax.swing.JOptionPane.showMessageDialog (null, "r.bottom != cyClient", "debug", javax.swing.JOptionPane.ERROR_MESSAGE);
		//		} else if (r.width != g_dataWidth) {
		//			javax.swing.JOptionPane.showMessageDialog (null, "r.right != cxClient", "debug", javax.swing.JOptionPane.ERROR_MESSAGE);
		//		}
		//		int l_margin = g_ps.m_xMax - g_ps.m_xMin;
		//		if (g_ps.m_yMax - g_ps.m_yMin > l_margin)
		//			l_margin = g_ps.m_yMax - g_ps.m_yMin;
		//
		//		if (g_zoomAndCenter.m_zoomPos < l_margin) //  * 1000
		//			l_margin = g_zoomAndCenter.m_zoomPos; // * 1000;
		//		l_margin /= 2;
		//
		//		//SCROLLINFO g_si;
		//		int g_siPage, g_siMin, g_siMax;
		//
		//		g_siPage = g_zoomAndCenter.m_zoomPos; // * 1000;
		//		g_siMin  = g_ps.m_xMin - l_margin;
		//		g_siMax  = g_ps.m_xMax + l_margin;
		//		if (g_siPage > g_siMax - g_siMin) {
		//			g_siPage = g_siMax - g_siMin + 1;
		//		}
		//		if (g_zoomAndCenter.m_scrollX > g_siMax - g_siPage / 2)
		//			g_zoomAndCenter.m_scrollX = g_siMax - g_siPage / 2;
		//		else if (g_zoomAndCenter.m_scrollX < g_siMin + g_siPage / 2)
		//			g_zoomAndCenter.m_scrollX = g_siMin + g_siPage / 2;
		//		int g_siPos = g_zoomAndCenter.m_scrollX - g_siPage / 2; //  = g_siTrackPos
		//		//TODO SetScrollInfo(hwnd, SB_HORZ, &g_si, fRedraw);
		//		g_siPage = g_zoomAndCenter.m_zoomPos * /* 1000.0 * */ r.height / r.width;
		//		g_siMin = g_ps.m_yMin - l_margin;
		//		g_siMax = g_ps.m_yMax + l_margin;
		//		if (g_siPage > g_siMax - g_siMin) {
		//			g_siPage = g_siMax - g_siMin+1;
		//		}
		//		if (g_zoomAndCenter.m_scrollY > g_siMax - g_siPage / 2)
		//			g_zoomAndCenter.m_scrollY = g_siMax - g_siPage / 2;
		//		else if (g_zoomAndCenter.m_scrollY < g_siMin + g_siPage / 2)
		//			g_zoomAndCenter.m_scrollY = g_siMin + g_siPage / 2;
		//		g_siPos = g_zoomAndCenter.m_scrollY - g_siPage / 2; //  = g_si.nTrackPos
		//		//TODO SetScrollInfo(hwnd, SB_VERT, &g_si, fRedraw);
		//		System.out.println (g_zoomAndCenter);
	}

	static void UpdateZoomAndScroll () {
		ZoomDlgModeless.g_scroll.setMinimum (g_ps.m_zoomMin);
		ZoomDlgModeless.g_scroll.setMaximum (g_ps.m_zoomMax);
		ZoomDlgModeless.g_scroll.setValue   (g_zoomAndCenter.m_zoomPos);
		ZoomDlgModeless.g_scroll.setVisibleAmount (5);
		ZoomDlgModeless.g_value.setText ("" + g_zoomAndCenter.m_zoomPos);
		UpdateMainWindowScrollbar(g_hMainWindow, true);
	}

	AnyAction m_aa;
	javax.swing.JTextArea m_l;

	private javax.swing.JMenuItem createMenuItem (javax.swing.JMenu menu, String name) {
		javax.swing.JMenuItem l_mi = new javax.swing.JMenuItem (name);
		l_mi.setActionCommand (name);
		menu.add (l_mi);
		l_mi.addActionListener (m_aa);
		return l_mi;
	}

	private javax.swing.JMenuItem createMenuItem (javax.swing.JMenu menu, String name, char mnemo) {
		javax.swing.JMenuItem l_mi = createMenuItem (menu, name);
		l_mi.setMnemonic (mnemo);
		return l_mi;
	}

	private javax.swing.JMenuItem createMenuItem (javax.swing.JMenu menu, String name, char mnemo, int accelerator) {
		javax.swing.JMenuItem l_mi = createMenuItem (menu, name, mnemo);
		l_mi.setAccelerator(javax.swing.KeyStroke.getKeyStroke(accelerator, java.awt.event.InputEvent.CTRL_MASK));
		return l_mi;
	}

	private javax.swing.JMenu createMenu (javax.swing.JMenuBar barre, String name, char mnemo) {
		javax.swing.JMenu l_m = new javax.swing.JMenu (name);
		l_m.setMnemonic (mnemo);
		barre.add (l_m);
		return l_m;
	}

	private BSU () {
		g_zoomAndCenter.m_zoomPos = (g_ps.m_zoomMax + g_ps.m_zoomMin)/2;
		g_zoomAndCenter.m_scrollX = 0; // (g_ps.m_xMin+g_ps.m_xMax)/2;
		g_zoomAndCenter.m_scrollY = 0; // (g_ps.m_yMin+g_ps.m_yMax)/2;

		m_aa = new AnyAction();

		javax.swing.ImageIcon i = new javax.swing.ImageIcon("images/bsu.gif");
		g_hMainWindow.setIconImage (i.getImage());

		javax.swing.JMenuBar l_menuBar = new javax.swing.JMenuBar (); 
		javax.swing.JMenu l_file = createMenu (l_menuBar, "File", 'F');

		createMenuItem (l_file, "New", 'N', java.awt.event.KeyEvent.VK_N);
		createMenuItem (l_file, "Open", 'O', java.awt.event.KeyEvent.VK_O);
		createMenuItem (l_file, "Save", 'S', java.awt.event.KeyEvent.VK_S);
		createMenuItem (l_file, "Save As...", 'A');
		l_file.addSeparator () ;
		createMenuItem (l_file, "Print...", 'P', java.awt.event.KeyEvent.VK_P);
		createMenuItem (l_file, "Export", 'E', java.awt.event.KeyEvent.VK_E);
		l_file.addSeparator () ;
		createMenuItem (l_file, "Exit", 'x');

		javax.swing.JMenu l_edit = createMenu (l_menuBar, "Edit", 'E');
		javax.swing.JCheckBoxMenuItem l_editPoint = new javax.swing.JCheckBoxMenuItem ("Point editor", true);
		l_editPoint.setMnemonic('P');
		l_edit.add (l_editPoint);

		javax.swing.JCheckBoxMenuItem l_editRoute = new javax.swing.JCheckBoxMenuItem ("Route editor", true);
		l_editRoute.setMnemonic('R');
		l_edit.add (l_editRoute);

		javax.swing.JMenu l_help = createMenu (l_menuBar, "?", '?');

		createMenuItem (l_help, "About map editor",'A');

		g_hMainWindow.setJMenuBar(l_menuBar);

		g_scrollPane = new javax.swing.JScrollPane(this);

		g_hMainWindow.getContentPane().add(g_scrollPane);
		g_hMainWindow.setDefaultCloseOperation(javax.swing.JFrame.EXIT_ON_CLOSE);
		java.awt.Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		java.awt.Insets l_inset = Toolkit.getDefaultToolkit().getScreenInsets (g_hMainWindow.getGraphicsConfiguration());
		screenSize.width -= l_inset.right + l_inset.left;
		screenSize.height -= l_inset.bottom + l_inset.top;
		int l_height = (3 * screenSize.width / 4) * 3 / 5;
		if (l_height > screenSize.height)
			l_height = screenSize.height;

		g_scrollPane.addComponentListener(new java.awt.event.ComponentAdapter() {
			public void componentResized(java.awt.event.ComponentEvent e) {
				java.awt.Dimension d = e.getComponent().getSize();
				int l_oldWidth = g_dataWidth;
				g_dataWidth = d.width - 20;
				double l_rat = (double)g_dataWidth / (double)l_oldWidth;
				g_area.width = g_ps.m_zoomMax * g_dataWidth / g_zoomAndCenter.m_zoomPos;
				g_area.height = (int)((double)g_area.width * g_ps.m_yMax / g_ps.m_xMax);
				javax.swing.JViewport l_viewport = g_scrollPane.getViewport();
				java.awt.Point p = l_viewport.getViewPosition();
				java.awt.Dimension d2 = l_viewport.getExtentSize();
				p.x = (int)(l_rat * p.x + d2.width * (l_rat - 1.0) / 2.0);
				if (p.x < 0) p.x = 0;
				p.y = (int)(l_rat * p.y + d2.height * (l_rat - 1.0) / 2.0);
				if (p.y < 0) p.y = 0;
				g_radar.setPreferredSize(g_area);
				l_viewport.setViewPosition(p);
			}
		});
		//g_hMainWindow.setSize(3 * screenSize.width / 4, l_height); remplac'e par les 2 lignes suivantes.
		g_scrollPane.setPreferredSize(new Dimension(3 * screenSize.width / 4, l_height));
		g_hMainWindow.pack();
		g_hMainWindow.setVisible(true);
	}


	public void paint (java.awt.Graphics g) {
		g_bd.UpdateScrollAndZoom(g_area, g_ps.m_zoomMax);
		g_bd.DrawAll((java.awt.Graphics2D)g, g_fd);
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		//BsuProjection bp = new BsuProjection(new CoordDegree(BsuUtil.string_to_lat("5325N"), BsuUtil.string_to_long("00616W")));
		BsuProjection bp = new BsuProjection(CoordDegree.c_Paris);
		//BsuProjection bp = new BsuProjection(new CoordDegree(BsuUtil.string_to_lat("6025N"), BsuUtil.string_to_long("00616E")));
		g_bd = new BsuData (bp);
		g_ps = g_bd.m_ps;
		g_fd = new FlightData (bp);
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				//try {
				//	javax.swing.UIManager.setLookAndFeel(javax.swing.UIManager.getSystemLookAndFeelClassName()) ;
				//} catch (Exception e) {System.out.println (e.toString());}
				ZoomAndCenterManager.init(g_zoomAndCenter = new ZoomAndCenter());
				g_hMainWindow = new javax.swing.JFrame ("ATC Player in Java V0.01");
				g_radar = new BSU ();
				g_hMainWindow.setLocationRelativeTo(null); // put the window in the center of the screen
				g_tc = new TimeControler (g_hMainWindow, g_fd);
				g_hZoomDlgModeless = new ZoomDlgModeless (g_hMainWindow);
				g_tc.setResizable(false); //ugly
				g_hZoomDlgModeless.setResizable(false); // ugly
			}});
	}

	class AnyAction implements java.awt.event.ActionListener {
		public void actionPerformed (java.awt.event.ActionEvent ae) {
			String l_action = ae.getActionCommand();
			System.out.println ("actionPerformed " + l_action);
			if (l_action.compareTo ("Inscrire") == 0) {
				//  DlgJI l_test = new DlgJI (f);
			} else if (l_action.compareTo ("About map editor") == 0) {
				//  AboutDlg l_about = new AboutDlg (f);
				System.out.println ("AboutDlg exit");
			} else if (l_action.compareTo ("Exit") == 0) {
				System.exit(0);
			} else {
				System.out.println ("Not yet implemented");
			}
		}
	}

	static class ZoomDlgModeless extends javax.swing.JDialog implements java.awt.event.AdjustmentListener {
		static final long serialVersionUID = -3787957532000147183L;
		static javax.swing.JScrollBar g_scroll;
		static javax.swing.JLabel g_value;

		public ZoomDlgModeless (javax.swing.JFrame frame) {
			super (frame, "Zoom");
			// 2 areas : the top and the bottom
			javax.swing.JPanel l_center = new javax.swing.JPanel (new java.awt.GridLayout (2, 1));

			javax.swing.JPanel l_top = new javax.swing.JPanel ();
			l_top.setLayout(new javax.swing.BoxLayout(l_top, javax.swing.BoxLayout.X_AXIS));
			l_top.add(new javax.swing.JLabel(g_ps.m_zoomMin + " Nm"));

			l_top.add(javax.swing.Box.createHorizontalGlue());
			l_top.add(g_value = new javax.swing.JLabel(Integer.toString(g_zoomAndCenter.m_zoomPos)));
			l_top.add(javax.swing.Box.createHorizontalGlue());

			l_top.add(new javax.swing.JLabel(g_ps.m_zoomMax + " Nm "));
			l_center.add(l_top);

			g_scroll = new javax.swing.JScrollBar (javax.swing.JScrollBar.HORIZONTAL, g_zoomAndCenter.m_zoomPos, 0, g_ps.m_zoomMin, g_ps.m_zoomMax);
			g_scroll.setBlockIncrement ((g_ps.m_zoomMax-g_ps.m_zoomMin)/20);
			g_scroll.setBorder (new javax.swing.border.EmptyBorder(0, 0, 0, 1));
			g_scroll.addAdjustmentListener(this);
			l_center.add(g_scroll);
			this.getContentPane().add(l_center, java.awt.BorderLayout.CENTER);
			java.awt.Rectangle r = g_tc.getBounds();
			r.y += r.height;
			//setResizable(false);
			setBounds(r);
			setVisible(true);
		}

		public void adjustmentValueChanged(java.awt.event.AdjustmentEvent e) {
			javax.swing.JScrollBar s = (javax.swing.JScrollBar)e.getSource();
			double l_oldZoom = (double)g_zoomAndCenter.m_zoomPos;
			g_value.setText(Integer.toString(g_zoomAndCenter.m_zoomPos = s.getValue()));
			javax.swing.JViewport l_viewport = g_scrollPane.getViewport();
			java.awt.Point p = l_viewport.getViewPosition();
			java.awt.Dimension d = l_viewport.getExtentSize();
			double l_rat = l_oldZoom / g_zoomAndCenter.m_zoomPos;
			p.x = (int)(l_rat * p.x + d.width * (l_rat - 1.0) / 2.0);
			if (p.x < 0) p.x = 0;
			p.y = (int)(l_rat * p.y + d.height * (l_rat - 1.0) / 2.0);
			if (p.y < 0) p.y = 0;
			
			g_area.width = g_ps.m_zoomMax * g_dataWidth / g_zoomAndCenter.m_zoomPos;
			g_area.height = (int)((double)g_area.width * g_ps.m_yMax / g_ps.m_xMax);
			g_radar.setPreferredSize(g_area);
			l_viewport.setViewPosition(p);
			g_radar.revalidate();
		}

	}
}

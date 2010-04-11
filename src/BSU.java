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
	ParamScroll m_ps;
	FlightData m_fd;
	BsuData m_bd;

	int m_zoomAndCenter;
	java.awt.Dimension m_area = new java.awt.Dimension();

	int m_dataWidth;
	int m_dataLastWidth, m_dataLastZoomPos;

	javax.swing.JFrame m_hMainWindow;
	BSU m_radar;
	TimeControler m_tc;
	ZoomDlgModeless m_hZoomDlgModeless;
	javax.swing.JScrollPane m_scrollPane;

	int m_destX, m_destY;

	// petites fonctions

	static void MoveLabel (FlightLabel i_label, int i_angle) {
		// bearing to trigonometric angle
		double l_angle = BsuUtil.deg2rad((double)((360 + 90 - i_angle) % 360));
		i_label.m_x = 10.0*java.lang.Math.cos(l_angle);
		i_label.m_y = -10.0*java.lang.Math.sin(l_angle);
	}

	static void UpdateMainWindowScrollbar (javax.swing.JFrame hwnd, boolean fRedraw) {
	}

	void UpdateZoomAndScroll () {
		m_hZoomDlgModeless.m_scroll.setMinimum (m_ps.m_zoomMin);
		m_hZoomDlgModeless.m_scroll.setMaximum (m_ps.m_zoomMax);
		m_hZoomDlgModeless.m_scroll.setValue   (m_zoomAndCenter);
		m_hZoomDlgModeless.m_scroll.setVisibleAmount (5);
		m_hZoomDlgModeless.m_value.setText ("" + m_zoomAndCenter);
		UpdateMainWindowScrollbar(m_hMainWindow, true);
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
		m_radar = this;
		//BsuProjection bp = new BsuProjection(new CoordDegree(BsuUtil.string_to_lat("5325N"), BsuUtil.string_to_long("00616W")));
		BsuProjection l_bp = new BsuProjection(CoordDegree.c_Paris);
		//BsuProjection bp = new BsuProjection(new CoordDegree(BsuUtil.string_to_lat("6025N"), BsuUtil.string_to_long("00616E")));
		m_bd = new BsuData (l_bp);
		m_fd = new FlightData (l_bp);
		m_ps = m_bd.m_ps;
		//try {
		//	javax.swing.UIManager.setLookAndFeel(javax.swing.UIManager.getSystemLookAndFeelClassName()) ;
		//} catch (Exception e) {System.out.println (e.toString());}
		m_hMainWindow = new javax.swing.JFrame ("ATC Player in Java V0.01");
		m_zoomAndCenter = (m_ps.m_zoomMax + m_ps.m_zoomMin)/2;

		m_aa = new AnyAction();

		javax.swing.ImageIcon i = new javax.swing.ImageIcon("images/bsu.gif");
		m_hMainWindow.setIconImage (i.getImage());

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

		m_hMainWindow.setJMenuBar(l_menuBar);

		m_scrollPane = new javax.swing.JScrollPane(this);

		m_hMainWindow.getContentPane().add(m_scrollPane);
		m_hMainWindow.setDefaultCloseOperation(javax.swing.JFrame.EXIT_ON_CLOSE);
		java.awt.Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		java.awt.Insets l_inset = Toolkit.getDefaultToolkit().getScreenInsets (m_hMainWindow.getGraphicsConfiguration());
		screenSize.width -= l_inset.right + l_inset.left;
		screenSize.height -= l_inset.bottom + l_inset.top;
		int l_height = (3 * screenSize.width / 4) * 3 / 5;
		if (l_height > screenSize.height)
			l_height = screenSize.height;

		m_scrollPane.addComponentListener(new java.awt.event.ComponentAdapter() {
			public void componentResized(java.awt.event.ComponentEvent e) {
				java.awt.Dimension d = e.getComponent().getSize();
				int l_oldWidth = m_dataWidth;
				m_dataWidth = d.width - 20;
				double l_rat = (double)m_dataWidth / (double)l_oldWidth;
				m_area.width = m_ps.m_zoomMax * m_dataWidth / m_zoomAndCenter;
				m_area.height = (int)((double)m_area.width * m_ps.m_yMax / m_ps.m_xMax);
				javax.swing.JViewport l_viewport = m_scrollPane.getViewport();
				java.awt.Point p = l_viewport.getViewPosition();
				java.awt.Dimension d2 = l_viewport.getExtentSize();
				p.x = (int)(l_rat * p.x + d2.width * (l_rat - 1.0) / 2.0);
				if (p.x < 0) p.x = 0;
				p.y = (int)(l_rat * p.y + d2.height * (l_rat - 1.0) / 2.0);
				if (p.y < 0) p.y = 0;
				m_radar.setPreferredSize(m_area);
				l_viewport.setViewPosition(p);
			}
		});
		//g_hMainWindow.setSize(3 * screenSize.width / 4, l_height); remplac'e par les 2 lignes suivantes.
		m_scrollPane.setPreferredSize(new Dimension(3 * screenSize.width / 4, l_height));
		m_hMainWindow.pack();
		m_hMainWindow.setVisible(true);
		
		m_hMainWindow.setLocationRelativeTo(null); // put the window in the center of the screen
		m_tc = new TimeControler (m_hMainWindow, m_fd);
		m_hZoomDlgModeless = new ZoomDlgModeless (m_hMainWindow, m_radar, m_bd.m_ps.m_zoomMin, m_bd.m_ps.m_zoomMax, m_tc);
		m_tc.setResizable(false); //ugly
		m_hZoomDlgModeless.setResizable(false); // ugly
		
	}


	public void paint (java.awt.Graphics g) {
		m_bd.UpdateScrollAndZoom(m_area, m_ps.m_zoomMax);
		m_bd.DrawAll((java.awt.Graphics2D)g, m_fd);
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				new BSU ();
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
}

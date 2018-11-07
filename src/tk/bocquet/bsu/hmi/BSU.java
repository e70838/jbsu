/*
 * @(#)BSU.java
 * 
 */

package tk.bocquet.bsu.hmi;

import tk.bocquet.bsu.data.FlightData;
import tk.bocquet.bsu.data.MapData;
import tk.bocquet.bsu.data.XmlReader;
import tk.bocquet.bsu.geometry.CoordDegree;
import tk.bocquet.bsu.geometry.CoordNm;
import tk.bocquet.bsu.geometry.Projection;
import tk.bocquet.bsu.viewer.records.ParamScroll;

//TODO:
// créer une fonte spéciale à inclure dans le .jar: commencé, mais à améliorer (basé sur CENA ATC fonts Blério)
// espacement entre vol et étiquette qui dépend non linéairement du zoom
// impression écran -> peut être amélioré pour sauver sous des noms différents.
// boîte about à améliorer
// l'icon de l'application a un soucis sous Linux
// lorsqu'on agrandit la fenêtre, le centre devrait rester immobile
// prendre en compte tout ce qui concerne ASASl'application a un soucis sous Linux
//lorsqu'on agrandit la fenêtre, le centre devrait rester immobile
//prendre en compte tout ce qui concerne ASAS
// options d'affichage: conventions papier versus écran radar, configurabilité

//Known bugs:
// - LPGL license for font is not respected -> in progress: mail sent to owner

/**
 * Main class of BSU ATC viewer
 * 
 * @author Jean-François Bocquet
 *
 */
public class BSU extends javax.swing.JPanel implements Runnable {
	static final long serialVersionUID = 3963266857345701745L;

	String title;

	/**
	 * The projection handles conversion from geographic location to screen coordinates in Nm and vice versa.
	 */
	Projection projection;

	/**
	 * The scroll parameters are used to convert from screen coordinates in Nm to pixels and vice versa. 
	 */
	ParamScroll paramScroll;

	/**
	 * FlightData contains all the flights related data and their drawing method.
	 */
	FlightData flightData;

	/**
	 * MapData contains all the background map related data and their drawing method.
	 */
	MapData mapData;

	javax.swing.JFrame mainWindow;
	TimeControler timeControler;
	RangeAndBearing rangeAndBearing;
	javax.swing.JScrollPane scrollPane;

	int zoomAndCenter;
	java.awt.Dimension area = new java.awt.Dimension();
	int dataWidth;

	/**
	 * Nested object that will handle all the events: mouse and menu
	 */
	AnyAction actionsHandler;

	/**
	 * Screendump
	 */
	public void printScreen () {
		javax.swing.JViewport l_viewPort = scrollPane.getViewport();
		java.awt.Point l_p = l_viewPort.getViewPosition();
		java.awt.Dimension l_d = l_viewPort.getExtentSize();
		java.awt.image.BufferedImage image = new java.awt.image.BufferedImage (l_d.width, l_d.height, java.awt.image.BufferedImage.TYPE_3BYTE_BGR);
		java.awt.Graphics2D g = image.createGraphics();
		g.translate(-l_p.x, -l_p.y);
		this.paint(g);
		try {
			javax.imageio.ImageIO.write(image, "png", new java.io.File ("saved.png"));
		} catch (java.io.IOException e) {
			System.out.println ("IOException");
		}
	}

	/**
	 * Draw all the graphical elements: map, then flights, then subtitle, then range and bearing
	 */
	public void paint (java.awt.Graphics g) {
		if (area.width == 0) return;
		g.setColor(Resources.g_hBrushBgrdAll);
		g.fillRect(0, 0, area.width, area.height);
		double l_rat  = (double)area.width / (double)paramScroll.zoomMax;
		long l_t0 = java.lang.System.nanoTime();
		mapData.draw((java.awt.Graphics2D)g, l_rat);
		long l_t1 = java.lang.System.nanoTime();
		flightData.draw((java.awt.Graphics2D)g, l_rat, paramScroll.shiftX, paramScroll.shiftY, area.width);
		long l_t2 = java.lang.System.nanoTime();
		tk.bocquet.bsu.viewer.records.Subtitle l_subs = flightData.currentSubtitle ();
		if (l_subs != null) {
			javax.swing.JViewport l_viewPort = scrollPane.getViewport();
			java.awt.Point l_p = l_viewPort.getViewPosition();
			java.awt.Dimension l_d = l_viewPort.getExtentSize();
			int y = l_p.y + l_d.height - 60;
			g.setFont (Resources.g_subfont);
			java.awt.font.FontRenderContext l_c = ((java.awt.Graphics2D)g).getFontRenderContext();
			while (l_subs != null) {
				if (l_subs.controller == timeControler.curController) {
					java.awt.geom.Rectangle2D l_r = Resources.g_subfont.getStringBounds (l_subs.sentence, l_c);
					int x = l_p.x + (int)(l_d.width - l_r.getWidth())/ 2;
					//java.awt.font.TextLayout l_tl = new java.awt.font.TextLayout (l_subs.m_sentence, Resources.g_subfont, l_c);
					//java.awt.geom.AffineTransform l_transform = new java.awt.geom.AffineTransform();
					//l_transform.setToTranslation(x, y);
					//java.awt.Shape shape = l_tl.getOutline (l_transform);
					g.setColor(Resources.g_colors[Resources.c_black]);
					//((java.awt.Graphics2D)g).draw(shape);
					g.drawString (l_subs.sentence, x-2, y);
					g.drawString (l_subs.sentence, x+2, y);
					g.drawString (l_subs.sentence, x, y-2);
					g.drawString (l_subs.sentence, x, y+2);
					g.drawString (l_subs.sentence, x-1, y-1);
					g.drawString (l_subs.sentence, x+1, y+1);
					g.drawString (l_subs.sentence, x+1, y-1);
					g.drawString (l_subs.sentence, x-1, y+1);
					g.setColor(Resources.g_colors[Resources.c_yellow]);
					g.drawString (l_subs.sentence, x, y);
					y += 25;
				}
				l_subs = l_subs.next;
			}
		}
		if (rangeAndBearing != null) {
			rangeAndBearing.draw((java.awt.Graphics2D)g, l_rat);
		}
		long l_t3 = java.lang.System.nanoTime();
		System.out.println ("map Drawing " + (l_t1 - l_t0)/1000);
		System.out.println ("flight Drawing " + (l_t2 - l_t1)/1000);
		System.out.println ("subs and R&B Drawing " + (l_t3 - l_t2)/1000);
	}

	/**
	 * Internal method to create a menu item
	 * @param menu parent menu
	 * @param name text of the menu entry
	 * @return
	 */
	private javax.swing.JMenuItem createMenuItem (javax.swing.JMenu menu, String name) {
		javax.swing.JMenuItem l_mi = new javax.swing.JMenuItem (name);
		l_mi.setActionCommand (name);
		menu.add (l_mi);
		l_mi.addActionListener (actionsHandler);
		return l_mi;
	}

	/**
	 * Internal method to create a menu item with a mnemonic (underlined character)
	 * @param menu parent menu
	 * @param name text of the menu entry
	 * @param mnemo letter to underline
	 * @return
	 */
	private javax.swing.JMenuItem createMenuItem (javax.swing.JMenu menu, String name, char mnemo) {
		javax.swing.JMenuItem l_mi = createMenuItem (menu, name);
		l_mi.setMnemonic (mnemo);
		return l_mi;
	}

	/**
	 * Internal method to create a menu item with a mnemonic and an accelerator
	 * @param menu parent menu
	 * @param name text of the menu entry
	 * @param mnemo letter to underline
	 * @param accelerator accelerator
	 * @return
	 */
	private javax.swing.JMenuItem createMenuItem (javax.swing.JMenu menu, String name, char mnemo, int accelerator) {
		javax.swing.JMenuItem l_mi = createMenuItem (menu, name, mnemo);
		l_mi.setAccelerator(javax.swing.KeyStroke.getKeyStroke(accelerator, java.awt.event.InputEvent.CTRL_MASK));
		return l_mi;
	}

	/**
	 * Internal method to add a new menu to a menu bar
	 * @param bar menu bar that will contain the menu
	 * @param name title of the menu
	 * @param mnemo letter to underline
	 * @return
	 */
	private javax.swing.JMenu createMenu (javax.swing.JMenuBar bar, String name, char mnemo) {
		javax.swing.JMenu l_m = new javax.swing.JMenu (name);
		l_m.setMnemonic (mnemo);
		bar.add (l_m);
		return l_m;
	}

	/**
	 * Default constructor of the main class
	 */
	private BSU () {
		rangeAndBearing = null;
		XmlReader fromXml = new XmlReader("t.xml"); // "t.xml" vs null
		this.title = fromXml.getTitle();
		this.flightData = fromXml.getFlightData();
		this.mapData = fromXml.getMapData();
		this.projection = fromXml.getProjection();
		this.paramScroll = fromXml.getParamScroll();
	}

	/**
	 * All the graphical objects are created in this method in order to be in Swing thread.
	 */
	public void run() {
		//		try { // "javax.swing.plaf.metal.MetalLookAndFeel"
		//			javax.swing.UIManager.setLookAndFeel(javax.swing.UIManager.getSystemLookAndFeelClassName());
		//		} catch (Exception e) {System.out.println (e.toString());}
		mainWindow = new javax.swing.JFrame (this.title);
		zoomAndCenter = (paramScroll.zoomMax + paramScroll.zoomMin)/2;

		actionsHandler = new AnyAction();

		javax.swing.ImageIcon i = Resources.createImageIcon("bsu.gif");
		mainWindow.setIconImage (i.getImage());

		javax.swing.JMenuBar l_menuBar = new javax.swing.JMenuBar (); 

		javax.swing.JMenu l_file = createMenu (l_menuBar, "File", 'F');
		createMenuItem (l_file, "Open", 'O', java.awt.event.KeyEvent.VK_O);
		l_file.addSeparator ();
		createMenuItem (l_file, "Print...", 'P', java.awt.event.KeyEvent.VK_P);
		l_file.addSeparator ();
		createMenuItem (l_file, "Exit", 'x');

		javax.swing.JMenu l_help = createMenu (l_menuBar, "?", '?');
		createMenuItem (l_help, "About BSU viewer",'A');

		this.mainWindow.setJMenuBar(l_menuBar);

		this.scrollPane = new javax.swing.JScrollPane(this);
		this.scrollPane.getViewport().setScrollMode(javax.swing.JViewport.SIMPLE_SCROLL_MODE);

		this.addMouseListener (actionsHandler);
		this.addMouseMotionListener (actionsHandler);

		this.mainWindow.getContentPane().add(this.scrollPane);
		this.mainWindow.setDefaultCloseOperation(javax.swing.JFrame.EXIT_ON_CLOSE);
		java.awt.Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
		java.awt.Insets l_inset = java.awt.Toolkit.getDefaultToolkit().getScreenInsets (mainWindow.getGraphicsConfiguration());
		screenSize.width -= l_inset.right + l_inset.left;
		screenSize.height -= l_inset.bottom + l_inset.top;
		if (screenSize.width > 1280) screenSize.width = 1280;
		int l_height = (3 * screenSize.width / 4) * 3 / 5;
		if (l_height > screenSize.height)
			l_height = screenSize.height;

		scrollPane.addComponentListener(new java.awt.event.ComponentAdapter() {
			public void componentResized(java.awt.event.ComponentEvent e) {
				java.awt.Dimension d = e.getComponent().getSize();
				// TODO : complete review of this block of code
				int l_oldWidth = dataWidth;
				dataWidth = d.width - 20;
				double l_rat = (double)dataWidth / (double)l_oldWidth;
				area.width = paramScroll.zoomMax * dataWidth / zoomAndCenter;
				area.height = (int)((double)area.width * paramScroll.yMax / paramScroll.xMax);
				javax.swing.JViewport l_viewport = scrollPane.getViewport();
				java.awt.Point p = l_viewport.getViewPosition();
				java.awt.Dimension d2 = l_viewport.getExtentSize();
				p.x = (int)(l_rat * p.x + d2.width * (l_rat - 1.0) / 2.0);
				if (p.x < 0) p.x = 0;
				p.y = (int)(l_rat * p.y + d2.height * (l_rat - 1.0) / 2.0);
				if (p.y < 0) p.y = 0;
				BSU.this.setPreferredSize(area);
				l_viewport.setViewPosition(p);
			}
		});
		//g_hMainWindow.setSize(3 * screenSize.width / 4, l_height); replaced by the 2 lines after.
		scrollPane.setPreferredSize(new java.awt.Dimension(3 * screenSize.width / 4, l_height));
		mainWindow.pack();
		mainWindow.setVisible(true);

		mainWindow.setLocationRelativeTo(null); // put the window in the center of the screen
		timeControler = new TimeControler (mainWindow, flightData, this, paramScroll.zoomMin, paramScroll.zoomMax);
		//timeControler.setResizable(false); //ugly
	}

	public void aboutDialog(java.awt.Component i_parent) {
		String[] message = {"BSU viewer", " ", "contributed by", "Stéphanie Sauer", "Nathalie Banoun", "Caroline Aiglon", "Frédéric Calichiama",
				"Tarek Benfadhel", "Jean-François Bocquet", "Frédéric Granie", "François Dalrue", "Ilham Bennani", "Tanguy Le-Duff", "Fabrice Brossard",
				"Nicolas Cune-Remy", "Thierry Mandon"};
		String options[] = {"Ok"};
		@SuppressWarnings("unused")
		int result = javax.swing.JOptionPane.showOptionDialog(i_parent, message, "About BSU viewer", javax.swing.JOptionPane.DEFAULT_OPTION, javax.swing.JOptionPane.INFORMATION_MESSAGE, null, options, null);
	}

	/**
	 * static main method.
	 * @param args arguments are ignored
	 */
	public static void main(String[] args) {
		// Single threaded application : the only thread is swing
		javax.swing.SwingUtilities.invokeLater(new BSU ());
	}

	/**
	 * AnyAction is the nested class that handles all events: mouse and menu.
	 */
	// MouseAdapter is not used because it has changed between java 1.5 and 1.6
	class AnyAction implements java.awt.event.ActionListener, java.awt.event.MouseMotionListener, java.awt.event.MouseListener {
		java.io.File file;
		
		/**
		 * Horizontal position of mouse at previous mouse drag event
		 */
		int lastMouseX;

		/**
		 * Vertical position of mouse at previous mouse drag event
		 */
		int lastMouseY;

		/**
		 * Last position of mouse in Nm
		 */
		private CoordNm lastMouseNm = new CoordNm();

		/**
		 * Last position of mouse in Degree
		 */
		private CoordDegree lastMouseDegree = new CoordDegree();

		/**
		 * A menu item has been clicked
		 */
		public void actionPerformed (java.awt.event.ActionEvent ae) {
			String l_action = ae.getActionCommand();
			System.out.println ("actionPerformed " + l_action);
			if (l_action.compareTo ("About BSU viewer") == 0) {
				aboutDialog(BSU.this.mainWindow);
			} else if (l_action.compareTo ("Exit") == 0) {
				System.exit(0);
			} else if (l_action.compareTo ("Open") == 0) {
				javax.swing.JFileChooser fileChooser;
				if (this.file == null) {
					fileChooser = new javax.swing.JFileChooser();
				} else {
					fileChooser = new javax.swing.JFileChooser(file);
				}
				int selected = fileChooser.showOpenDialog(BSU.this.mainWindow);
				if (selected == javax.swing.JFileChooser.APPROVE_OPTION) {
					file = fileChooser.getSelectedFile();
					try {
						XmlReader fromXml = new XmlReader(file.getCanonicalPath());
						BSU.this.title = fromXml.getTitle();
						BSU.this.mainWindow.setTitle(BSU.this.title);
						BSU.this.flightData = fromXml.getFlightData();
						BSU.this.mapData = fromXml.getMapData();
						BSU.this.projection = fromXml.getProjection();
						BSU.this.paramScroll = fromXml.getParamScroll();
						zoomAndCenter = (paramScroll.zoomMax + paramScroll.zoomMin)/2;
						BSU.this.timeControler.dispose();
						BSU.this.timeControler = new TimeControler (BSU.this.mainWindow, BSU.this.flightData, BSU.this, BSU.this.paramScroll.zoomMin, BSU.this.paramScroll.zoomMax);
						BSU.this.mainWindow.repaint ();
					} catch (Error e) {
						System.out.println ("Error " + e);
					} catch (java.io.IOException e) {
						System.out.println ("IOException " + e);
					}
				}
				System.out.println ("Open Not yet implemented");
			} else {
				System.out.println ("Not yet implemented");
			}
		}

		/**
		 * A mouse button has been pressed
		 */
		public void mousePressed(java.awt.event.MouseEvent e) {
			// Only the left button is used.
			if ((e.getButton() & java.awt.event.MouseEvent.BUTTON1) != 0) {
				if (BSU.this.rangeAndBearing == null && BSU.this.flightData.selectedLabel == null) {
					BSU.this.flightData.hotspot(this.lastMouseX = e.getX(), this.lastMouseY = e.getY());
				}
				if (BSU.this.flightData.selectedLabel == null) {
					BSU.this.rangeAndBearing = new RangeAndBearing (BSU.this.projection, BSU.this.paramScroll, e.getPoint(), (double)BSU.this.area.width / (double)BSU.this.paramScroll.zoomMax);
				}
			}
		}

		/**
		 * The mouse has been moved while a button was pressed
		 */
		public void mouseDragged(java.awt.event.MouseEvent e) {
			updateLatLong (e.getPoint());
			if (BSU.this.rangeAndBearing != null) {
				BSU.this.rangeAndBearing.move(e.getPoint(), (double)BSU.this.area.width / (double)BSU.this.paramScroll.zoomMax);
				BSU.this.repaint();
			} else if (BSU.this.flightData.selectedLabel != null) {
				BSU.this.flightData.selectedLabel.dx += e.getX() - this.lastMouseX;
				BSU.this.flightData.selectedLabel.dy += e.getY() - this.lastMouseY;
				this.lastMouseX = e.getX();
				this.lastMouseY = e.getY();
				BSU.this.repaint();
			}
		}

		/**
		 * A mouse button has been released
		 */
		public void mouseReleased(java.awt.event.MouseEvent e) {
			if (BSU.this.rangeAndBearing != null) {
				BSU.this.rangeAndBearing = null;
				BSU.this.repaint();
			} else if (BSU.this.flightData.selectedLabel != null ) {
				double l_rat  = (double)BSU.this.area.width / (double)BSU.this.paramScroll.zoomMax;
				BSU.this.flightData.applyLabelMove (l_rat, BSU.this.paramScroll.shiftX, BSU.this.paramScroll.shiftY);
			}
		}

		/**
		 * Mouse move. If this happens after a mouse pressed, this means that a mouse released has not been detected
		 */
		public void mouseMoved(java.awt.event.MouseEvent e) {
			updateLatLong (e.getPoint());
			mouseReleased (e);
		}

		/**
		 * Mouse entering in the window is ignored
		 */
		public void mouseEntered(java.awt.event.MouseEvent e) {
		}

		/**
		 * Mouse exiting the window is ignored
		 */
		public void mouseExited(java.awt.event.MouseEvent e) {
		}

		/**
		 * Mouse click are ignored
		 */
		public void mouseClicked(java.awt.event.MouseEvent e) {
		}

		private void updateLatLong (java.awt.Point i_point) {
			double l_rat  = (double)BSU.this.area.width / (double)BSU.this.paramScroll.zoomMax;
			this.lastMouseNm.x = (double)i_point.x / l_rat - BSU.this.paramScroll.shiftX;
			this.lastMouseNm.y = (double)i_point.y / l_rat - BSU.this.paramScroll.shiftY;
			BSU.this.projection.stereo2geo(this.lastMouseNm, this.lastMouseDegree);
			BSU.this.timeControler.updateLatLong(this.lastMouseDegree);
		}
	}
}

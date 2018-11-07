/*
 * @(#)TimeControler.java
 * 
 */

package tk.bocquet.bsu.hmi;


import tk.bocquet.bsu.data.FlightData;
import tk.bocquet.bsu.geometry.CoordDegree;
import tk.bocquet.bsu.geometry.ParseLatLong;

/**
 * TimeControler constructs the floating window that contains VCR like commands and implements its behaviour.
 * 
 * @author Jean-François Bocquet
 *
 */
public class TimeControler extends javax.swing.JDialog implements java.awt.event.ActionListener, java.awt.event.AdjustmentListener {
	BSU radar;
	
	/**
	 * Mean delay between two time ticks expressed in milliseconds.
	 */
	private int timeTick;
	
	/**
	 * link to the flight data
	 */
	private FlightData flightData;

	/**
	 * Time in millisecond of the creation of the class
	 */
	private long startCounter;

	/**
	 * Scrollbar to adjust zoom ratio. Part of the window that may be displayed or hiden
	 */
	ZoomPanel zoompanel;

	/**
	 * Current simulation time (relative to exercise start time) in millisecond
	 */
	int curuTime;

	/**
	 * Last value returned by UTime taken into account.
	 */
	int startUTime;

	/**
	 * Speed values
	 */
	final int c_timeSteps[] = {-4, 1, 2, 4, 8, 16, 32};

	/**
	 * Speed values description 
	 */
	final String c_playingSteps[] = {"Rewind x4", "Playing", "Playing x2", "Playing x4", "Playing x8", "Playing x16", "Playing x32"};

	/**
	 * Default index of reading speed.
	 */
	final int c_defaultTimeStep = 1;

	/**
	 * Index of current reading speed.
	 */
	int curTimeStep = c_defaultTimeStep;

	/**
	 * Index of current controller (speaker)
	 */
	int curController;
	
	/**
	 * serialVersionUID ???
	 */
	static final long serialVersionUID = -1652849151066093895L;

	/**
	 * The Start/Pause toggle button has variable tooltiptext and can have its
	 * state changed without user action (automatic stop at the end)
	 */
	javax.swing.JToggleButton startPause;
	
	/**
	 * The scrollbar position adjusts automatically when time flows
	 */
	javax.swing.JScrollBar scroll;
	
	/**
	 * The label indicating current simulation time is always updated
	 */
	javax.swing.JLabel hWndTime;
	
	/**
	 * Latitude of the point below the mouse
	 */
	javax.swing.JLabel hLat;
	
	/**
	 * Longitude of the point below the mouse
	 */
	javax.swing.JLabel hLong;
	
	/**
	 * Popup menu for speed selection
	 */
	javax.swing.JPopupMenu speedMenu;
	
	/**
	 * Popup menu for frequency speaking selection
	 */
	javax.swing.JPopupMenu speakerMenu;

	/**
	 * Internal method to add a button to the toolbar
	 * @param tb      toolbar object
	 * @param image   image displayed on the button 16x16
	 * @param tooltip tooltip
	 * @param insets  margin used around the button
	 * @return        the newly created JButton object
	 */
	private javax.swing.JButton createToolbarButton (javax.swing.JToolBar tb, String image, String tooltip, java.awt.Insets insets) {
		javax.swing.JButton l_b = new javax.swing.JButton(Resources.createImageIcon(image));
		l_b.setMargin (insets);
		l_b.setToolTipText(tooltip);
		l_b.setActionCommand (tooltip);
		tb.add(l_b);
		l_b.addActionListener (this);
		return l_b;
	}

	/**
	 * Update the coordinates displayed. This indicates the mouse position.
	 * @param i_coord
	 */
	public void updateLatLong (CoordDegree i_coord) {
		this.hLat.setText(ParseLatLong.latitudeToString(i_coord.latitude));
		this.hLong.setText(ParseLatLong.longitudeToString(i_coord.longitude));
	}
	
	/**
	 * Compute the time elapsed since the start of the program.
	 */
	int UTime () {
		return (int)(java.lang.System.currentTimeMillis() - this.startCounter);
	}

	/**
	 * this function makes the time flow. If time tick has changed, everything is updated
	 */
	void TimeFlowing () {
		int l_delta = UTime () - this.startUTime;
		if (l_delta == 0) return;
		this.startUTime += l_delta;
		SetCurUTime (this.curuTime + l_delta * this.c_timeSteps[this.curTimeStep]);
		if (this.scroll.getValue() != this.curuTime / this.timeTick)
			this.scroll.setValue (this.curuTime / this.timeTick);
	}

	/**
	 * this function updates the current time, but not the scrollbar
	 * @param i_uTime the future value for simulation time : m_curuTime. This will be adjusted in the range 0 .. m_fd.getNbTimeTick() * m_timeTick - 1
	 */
	void SetCurUTime (int i_uTime) {
		// Adjust the desired time position in valid range
		if (i_uTime < 0) {
			i_uTime = 0;
			this.startPause.setSelected(false);
			this.curTimeStep = this.c_defaultTimeStep;
			this.startPause.setToolTipText ("Start");
			setTitle ("Paused");
		} else if (i_uTime > this.flightData.timeTicks.size() * this.timeTick - 1) {
			i_uTime = this.flightData.timeTicks.size() * this.timeTick - 1;
			this.startPause.setSelected(false);
			this.curTimeStep = this.c_defaultTimeStep;
			this.startPause.setToolTipText ("Start");
			setTitle ("Paused");
		}
		// do nothing if time has not changed
		if (i_uTime == this.curuTime) return;
		// update the buttons status
		boolean l_timeHasChanged = (i_uTime / this.timeTick) != (this.curuTime / this.timeTick);
		this.curuTime = i_uTime;
		// TODO if (LabelMove.setTime (m_curuTime/c_timeTick + FlightData.GetFirstTime ()/5)) l_st = true;
		if (l_timeHasChanged)
			hWndTime.setText (this.flightData.setCurTimeTick (this.curuTime / this.timeTick));
		//if (ZoomAndCenterManager.setTime(m_fd.getCurTime ())) {
			//TODO BSU.UpdateZoomAndScroll ();
		//	l_timeHasChanged = true;
		//}
		if (l_timeHasChanged)
			//BSU.m_hMainWindow.repaint ();
			getOwner ().repaint();
	}

	/**
	 * Constructor
	 * @param i_frame
	 * @param i_fd
	 * @param i_radar
	 * @param i_zoomMin
	 * @param i_zoomMax
	 */
	public TimeControler (javax.swing.JFrame i_frame, FlightData i_fd, BSU i_radar, int i_zoomMin, int i_zoomMax) {
		super (i_frame, "Playing");
		this.radar = i_radar;
		this.flightData = i_fd;
		this.startCounter = java.lang.System.currentTimeMillis();
		this.curuTime = 0;
		this.curController = 0;
		this.speedMenu = new javax.swing.JPopupMenu ();
		this.speedMenu.add ("x-4").addActionListener(this);
		this.speedMenu.add ("x1").addActionListener(this);
		this.speedMenu.add ("x2").addActionListener(this);
		this.speedMenu.add ("x4").addActionListener(this);
		this.speedMenu.add ("x8").addActionListener(this);
		this.speedMenu.add ("x16").addActionListener(this);
		this.speedMenu.add ("x32").addActionListener(this);
		this.speedMenu.pack ();
		String [] l_controlers = this.flightData.controllers;
		if (l_controlers.length > 1) {
			this.speakerMenu = new javax.swing.JPopupMenu ();
			for (String l : l_controlers) {
				this.speakerMenu.add (l).addActionListener(this);
			}
		} else {
			this.speakerMenu = null;
		}
		//this.setUndecorated (true);
		javax.swing.JPanel l_right = new javax.swing.JPanel (new java.awt.GridLayout (2, 1));
		l_right.setBorder (new javax.swing.border.EtchedBorder(javax.swing.border.EtchedBorder.LOWERED));
		this.hLat = new javax.swing.JLabel ("49�00N00");
		l_right.add (this.hLat);
		this.hLong = new javax.swing.JLabel ("  2�00E00");
		l_right.add (this.hLong);
		
		javax.swing.JPanel l_fixedPart = new javax.swing.JPanel (new java.awt.BorderLayout ());
		l_fixedPart.add(l_right, java.awt.BorderLayout.EAST);

		java.awt.Insets l_insets = new java.awt.Insets (0, 0, 0, 0);
		javax.swing.JToolBar l_hwndTB = new javax.swing.JToolBar();
		l_hwndTB.setFloatable(false);

		this.startPause = new javax.swing.JToggleButton(Resources.createImageIcon("start.gif"), true);
		this.startPause.setSelectedIcon(Resources.createImageIcon("pause.gif"));
		this.startPause.setMargin (l_insets);
		this.startPause.setToolTipText("Pause");
		this.startPause.setActionCommand ("StartPause");
		l_hwndTB.add(this.startPause);
		this.startPause.addActionListener (this);

		createToolbarButton (l_hwndTB, "stop.gif", "Stop", l_insets);
		l_hwndTB.addSeparator();
		createToolbarButton (l_hwndTB, "step_rwd.gif", "5s rewind", l_insets);
		this.hWndTime = new javax.swing.JLabel (flightData.setCurTimeTick(0));
		javax.swing.border.BevelBorder l_b = new javax.swing.border.BevelBorder (javax.swing.border.BevelBorder.LOWERED);
		this.hWndTime.setBorder (new javax.swing.border.CompoundBorder (new javax.swing.border.EmptyBorder(0, 3, 0, 3),
				new javax.swing.border.CompoundBorder (l_b, new javax.swing.border.EmptyBorder(1, 4, 1, 4))));
		l_hwndTB.add (this.hWndTime);
		createToolbarButton (l_hwndTB, "step_fwd.gif", "5s forward", l_insets);
		l_hwndTB.addSeparator();
		createToolbarButton (l_hwndTB, "speed.gif",    "Playback speed", l_insets).setComponentPopupMenu(this.speedMenu);
		l_hwndTB.addSeparator();
		//createToolbarButton (l_hwndTB, "loop.gif",     "Loop at the end");
		if (this.speakerMenu != null)
			createToolbarButton (l_hwndTB, "speaker.gif",  "Controler choice", l_insets).setComponentPopupMenu(this.speakerMenu);
		
		javax.swing.JToggleButton l_tb = new javax.swing.JToggleButton(Resources.createImageIcon("zoom.gif"));
		l_tb.setMargin (l_insets);
		l_tb.setToolTipText("Zoom");
		l_tb.setActionCommand ("Zoom");
		l_hwndTB.add(l_tb);
		l_tb.addActionListener (this);
		
		createToolbarButton (l_hwndTB, "screendump.gif", "Screendump", l_insets);
		l_hwndTB.addSeparator();
		createToolbarButton (l_hwndTB, "about.gif", "About BSU viewer", l_insets);

		javax.swing.JPanel l_fixedPartLeft = new javax.swing.JPanel (new java.awt.GridLayout (2, 1));
		l_fixedPartLeft.add(l_hwndTB);
		this.scroll = new javax.swing.JScrollBar (javax.swing.JScrollBar.HORIZONTAL, 0, 0, 0, this.flightData.timeTicks.size()-1);
		
		this.timeTick = (this.flightData.getLastTime() - this.flightData.getFirstTime()) * 1000 / (this.flightData.timeTicks.size() - 1);
		System.out.println ("m_timeTick = " + this.timeTick);
		this.scroll.addAdjustmentListener (this); // TODO prise en compte des raccourcis clavier
		this.scroll.setBorder (new javax.swing.border.EmptyBorder(3, 1, 0, 0));
		l_fixedPartLeft.add(this.scroll);
		l_fixedPart.add(l_fixedPartLeft, java.awt.BorderLayout.CENTER);
		this.getContentPane().setLayout(new java.awt.GridLayout (1, 1));
		this.getContentPane().add(l_fixedPart);
		this.zoompanel = new ZoomPanel (i_frame, i_radar, i_zoomMin, i_zoomMax, this);
		//this.getContentPane().add(zoompanel);
		//this.getContentPane().add(l_topAndBottom, java.awt.BorderLayout.CENTER);
		//TODO java.awt.Point p = BSU.m_hMainWindow.getLocation();
		java.awt.Point p = getOwner().getLocation();
		if (p.x < 0) p.x = 0;
		if (p.y < 0) p.y = 0;
		setLocation (p.x+4, p.y+20);
		pack();
		this.setDefaultCloseOperation(javax.swing.JDialog.DO_NOTHING_ON_CLOSE);
		setVisible(true);

		javax.swing.Timer l_timer = new javax.swing.Timer(10, this);
		l_timer.setActionCommand("Timer");
		this.setResizable(false); // ugly
		l_timer.start();
	}

	/**
	 * Take into account modification of scrollbar position.
	 */
	public void adjustmentValueChanged (java.awt.event.AdjustmentEvent e) {
		if (e.getValue () != this.curuTime / this.timeTick)
			SetCurUTime (this.timeTick * e.getValue ());
	}
	
	static void Bug (String i_msg) {
		// TODO: add a fatal error message box ?
		throw new Error (i_msg);
	}

	/**
	 * Button pressed or timer event.
	 */
	public void actionPerformed (java.awt.event.ActionEvent ae) {
		int l_newSpeed = 100;
		String l_action = ae.getActionCommand();
		if (ae.getSource() instanceof javax.swing.JMenuItem) {
			java.awt.Container l_c = ((javax.swing.JMenuItem)ae.getSource()).getParent();
			if (l_c == this.speedMenu) {
				if (l_action.compareTo ("x-4") == 0) {
					l_newSpeed = 0;
				} else if (l_action.compareTo ("x1") == 0) {
					l_newSpeed = 1;
				} else if (l_action.compareTo ("x2") == 0) {
					l_newSpeed = 2;
				} else if (l_action.compareTo ("x4") == 0) {
					l_newSpeed = 3;
				} else if (l_action.compareTo ("x8") == 0) {
					l_newSpeed = 4;
				} else if (l_action.compareTo ("x16") == 0) {
					l_newSpeed = 5;
				} else if (l_action.compareTo ("x32") == 0) {
					l_newSpeed = 6;
				} else {
					Bug("Unknown speed " + l_action);
				}
			} else if (l_c == this.speakerMenu) {
				String [] l_controlers = this.flightData.controllers;
				int l_r = 100;
				for (int i = 0 ; i < l_controlers.length; i++) {
					if (l_controlers[i].compareTo(l_action) == 0) {
						l_r = i;
					}
				}
				if (l_r != 100) {
					this.curController = l_r;
				} else {
					Bug("Unknown speaker " + l_action);
				}
			} else {
				System.out.println ("Click on a menuItem of nothing known");
			}
		} else if (l_action.compareTo ("Timer") == 0) {
			if (this.startPause.isSelected()) {
				TimeFlowing ();
			}
		} else if (l_action.compareTo ("StartPause") == 0) {
			if (this.startPause.isSelected()) {
				this.startPause.setToolTipText ("Pause");
				setTitle (this.c_playingSteps[this.curTimeStep]);
				this.startUTime = UTime ();
			} else {
				this.startPause.setToolTipText ("Start");
				setTitle ("Paused");
			}
		} else if (l_action.compareTo ("Stop") == 0) {
			// shut sound
			if (this.curuTime == 0) this.curuTime = 1;
			SetCurUTime (-1);
			this.scroll.setValue (0);
		} else if (l_action.compareTo ("5s rewind") == 0) {
			SetCurUTime (this.curuTime - this.timeTick);
			this.scroll.setValue (this.curuTime / this.timeTick);
		} else if (l_action.compareTo ("5s forward") == 0) {
			SetCurUTime (curuTime + timeTick);
			this.scroll.setValue (curuTime / timeTick);
		} else if (l_action.compareTo ("Playback speed") == 0) {
			this.speedMenu.show ((javax.swing.JButton)ae.getSource(), 0, 0);
		} else if (l_action.compareTo ("Controler choice") == 0) {
			this.speakerMenu.show ((javax.swing.JButton)ae.getSource(), 0, 0);
		} else if (l_action.compareTo ("Zoom") == 0) {
			java.awt.Dimension r = this.getSize();
			java.awt.Dimension client = this.getContentPane().getSize();
			boolean l_visible = this.zoompanel.isVisible();
			if (l_visible) {
				this.getContentPane().remove(this.zoompanel);
				((java.awt.GridLayout)(this.getContentPane().getLayout())).setRows(1);
				r.height -= client.height / 2;
			} else {
				((java.awt.GridLayout)(this.getContentPane().getLayout())).setRows(2);
				this.getContentPane().add(this.zoompanel);
				r.height += client.height;
			}
			this.zoompanel.setVisible(! l_visible);
			this.setSize(r);
		} else if (l_action.compareTo ("Screendump") == 0) {
			this.radar.printScreen();
		} else if (l_action.compareTo ("About BSU viewer") == 0) {
			this.radar.aboutDialog(this);
		} else {
			System.out.println ("Unexpected " + l_action + " source : " + ae.getSource().getClass().getName());
		}
		if (l_newSpeed != 100) {
			this.curTimeStep = l_newSpeed;
			setTitle (this.startPause.isSelected() ? this.c_playingSteps[this.curTimeStep] : "Paused");
		}
	}
}

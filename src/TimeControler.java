/**
 * TimeControler constructs the floating window that contains VCR like commands and implements its behaviour.
 * 
 * @author Jean-Francois Bocquet
 *
 */

class TimeControler extends javax.swing.JDialog implements java.awt.event.ActionListener, java.awt.event.AdjustmentListener {
	/**
	 * Mean delay between two time ticks expressed in milliseconds.
	 */
	private int m_timeTick;
	
	/**
	 * link to the flight data
	 */
	private FlightData m_fd;

	/**
	 * Time in millisecond of the creation of the class
	 */
	private long m_startCounter;

	/**
	 * Freeze or unfreeze time evolution
	 */
	boolean m_isPaused;

	/**
	 * Current simulation time (relative to exercise start time) in millisecond
	 */
	int m_curuTime;

	/**
	 * Last value returned by UTime taken into account.
	 */
	int m_startUTime;

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
	int m_curTimeStep = c_defaultTimeStep;

	/**
	 * Index of current controller (speaker)
	 */
	int m_curController;
	
	/**
	 * serialVersionUID ???
	 */
	static final long serialVersionUID = -1652849151066093895L;

	javax.swing.ImageIcon m_imgPause, m_imgStart;
	javax.swing.JButton m_startPause;
	javax.swing.JScrollBar m_scroll;
	javax.swing.JLabel m_hWndTime, m_hLat, m_hLong;
	javax.swing.JPopupMenu m_speed, m_speaker;


	/**
	 * Internal method to add a button to the toolbar
	 * @param tb      toolbar object
	 * @param image   image displayed on the button 16x16
	 * @param tooltip tooltip
	 * @param insets  margin used around the button
	 * @return        the newly created JButton object
	 */
	private javax.swing.JButton createToolbarButton (javax.swing.JToolBar tb, String image, String tooltip, java.awt.Insets insets) {
		javax.swing.JButton l_b = new javax.swing.JButton(new javax.swing.ImageIcon(image));
		l_b.setMargin (insets);
		l_b.setToolTipText(tooltip);
		l_b.setActionCommand (tooltip);
		tb.add(l_b);
		l_b.addActionListener (this);
		return l_b;
	}

	/**
	 * Compute the time elapsed since the start of the program. Expressed in multiple of c_granularity.
	 */
	int UTime () {
		return (int)(java.lang.System.currentTimeMillis() - m_startCounter);
	}

	/**
	 * this function makes the time flow. If time tick has changed, everything is updated
	 */
	void TimeFlowing () {
		int l_delta = UTime () - m_startUTime;
		if (l_delta == 0) return;
		m_startUTime += l_delta;
		SetCurUTime (m_curuTime + l_delta * c_timeSteps[m_curTimeStep]);
		if (m_scroll.getValue() != m_curuTime / m_timeTick)
			m_scroll.setValue (m_curuTime / m_timeTick);
	}

	/**
	 * this function updates the current time, but not the scrollbar
	 * @param i_uTime the future value for simulation time : m_curuTime. This will be adjusted in the range 0 .. m_fd.getNbTimeTick() * m_timeTick - 1
	 */
	void SetCurUTime (int i_uTime) {
		// Adjust the desired time position in valid range
		if (i_uTime < 0) {
			i_uTime = 0;
			m_isPaused = true;
			m_curTimeStep = c_defaultTimeStep;
			m_startPause.setIcon (m_imgPause);
			setTitle ("Paused");
		} else if (i_uTime > m_fd.getNbTimeTick() * m_timeTick - 1) {
			i_uTime = m_fd.getNbTimeTick() * m_timeTick - 1;
			m_isPaused = true;
			m_curTimeStep = c_defaultTimeStep;
			m_startPause.setIcon (m_imgPause);
			setTitle ("Paused");
		}
		// do nothing if time has not changed
		if (i_uTime == m_curuTime) return;
		// update the buttons status
		boolean l_timeHasChanged = (i_uTime / m_timeTick) != (m_curuTime / m_timeTick);
		m_curuTime = i_uTime;
		// TODO if (LabelMove.setTime (m_curuTime/c_timeTick + FlightData.GetFirstTime ()/5)) l_st = true;
		if (l_timeHasChanged)
			m_hWndTime.setText (m_fd.setCurTimeTick (m_curuTime / m_timeTick));
		if (ZoomAndCenterManager.setTime(m_fd.getCurTime ())) {
			BSU.UpdateZoomAndScroll ();
			l_timeHasChanged = true;
		}
		if (l_timeHasChanged)
			BSU.g_hMainWindow.repaint ();
	}

	/**
	 * Constructor
	 * @param frame
	 */
	public TimeControler (javax.swing.JFrame frame, FlightData i_fd) {
		super (frame, "Playing");
		m_fd = i_fd;
		m_startCounter = java.lang.System.currentTimeMillis();
		m_isPaused = false;
		m_curuTime = 0;
		m_curController = 0;
		m_imgPause = new javax.swing.ImageIcon ("images/pause.gif");
		m_imgStart = new javax.swing.ImageIcon ("images/start.gif");
		m_speed = new javax.swing.JPopupMenu ();
		m_speed.add ("x-4").addActionListener(this);
		m_speed.add ("x1").addActionListener(this);
		m_speed.add ("x2").addActionListener(this);
		m_speed.add ("x4").addActionListener(this);
		m_speed.add ("x8").addActionListener(this);
		m_speed.add ("x16").addActionListener(this);
		m_speed.add ("x32").addActionListener(this);
		m_speed.pack ();
		String [] l_controlers = m_fd.getControlers();
		if (l_controlers.length > 1) {
			m_speaker = new javax.swing.JPopupMenu ();
			for (String l : l_controlers) {
				m_speaker.add (l).addActionListener(this);
			}
		} else {
			m_speaker = null;
		}
		//this.setUndecorated (true);
		javax.swing.JPanel l_right = new javax.swing.JPanel (new java.awt.GridLayout (2, 1));
		l_right.setBorder (new javax.swing.border.EtchedBorder(javax.swing.border.EtchedBorder.LOWERED));
		m_hLat = new javax.swing.JLabel ("49°00N00");
		l_right.add (m_hLat);
		m_hLong = new javax.swing.JLabel ("  2°00E00");
		l_right.add (m_hLong);
		this.getContentPane().add(l_right, java.awt.BorderLayout.EAST);

		java.awt.Insets l_insets = new java.awt.Insets (0, 0, 0, 0);
		javax.swing.JToolBar l_hwndTB = new javax.swing.JToolBar();
		l_hwndTB.setFloatable(false);
		m_startPause = createToolbarButton (l_hwndTB, "images/pause.gif", "Pause", l_insets);
		createToolbarButton (l_hwndTB, "images/stop.gif", "Stop", l_insets);
		l_hwndTB.addSeparator();
		createToolbarButton (l_hwndTB, "images/step_rwd.gif", "5s rewind", l_insets);
		m_hWndTime = new javax.swing.JLabel (m_fd.setCurTimeTick(0));
		javax.swing.border.BevelBorder l_b = new javax.swing.border.BevelBorder (javax.swing.border.BevelBorder.LOWERED);
		m_hWndTime.setBorder (new javax.swing.border.CompoundBorder (new javax.swing.border.EmptyBorder(0, 3, 0, 3),
				new javax.swing.border.CompoundBorder (l_b, new javax.swing.border.EmptyBorder(1, 4, 1, 4))));
		l_hwndTB.add (m_hWndTime);
		createToolbarButton (l_hwndTB, "images/step_fwd.gif", "5s forward", l_insets);
		l_hwndTB.addSeparator();
		createToolbarButton (l_hwndTB, "images/speed.gif",    "Playback speed", l_insets);
		l_hwndTB.addSeparator();
		//createToolbarButton (l_hwndTB, "images/loop.gif",     "Loop at the end");
		if (m_speaker != null)
			createToolbarButton (l_hwndTB, "images/speaker.gif",  "Controler choice", l_insets);
		createToolbarButton (l_hwndTB, "images/zoom.gif",     "Zoom", l_insets);
		createToolbarButton (l_hwndTB, "images/screendump.gif", "Screendump", l_insets);
		l_hwndTB.addSeparator();
		createToolbarButton (l_hwndTB, "images/about.gif", "About ATC Player", l_insets);

		javax.swing.JPanel l_center = new javax.swing.JPanel (new java.awt.GridLayout (2, 1));
		l_center.add(l_hwndTB);
		m_scroll = new javax.swing.JScrollBar (javax.swing.JScrollBar.HORIZONTAL, 0, 0, 0, m_fd.getNbTimeTick()-1);
		
		m_timeTick = (m_fd.getLastTime() - m_fd.getFirstTime()) * 1000 / (m_fd.getNbTimeTick() - 1);
		System.out.println ("m_timeTick = " + m_timeTick);
		m_scroll.addAdjustmentListener (this); // TODO prise en compte des raccourcis clavier
		m_scroll.setBorder (new javax.swing.border.EmptyBorder(0, 0, 0, 1));
		l_center.add(m_scroll);
		this.getContentPane().add(l_center, java.awt.BorderLayout.CENTER);
		java.awt.Point p = BSU.g_hMainWindow.getLocation();
		setLocation (p.x+4, p.y+20);
		pack();
		this.setDefaultCloseOperation(javax.swing.JDialog.DO_NOTHING_ON_CLOSE);
		setVisible(true);

		javax.swing.Timer l_timer = new javax.swing.Timer(100, this);
		l_timer.setActionCommand("Timer");
		l_timer.start();
	}

	/**
	 * Take into account modification of scrollbar position.
	 */
	public void adjustmentValueChanged (java.awt.event.AdjustmentEvent e) {
		if (e.getValue () != m_curuTime / m_timeTick)
			SetCurUTime (m_timeTick * e.getValue ());
	}
	
	static void Bug (String i_msg) {
		// TODO : add a fatal error message box ?
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
			if (l_c == m_speed) {
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
			} else if (l_c == m_speaker) {
				String [] l_controlers = m_fd.getControlers();
				int l_r = 100;
				for (int i = 0 ; i < l_controlers.length; i++) {
					if (l_controlers[i].compareTo(l_action) == 0) {
						l_r = i;
					}
				}
				if (l_r != 100) {
					m_curController = l_r;
				} else {
					Bug("Unknown speaker " + l_action);
				}
			} else {
				System.out.println ("Click on a menuItem of nothing known");
			}
		} else if (l_action.compareTo ("Timer") == 0) {
			if (!m_isPaused) {
				TimeFlowing ();
			}
		} else if (l_action.compareTo ("Pause") == 0) {
			if (m_isPaused) {
				m_isPaused = false;
				m_startPause.setToolTipText ("Pause");
				m_startPause.setIcon (m_imgPause);
				setTitle (c_playingSteps[m_curTimeStep]);
				m_startUTime = UTime ();
			} else {
				// shut sound
				m_isPaused = true;
				m_startPause.setToolTipText ("Start");
				m_startPause.setIcon (m_imgStart);
				setTitle ("Paused");
			}
		} else if (l_action.compareTo ("Stop") == 0) {
			// shut sound
			if (m_curuTime == 0) m_curuTime = 1;
			SetCurUTime (-1);
			m_scroll.setValue (0);
		} else if (l_action.compareTo ("5s rewind") == 0) {
			SetCurUTime (m_curuTime - m_timeTick);
			m_scroll.setValue (m_curuTime / m_timeTick);
		} else if (l_action.compareTo ("5s forward") == 0) {
			SetCurUTime (m_curuTime + m_timeTick);
			m_scroll.setValue (m_curuTime / m_timeTick);
		} else if (l_action.compareTo ("Playback speed") == 0) {
			m_speed.show ((javax.swing.JButton)ae.getSource(), 0, 0);
		} else if (l_action.compareTo ("Controler choice") == 0) {
			m_speaker.show ((javax.swing.JButton)ae.getSource(), 0, 0);
		} else if (l_action.compareTo ("Zoom") == 0) {
			System.out.println ("Not yet implemented");
		} else if (l_action.compareTo ("Screendump") == 0) {
			System.out.println ("Not yet implemented");
		} else if (l_action.compareTo ("About ATC Player") == 0) {
			System.out.println ("Not yet implemented");
		} else {
			System.out.println ("Unexpected " + l_action + " source : " + ae.getSource().getClass().getName());
		}
		if (l_newSpeed != 100) {
			m_curTimeStep = l_newSpeed;
			setTitle (m_isPaused ? "Paused" : c_playingSteps[m_curTimeStep]);
		}
	}
}

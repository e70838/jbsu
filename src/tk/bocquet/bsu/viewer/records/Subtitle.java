/*
 * @(#)Subtitle.java
 * 
 */

package tk.bocquet.bsu.viewer.records;

/**
 * Subtitles are used to display the orders entered by pilots.
 * 
 * @author Jean-François Bocquet
 *
 */
public class Subtitle {
	/**
	 * Orders are associated with the name of the controller who was controlling the flight.
	 */
	public int controller;
	
	/**
	 * The text of the order
	 */
	public String sentence;
	
	/**
	 * Link to any other Subtitle that has been entered during the same time tick
	 */
	public Subtitle next;
}

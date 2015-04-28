package components;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * A Component is the top class of the System Modeling structure.  Components must have a way
 * to store and move resources and update incrementally.  Components hold two values (previous
 * and current) so that updates values can be made, but other related components can still use the
 * value at the current time step.
 * 
 * @author Kenny Kong
 * @author Kevin E. Anderson (k3a@uw.edu)
 * @version 2013.07.20
 */
public class Component {
	//TODO CREATE AN ENUMAERATION FOR COMPONENT TYPING

	/**
	 * A String <-> Integer mapping of the components and their integer counterparts.  These values
	 * are made public in the TYPE_* list.
	 */
	private static Map<String, Integer> type_map;
	static {
		Map<String,Integer> tmpMap = new HashMap<String,Integer>();
		tmpMap.put("flow", 0);
		tmpMap.put("stock", 1);
		tmpMap.put("subsystem", 2);
		tmpMap.put("control", 3);
		tmpMap.put("sensor", 4);
		tmpMap.put("cloud", 5);
		type_map = Collections.unmodifiableMap(tmpMap);
	}
	/**
	 * Accepts a string and converts it to the proper integer value 
	 * that relates to component type.  It is NOT case sensitive.
	 * @param the_name The string name of the component. 
	 * @return The related Static integer of that component.
	 * @throws IllegalArgumentException When the string provided is not a valid component name.
	 */
	public static int convertName(String the_name)
	{
		Integer to_return = type_map.get(the_name.toLowerCase());
		
		if (to_return == null)
		{
			throw new IllegalArgumentException(the_name + " is not a valid Component Type");
		} else {
			return to_return;
		}
		
	}

	/**
	 * Denotes a {@link Component} of type {@link Flow}.
	 */
	public static final int TYPE_FLOW = 0;
	
	/**
	 * Denotes a {@link Component} of type {@link Stock}.
	 */
	public static final int TYPE_STOCK = 1;
	
	/**
	 * A Subsystem is not implemented in this version.  Subsystems are systems that exist within
	 * a system which could theoretically be simulated recursively at each time step.  This would
	 * allow for easier and iterative creation of large/complex systems.
	 */
	public static final int TYPE_SUBSYSTEM = 2;
	
	/**
	 * Denotes a {@link Component} of type {@link Control}.
	 */
	public static final int TYPE_CONTROL = 3;
	
	/**
	 * A Sensor could be attached to any {@link Component} and then to a {@link Flow}.  These are 
	 * used heavily in {@link Controls}.  The way this software is implemented, no Sensors are required.
	 * Components can be identified by name so we simulate sensors in code.  They do not need explicitly
	 * included in the system.
	 */
	public static final int TYPE_SENSOR = 4;
	
	/**
	 * Denotes a {@link Component} of type {@link Cloud}.
	 */
	public static final int TYPE_CLOUD = 5;

	/**
	 * The unique name of this {@link Component}.  
	 * In this version the {@link Component#name} is the unique identifier and 
	 * the {@link Component#id} is arbitrary.
	 */
	protected final String name;

	/**
	 * The unique dotted integer id of this {@link Component}.
	 */
	protected final String id;

	/**
	 * The "TYPE" of the {@link Component} as represented by the TYPE_* named <code>public static final int</code>
	 * <ul>
	 * <li>{@link Component#TYPE_FLOW}</li>
	 * <li>{@link Component#TYPE_STOCK}</li>
	 * <li>{@link Component#TYPE_SUBSYTEM}</li>
	 * <li>{@link Component#TYPE_CONTROL}</li>
	 * <li>{@link Component#TYPE_SENSOR}</li>
	 * <li>{@link Component#TYPE_CLOUD}</li>
	 */
	protected int type;
	
	/**
	 * The previous value.  To be used as the value of the {@link Component} at the 
	 * time step currently being calculated.
	 */
	protected double previous_value;

	/**
	 * The current value. To be used and updated during a time step calculation.
	 */
	protected double current_value;

	/**
	 * Instantiate this component.
	 * 
	 * @param the_name The unique name of this component.
	 * @param the_id The unique ID of this component, arbitrary and unused in this version.
	 * @param the_type The integer representation of the type of this component.  See {@link Component#type_map}
	 */
	public Component(final String the_name, final String the_id, final int the_type) {
		
		//  SET PRIMARY FIELDS
		name   =  the_name;
		id     =  the_id;
		type   =  the_type;
	}

	/**
	 * Set the previous value to current value.
	 */
	public void backup() {
		setPreviousValue(getCurrentValue());
	}

	/**
	 * Calculates next value of the component.  Most {@link Component} types will override this 
	 * method to give them unique behavior.  If they do not, then the default behavior is to 
	 * not change values at all. 
	 */
	public void calcNewValue()
	{
		// should be override by subclass if they need to calculate new value.
	}

	/**
	 * Get the current value of this {@link Component}.
	 * 
	 * @return The current value of the {@link Component}.
	 */
	public double getCurrentValue() {
		return current_value;
	}

	/**
	 * Get the ID of this {@link Component}.
	 * 
	 * @return the unique dotted integer ID of the {@link Component}
	 */
	public String getID() {
		return id;
	}

	/**
	 * Get the name of this {@link Component}.
	 * 
	 * @return The Unique name of this {@link Component}.
	 */
	public String getName() {
		return name;
	}

	/**
	 * Get the previous value of this {@link Component}.
	 * 
	 * @return The previous value of this {@link Component}.
	 */
	public double getPreviousValue() {
		return previous_value;
	}

	/**
	 * Get the type of this {@link Component}.
	 * 
	 * @return The integer value representing the type of the {@link Component}.
	 */
	public int getType() {
		return type;
	}
	
	/**
	 * Set current value of this component.  Assumes no maximum capacity.
	 * 
	 * @param the_value The new value of the component.  Can be positive or negative.
	 */
	public void setCurrentValue(final double the_value) {
		current_value = the_value;
	}

	/**
	 * Set the "backup" or previous value of this component.
	 * 
	 * @param the_value The value to set the "backup" or previous value to.
	 * This should generally come from {@link Component#current_value}
	 */
	public void setPreviousValue(final double the_value) {
		previous_value = the_value;
	}

	/**
	 * Allow an object to explicitly add to the current value.
	 * Primarily used for {@link Flow}s to blindly add to a {@link Component}
	 * @param value The amount in which to add.  Use of negative numbers will cause subtraction
	 * which may be the desired result in a reverse {@link Flow}.
	 */
	protected void add(final double value)
	{
		setCurrentValue(current_value+value);
	}
	
	/**
	 * Allow an object to explicitly subtract to the current value.
	 * Primarily used for {@link Flow}s to blindly remove from a {@link Component}
	 * @param value The amount in which to subtract.  Use of negative numbers will cause addition
	 * which may be the desired result in a reverse {@link Flow}.
	 */
	protected void subtract(final double value)
	{
		setCurrentValue(current_value-value);
	}

	/**
	 * Set the type of this {@link Component} for objects that utilize inheritance.
	 * @param the_type 
	 * @throws IllegalArgumentException
	 */
	protected void setType(final int the_type) {
		if (0 > the_type || the_type > 6)
			throw new IllegalArgumentException(the_type + " is not a valid type.");
		
		type = the_type;
	}
}

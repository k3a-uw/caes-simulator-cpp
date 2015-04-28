package input;

/**
 * A component step represents a data structure to alter the 
 * {@link datamodel.SimulationDataStructure} at a particular step in the simulation
 * as defined by the user from an input XML file.  ComponentStep knows which 
 * {@link components.Component} to update, when to update it, and how to update it.
 * @author Kevin E. Anderson (k3a@uw.edu)
 * @version 2013.07.21
 */
public class ComponentStep {
	
	/**
	 * Denotes a full replacement of the {@link components.Component}'s current value.
	 */
	private static final int TYPE_VALUESET = 0;
	
	/**
	 * Denotes addition/subtraction to/from the {@link components.Component}'s current
	 * value.
	 */
	private static final int TYPE_VALUEADD = 1;
	
	/**
	 * Denotes a scaling (multiplication) of the {@link components.Component}'s current
	 * value.
	 */
	private static final int TYPE_VALUESCALE = 2;
	
	/**
	 * Converts a provided String into the integer representation of the public static
	 * int varialbes for Set, Add and Scale.  Is NOT case sensitive.
	 * @param the_name The string to convert to integer representation.
	 * @return The integer representation of the provided value type.
	 * @throws IllegalArgumentException When the provided string is not a valid
	 * value type.
	 */
	public static int convertName(String the_name)
	{
		//TODO Convert ComponentStep public static finals to Enumerations.
		if (the_name.toUpperCase().equals("VALUESET"))
		{
			return TYPE_VALUESET;
		} else if (the_name.toUpperCase().equals("VALUEADD"))
		{
			return TYPE_VALUEADD;
		} else if (the_name.toUpperCase().equals("VALUESCALE"))
		{
			return TYPE_VALUESCALE;
		} else
		{
			throw new IllegalArgumentException(the_name + " is not a valid ComponentStep Type");
		}
	
		
		
	}

	/**
	 * The dotted integer ID of the {@link components.Component} to update.  In this 
	 * version this is unused and arbitrary.
	 */
	private String id;
	
	/**
	 * The unique name of the {@link components.Component} to update.
	 */
	private String name; 
	
	/**
	 * Stores the TYPE of update to perform.  A Set, Add or Scale value as denoted
	 * in the PUBLIC STATIC FINAL variables of this class.
	 */
	private int    value_set_type;
	
	/**
	 * The value to use during the update process.
	 */
	private double update_value;
	
	/**
	 * Create a new ComponentStep ready to store in the {@link TimeStep}.
	 * @param the_id The dotted integer ID of the {@link components.Component} to update.
	 * @param the_name The unique name of the {@link components.Component} to update.
	 * @param the_type The type of update to perform (TYPE_VALUESET, TYPE_VALUEADD, 
	 * TYPE_VALUESCALE).
	 * @param the_value The value to use during the update process.
	 */
	public ComponentStep(final String the_id, final String the_name, final int the_type, final double the_value)
	{
		id               =  the_id;
		name             =  the_name;
		value_set_type   =  the_type;
		update_value  =  the_value;
	}

	/**
	 * Exposes the dotted integer ID of the {@link components.Component} to update.
	 * @return The dotted integer ID of the {@link components.Component} to update.
	 */
	public String getId() {
		return id;
	}
	
	/**
	 * Exposes the unique name of the {@link components.Component} to update.
	 * @return The unique name of the {@link components.Component} to update.
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Exposes the type of update to perform.
	 * @return The type of update to perform.
	 */
	public int getType() {
		return value_set_type;
	}
	
	/**
	 * Returns the value to be used during the update process.
	 * @return The value to be used during the update process.
	 */
	public double getValue() {
		return update_value;
	}
	
	/**
	 * Uses the provided value (from a {@link components.Component}) and
	 * based on the {@link ComponentStep#value_set_type} and the 
	 * {@link ComponentStep#update_value} will calculate a new value to be input into
	 * the {@link components.Component}'s current value.
	 * @param value The current value of a specific Component.
	 * @return The newly calculated value to be used for a component's current value.
	 */
	public double calcNewDouble(double value)
	{
		switch (value_set_type)
		{
		case TYPE_VALUESET:
			return update_value;
		case TYPE_VALUEADD:
			return update_value + value;
		case TYPE_VALUESCALE:
			return update_value * value;
		default:
			return update_value;
		}
	}	
}

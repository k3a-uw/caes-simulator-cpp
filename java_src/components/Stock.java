package components;

/**
 * Stock components represent collections or inventories of resources and energy.
 * By nature, stocks do not not calculate any new values, but are attached to Flows
 * which pull resources out of, or push resources into the Stock. 
 * 
 * @author Kenny Kong
 * @author Kevin E. Anderson (k3a@uw.edu)
 * @version 2013.07.20
 */
public class Stock extends Component {

	/**
	 * The maximum value possible for the stock.  A stocks current value
	 * cannot exceed max_value.
	 */
	private double max_value;
	
	/**
	 * The unit of the resource of the stock.  This could be "People" or "Watts" or "Cogs" or "Watzits"
	 * In the current state of the CAES-Simulator, units are arbitrary.
	 */
	private String unit;


	/**
	 * Construct a new stock ready for use.
	 * 
	 * @param the_name The Name of this stock used as a unique identifier of the object. 
	 * @param the_id The ID of this stock.  Is currently arbitrary, but in future releases of
	 * the simulator the ID will be used as the unique identifier, not the name.
	 * @param the_max_value The maximum value this {@link Stock} can hold.
	 * @param the_unit The unit of measure associated with the {@link Stock}.
	 */
	public Stock(final String the_name, final String the_id,
		     final double the_max_value, final String the_unit) {
		//  BUILD A STOCK COMPONENT
		super(the_name, the_id, Component.TYPE_STOCK);
		
		//  SET PRIMARY FIELDS
		max_value  =  the_max_value;
		unit       =  the_unit;
	}
 
	/**
	 * Set the current value of this stock up to its maximum value.  To be used primarily
	 * by {@link Flow}s or {@link input.ComponentStep}
	 * 
	 * @param the_current_value the value to be set.
	 */
	public void setCurrentValue(final double the_current_value) {
		if (Double.isInfinite(max_value)) {
			current_value = the_current_value;
		} else {
			current_value = Math.min(the_current_value, max_value);
		}
	}
		
	/**
	 * Get this measuring unit.  Currently unused.
	 * 
	 * @return measuring unit.
	 */
	public String getUnit() {
		return unit;
	}
}

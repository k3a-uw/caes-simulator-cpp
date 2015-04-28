package components;

/**
 * A Cloud is just an un-modeled source of resources.  It behaves basically like a {@link Stock}
 * but it has infinite capacity and does not track current levels.
 * 
 * @author Fuat
 * @author Kevin E. Anderson (k3a@uw.edu)
 */

public class Cloud extends Stock
{
	/**
	 * Create a stock that is ready to use.
	 * @param the_name The unique name used to identify this instance.
	 * @param the_id The dotted integer ID.  In this version, this ID is unused and arbitrary.
	 * @param the_unit The unit of the recourses stored in this cloud.  In this version, the unit
	 * is arbitrary.
	 */
	public Cloud(String the_name, String the_id, String the_unit)
	{
		//  CONSTRUCT A NEW STOCK
		super(the_name, the_id, Double.POSITIVE_INFINITY, the_unit);

		//  MAKE IT A CLOUD
		type = Component.TYPE_CLOUD;
		previous_value = Double.POSITIVE_INFINITY;
		current_value  = Double.POSITIVE_INFINITY;
	}

}

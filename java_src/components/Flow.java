package components;


/**
 * A Flow connects two {@link Component}s together in the system and based on 
 * the value of its {@link Control} will remove value from its "source" {@link Component} and
 * add that value to its "sink" {@link Component}.  Flows generally connect stocks, but could
 * potentially connect any two components in the system (with mixed results).
 * 
 * @author Shane Kwon
 * @author Sean Chung
 * @author Kevin E. Anderson (k3a@uw.edu)
 * @version 2013.07.20
 * 
 */
public class Flow extends Component
{
	/**
	 * A reference to the "source" {@link Component} from which to remove resources.
	 */
	private Component source;
	
	/**
	 * A reference to the "sink" {@link Component} to which to add resources.
	 */
	private Component sink;
	
	/**
	 * The control associated with the flow acts as a valve, regulating the resource quantity to
	 * transition between the source and the sink {@link Component}.
	 */
	private Control control;

	/**
	 * A reference to the name of the {@link Component} to be used as the sink for this {@link Flow}.  
	 * Used by the {@link Loader} to link references to the {@link Component} in the System by that name. 
	 */
	private String sink_name;

	/**
	 * A reference to the name of the {@link Component} to be used as the source for this {@link Flow}.
	 * Used by the {@link Loader} to link references to the {@link Component} in the System by that name.
	 */
	private String source_name;

	/**
	 * A reference to the name of the {@link Control} to be used as the source for this {@link Flow}.
	 * Used by the {@link Loader} to link references to the {@link Control} in the System by that name.
	 */
	private String control_name;

	/**
	 * The maximum rate of this flow.
	 */
	private double max_flow_rate;

	/**
	 * Construct a new Flow with place holding "names" of the components to be linked to later.  A
	 * flow is not fully ready for use until the "linker" sets the source, sink and control.
	 * 
	 * @param the_name The name of the Flow.  Used as a unique identifier.
	 * @param the_id The ID of the Flow.  Currently unused and arbitrary.
	 * @param the_source_name The unique name of the source to later be used by the {@link Loader}. 
	 * @param the_sink_name The unique name of the sink to later be used by the {@link Loader}.
	 * @param the_max_flow_rate The maximum flow rate of this flow.
	 */
	public Flow(final String the_name, final String the_id,
			final String the_source_name, final String the_sink_name,
			final double the_max_flow_rate, final double the_cur_level,
			final String the_control_name) 
	{
		//  CREATE A FLOW COMPONENT
		super(the_name, the_id, Component.TYPE_FLOW);

		//  SET PRIMARY FIELDS
		source_name    = the_source_name;
		sink_name      = the_sink_name;
		max_flow_rate  = the_max_flow_rate;
		control_name   = the_control_name;
		
		// DO NOT USE 'setCurrentValue' in a constructor.  NEVER CALL PUBLIC METHODS FROM CONSTRUCTORS!
		current_value  = Math.min(the_cur_level, max_flow_rate);
	}

	/**
	 * When a {@link Flow} calculates its new value, it performs a movement
	 * of resources from its associated "source" {@link Component} to its associated
	 * "sink" {@link Component}.  To determine the magnitude 
	 */
	@Override
	public void calcNewValue()
	{
		//  OBTAIN VALUE FROM THE CONTROL
		double val = control.getPreviousValue();
	
		//  SET THE CURRENT VALUE UP TO THE MAX_FLOW_RATE
		setCurrentValue(val);
	
		//  SUBTRACT FROM SOURCE
		source.subtract(current_value);
		
		//  ADD TO SINK
		sink.add(current_value);
	
	}

	/**
	 * Set the current value (or flow rate) of this {@link Flow}.  The value
	 * of {@link Component#current_value} for a {@link Flow} is not guaranteed
	 * to match the value of its associated {@link Control}.  The current value
	 * will be no larger than the {@link Flow#max_flow_rate}.
	 * 
	 * @param the_current_value the value to be set.
	 */
	@Override
	public void setCurrentValue(final double the_current_value) {
		if (Double.isInfinite(max_flow_rate)) {
			current_value = the_current_value;
		} else {
			current_value = Math.min(the_current_value, max_flow_rate);
		}
	}

	/**
	 * Returns the name of the {@link Control} (to be) associated with the {@link Flow}.  To be
	 * used primarily by the {@link datamodel.Loader}.
	 * @return The name of the {@link Control} (to be) associated with the {@link Flow}.
	 */
	public String getControlName()
	{
		//  IF NO CONTROL IS SET, USE THE EXPECTED CONTROL NAME.
		return (control == null ? control_name : control.getName());		
	}

	/**
	 * Return the name of the "sink" {@link Component} (to be) associated with the {@link Flow}.  To be
	 * used primarily by the {@link datamodel.Loader}.
	 * 
	 * @return The name of the "sink" {@link Component} (to be) associated with the {@link Flow}.
	 */
	public String getSinkName()
	{
		//  IF THERE IS NO SINK SET, USE THE EXPECTED SINK_NAME
		return (sink == null ? sink_name : sink.getName());
	}

	/**
	 * Return the name of the "source" {@link Component} (to be) associated with the {@link Flow}.  To
	 * be used primarily by the {@link datamodel.Loader}.
	 * 
	 * @return The name of the "source" {@link Component} (to be) associated with the {@link Flow}.
	 */
	public String getSourceName()
	{
		//  IF THERE IS NO SOURCE SET, USE THE EXPECTED SOURCE_NAME
		return (source == null ? source_name : source.getName()); 
	}

	/**
	 * Provides the {@link Flow} with a reference to the {@link Control} that will ultimately
	 * control the {@link Component#current_value}
	 * @param c A reference to the {@link Control} used to control this flow.
	 * @throws IllegalArgumentException When <code>c</code> is <code>null</code>.
	 */
	public void setControl(Control c)
	{
		if (c == null)
			throw new IllegalArgumentException("Invalid control.  Control cannot be null.");

		control = c;
	}
	
	/**
	 * Provides the {@link Flow} with a reference to the {@link Component} that will act as
	 * the "sink".
	 * @param c A reference to the {@link Component} to be used as the "sink".
	 * @throws IllegalArgumentException When <code>c</code> is <code>null</code>.
	 */
	public void setSink(Component c)
	{
		if (c == null)
			throw new IllegalArgumentException("Invalid Sink.  Component cannot be null.");

		sink = c;
	}

	/**
	 * Provides the {@link Flow} with a reference to the {@link Component} that will act as
	 * the "source".
	 * @param c A reference to the {@link Component} to be used as the "source".
	 * @throws IllegalArgumentException When <code>c</code> is <code>null</code>.
	 */
	public void setSource(Component c)
	{
		if (c == null)
			throw new IllegalArgumentException("Invalid Source.  Component cannot be null.");
		
		source = c;
	}

}

package input;

import java.util.ArrayList;

/**
 * A TimeStep tells the simulator what {@link ComponentSteps} should be used to update  
 * the simulation at which "step"
 * 
 * @author Kevin E. Anderson (k3a@uw.edu)
 * @version 2013.07.21
 *
 */
public class TimeStep {
	
	/**
	 * The step value in which this TimeStep should be called upon to update the
	 * simulation.
	 */
	private int stepValue;
	
	/**
	 * A collection of {@link ComponentStep}s which tells the simulation what changes 
	 * should be made to which {@link components.Component}s at this timestep.
	 */
	private ArrayList<ComponentStep> components;
	
	/**
	 * Create a new empty (useless) {@link TimeStep}.  Timesteps are not useful until
	 * the {@link TimeStep#setStepValue(int)} and 
	 * {@link TimeStep#addComponent(ComponentStep)} are used.  
	 * Sets {@link TimeStep#stepValue} to sentinal value of -1;
	 */
	public TimeStep()
	{
		stepValue = -1;
		components = new ArrayList<ComponentStep>();
	}
	
	/**
	 * Assigns the provided parameter as the {@link TimeStep#stepValue}.
	 * @param value The step in the simulation that this Timestep's changes should be
	 * applied.
	 */
	public void setStepValue(final int value)
	{
		stepValue = value;
	}

	/**
	 * Adds a {@link ComponentStep} to this {@link TimeStep}.
	 * @param value A reference to the {@link ComponentStep} to add.
	 */
	public void addComponent(ComponentStep value)
	{
		components.add(value);
	}
	
	/**
	 * Returns the {@link TimeStep#stepValue} of this {@link TimeStep}.
	 * @return The {@link TimeStep#stepValue} of this {@link TimeStep}.
	 */
	public int getStepValue()
	{
		return stepValue;
	}
	
	/**
	 * Returns an array of {@link ComponentSteps} to allow the {@datamodel.MainControl}
	 * to iterate through and make updates to the simulation.
	 * @returns An array of {@link ComponentSteps} to allow the {@datamodel.MainControl}
	 * to iterate through and make updates to the simulation.
	 */
	public ComponentStep[] getComponentSteps()
	{
		ComponentStep[] toReturn = new ComponentStep[components.size()];
		for (int i=0; i < components.size(); i++)
			toReturn[i] = components.get(i);
		return toReturn;
	}
	
	/**
	 * Will return a String representation of the {@link TimeStep} and the number of 
	 * {@link ComponentStep}s that are associated with this {@link TimeStep}.
	 */
	@Override
	public String toString()
	{
		return "StepValue="+stepValue+"\tComponent Count: " + (components == null ? "null" : components.size());
	}
 
}

package datamodel;

import input.ComponentStep;
import input.InputStreamer;
import input.TimeStep;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

import components.Component;
/**
 * MainControl holds a reference to the {@link SimulationDataStructure} that contains the
 * model and the {@link input.InputStreamer} that holds input data.  MainControl has 
 * several running times.  Though it does not implement runnable, MainControl does run
 * in its own thread using a {@link javax.swing.SwingWorker} created in 
 * {@link ui.Simulator}.  The MainControl will run as quickly as it can, or will allow
 * a user to pause and step through the System simulation one time step at a time.
 * MainControl also writes the data from the {@link SimulationDataStructure} to a csv
 * file for later analysis. 
 * @author Shane Kwon
 * @author Kevin E. Anderson (k3a@uw.edu)
 * @version 2013.07.21
 */
public class MainControl
{
	/**
	 * Denotes the behavior where the {@link MainControl} has reached the maximum time
	 * steps requested by the modeled system.
	 */
	private static final int STOPPED = 1;
	
	/**
	 * Denotes the behavior where the {@link MainControl} is running as quickly as it can
	 * until it is paused, or reaches the maximum time steps requested by the model.
	 */
	private static final int RUNNING = 2;
	
	/**
	 * Denotes the behavior where the {@link MainControl} is no longer running, but can
	 * be stepped through, one time step at a time.
	 */
	private static final int PAUSED  = 3;
	
	/**
	 * Denotes the behavior where the {@link MainControl} has completed the simulation of 
	 * the model and cannot be started.
	 */
	private static final int COMPLETED = 4;
	
	/**
	 * The current behavior state of the running (or paused) simulation.
	 */
	private int control_state;
	
	/**
	 * Stores the current (or last simulated) step in the model.
	 */
	private int current_step = 0;

	/**
	 * A structure that stores all {@link components.Component} objects described by the
	 * model (as defined by the user).  Provided by the {@link Loader}.
	 */			
	private SimulationDataStructure data_structure;
	
	/**
	 * The maximum number of steps to simulate before moving to a 
	 * {@link MainControl#COMPLETED} state.
	 */
	private int max_steps;
	
	/**
	 * The file (CSV) to store the output information to for external analysis.
	 */
	private File output_file;
	
	/**
	 * A writer for the {@link MainControl#output_file}.
	 */
	private PrintWriter output_writer;
	
	/**
	 * The stream to make modifications to {@link MainControl#data_structure} based on
	 * predefined input data provided by the user.
	 */
	private InputStreamer input_stream;
	
	/**
	 * Create a new main control that is ready to use (or ready to be initialized with
	 * an {@link input.InputStreamer}.
	 * @param the_data_structure A reference to the {@link SimulationDataStructure} to be
	 * used as the {@link MainControl#data_structure}.
	 * @param the_max_steps The maximum time steps this {@link MainControl} will simulate
	 * before stopping. 
	 */
	public MainControl(SimulationDataStructure the_data_structure, final int the_max_steps) {
		data_structure = the_data_structure;
		max_steps = the_max_steps;
		output_file = new File("output.csv");
		try
		{
			output_writer = new PrintWriter(output_file);
		} catch (FileNotFoundException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// write the columns of the csv output
		output_writer.print("time step,");
		for (Component c : data_structure.getAllComponents()) {
			output_writer.print(c.getName() + ",");
		}
		output_writer.println();
	}
	
	/**
	 * Sets {@link MainControl input_stream} to allow for a data driven model to be
	 * simulated.
	 * @param input The {@link input.InputStreamer} constructed by {@link ui.Simulator}.
	 */
	public void setInputStreamer(InputStreamer input)
	{
		input_stream = input;
	}
	
	/**
	 * Will continue to simulate time steps until the simulation is paused or until the
	 * {@link MainControl#max_steps} is reached.
	 */
	public void run() {
		synchronized (this)
		{
			control_state = RUNNING;
		}
				
		while (control_state == RUNNING) {
			next();
			
			if (current_step >= max_steps)
			{
				synchronized (this)
				{
					control_state = COMPLETED;
				}
			}
		}
		output_writer.close();
	}
	
	/**
	 * Pauses the simulation in progress.
	 */
	public void pause()
	{
		synchronized (this)
		{
			control_state = PAUSED;
		}
	}
	
	/**
	 * Makes a call to simulate a single time step.
	 */
	public void step()
	{
		next();
	}
	
	/**
	 * Simulates a single time step.  Is called continuously while simulator is in
	 * {@link MainControl#RUNNING} state.  To simulate each step the model must first
	 * have all components updated based on any input that was previously define by the
	 * user for the current time step.  Each component is then "backed up" to prevent
	 * skewing results by calculating functions after they have already been updated.
	 * Lastly, every component is updated with their new value.  The results are then 
	 * added to {@link MainControl.output_file}. 
	 */
	private void next() {
		
		output_writer.print(current_step + ",");
		
		//INTRODUCE METHOD HERE FOR DATA INPUT SET ALL OF THE CURRENT VALUES TO THE NEW
		//INPUT VALUES BASED ON THE INPUT SETS
		if (input_stream != null && input_stream.hasNext())
			updateFromInput();

		for (Component c : data_structure.getAllComponents()) {
			output_writer.print(c.getCurrentValue() + ",");
			c.backup();
		}
		for (Component c : data_structure.getAllComponents()) {
			c.calcNewValue();
		}
		
		output_writer.println();
		current_step++;

		data_structure.setChanged(true); //TODO FIX THIS HACK!
		data_structure.notifyObservers();
	}
	
	/**
	 * Checks the input_stream to determine if there is an {@link input.TimeStep} to 
	 * pull data from for the current_step.  If so, all components with a relating
	 * {@link input.ComponentStep} has there value updated.
	 */
	private void updateFromInput() {
		if (input_stream.peek().getStepValue() == current_step)
		{
			TimeStep timestep = input_stream.getNextStep();
			ComponentStep[] compstep = timestep.getComponentSteps();
			for (int i=0; i < compstep.length; i++)
			{
				for(Component c : data_structure.getAllComponents()) {
					//TODO FOCUS MORE ON IDS, BUT FOR NOW USING NAMES
					if (c.getName().equals(compstep[i].getName()))
					{ // IF NAME MATCHES SET THE VALUE
					  //CALCULATE A NEW VALUE FOR THE COMPONENT.
						c.setCurrentValue(compstep[i].calcNewDouble(c.getCurrentValue()));
					}
				}
			}
		}
		
	}

	/**
	 * Exposes {@link MainControl#current_step}
	 * @return The current (or last) step simulated.
	 */
	public int getCurrentStep()
	{
		return current_step;
	}
	
	/**
	 * Return a reference to the output.csv file.
	 * @return A reference to the output.csv file.
	 */
	public File getFile() {
		return output_file;
	}
}

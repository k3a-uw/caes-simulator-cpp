package components;

import java.util.ArrayList;

import net.sourceforge.jeval.EvaluationException;
import net.sourceforge.jeval.Evaluator;
import error.InvalidXMLException;

/**
 * A {@link Control} serves as a valve to a {@link Flow}, regulating the ammount of resources to pass between
 * them by using different types of functions that can reference the value of any other
 * {@link Component} in the System.  A function stores the text of the function defined by the
 * user and the last value calculated of that function.  {@link Control} relies heavily on a GNU Licensed
 * software titled <b>Jeval</b>.
 * 
 * @author Sean Chung
 * @author Kenny Kong
 * @author Kevin Anderson (k3a@uw.edu)
 * @version 2013.07.21
 */
public class Control extends Component {

	/**
	 * Denotes a "conditional" function.  This function type
	 * first makes a decision based on two operands and an operator.
	 * The comma separated list works thusly:
	 * <code>Operand, Operator, Operand, ValueIfTrue, ValueIfFalse</code>
	 * <br />Ex: "component1 , > , component2 , 1 , 0"
	 */
	public static final String CONDITIONAL = "conditional";

	/**
	 * Denotes a standard "function".  A standard function is written just as a mathematical
	 * function would be using names of components as variables. <br />
	 * Ex: "component1 * component2 + component3^2"
	 */
	public static final String FUNCTION    = "function";

	/**
	 * A "recursive" function is self referencing.  It behaves just as a standard "function"
	 * does but due to its self referencing nature, it requires an initial value to be set
	 * in another field.
	 */
	public static final String RECURSIVE   = "recursive";

	/**
	 * A "constant" function behaves just as a standard "function" but is a reference to 
	 * a value and cannot change.
	 */
	public static final String CONSTANT    = "constant";

	/**
	 * The set values extracted from a {@link Control#CONDITIONAL} function.
	 */
	private ArrayList<String> conditional_elements;

	/**
	 * The set of parameters to use in string replace when passing the function
	 * to the parser.
	 */
	private ArrayList<Component> function_parameters;

	/**
	 * The user defined function for the control, used to calculate new values
	 * at each time step.
	 */
	private String function_text;

	/**
	 * The type of function related to this control.  Should
	 * equal one of the public static finals.
	 */
	private String function_type;

	/**
	 * The initial value.  The use of wrapper class {@link Double} to check for null values.  Required
	 * by the design of {@link datamodel.Loader}.
	 */
	private Double initial_value;

	/**
	 * A boolean to know if the control is attempting to be initialized.
	 * Used to determine circular references that would disallow
	 * accurate calculations.
	 */
	private boolean isInitializing;

	/**
	 * A boolean to identify if a control has been initialized.  Used in
	 * conjunction with {@link Control#isInitializing} to help identify
	 * circular references that would disallow accurate calculations.
	 */
	private boolean isInitialized;

	/**
	 * An evaluator used to read and compute mathematical user defined functions.
	 */
	private Evaluator evaluator;

	/**
	 * Builds a new {@link Control}.  Before an instance can be used, it must be 
	 * initialized using {@link Control#initValue()}.  Initializations cannot be
	 * completed by the {@link datamodel.Loader} until all of the
	 * {@link Component}s have been instantiated and linked.  
	 * 
	 * @param the_name The unique name of the Control
	 * @param the_id The dotted integer identifier of the control.  In this
	 * version, the id is unused and arbitrary.
	 * @param the_function The string representation of the mathematical function
	 * used to calculate the next value of the control.
	 * @param the_function_type The type of function expected.  This should match
	 * on of the following: <br>
	 * <ul>
	 * <li>{@link Control#CONDITIONAL}</li>
	 * <li>{@link Control#FUNCTION}</li>
	 * <li>{@link Control#RECURSIVE}</li>
	 * <li>{@link Control#CONSTANT}</li>
	 * </ul>
	 * @param the_initial_value The first value of the function.  Required for
	 * {@link Control#RECURSIVE} and {@link Control#CONSTANT}, ignored otherwise.
	 */
	public Control(final String the_name,  final String the_id,
			final String the_function, final String the_function_type,
			final double the_initial_value) 
	{
		//  BUILD A COMPONENT
		super(the_name, the_id, Component.TYPE_CONTROL);

		evaluator = new Evaluator();

		//  SET PRIMARY FIELDS
		function_text    = the_function;
		function_type    = the_function_type;

		//  SET ALL VALUES TO THE INITIAL VALUE PROVIDED
		initial_value  = current_value = previous_value = the_initial_value;

	}

	/**
	 * Updates the current value to the value returned from a freshly evaluated function.
	 * The value is determined by the result of {@link Control#evaluateFunction()}.
	 */
	public void calcNewValue() {
		setCurrentValue(evaluateFunction());
	}

	/**
	 * Return the {@link String} representation of the type of user defined function.
	 * @return The {@link String} representation of the type of user defined function
	 * associated with this {@link Control}.
	 */
	public String getFunctionType()
	{
		return function_type;
	}

	/**
	 * Returns the text of the function used in calculations performed by this
	 * {@link Control}.
	 * 
	 * @return The text of the function used in calculations performed by this
	 * {@link Control}. 
	 */
	public String getFunctionText() {
		return function_text;
	}

	/**
	 * Recursively checks and initializes all uninitialized {@link Control}s labeled as
	 * parameters of this {@link Control}.  Each control is flag as being initialized 
	 * at the start of this method.  If a {@link Control} is asked to initialize while
	 * already initializing, this represents a circular reference and an exception is
	 * thrown.
	 * @throws InvalidXMLException If a circular reference occurs in the set of
	 * {@link Controls} and this control cannot be properly initialized.
	 */
	public void initValue() throws InvalidXMLException
	{
		if (isInitializing)
			throw new InvalidXMLException("The is an infinite loop in initializating "
					+ name + ".  Please check your initial values and references.");

		isInitializing = true;
		if (!isInitialized())
		{
			for(Component c : function_parameters)
			{
				if (c.getType() == Component.TYPE_CONTROL)
				{
					Control cc = (Control) c;

					if (!cc.isInitialized())
						cc.initValue();
				}
			}

			initial_value = evaluateFunction();
			setCurrentValue(initial_value);
			setPreviousValue(initial_value);
		}
		setInitialized(true);
		isInitializing = false;
	}

	/**
	 * Returns the initialization status of a control.
	 * @return <code>true</code> if the control has been initialized, false otherwise.
	 */
	public boolean isInitialized()
	{
		return isInitialized;
	}

	/**
	 * Sets the array of elements from a {@link Control#CONDITIONAL} function.  These
	 * are used to evaluate the next value of the function.
	 * @param the_elements An array list containing the values associated with the
	 * {@link Control#CONDITIONAL} function.  Should be in following order: <br />
	 * <ol start="0">
	 * <li>Component1</li>
	 * <li>Operator</li>
	 * <li>Component2</li>
	 * <li>ValueIfTrue</li>
	 * <li>ValueIfFalse</li>
	 * </ol>
	 */
	public void setConditionalElements(ArrayList<String> the_elements)
	{
		conditional_elements = the_elements;
	}

	/**
	 * Set the function to use in any future calculations.  Normally functions
	 * are only set during instantiation, but some data driven models might
	 * cause function values to change.
	 * 
	 * @param the_function The function to be used in any future calculations.
	 */
	public void setFunctionText(final String the_function) {
		function_text = the_function;
	}

	/**
	 * Sets {@link Control#isInitialized} to the value provided.  Should only
	 * be used via {@link Control#initValue()} to ensure that all upstream
	 * {@link Control}s have been resursively initialized prior to committing
	 * <em>this</em> {@link Control} to being initialized.
	 * @param value <code>true</code> if the value has been initialized, 
	 * <code>false</code> otherwise.
	 */
	public void setInitialized(boolean value)
	{
		isInitialized = value;
	}

	/**
	 * Accepts references to the parameters that exist in the function.
	 * @param the_params Can be empty if no parameters exist for the control.  This
	 * happens when the type is constant (not conditional or function).
	 */
	public void setParameters(ArrayList<Component> the_params)
	{
		function_parameters = the_params;
	}

	/**
	 * Requires use of JEval software.  {@link Control#evaluateFunction()} reads the
	 * {@link Control#function_type} and returns a value accordingly.
	 * @return A different value depending on case.  See below<br><br>
	 * <ul>
	 * <li><b>CONDITIONAL</b><br>Loops through {@link Control#conditional_elements} and
	 * evaluates whether the conditional results in true or false and returns the 
	 * appropriate value (ValueIfTrue or ValueIfFalse)</li>
	 * <li><b>FUNCTION</b><br>Loops through all {@link Control#function_parameters} and
	 * replaces the string representations with the {@link Component}s values.  The
	 * resulting function is then passed to an evaluator and the result is returned.</li>
	 * <li><b>RECURSIVE</b><br>Behaves exactly as FUNCTION</li>
	 * <li><b>CONSTANT</b><br>No values are calculated, simply returns
	 * {@link Control#initial_value}
	 */
	private double evaluateFunction()
	{
		String a_function = "";

		switch (function_type) {
		case CONDITIONAL:
			String flag = "";
			a_function = 
					String.format("%.20f", function_parameters.get(0).getPreviousValue()) + 
					conditional_elements.get(1) + 
					String.format("%.20f", function_parameters.get(1).getPreviousValue());

			try
			{
				flag = evaluator.evaluate(a_function);
			} catch (EvaluationException e)
			{
				System.err.println(e.getMessage() + ": " + a_function);
			}

			if (flag.equals("1.0")) { // true
				try
				{
					return Double.parseDouble(conditional_elements.get(2));
				}
				catch (NumberFormatException e)
				{
					e.printStackTrace();
				}
			} else if (flag.equals("0.0")) { // false
				try
				{
					return Double.parseDouble(conditional_elements.get(3));
				} catch (NumberFormatException e)
				{
					// TODO Need to handle problems with parsing in Control.
					e.printStackTrace();
				}
			}

			break;
		case FUNCTION:
		case RECURSIVE:
			a_function = function_text;

			for (int i = 0; i < function_parameters.size(); i++) {
				a_function = a_function.replace("{" + function_parameters.get(i).getName() + "}",
						"" + String.format("%.20f", function_parameters.get(i).getPreviousValue()));
			}

			try
			{
				return Double.parseDouble(evaluator.evaluate(a_function));
			}
			catch (NumberFormatException e)
			{
				e.printStackTrace();
			}
			catch (EvaluationException e)
			{
				System.err.println(e.getMessage() + ": " + a_function);
			}
			break;
		case CONSTANT:
			return initial_value;
		}

		//  IF ALL ELSE FAILS, JUST RETURN ZERO.
		return 0.0;
	}
}
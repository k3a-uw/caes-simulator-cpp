package error;

/**
 * A custom exception to handle issues with the XML parsing special to this
 * application.
 * @author Kevin E. Anderson (k3a@uw.edu)
 * @version 2013.07.21
 *
 */
public class InvalidXMLException extends Exception
{
	/**
	 * A serial ID
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Creates a new Exception with the provided message.
	 * @param message The message to be passed through the exception.
	 */
	public InvalidXMLException(final String message)
	{
		super(message);
	}

}

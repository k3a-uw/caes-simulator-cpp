package input;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayDeque;
import java.util.Iterator;

import javax.xml.stream.EventFilter;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import error.InvalidXMLException;

/**
 * The {@link InputStreamer} receives an xml file formatted according to the input.xsd
 * schema.  Because the xml can be arbitrarily large, it caches only 
 * {@link InputStreamer#CACHE_SIZE} {@link TimeStep}s at a time.  The streamer will 
 * continue to provide records as needed until the {@link InputStreamer#inputFile} has
 * reached its end. 
 * @author Kevin E. Anderson (k3a@uw.edu)
 * @version 2013.07.21
 */
public class InputStreamer {

	/**
	 * The number of {@link TimeStep}s to read from the file at a time.  The larger the
	 * value the faster the simulation, but the larger the memory foot print.
	 */
	private final int CACHE_SIZE = 10;
	
	/**
	 * A file to represent the input.xml data.
	 */
	private File inputFile;
	
	/**
	 * The event reader that allows for quick and easy streaming of the xml data from
	 * disk into memory.
	 */
	private XMLEventReader stream;
	
	/**
	 * An array to store the {@link TimeStep}s as a cache.
	 */
	private ArrayDeque<TimeStep> step_cache;

	/**
	 * Construct a new InputStreamer for the provided file.  Is ready to use upon
	 * instantiation.
	 * @param filename The path of the input file.
	 * @throws FileNotFoundException When the file is not found.
	 * @throws XMLStreamException When the provided file is not in the proper format.
	 */
	public InputStreamer(String filename) throws FileNotFoundException, XMLStreamException
	{
		inputFile = new File(filename);
		if(!inputFile.exists())
			throw new FileNotFoundException();

		createStream();
		step_cache = new ArrayDeque<TimeStep>();
	}

	/**
	 * Returns and consumes the next timestep in cache.
	 * If one does not exist getNextStep() will try and fill 
	 * the cache before returning the timestep.
	 * @return The {@link TimeStep} at the front of the cache.  If there are 
	 * no steps remaining the method will return <code>null</code>.
	 */
	public TimeStep getNextStep()
	{
		checkCache();
		return step_cache.poll();
	}

	/**
	 * Checks the both the stream and cache for additional records.  Will return 
	 * <code>true<cod> if either has records, otherwise <code>false</code>. 
	 * @return <code>true</code> if there are additional records to be read from the
	 * input file.  Otherwise, returns <code>false</code>.
	 */
	public boolean hasNext()
	{
		return stream.hasNext() || !step_cache.isEmpty();
	}

	/**
	 * Returns but does not consume the next timestep in cache.
	 * If one does not exist getNextStep() will try and fill 
	 * the cache before returning the timestep.
	 * @return The {@link TimeStep} at the front of the cache.  If there are 
	 * no steps remaining the method will return <code>null</code>.
	 */
	public TimeStep peek()
	{
		checkCache();
		return step_cache.peek();
	}

	/**
	 * Looks to see if the cache is empty.  Will fill the cache with records from the
	 * {@link InputStreamer#stream} if they exist.
	 */
	private void checkCache()
	{
		if (step_cache.isEmpty() && stream.hasNext())
			try {
				fillCache();
			} catch (XMLStreamException | InvalidXMLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	
	}

	/**
	 * Builds {@link InputStreamer#stream} using a {@link StartFilter}.
	 * @throws FileNotFoundException  If {@link InputStreamer#inputFile does not exist.
	 * @throws XMLStreamException If {@link InputStreamer#inputFile is not properly 
	 * formatted in the proper schema.
	 */
	private void createStream() throws FileNotFoundException, XMLStreamException
	{
		FileReader fr = new FileReader(inputFile);
		XMLInputFactory factory = XMLInputFactory.newInstance();
		stream = factory.createFilteredReader(
				factory.createXMLEventReader(fr), new StartFilter());
	}

	/**
	 * Will extract elements from the {@link InputStreamer#stream}, create new
	 * {@link TimeStep} objects and insert them into the {@link InputStreamer#step_cache}
	 * in chucks of {@link InputStreamer#CACHE_SIZE}.
	 * @throws XMLStreamException If {@link InputStreamer#inputFile} is not well formed.
	 * @throws InvalidXMLException If {@link InputStreamer#inputFile} is not well formed.
	 */
	private void fillCache() throws XMLStreamException, InvalidXMLException
	{
	
		while (stream.hasNext() && step_cache.size() < CACHE_SIZE)
		{
			StartElement e = (StartElement) stream.nextEvent();
	
			//START READING TIMESTEP
			if (e.getName().getLocalPart().equals("timestep"))
			{
				TimeStep newStep = new TimeStep();
				Iterator stepIterator = e.getAttributes();
				while (stepIterator.hasNext())
				{
					Attribute attribute = (Attribute) stepIterator.next();
					if(attribute.getName().toString().equals("stepValue"))
					{
						newStep.setStepValue(Integer.parseInt(attribute.getValue()));
					}
				}
	
				//CONTINUE TO STREAM UNTI YOU FIND THE NEXT TIMESTEP.  CREATE COMPONENTS ALONG THEW AY
				while(stream.hasNext() && !stream.peek().asStartElement().getName().getLocalPart().equals("timestep"))
				{
					StartElement nextElement = stream.nextEvent().asStartElement();
					Iterator componentIterator = nextElement.getAttributes();
	
					String id   = "";
					String name = "";
					int type    = -1;
					double value = 0;
	
					//EXTRACT THE ATTRIBUTES
					while(componentIterator.hasNext())
					{
						Attribute attr = (Attribute) componentIterator.next();
						String attrName = attr.getName().getLocalPart().toString();
						if(attrName.equals("id"))
						{
							id = attr.getValue();
						} else if (attrName.equals("name"))
						{
							name = attr.getValue();
						} else if (attrName.equals("type"))
						{
							type = ComponentStep.convertName(attr.getValue());
						} else if (attrName.equals("value"))
						{
							value = Double.parseDouble(attr.getValue());
						} // IGNORE INVALID ATTRIBUTES.
					}
	
					if (id.isEmpty() || name.isEmpty() || type == -1)
						throw new InvalidXMLException("A Component for TimeStep ID: " + newStep.getStepValue() + " is Invalid!");
	
					newStep.addComponent(new ComponentStep(id, name, type, value));					
				}
	
				//NOW THAT I HAVE A BUILD TIMESTEP I CAN ADD IT TO THE CACHE
				step_cache.add(newStep);
			} //END READING TIMESTEP
		}
	}

	/**
	 * A small inner class to only look for {@link javax.xml.stream.events.StartElement}s.
	 * @author Kevin E. Anderson (k3a@uw.edu)
	 * @version 2013.07.21
	 *
	 */
	class StartFilter implements EventFilter
	{
		@Override
		public boolean accept(XMLEvent event) {
			return event.isStartElement();
		}

	}
}

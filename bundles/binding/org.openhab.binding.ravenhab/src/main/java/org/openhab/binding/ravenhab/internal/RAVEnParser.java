package org.openhab.binding.ravenhab.internal;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.openhab.core.events.EventPublisher;
import org.openhab.core.library.types.DecimalType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.InputSource;
import java.io.StringReader;
import java.util.ArrayList;

/**
 * This class implements the XML to PowerMeasurement conversion.
 *
 * @author		rub-a-dub-dub
 * @version		1.0
 * @since		2014-07-29
 */
public class RAVEnParser {
	
	private String itemName;
	private EventPublisher eventPublisher;
	private static final Logger logger = LoggerFactory.getLogger(SerialListener.class);
	
	public RAVEnParser(EventPublisher ev, String name) throws IllegalArgumentException {
		if (ev == null) throw new IllegalArgumentException("Event publisher needs to be available!");
		if (name == null) throw new IllegalArgumentException("Item name cannot be null!");
		itemName = name;
		eventPublisher = ev;
	}
	
	/**
	 * This function converts an XML response into a measurement object.
	 * <p>
	 * Pass this function the raw String data directly from the RAVEn. It will
	 * automatically decode the contents into the PowerMeasurement object, which
	 * can then be sent down the data pipeline.
	 * </p>
	 *
	 * @param rawChunk This is the raw XML data (single tag only) from the RAVEn
	 */
	public void chunkParser(String rawChunk) {
		// First add some dummy information to the chunks to allow for correct parsing
		String myChunk = "<?xml version=\"1.0\"?><root>" + rawChunk + "</root>";
		// Setup for parsing using SAX
		SAXParserFactory factory = SAXParserFactory.newInstance();

		try {
			SAXParser parser = factory.newSAXParser();
			RAVEnXMLHandler handler = new RAVEnXMLHandler(eventPublisher, itemName);

			// Convert the input string to an InputSource for the parser - http://stackoverflow.com/questions/3906892/parse-an-xml-string-in-java
			InputSource is = new InputSource(new StringReader(myChunk));

			// Call the parser
			parser.parse(is, handler);
		} catch (Exception e) {
			logger.info("XML parsing error: " + e.getMessage());
		}
	}

	/**
	 * This is an internal class used for the SAX parser's callbacks.
	 *
	 * @author		rub-a-dub-dub
	 * @version		1.0
	 * @since		2014-07.29
	 */
	private static final class RAVEnXMLHandler extends DefaultHandler {

		@SuppressWarnings("unused")
		private boolean startInstDemand;
		private boolean startTimeStamp;
		private boolean startDemand;
		private boolean startMultiplier;
		private boolean startDivisor;
		private String tDem, tTime, tMul, tDiv;
		private ArrayList<PowerMeasurement> items = new ArrayList<PowerMeasurement>();
		
		private String itemName;
		private EventPublisher eventPublisher;

		/**
		 * This overriden method just catches our tags of interest.
		 */
		@Override
		public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
			super.startElement(uri, localName, qName, attributes);
			if (qName.equalsIgnoreCase("InstantaneousDemand"))
				startInstDemand = true;
			if (qName.equalsIgnoreCase("TimeStamp"))
				startTimeStamp = true;
			if (qName.equalsIgnoreCase("Demand"))
				startDemand = true;
			if (qName.equalsIgnoreCase("Multiplier"))
				startMultiplier = true;
			if (qName.equalsIgnoreCase("Divisor"))
				startDivisor = true;
		}

		/**
		 * This overriden method just catches our tags of interest.
		 */
		@Override
		public void endElement(String uri, String localName, String qName) throws SAXException {
			super.endElement(uri, localName, qName);
			if (qName.equalsIgnoreCase("InstantaneousDemand")) {
				startInstDemand = false;
				// Add a measurement to our list
				items.add(new PowerMeasurement(tTime, tDem, tMul, tDiv));
				// Reset our internal variables
				tTime = "";
				tDem = "";
				tMul = "";
				tDiv = "";
			}
			if (qName.equalsIgnoreCase("TimeStamp")) {
				startTimeStamp = false;
			}
			if (qName.equalsIgnoreCase("Demand")) {
				startDemand = false;
			}
			if (qName.equalsIgnoreCase("Multiplier")) {
				startMultiplier = false;
			}
			if (qName.equalsIgnoreCase("Divisor")) {
				startDivisor = false;
			}
		}

		/**
		 * This overriden method decodes the CDATA within each tag
		 */
		@Override
		public void characters(char[] ch, int start, int length) throws SAXException {
			super.characters(ch, start, length);
			// The "+/- 2" in the lines below strip out the 0x hex prefix
			if (startTimeStamp)
				tTime = new String(ch, start + 2, length - 2);
			if (startDemand)
				tDem = new String(ch, start + 2, length - 2);
			if (startMultiplier)
				tMul = new String(ch, start + 2, length - 2);
			if (startDivisor)
				tDiv = new String(ch, start + 2, length - 2);
		}

		/**
		 * This overriden method will send a list of measurements down the data pipeline.
		 */
		@Override
		public void endDocument() throws SAXException {
			super.endDocument();

			for (PowerMeasurement pm: items) {
				double sentValue = (double) Math.round(pm.getValue()*1000);
				eventPublisher.postUpdate(itemName, new DecimalType(sentValue));
			}

			// Clear our list
			items.clear();
		}

		/**
		 * This overriden method will initialise the class' private state variables
		 */
		@Override
		public void startDocument() throws SAXException {
			startDivisor = false;
			startMultiplier = false;
			startDemand = false;
			startTimeStamp = false;
			startInstDemand = false;
		}
		
		public RAVEnXMLHandler(EventPublisher ev, String name) throws IllegalArgumentException {
			super();
			if (ev == null) throw new IllegalArgumentException("Event publisher needs to be available!");
			if (name == null) throw new IllegalArgumentException("Item name cannot be null!");
			itemName = name;
			eventPublisher = ev;
		}
	}
}
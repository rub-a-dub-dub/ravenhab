package org.openhab.binding.ravenhab.internal;

import gnu.io.*;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import org.openhab.core.events.EventPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is used to listen for serial port events from the RAVEn
 *
 * @author rub-a-dub-dub
 * @version 1.0
 * @since 2014-07-30
 */
public class SerialListener implements SerialPortEventListener {
	
	private static final Logger logger = LoggerFactory.getLogger(SerialListener.class);

	/**
	 * Holds all messages before being sent for processing
	 */
	ByteArrayOutputStream output;
	/**
	 * Number of messages that will be batched together
	 */
	private int groupMessageCount = 1;
	/**
	 * Number of messages that have been processed in the current batch
	 */
	private int numMessages = 0;

	/**
	 * Used to send information back to OpenHAB
	 */
	private EventPublisher eventPublisher = null;
	
	/**
	 * The item that we are bound to.
	 */
	private String itemName;
	
	public void setEventPublisher(EventPublisher eventPublisher) {
		this.eventPublisher = eventPublisher;
	}
	
	public void unsetEventPublisher(EventPublisher eventPublisher) {
		this.eventPublisher = null;
	}
	
	public void setItemName(String newName) {
		itemName = newName;
	}
	
	public String getItemName() {
		return itemName;
	}
	
	/**
	 * This method is overriden and handles managing the triggered serial port event
	 *
	 * @param event The SerialPortEvent passed into from the serial port.
	 */
	@Override
	public void serialEvent(SerialPortEvent event) {
		// Only the DATA_AVAILABLE event is handled
		switch(event.getEventType()) {
			case SerialPortEvent.DATA_AVAILABLE:
				dataAvailable(event);
				break;
		}
	}

	/**
	 * The default constructor initialises this listener for passing every message downstream.
	 */
	public SerialListener() {
		super();
		output = new ByteArrayOutputStream();
		numMessages = 0;
		groupMessageCount = 1;
	}

	/**
	 * This constructor lets you batch messages together before sending them downstream.
	 *
	 * @param mCount The number of messages to group together for processing
	 * @throws IllegalArgumentException
	 */
	public SerialListener(int mCount) throws IllegalArgumentException {
		super();
		if (mCount <=0 ) throw(new IllegalArgumentException("Number of messages in a batch >= 1."));
		output = new ByteArrayOutputStream();
		groupMessageCount = mCount;
		numMessages = 0;
	}

	/**
	 * This method is called when the SerialPort has data available to read
	 *
	 * @param event SerialPortEvent object provided by the caller
	 */
	private void dataAvailable(SerialPortEvent event) {
		numMessages++;
		try {
			InputStream is = ((SerialPort) event.getSource()).getInputStream();

			do {
				while (is.available() > 0) {
					int c;
					c = is.read();
					output.write(c);
				}
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					
				}
			} while (is.available() > 0);

			if (numMessages == groupMessageCount) {
				// Convert our buffer into a String and send for processing
				String batchAnswer = output.toString();
				output.reset();
				
				if (eventPublisher != null && itemName != null) {
					RAVEnParser myParser = new RAVEnParser(eventPublisher, itemName);
					myParser.chunkParser(batchAnswer);
				}
				numMessages = 0;
			}
		} catch (Exception e) {
			logger.debug("Error receiving data from RAVEn: {}", e.getMessage());
		}
	}
}
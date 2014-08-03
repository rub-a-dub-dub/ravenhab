package org.openhab.binding.ravenhab.internal;

import gnu.io.*;
import java.util.Enumeration;
import java.util.TooManyListenersException;

import org.openhab.core.events.EventPublisher;

/**
 * This class will connect to a RAVEn device and process messages
 * @author rub-a-dub-dub
 * @version 1.0
 * @since 2014-07-30
 */
public class RAVEnPort {

	/**
	 * This variable holds the "canonical" port name (/dev/... in *nix)
	 */
	private final String portName;
	/**
	 * This variable holds the Java unique port identifier for the given portName
	 */
	private CommPortIdentifier portID;
	/**
	 * This is the serial port object that we use to communicate with the RAVEn
	 */
	private SerialPort myPort;
	/**
	 * This is our SerialPortEventListener object that gets called when there's data available.
	 */
	private final SerialListener myListener;

	@SuppressWarnings("unused")
	private EventPublisher eventPublisher = null;
	
	public void setEventPublisher(EventPublisher eventPublisher) {
		this.eventPublisher = eventPublisher;
		myListener.setEventPublisher(eventPublisher);
	}
	
	public void unsetEventPublisher(EventPublisher eventPublisher) {
		this.eventPublisher = null;
		myListener.unsetEventPublisher(eventPublisher);
	}
	
	public void setItemName(String newName) {
		myListener.setItemName(newName);
	}
	
	public String getItemName() {
		return myListener.getItemName();
	}
	
	/**
	 * Default constructor looks for the RAVEn on /dev/ttyUSB0 and disables the message queuing.
	 *
	 * @throws IllegalArgumentException, PortInUseException, TooManyListenersException, UnsupportedCommOperationException
	 */
	public RAVEnPort() throws IllegalArgumentException, PortInUseException, TooManyListenersException, UnsupportedCommOperationException {
		this("/dev/ttyUSB0", 1);
	}

	/**
	 * This constructor uses the provided device name to talk to the RAVEn and disables message queuing.
	 *
	 * @param devName A String representing the serial port to talk to the RAVEn on.
	 * @throws IllegalArgumentException, PortInUseException, TooManyListenersException, UnsupportedCommOperationException
	 */
	public RAVEnPort(String devName) throws IllegalArgumentException, PortInUseException, TooManyListenersException, UnsupportedCommOperationException {
		this(devName, 1);
	}

	/**
	 * This constructor connects to the RAVEn with the specified port and sets the message queue depth.
	 *
	 * @param devName A String representing the serial port to talk to the RAVEn on.
	 * @param mCount An int (>0) which is the depth of the message queue. These many messages are batched together when sent for processing.
	 * @throws IllegalArgumentException, PortInUseException, TooManyListenersException, UnsupportedCommOperationException
	 */
	public RAVEnPort(String devName, int mCount) throws IllegalArgumentException, PortInUseException, TooManyListenersException, UnsupportedCommOperationException {
		portName = devName;
		portID = null;
		myPort = null;
		openPort();
		myListener = new SerialListener(mCount);
		// TODO:Add code to reset the RAVEn's reporting interval, setting only InstantaneousDemand and disabling all others 
		myPort.addEventListener(myListener);	
		myPort.notifyOnDataAvailable(true);
	}

	/**
	 * This function opens up the serial port to communicate with the RAVEn.
	 *
	 * @throws IllegalArgumentException, PortInUseException, UnsupportedCommOperationException
	 */
	private void openPort() throws IllegalArgumentException, PortInUseException, UnsupportedCommOperationException {
		@SuppressWarnings("unchecked")
		Enumeration<CommPortIdentifier> portIdentifiers = CommPortIdentifier.getPortIdentifiers();
		while (portIdentifiers.hasMoreElements()) {
		    CommPortIdentifier pid = (CommPortIdentifier) portIdentifiers.nextElement();
		    if(pid.getPortType() == CommPortIdentifier.PORT_SERIAL &&
		       pid.getName().equals(portName)) {
		        portID = pid;
		        break;
		    }
		}
		if (portID == null) {
			StringBuilder sb = new StringBuilder();
			@SuppressWarnings("unchecked")
			Enumeration<CommPortIdentifier> portList = CommPortIdentifier.getPortIdentifiers();
			while (portList.hasMoreElements()) {
				CommPortIdentifier id = (CommPortIdentifier) portList.nextElement();
				if (id.getPortType() == CommPortIdentifier.PORT_SERIAL) {
					sb.append(id.getName() + "\n");
				}
			}
			throw(new IllegalArgumentException("Available ports are: "+ sb.toString()));
		}
		myPort = (SerialPort) portID.open("ravenhab", 2000);
		myPort.setSerialPortParams(115200, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
	}

	/**
	 * This function is used to close the serial port when GC is triggered.
	 */
	public void finalise() {
		if (myPort != null) {
			myPort.removeEventListener();
			myPort.close();
		}
	}
}
package org.openhab.binding.ravenhab.internal;

/**
 * This class represents a generic measurement that a RAVEn makes.
 *
 * @author		rub-a-dub-dub
 * @version		1.0
 * @since		2014-07-29
 */

public class RAVEnMeasurement {
	/**
	 * Used to retain the Unix compatible timestamp.
	 */
	protected long timestamp;
	/**
	 * Used to retain the raw value from the RAVEn.
	 */
	protected Number reading;
	/**
	 * The number of milliseconds from the Unix epoch to 1/1/2000 00:00h
	 */
	public static final long TIMEY2KFROMEPOCHMS = 946684800;
	/**
	 * The number of microseconds from the Unix epoch to 1/1/2000 00:00h
	 */
	public static final long TIMEY2KFROMEPOCHUS = TIMEY2KFROMEPOCHMS*1000;

	public RAVEnMeasurement(String time, String value) {
		timestamp = Long.parseLong(time, 16);
		reading = 0; // Value ignored
	}

	/**
	 * Returns the time for this measurement in microseconds.
	 * @return 	long integer
	 */
	public long getTimeMicroseconds() {
		return (timestamp*1000 + TIMEY2KFROMEPOCHUS);
	}

	/**
	 * Returns the UNIX timestamp for this measurement.
	 * @return long integer
	 */
	public long getTimestamp() {
		return (timestamp + TIMEY2KFROMEPOCHMS);
	}

	/**
	 * Returns the raw reading
	 * @return String
	 */
	public Number getValue() {
		return reading;
	}

	/**
	 * This is a debug method that will convert this object to a String
	 *
	 * @return String
	 */
	public String toString() {
		return getTimeMicroseconds() + ":" + getValue();
	}
}
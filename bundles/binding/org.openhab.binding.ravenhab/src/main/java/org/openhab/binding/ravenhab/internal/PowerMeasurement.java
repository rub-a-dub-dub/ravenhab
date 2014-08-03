package org.openhab.binding.ravenhab.internal;

/**
 * @author		rub-a-dub-dub
 * @version		1.0
 * @since		2014-07-29
 */
public class PowerMeasurement extends RAVEnMeasurement {
	/**
	 * Used to retain the power meter reading.
	 */
	private Double reading;

	/**
	 * Default constructor
	 * <p>
	 * The only constructor allowed is this one where the time, reading, divisor and multiplier
	 * must be extracted from the processing stream and passed in. All parameter inputs are 
	 * assumed to be raw hex coded Strings WITHOUT the prefix "0x" present. 
	 * </p>
	 * 
	 * @param timestamp The device reported timestamp
	 * @param measurement The power reported by the device
	 * @param multiplier The multiplier reported by the device
	 * @param divisor The divisor reported by the device
	 */
	PowerMeasurement(String time, String measurement, String multiplier, String divisor) {
		super(time, measurement);

		// Create our temporary variables
		Integer tMult, tDiv, tRead;

		// Convert all of our Strings into Numbers
		tMult = Integer.parseInt(multiplier, 16);
		tDiv = Integer.parseInt(divisor, 16);
		tRead = Integer.parseInt(measurement, 16);

		// Perform some cleaning off the data (per the RAVEn XML API spec)
		tMult = (tMult == 0) ? 1:tMult;
		tDiv = (tDiv == 0) ? 1:tDiv;

		// Finally set the reading and we're done
		reading = (double) tRead/tDiv*tMult;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public Double getValue() {
		return reading;
	}
}
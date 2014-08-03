package org.openhab.binding.ravenhab.internal;

import gnu.io.PortInUseException;
import gnu.io.UnsupportedCommOperationException;

import org.openhab.core.events.AbstractEventSubscriber;
import org.openhab.core.events.EventPublisher;
import org.openhab.core.items.Item;
import org.openhab.core.library.items.NumberItem;
import org.openhab.model.item.binding.BindingConfigParseException;
import org.openhab.model.item.binding.BindingConfigReader;
	

/**
 * This class implements an async RAVEn binding. Code heavily based on the serial binding.
 * 
 * @author rub-a-dub-dub
 * @since 1.6.0
 */
public class RAVEnHABBinding extends AbstractEventSubscriber implements BindingConfigReader {

	/**
	 * This is the serial port through which the RAVEn communicates
	 */
	private RAVEnPort commPort = null;
	
	private EventPublisher eventPublisher = null;
	
	public void setEventPublisher(EventPublisher eventPublisher) {
		this.eventPublisher = eventPublisher;
		if (commPort != null)
			commPort.setEventPublisher(eventPublisher);
	}
	
	public void unsetEventPublisher(EventPublisher eventPublisher) {
		this.eventPublisher = null;
		if (commPort != null)
			commPort.unsetEventPublisher(eventPublisher);
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getBindingType() {
		return "ravenhab";
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public void validateItemType(Item item, String bindingConfig)
			throws BindingConfigParseException {
		if (!(item instanceof NumberItem)) {
			throw new BindingConfigParseException("item '" + item.getName()
					+ "' is of type '" + item.getClass().getSimpleName()
					+ "', only NumberItems are allowed.");
		}
		
	}


	@Override
	public void processBindingConfiguration(String context, Item item,
			String bindingConfig) throws BindingConfigParseException {
		String port = bindingConfig;
		try {
			// If the com port has already been initialised, reset it
			if (commPort != null)
				commPort = null;
			
			// Connect to the com port
			commPort = new RAVEnPort(port);
			if (eventPublisher != null)
				commPort.setEventPublisher(eventPublisher);
			
			if (commPort.getItemName() != null)
				throw(new BindingConfigParseException("There is already another NumberItem assigned to the RAVEn!"));
			else
				commPort.setItemName(item.getName());
		} catch (PortInUseException e) {
			throw(new BindingConfigParseException("Couldn't open serial port " + port + ": already in use!"));
		} catch (UnsupportedCommOperationException e) {
			throw(new BindingConfigParseException("Couldn't open serial port " + port + ": unsupported operation returned."));
		} catch (IllegalArgumentException e) {
			throw(new BindingConfigParseException("Couldn't open serial port " + port + ": illegal argument received! " + e.getMessage()));
		} catch (Exception e) {
			throw(new BindingConfigParseException("Couldn't open serial port " + port + ": " + e.getClass().getSimpleName() + "," + e.getMessage()));
		}
	}


	@Override
	public void removeConfigurations(String context) {
		// Just delete the port we setup earlier
		commPort.finalise();
		commPort.setItemName(null);
		commPort = null;
	}
}
